package com.tubitech.copilot.agent.loop;

import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.ResponseNormalizer;
import com.tubitech.copilot.agent.tools.ToolCall;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolRegistry;
import com.tubitech.copilot.agent.tools.ToolResult;
import com.tubitech.copilot.api.ConversationApiClient;
import com.tubitech.copilot.api.StreamListener;
import com.tubitech.copilot.api.model.ChatRequest;
import com.tubitech.copilot.editor.bundle.LastSendSnapshotWriter;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AgentLoopRunner {
   private static final Gson GSON = new Gson();
   private static final Logger LOG = Logger.getInstance(AgentLoopRunner.class);
   private static final int DEFAULT_MAX_ITERATIONS = 25;
   private static final long SEND_TIMEOUT_SECONDS = 180L;
   private static final int MAX_QUESTION_CHARS = 20000;
   private static final int MAX_CONTINUATION_RETRIES = 2;
   private static final Set<String> WRITE_TOOLS = Set.of("edit_file", "create_file", "delete_file", "run_command");
   private static final String TASK_DONE_TOOL = "task_done";
   private static final String PLAN_DONE_TOOL = "plan_done";
   private static final String CONTINUATION_PROMPT_TEXT_ONLY = "You described what to do but did not use any tools. Act now using <tool_call> tags. If done, call task_done.";
   private static final String CONTINUATION_PROMPT_EMPTY = "No response received. Reply with a tool call:\n<tool_call>{\"id\":\"c1\",\"name\":\"read_file\",\"arguments\":{\"path\":\"src/Main.java\"}}</tool_call>\nOr finish: <tool_call>{\"id\":\"c1\",\"name\":\"task_done\",\"arguments\":{\"summary\":\"Done\"}}</tool_call>";
   private static final Set<String> READ_TOOLS = Set.of("read_file");
   private static final Set<String> PLAN_BLOCKED_TOOLS = Set.of("edit_file", "create_file", "delete_file", "task_done", "run_command");
   private final ConversationApiClient apiClient;
   private final Project project;
   private final AgentLoopCallbacks callbacks;
   private final int maxIterations;
   private final boolean planningPhase;
   @NotNull
   private volatile String serverConversationId;
   private final AtomicBoolean cancelled = new AtomicBoolean(false);
   private volatile CountDownLatch activeLatch;
   private int consecutiveTextOnly = 0;

   public AgentLoopRunner(@NotNull ConversationApiClient apiClient, @NotNull Project project, @NotNull AgentLoopCallbacks callbacks) {
      this(apiClient, project, callbacks, 25, false, "");
   }

   public AgentLoopRunner(@NotNull ConversationApiClient apiClient, @NotNull Project project, @NotNull AgentLoopCallbacks callbacks, boolean planningPhase) {
      this(apiClient, project, callbacks, 25, planningPhase, "");
   }

   public AgentLoopRunner(
      @NotNull ConversationApiClient apiClient,
      @NotNull Project project,
      @NotNull AgentLoopCallbacks callbacks,
      boolean planningPhase,
      @NotNull String serverConversationId
   ) {
      this(apiClient, project, callbacks, 25, planningPhase, serverConversationId);
   }

   public AgentLoopRunner(
      @NotNull ConversationApiClient apiClient,
      @NotNull Project project,
      @NotNull AgentLoopCallbacks callbacks,
      int maxIterations,
      boolean planningPhase,
      @NotNull String serverConversationId
   ) {
      this.apiClient = apiClient;
      this.project = project;
      this.callbacks = callbacks;
      this.maxIterations = maxIterations;
      this.planningPhase = planningPhase;
      this.serverConversationId = serverConversationId;
   }

   public void cancel() {
      this.cancelled.set(true);
      this.apiClient.cancel();
      CountDownLatch latch = this.activeLatch;
      if (latch != null) {
         latch.countDown();
      }
   }

   public void run(@NotNull ChatRequest initialRequest) {
      boolean finished = false;

      try {
         finished = this.runLoop(initialRequest);
      } catch (Exception e) {
         LOG.warn("AgentLoop: unexpected error", e);
         this.callbacks.onLoopError(e);
         finished = true;
      } finally {
         if (!finished) {
            LOG.info("AgentLoop: exited without finishing (cancelled or interrupted)");
            this.callbacks.onLoopFinished("", null, initialRequest.getConversationId());
         }
      }
   }

   private boolean runLoop(@NotNull ChatRequest initialRequest) {
      ChatRequest currentRequest = initialRequest;
      String lastMessageId = null;
      String conversationId = currentRequest.getConversationId();
      int totalIterations = 0;
      int iterationsInBatch = 0;

      while (!this.cancelled.get()) {
         if (iterationsInBatch >= this.maxIterations) {
            LOG.info("AgentLoop: reached batch limit (" + totalIterations + " total), asking user to continue");
            if (!this.callbacks.onIterationLimitReached(totalIterations)) {
               LOG.info("AgentLoop: user chose to stop");
               this.callbacks.onLoopFinished("", lastMessageId, conversationId);
               return true;
            }

            LOG.info("AgentLoop: user chose to continue");
            iterationsInBatch = 0;
         }

         this.callbacks.onLoopIteration(totalIterations, this.maxIterations);
         totalIterations++;
         iterationsInBatch++;
         this.callbacks.onWaitingForResponse();
         AgentLoopRunner.LoopStepResult step = this.sendAndWait(currentRequest);
         if (step == null) {
            return false;
         }

         lastMessageId = step.messageId;
         if (step.conversationId != null) {
            conversationId = step.conversationId;
            this.serverConversationId = step.conversationId;
         }

         String content = step.fullContent != null ? step.fullContent : "";
         List<ToolCall> toolCalls = ToolCallParser.parse(content);
         String visibleText = ResponseNormalizer.normalize(ToolCallParser.stripToolCalls(content));
         int malformedCount = (int)toolCalls.stream().filter(c -> "_malformed_tool_call".equals(c.getName())).count();
         toolCalls.removeIf(c -> "_malformed_tool_call".equals(c.getName()));
         if (malformedCount > 0) {
            LOG.warn("AgentLoop: " + malformedCount + " malformed tool call(s) dropped");
         }

         if (toolCalls.isEmpty() && content.contains("<tool_call>")) {
            LOG.warn(
               "AgentLoop: response contains <tool_call> but parser returned 0 valid calls. Content length="
                  + content.length()
                  + ", has </tool_call>="
                  + content.contains("</tool_call>")
            );
            this.saveDebugResponse(content, totalIterations);
         }

         if (toolCalls.isEmpty()) {
            boolean isEmpty = content.isBlank();
            this.consecutiveTextOnly++;
            if (this.consecutiveTextOnly > 2) {
               LOG.info("AgentLoop: no tool calls after " + this.consecutiveTextOnly + " retries at iteration " + totalIterations + ", finishing");
               this.callbacks.onLoopFinished(isEmpty ? "" : content, lastMessageId, conversationId);
               return true;
            }

            String prompt = isEmpty
               ? "No response received. Reply with a tool call:\n<tool_call>{\"id\":\"c1\",\"name\":\"read_file\",\"arguments\":{\"path\":\"src/Main.java\"}}</tool_call>\nOr finish: <tool_call>{\"id\":\"c1\",\"name\":\"task_done\",\"arguments\":{\"summary\":\"Done\"}}</tool_call>"
               : "You described what to do but did not use any tools. Act now using <tool_call> tags. If done, call task_done.";
            LOG.info(
               "AgentLoop: "
                  + (isEmpty ? "empty" : "text-only")
                  + " response at iteration "
                  + totalIterations
                  + ", sending continuation (attempt "
                  + this.consecutiveTextOnly
                  + ")"
            );
            if (!isEmpty && !visibleText.isBlank()) {
               this.callbacks.onAssistantText(visibleText);
            }

            currentRequest = ChatRequest.builder()
               .question(prompt)
               .parentMessageId(lastMessageId)
               .conversationId(conversationId)
               .modelId(currentRequest.getModelId())
               .metadata(initialRequest.getMetadata())
               .build();
         } else {
            this.consecutiveTextOnly = 0;
            if (!visibleText.isBlank()) {
               this.callbacks.onAssistantText(visibleText);
            }

            Set<String> readPaths = new HashSet<>();
            Set<String> editPaths = new HashSet<>();

            for (ToolCall c : toolCalls) {
               String p = c.getArguments() != null ? c.getArguments().getOrDefault("path", "") : "";
               if (!p.isBlank()) {
                  if (READ_TOOLS.contains(c.getName())) {
                     readPaths.add(p);
                  }

                  if (WRITE_TOOLS.contains(c.getName())) {
                     editPaths.add(p);
                  }
               }
            }

            Set<String> conflictPaths = new HashSet<>(readPaths);
            conflictPaths.retainAll(editPaths);
            StringBuilder toolResults = new StringBuilder();
            boolean done = false;
            int failCount = 0;
            boolean userDenied = false;
            Set<String> editedPaths = new HashSet<>();
            Iterator rawResults = toolCalls.iterator();

            while (true) {
               if (rawResults.hasNext()) {
                  ToolCall call = (ToolCall)rawResults.next();
                  if (this.cancelled.get()) {
                     return false;
                  }

                  if (!"task_done".equals(call.getName()) && !"plan_done".equals(call.getName())) {
                     this.callbacks.onToolExecuting(call);
                     if (this.planningPhase && PLAN_BLOCKED_TOOLS.contains(call.getName())) {
                        ToolResult blocked = new ToolResult(
                           call.getId(),
                           call.getName(),
                           false,
                           "BLOCKED: You are in Planning phase. Tools that modify the project (create_file, edit_file, delete_file, run_command, task_done) are not allowed. Output your plan as text, then call plan_done.",
                           0L
                        );
                        this.callbacks.onToolComplete(blocked);
                        this.appendToolResult(blocked, toolResults);
                        failCount++;
                        continue;
                     }

                     if (WRITE_TOOLS.contains(call.getName()) && !conflictPaths.isEmpty()) {
                        String callPath = call.getArguments() != null ? call.getArguments().getOrDefault("path", "") : "";
                        if (conflictPaths.contains(callPath)) {
                           ToolResult conflict = new ToolResult(
                              call.getId(),
                              call.getName(),
                              false,
                              "REJECTED: You called read_file and "
                                 + call.getName()
                                 + " on the same file ("
                                 + callPath
                                 + ") in one response. The edit executes BEFORE you see the read result, so old_text is a blind guess. First read the file in one response, then edit it in the NEXT response.",
                              0L
                           );
                           this.callbacks.onToolComplete(conflict);
                           this.appendToolResult(conflict, toolResults);
                           failCount++;
                           continue;
                        }
                     }

                     if ("edit_file".equals(call.getName())) {
                        String callPath = call.getArguments() != null ? call.getArguments().getOrDefault("path", "") : "";
                        if (!callPath.isBlank() && editedPaths.contains(callPath)) {
                           ToolResult rejected = new ToolResult(
                              call.getId(),
                              call.getName(),
                              false,
                              "REJECTED: You already edited '"
                                 + callPath
                                 + "' in this response. Each edit changes the file content, so the next edit's old_text won't match. Send one edit_file per file per response. Re-read the file first, then edit again in the NEXT response.",
                              0L
                           );
                           this.callbacks.onToolComplete(rejected);
                           this.appendToolResult(rejected, toolResults);
                           failCount++;
                           continue;
                        }
                     }

                     if (WRITE_TOOLS.contains(call.getName())
                        && !TubiCopilotSettings.getInstance().autoApproveWriteTools
                        && !this.callbacks.onToolApprovalRequired(call)) {
                        ToolResult rejection = new ToolResult(
                           call.getId(),
                           call.getName(),
                           false,
                           "USER DENIED this operation. Do NOT retry — the user chose to reject it. Acknowledge the denial and call task_done to finish, or ask the user what to do instead.",
                           0L
                        );
                        this.callbacks.onToolComplete(rejection);
                        this.appendToolResult(rejection, toolResults);
                        userDenied = true;
                        continue;
                     }

                     ToolResult result = this.executeTool(call);
                     this.callbacks.onToolComplete(result);
                     this.appendToolResult(result, toolResults);
                     if (!result.isSuccess()) {
                        failCount++;
                     }

                     if ("edit_file".equals(call.getName()) && result.isSuccess()) {
                        String callPath = call.getArguments() != null ? call.getArguments().getOrDefault("path", "") : "";
                        if (!callPath.isBlank()) {
                           editedPaths.add(callPath);
                        }
                     }
                     continue;
                  }

                  if (failCount > 0 && !userDenied) {
                     ToolResult rejected = new ToolResult(
                        call.getId(),
                        call.getName(),
                        false,
                        "REJECTED: "
                           + failCount
                           + " tool call(s) failed in this response. Review the errors above, fix the issues, then call "
                           + call.getName()
                           + " in a NEW response.",
                        0L
                     );
                     this.callbacks.onToolComplete(rejected);
                     this.appendToolResult(rejected, toolResults);
                     continue;
                  }

                  if ("plan_done".equals(call.getName()) && toolCalls.size() > 1) {
                     ToolResult rejected = new ToolResult(
                        call.getId(),
                        call.getName(),
                        false,
                        "plan_done must be the ONLY tool call in your response. First, finish your exploration and output the plan text. Then call plan_done alone in a separate response.",
                        0L
                     );
                     this.callbacks.onToolComplete(rejected);
                     this.appendToolResult(rejected, toolResults);
                     continue;
                  }

                  ToolResult result = this.executeTool(call);
                  this.callbacks.onToolComplete(result);
                  String summary = call.getArguments() != null ? call.getArguments().getOrDefault("summary", "") : "";
                  if (!summary.isBlank()) {
                     this.callbacks.onAssistantText(summary);
                  }

                  done = true;
               }

               if (done) {
                  LOG.info("AgentLoop: task_done at iteration " + totalIterations);
                  this.callbacks.onLoopFinished(content, lastMessageId, conversationId);
                  return true;
               }

               String rawResultsx = toolResults.toString().trim();
               String questionPayload;
               if (rawResultsx.length() > 20000) {
                  String endTag = "</tool_result>";
                  int cutPoint = rawResultsx.lastIndexOf("</tool_result>", 20000);
                  if (cutPoint >= 0) {
                     cutPoint += "</tool_result>".length();
                  } else {
                     cutPoint = 20000;
                  }

                  questionPayload = rawResultsx.substring(0, cutPoint)
                     + "\n\n... [TRUNCATED at "
                     + cutPoint
                     + " chars] Output too large. Use pagination parameters (start_line/end_line for read_file, offset for search_code, path for list_files/git_diff) to retrieve data in smaller chunks.";
               } else {
                  questionPayload = rawResultsx;
               }

               currentRequest = ChatRequest.builder()
                  .question(questionPayload)
                  .parentMessageId(lastMessageId)
                  .conversationId(conversationId)
                  .modelId(currentRequest.getModelId())
                  .metadata(initialRequest.getMetadata())
                  .build();
               break;
            }
         }
      }

      LOG.info("AgentLoop: cancelled at iteration " + totalIterations);
      return false;
   }

   private void appendToolResult(@NotNull ToolResult result, @NotNull StringBuilder toolResults) {
      toolResults.append("<tool_result>{\"id\":")
         .append(GSON.toJson(result.getToolCallId()))
         .append(",\"status\":\"")
         .append(result.isSuccess() ? "ok" : "error")
         .append("\",\"output\":")
         .append(GSON.toJson(result.getOutput()))
         .append("}</tool_result>\n");
   }

   @Nullable
   private AgentLoopRunner.LoopStepResult sendAndWait(@NotNull ChatRequest request) {
      final CountDownLatch latch = new CountDownLatch(1);
      this.activeLatch = latch;
      final AtomicReference<String> contentRef = new AtomicReference<>();
      final AtomicReference<String> messageIdRef = new AtomicReference<>();
      final AtomicReference<String> conversationIdRef = new AtomicReference<>();
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      this.apiClient.sendMessage(request, new StreamListener() {
         @Override
         public void onStart() {
         }

         @Override
         public void onToken(@NotNull String delta) {
         }

         @Override
         public void onComplete(@NotNull String fullContent, @Nullable String messageId, @Nullable String conversationId) {
            contentRef.set(fullContent);
            messageIdRef.set(messageId);
            conversationIdRef.set(conversationId);
            latch.countDown();
         }

         @Override
         public void onError(@NotNull Throwable error) {
            errorRef.set(error);
            latch.countDown();
         }
      });

      try {
         if (!latch.await(180L, TimeUnit.SECONDS)) {
            this.activeLatch = null;
            this.apiClient.cancel();
            throw new RuntimeException("API response timed out after 180s");
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Agent loop interrupted", e);
      } finally {
         this.activeLatch = null;
      }

      if (this.cancelled.get()) {
         return null;
      } else if (errorRef.get() != null) {
         throw new RuntimeException("API error", errorRef.get());
      } else {
         return new AgentLoopRunner.LoopStepResult(contentRef.get(), messageIdRef.get(), conversationIdRef.get());
      }
   }

   @NotNull
   private ToolResult executeTool(@NotNull ToolCall call) {
      long start = System.currentTimeMillis();
      ToolExecutor executor = ToolRegistry.getInstance().get(call.getName());
      if (executor == null) {
         return new ToolResult(call.getId(), call.getName(), false, "Error: unknown tool '" + call.getName() + "'", System.currentTimeMillis() - start);
      }

      try {
         String output = executor.execute(call.getArguments(), this.project);
         return new ToolResult(call.getId(), call.getName(), true, output, System.currentTimeMillis() - start);
      } catch (Exception e) {
         LOG.warn("AgentLoop: tool failed: " + call.getName(), e);
         String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
         return new ToolResult(call.getId(), call.getName(), false, "Error (" + e.getClass().getSimpleName() + "): " + msg, System.currentTimeMillis() - start);
      }
   }

   private void saveDebugResponse(@NotNull String content, int iteration) {
      try {
         LastSendSnapshotWriter.saveDebugBundle(
            "agent_parse_fail_iter" + iteration + ".md",
            "# Parse failure at iteration "
               + iteration
               + "\n\nContent length: "
               + content.length()
               + "\nHas <tool_call>: "
               + content.contains("<tool_call>")
               + "\nHas </tool_call>: "
               + content.contains("</tool_call>")
               + "\n\n## Raw content\n\n```\n"
               + content
               + "\n```\n",
            this.project
         );
      } catch (Exception e) {
         LOG.warn("AgentLoop: failed to save debug response", e);
      }
   }

   private static final class LoopStepResult {
      final String fullContent;
      final String messageId;
      final String conversationId;

      LoopStepResult(String fullContent, String messageId, String conversationId) {
         this.fullContent = fullContent;
         this.messageId = messageId;
         this.conversationId = conversationId;
      }
   }
}
