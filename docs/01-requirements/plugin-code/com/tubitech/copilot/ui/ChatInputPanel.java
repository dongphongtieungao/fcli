package com.tubitech.copilot.ui;

import com.google.gson.Gson;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.tubitech.copilot.agent.AgentNameResolver;
import com.tubitech.copilot.agent.FileChangeProposal;
import com.tubitech.copilot.agent.FileProposalParser;
import com.tubitech.copilot.agent.InstructionsFileManager;
import com.tubitech.copilot.agent.ResponseNormalizer;
import com.tubitech.copilot.agent.SystemPromptLoader;
import com.tubitech.copilot.agent.loop.AgentLoopCallbacks;
import com.tubitech.copilot.agent.loop.AgentLoopRunner;
import com.tubitech.copilot.agent.loop.ToolCallParser;
import com.tubitech.copilot.agent.tools.ToolCall;
import com.tubitech.copilot.agent.tools.ToolRegistry;
import com.tubitech.copilot.agent.tools.ToolResult;
import com.tubitech.copilot.api.AgentApiClient;
import com.tubitech.copilot.api.AgentApiClientImpl;
import com.tubitech.copilot.api.FileUploadListener;
import com.tubitech.copilot.api.StreamListener;
import com.tubitech.copilot.api.model.AgentDetail;
import com.tubitech.copilot.api.model.AgentSummary;
import com.tubitech.copilot.api.model.AttachmentRef;
import com.tubitech.copilot.api.model.ChatRequest;
import com.tubitech.copilot.api.model.CreateAgentRequest;
import com.tubitech.copilot.api.model.FileReference;
import com.tubitech.copilot.api.model.MessageMetadata;
import com.tubitech.copilot.editor.MentionBundleService;
import com.tubitech.copilot.editor.MentionParser;
import com.tubitech.copilot.editor.MentionSuggestion;
import com.tubitech.copilot.editor.MentionSuggestionProvider;
import com.tubitech.copilot.editor.bundle.LastSendSnapshotWriter;
import com.tubitech.copilot.history.ConversationSession;
import com.tubitech.copilot.history.HistoryMessage;
import com.tubitech.copilot.history.ProjectHistoryService;
import com.tubitech.copilot.settings.ChatMode;
import com.tubitech.copilot.settings.ModelCapability;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import com.tubitech.copilot.strategy.ChatModeStrategy;
import com.tubitech.copilot.strategy.ChatModeStrategyFactory;
import com.tubitech.copilot.strategy.SendPayload;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChatInputPanel extends JBPanel<ChatInputPanel> {
   private static final Logger LOG = Logger.getInstance(ChatInputPanel.class);
   private static final int UPLOAD_TIMEOUT_SECONDS = 30;
   private static final int MAX_QUESTION_CHARS = 18000;
   private static final int MIN_INPUT_HEIGHT_PX = 26;
   private static final int MAX_INPUT_HEIGHT_PX = 108;
   private final Project project;
   private final ChatPanel chatPanel;
   private final JBTextArea inputTextArea;
   private final InlineNoticeBar inlineNoticeBar = new InlineNoticeBar();
   private ConversationSession currentSession;
   @Nullable
   private MessageBubble currentAssistantBubble;
   private final AtomicBoolean streaming = new AtomicBoolean(false);
   @Nullable
   private String pendingConversationName;
   @Nullable
   private volatile String lastSyncedModelId;
   @Nullable
   private volatile String lastSyncedInstructionsHash;
   private final List<AttachmentRef> pendingImageRefs = new ArrayList<>();
   private final List<String> pendingImageNames = new ArrayList<>();
   private final AtomicInteger pastedImageCounter = new AtomicInteger(0);
   private final KeyEventDispatcher imagePasteDispatcher;
   private final MentionAutocompletePopup mentionPopup;
   private final Timer mentionDebounceTimer;
   private DocumentListener layoutDocListener;
   private DocumentListener mentionDocListener;
   private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");

   public ChatInputPanel(@NotNull Project project, @NotNull ChatPanel chatPanel) {
      super(new BorderLayout());
      this.project = project;
      this.chatPanel = chatPanel;
      this.currentSession = ConversationSession.newSession();
      this.inputTextArea = new JBTextArea(1, 0) {
         public Dimension getPreferredScrollableViewportSize() {
            Dimension natural = this.getPreferredSize();
            int minH = JBUI.scale(26);
            int maxH = JBUI.scale(108);
            return new Dimension(natural.width, Math.min(Math.max(natural.height, minH), maxH));
         }
      };
      this.inputTextArea.setLineWrap(true);
      this.inputTextArea.setWrapStyleWord(true);
      this.inputTextArea.setBorder(Borders.empty());
      this.inputTextArea.setOpaque(false);
      this.updatePlaceholder(TubiCopilotSettings.getInstance().getSelectedChatMode());
      InputMap im = this.inputTextArea.getInputMap();
      im.put(KeyStroke.getKeyStroke(10, 0), "tubiSendOrStop");
      im.put(KeyStroke.getKeyStroke(10, 64), "tubiInsertNewline");
      ActionMap am = this.inputTextArea.getActionMap();
      am.put("tubiSendOrStop", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ChatInputPanel.this.handleSendOrStop();
         }
      });
      am.put("tubiInsertNewline", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ChatInputPanel.this.inputTextArea.insert("\n", ChatInputPanel.this.inputTextArea.getCaretPosition());
         }
      });
      this.imagePasteDispatcher = e -> {
         if (e.getID() != 401) {
            return false;
         } else if (e.getKeyCode() != 86) {
            return false;
         } else if ((e.getModifiersEx() & 128) == 0) {
            return false;
         } else if (!this.inputTextArea.isFocusOwner()) {
            return false;
         } else {
            LOG.info("Tubi image paste: Ctrl+V detected, checking clipboard...");
            if (this.tryPasteImageFromClipboard()) {
               e.consume();
               return true;
            } else {
               LOG.info("Tubi image paste: no image in clipboard, falling through to text paste");
               return false;
            }
         }
      };
      this.layoutDocListener = new DocumentListener() {
         private void refresh() {
            ChatInputPanel.this.revalidate();
            ChatInputPanel.this.repaint();
         }

         @Override
         public void insertUpdate(DocumentEvent e) {
            this.refresh();
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            this.refresh();
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
         }
      };
      this.inputTextArea.getDocument().addDocumentListener(this.layoutDocListener);
      this.mentionPopup = new MentionAutocompletePopup(project, this.inputTextArea, this::onMentionSelected);
      this.mentionDebounceTimer = new Timer(150, e -> this.fireMentionQuery());
      this.mentionDebounceTimer.setRepeats(false);
      this.inputTextArea.addKeyListener(this.mentionKeyListener());
      this.mentionDocListener = this.mentionDocumentListener();
      this.inputTextArea.getDocument().addDocumentListener(this.mentionDocListener);
      JBScrollPane inputScrollPane = new JBScrollPane(this.inputTextArea);
      inputScrollPane.setBorder(Borders.empty());
      inputScrollPane.setViewportBorder(Borders.empty(6, 8, 6, 8));
      inputScrollPane.setVerticalScrollBarPolicy(20);
      inputScrollPane.setHorizontalScrollBarPolicy(31);
      this.add(inputScrollPane, "Center");
      this.add(this.inlineNoticeBar, "South");
   }

   @NotNull
   public ConversationSession getCurrentSession() {
      return this.currentSession;
   }

   public void startNewSession() {
      this.currentSession = ConversationSession.newSession();
      synchronized (this.pendingImageRefs) {
         this.pendingImageRefs.clear();
         this.pendingImageNames.clear();
      }

      this.chatPanel.getAttachmentPreviewPanel().clear();
   }

   public void setPendingConversationName(@Nullable String name) {
      this.pendingConversationName = name;
   }

   public void loadSession(@NotNull ConversationSession session) {
      this.currentSession = session;
      this.chatPanel.getAttachmentPreviewPanel().setVisible(false);
   }

   public void addNotify() {
      super.addNotify();
      if (this.layoutDocListener != null) {
         this.inputTextArea.getDocument().removeDocumentListener(this.layoutDocListener);
         this.inputTextArea.getDocument().addDocumentListener(this.layoutDocListener);
      }

      if (this.mentionDocListener != null) {
         this.inputTextArea.getDocument().removeDocumentListener(this.mentionDocListener);
         this.inputTextArea.getDocument().addDocumentListener(this.mentionDocListener);
      }

      KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.imagePasteDispatcher);
   }

   public void removeNotify() {
      super.removeNotify();
      this.inlineNoticeBar.dispose();
      this.mentionDebounceTimer.stop();
      this.mentionPopup.hide();
      if (this.layoutDocListener != null) {
         this.inputTextArea.getDocument().removeDocumentListener(this.layoutDocListener);
      }

      if (this.mentionDocListener != null) {
         this.inputTextArea.getDocument().removeDocumentListener(this.mentionDocListener);
      }

      KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.imagePasteDispatcher);
   }

   public void showSuccess(@NotNull String message) {
      this.inlineNoticeBar.show(InlineNoticeBar.NoticeType.SUCCESS, message);
   }

   public void showValidationError(@NotNull String message) {
      this.inlineNoticeBar.show(InlineNoticeBar.NoticeType.VALIDATION, message);
   }

   public void showSystemError(@NotNull String message) {
      this.inlineNoticeBar.show(InlineNoticeBar.NoticeType.SYSTEM, message);
   }

   public void updatePlaceholder(@NotNull ChatMode mode) {
      this.inputTextArea.getEmptyText().clear();

      String hint = switch (mode) {
         case AGENT -> "Ask Tubi to do something…  (has tool access to read/edit files)";
         case PLAN -> "Describe what to plan…  (has tool access to read/edit files)";
         default -> "Ask anything…  @file or @folder to attach";
      };
      this.inputTextArea.getEmptyText().setText(hint);

      try {
         Method m = this.inputTextArea.getEmptyText().getClass().getMethod("setTextAlignment", float.class);
         m.invoke(this.inputTextArea.getEmptyText(), 0.0F);
      } catch (ReflectiveOperationException var4) {
      }
   }

   public JBTextArea getInputTextArea() {
      return this.inputTextArea;
   }

   public void handleSendOrStop() {
      if (this.streaming.get()) {
         this.chatPanel.getConversationApiClient().cancel();
         AgentLoopRunner runner = this.chatPanel.getActiveAgentLoopRunner();
         if (runner != null) {
            runner.cancel();
            this.chatPanel.setActiveAgentLoopRunner(null);
         }
      } else {
         this.sendMessage();
      }
   }

   private void sendMessage() {
      if (!this.streaming.get()) {
         if (!EolEnforcementService.isExpired()) {
            String rawText = this.inputTextArea.getText().trim();
            if (rawText.isEmpty()) {
               this.showValidationError("Message cannot be empty");
            } else {
               PasswordSafe ps = PasswordSafe.getInstance();
               Credentials refreshCreds = ps.get(TubiCopilotSettings.getRefreshTokenCredentialAttributes());
               String refreshToken = refreshCreds == null ? null : refreshCreds.getPasswordAsString();
               if (refreshToken != null && !refreshToken.isBlank()) {
                  TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
                  String modelId = settings.getSelectedModelId();
                  ChatMode requestedMode = settings.getSelectedChatMode();
                  ChatMode effectiveMode = ModelCapability.effectiveMode(requestedMode, modelId);
                  if (effectiveMode != requestedMode) {
                     this.chatPanel
                        .showSyncResult(
                           InlineNoticeBar.NoticeType.SYSTEM, "This model does not support " + requestedMode.name() + " mode. Using Ask mode instead."
                        );
                  }

                  ChatModeStrategy strategy = ChatModeStrategyFactory.get(effectiveMode);
                  String validationError = strategy.validate(rawText);
                  if (!validationError.isEmpty()) {
                     this.showValidationError(validationError);
                  } else {
                     this.sendWithStrategy(rawText, strategy);
                  }
               } else {
                  this.chatPanel.showError("API token not configured. Go to Settings â†' Tools â†' Tubi Copilot.");
               }
            }
         }
      }
   }

   private void sendWithStrategy(@NotNull String rawText, @NotNull ChatModeStrategy strategy) {
      SendPayload payload;
      try {
         payload = strategy.buildPayload(rawText, this.project);
      } catch (Exception e) {
         this.showValidationError(e.getMessage() != null ? e.getMessage() : "Failed to build payload");
         return;
      }

      this.currentSession.addUserMessage(rawText, null);
      this.setStreamingState(true);
      this.inputTextArea.setText("");
      List<String> imageNames;
      List<AttachmentRef> capturedImageRefs;
      synchronized (this.pendingImageRefs) {
         imageNames = this.pendingImageNames.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.pendingImageNames);
         capturedImageRefs = this.pendingImageRefs.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.pendingImageRefs);
         this.pendingImageRefs.clear();
         this.pendingImageNames.clear();
      }

      this.chatPanel.addUserBubble(rawText, imageNames);
      this.chatPanel.showTypingIndicator();
      this.chatPanel.getAttachmentPreviewPanel().clear();
      ProjectHistoryService historyService = ProjectHistoryService.getInstance(this.project);
      int turnIndex = historyService.getNextTurnIndex(this.currentSession.id);
      if (turnIndex == 0) {
         historyService.insertSession(
            this.currentSession.id,
            this.currentSession.title,
            this.currentSession.createdAt,
            this.currentSession.updatedAt,
            this.currentSession.serverConversationId
         );
      }

      String userMeta = imageNames.isEmpty() ? null : new Gson().toJson(Map.of("attachments", imageNames));
      historyService.insertMessage(HistoryMessage.userMessage(this.currentSession.id, turnIndex, rawText, userMeta));
      if (this.currentSession.title != null) {
         historyService.updateSessionTitle(this.currentSession.id, this.currentSession.title);
      }

      historyService.updateSessionTimestamp(this.currentSession.id, System.currentTimeMillis());
      TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
      ApplicationManager.getApplication()
         .executeOnPooledThread(
            () -> {
               LastSendSnapshotWriter.clearDebugBundles(this.project);
               LastSendSnapshotWriter.saveChatPrompt(rawText, this.project);
               LastSendSnapshotWriter.saveChatAfter(payload.getQuestion(), this.project);
               List<AttachmentRef> contextRefs = new ArrayList<>();
               List<String> ctxPaths = payload.getContextPaths() != null ? payload.getContextPaths() : Collections.emptyList();
               List<String> reqPaths = payload.getRequestPaths() != null ? payload.getRequestPaths() : Collections.emptyList();
               MentionBundleService.BundleUploadResult bundleResult;
               if (strategy.canUseTools()) {
                  LastSendSnapshotWriter.saveBundleContext(null, this.project);
                  bundleResult = null;
               } else if (reqPaths.isEmpty() && ctxPaths.isEmpty()) {
                  bundleResult = this.uploadMentionSingleBundle(rawText, contextRefs);
               } else {
                  bundleResult = this.uploadMentionSingleBundle(reqPaths, ctxPaths, contextRefs);
               }

               contextRefs.addAll(capturedImageRefs);
               String copilotAgentId = settings.getCopilotAgentId();
               if (copilotAgentId.isBlank()) {
                  copilotAgentId = this.ensureAgentExists(settings);
               }

               String effectiveAgentId = copilotAgentId.isBlank() ? null : copilotAgentId;
               this.syncAgentIfNeeded(settings, effectiveAgentId);
               String reasoningEffort = strategy.canUseTools() ? "high" : null;
               MessageMetadata metadata = new MessageMetadata(contextRefs, reasoningEffort, effectiveAgentId);
               boolean serverHasInstructions = effectiveAgentId != null;
               String contextBlock;
               if (bundleResult != null) {
                  contextBlock = buildBundleAttachedInstruction(bundleResult);
               } else {
                  contextBlock = "";
               }

               String userPart = contextBlock + payload.getQuestion();
               String injectedQuestion;
               if (serverHasInstructions) {
                  injectedQuestion = strategy.getModeTag() + "\n" + userPart;
               } else {
                  String modePrompt = strategy.getSystemPrompt();
                  String customInstructions = InstructionsFileManager.readAll(this.project);
                  String customText = customInstructions != null ? customInstructions : "";
                  String overflowErr = InstructionsFileManager.validate(modePrompt, customText);
                  if (overflowErr != null) {
                     SwingUtilities.invokeLater(() -> {
                        this.setStreamingState(false);
                        this.chatPanel.showSyncResult(InlineNoticeBar.NoticeType.VALIDATION, "Instructions overflow: " + overflowErr);
                     });
                     return;
                  }

                  String fullSystemPrompt = InstructionsFileManager.buildInstructions(modePrompt, customText).getInstructions();
                  String toolBlock = strategy.canUseTools() ? "\n\n" + ToolRegistry.getInstance().generateToolsPromptBlock() : "";
                  String systemBlock = "<system_instructions>\n" + fullSystemPrompt + toolBlock + "\n</system_instructions>";
                  injectedQuestion = systemBlock + "\n\n" + userPart;
               }

               ChatRequest request = ChatRequest.builder()
                  .question(injectedQuestion)
                  .parentMessageId(this.chatPanel.getLastMessageId())
                  .conversationId(this.chatPanel.getCurrentConversationId())
                  .modelId(settings.getSelectedModelId())
                  .metadata(metadata)
                  .build();
               if (strategy.canUseTools()) {
                  this.runAgentLoop(request, strategy);
               } else {
                  this.chatPanel.getConversationApiClient().sendMessage(request, this.buildStreamListener());
               }
            }
         );
   }

   private void syncAgentIfNeeded(@NotNull TubiCopilotSettings settings, @Nullable String agentId) {
      if (agentId != null && !agentId.isBlank()) {
         String currentModel = settings.getSelectedModelId();
         String instructions = this.buildFullServerInstructions();
         String instrHash = Integer.toHexString(instructions.hashCode());
         boolean modelChanged = !currentModel.equals(this.lastSyncedModelId);
         boolean instrChanged = !instrHash.equals(this.lastSyncedInstructionsHash);
         if (modelChanged || instrChanged) {
            try {
               AgentApiClient client = new AgentApiClientImpl(settings);
               String agentName = AgentNameResolver.copilotAgentName(this.project);
               client.updateAgent(agentId, new CreateAgentRequest(agentName, "", instructions, currentModel));
               this.lastSyncedModelId = currentModel;
               this.lastSyncedInstructionsHash = instrHash;
               LOG.info("Auto-synced agent: model=" + currentModel + ", instructions=" + instructions.length() + " chars");
            } catch (Exception e) {
               LOG.warn("Failed to auto-sync agent", e);
            }
         }
      }
   }

   @NotNull
   private String ensureAgentExists(@NotNull TubiCopilotSettings settings) {
      String var10;
      try {
         AgentApiClient client = new AgentApiClientImpl(settings);
         String agentName = AgentNameResolver.copilotAgentName(this.project);
         String instructions = this.buildFullServerInstructions();

         for (AgentSummary s : client.listMyAgents()) {
            if (agentName.equalsIgnoreCase(s.getName())) {
               settings.setCopilotAgentId(s.getId());
               LOG.info("Found existing agent: " + s.getId());
               return s.getId();
            }
         }

         AgentDetail created = client.createAgent(new CreateAgentRequest(agentName, "", instructions, settings.getSelectedModelId()));
         settings.setCopilotAgentId(created.getId());
         this.lastSyncedModelId = settings.getSelectedModelId();
         this.lastSyncedInstructionsHash = Integer.toHexString(instructions.hashCode());
         LOG.info("Auto-created agent: " + created.getId());
         var10 = created.getId();
      } catch (Exception e) {
         LOG.warn("Failed to auto-create agent", e);
         return "";
      }

      return var10;
   }

   @NotNull
   private String buildFullServerInstructions() {
      String basePrompt = SystemPromptLoader.load("/copilot-base-system.instructions.md");
      String askPrompt = SystemPromptLoader.load("/ask-system.instructions.md");
      String agentPrompt = SystemPromptLoader.load("/agent-system.instructions.md");
      String planPrompt = SystemPromptLoader.load("/plan-system.instructions.md");
      String toolBlock = ToolRegistry.getInstance().generateToolsPromptBlock(false);
      String custom = InstructionsFileManager.readAll(this.project);
      String fullBase = basePrompt + "\n\n---\n\n" + askPrompt + "\n\n---\n\n" + agentPrompt + "\n\n---\n\n" + planPrompt + "\n\n" + toolBlock;
      return InstructionsFileManager.buildInstructions(fullBase, custom != null ? custom : "").getInstructions();
   }

   private void runAgentLoop(@NotNull ChatRequest request, @NotNull final ChatModeStrategy strategy) {
      final AgentProgressPanel[] progressHolder = new AgentProgressPanel[1];
      final ToolCallRow[] currentRowHolder = new ToolCallRow[1];
      final MessageBubble[] agentBubbleHolder = new MessageBubble[1];
      SwingUtilities.invokeLater(() -> {
         this.chatPanel.hideTypingIndicator();
         agentBubbleHolder[0] = this.chatPanel.addAssistantBubble();
         progressHolder[0] = new AgentProgressPanel();
         agentBubbleHolder[0].embedAgentProgress(progressHolder[0]);
         this.chatPanel.scrollToBottomLater();
      });
      boolean planPhase1 = strategy.requiresConfirmation();
      AgentLoopCallbacks loopCallbacks = new AgentLoopCallbacks() {
         @Override
         public void onToolExecuting(@NotNull ToolCall toolCall) {
            ApplicationManager.getApplication()
               .executeOnPooledThread(
                  () -> {
                     ProjectHistoryService hs = ProjectHistoryService.getInstance(ChatInputPanel.this.project);
                     int turn = hs.getNextTurnIndex(ChatInputPanel.this.currentSession.id);
                     String argsJson = toolCall.getArguments() != null ? new Gson().toJson(toolCall.getArguments()) : "{}";
                     String callContent = "<tool_call>{\"id\":\""
                        + toolCall.getId()
                        + "\",\"name\":\""
                        + toolCall.getName()
                        + "\",\"arguments\":"
                        + argsJson
                        + "}</tool_call>";
                     hs.insertMessage(HistoryMessage.toolCallMessage(ChatInputPanel.this.currentSession.id, turn, callContent, null));
                  }
               );
            SwingUtilities.invokeLater(
               () -> {
                  if (progressHolder[0] != null) {
                     currentRowHolder[0] = progressHolder[0]
                        .addToolCall(toolCall.getName(), toolCall.getName() + "(" + ChatInputPanel.summarizeArgs(toolCall) + ")");
                  }

                  ChatInputPanel.this.chatPanel.scrollToBottomLater();
               }
            );
         }

         @Override
         public void onToolComplete(@NotNull ToolResult result) {
            ChatInputPanel.LOG
               .info("AgentLoop: " + result.getToolName() + " -> " + (result.isSuccess() ? "OK" : "FAIL") + " (" + result.getDurationMs() + "ms)");
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
               ProjectHistoryService hs = ProjectHistoryService.getInstance(ChatInputPanel.this.project);
               int turn = hs.getNextTurnIndex(ChatInputPanel.this.currentSession.id);
               hs.insertMessage(HistoryMessage.toolResultMessage(ChatInputPanel.this.currentSession.id, turn, result.toResponseBlock(), null));
            });
            SwingUtilities.invokeLater(() -> {
               if (currentRowHolder[0] != null && progressHolder[0] != null) {
                  if (result.isSuccess()) {
                     progressHolder[0].markToolComplete(currentRowHolder[0], result.getDurationMs());
                  } else {
                     progressHolder[0].markToolFailed(currentRowHolder[0], result.getOutput());
                  }

                  currentRowHolder[0] = null;
               }
            });
         }

         @Override
         public boolean onToolApprovalRequired(@NotNull ToolCall toolCall) {
            try {
               return ChatInputPanel.this.chatPanel.showToolApproval(toolCall).get(120L, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
               ChatInputPanel.LOG.warn("Tool approval timed out, rejecting: " + toolCall.getName());
               return false;
            } catch (Exception e) {
               return false;
            }
         }

         @Override
         public void onAssistantText(@NotNull String text) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
               ProjectHistoryService hs = ProjectHistoryService.getInstance(ChatInputPanel.this.project);
               int turn = hs.getNextTurnIndex(ChatInputPanel.this.currentSession.id);
               hs.insertMessage(HistoryMessage.systemMessage(ChatInputPanel.this.currentSession.id, turn, text, "{\"type\":\"intermediate\"}"));
            });
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               if (progressHolder[0] != null) {
                  progressHolder[0].addIntermediateText(text);
               }

               ChatInputPanel.this.chatPanel.scrollToBottomLater();
            });
         }

         @Override
         public void onLoopIteration(int iteration, int maxIterations) {
            ChatInputPanel.LOG.info("AgentLoop: iteration " + iteration + "/" + maxIterations);
            if (iteration > 0) {
               SwingUtilities.invokeLater(() -> ChatInputPanel.this.chatPanel.scrollToBottomLater());
            }
         }

         @Override
         public void onWaitingForResponse() {
            SwingUtilities.invokeLater(() -> {
               if (progressHolder[0] != null) {
                  progressHolder[0].showWaitingForResponse();
                  ChatInputPanel.this.chatPanel.scrollToBottomLater();
               }
            });
         }

         @Override
         public boolean onIterationLimitReached(int iterationsUsed) {
            try {
               return ChatInputPanel.this.chatPanel.showContinuePrompt(iterationsUsed).get();
            } catch (Exception e) {
               return false;
            }
         }

         @Override
         public void onLoopFinished(@NotNull String finalResponse, @Nullable String messageId, @Nullable String conversationId) {
            String normalizedContent = ResponseNormalizer.normalize(finalResponse);
            String visibleText = ToolCallParser.stripToolCalls(normalizedContent);
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
               ProjectHistoryService hs = ProjectHistoryService.getInstance(ChatInputPanel.this.project);
               int turn = hs.getNextTurnIndex(ChatInputPanel.this.currentSession.id);
               hs.insertMessage(HistoryMessage.assistantMessage(ChatInputPanel.this.currentSession.id, turn, visibleText, null, messageId));
               if (conversationId != null) {
                  hs.updateServerConversationId(ChatInputPanel.this.currentSession.id, conversationId);
                  ChatInputPanel.this.currentSession.serverConversationId = conversationId;
               }

               hs.updateSessionTimestamp(ChatInputPanel.this.currentSession.id, System.currentTimeMillis());
            });
            ChatInputPanel.this.chatPanel.setActiveAgentLoopRunner(null);
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               if (progressHolder[0] != null) {
                  progressHolder[0].markLoopFinished();
               }

               ChatInputPanel.this.currentSession.addAssistantMessage(visibleText, messageId);
               ChatInputPanel.this.chatPanel.setLastMessageId(messageId);
               if (conversationId != null) {
                  ChatInputPanel.this.chatPanel.setCurrentConversationId(conversationId);
               }

               if (agentBubbleHolder[0] != null && !visibleText.isBlank()) {
                  agentBubbleHolder[0].finalizeWithMarkdown(visibleText, ChatInputPanel.this.project);
               }

               ChatInputPanel.this.resetInput();
               ChatInputPanel.this.chatPanel.scrollToBottomLater();
            });
            if (strategy.shouldParseFileProposals()) {
               List<FileChangeProposal> proposals = FileProposalParser.parse(normalizedContent, ChatInputPanel.this.project);
               if (!proposals.isEmpty()) {
                  SwingUtilities.invokeLater(() -> ChatInputPanel.this.chatPanel.showAgentChangesPanel(proposals));
               }
            }

            if (strategy.requiresConfirmation() && !visibleText.isBlank()) {
               String fMsgId = messageId;
               String fConvId = conversationId;
               ChatInputPanel.this.chatPanel
                  .showPlanConfirmation(
                     () -> ChatInputPanel.this.executePlanContinuation(strategy, fMsgId, fConvId),
                     () -> ChatInputPanel.LOG.info("Plan: user cancelled execution")
                  );
            }
         }

         @Override
         public void onLoopError(@NotNull Throwable error) {
            ChatInputPanel.this.chatPanel.setActiveAgentLoopRunner(null);
            ChatInputPanel.LOG.warn("AgentLoop: error", error);
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               if (progressHolder[0] != null) {
                  progressHolder[0].markLoopError(error.getMessage() != null ? error.getMessage() : "Unknown error");
               }

               String msg = error.getMessage() != null ? error.getMessage() : "An unexpected error occurred.";
               ChatInputPanel.this.chatPanel.showError(msg);
               ChatInputPanel.this.showSystemError(msg);
               ChatInputPanel.this.resetInput();
            });
         }
      };
      String convId = this.currentSession.serverConversationId != null ? this.currentSession.serverConversationId : "";
      AgentLoopRunner runner = new AgentLoopRunner(this.chatPanel.getConversationApiClient(), this.project, loopCallbacks, planPhase1, convId);
      this.chatPanel.setActiveAgentLoopRunner(runner);
      runner.run(request);
   }

   private void executePlanContinuation(@NotNull ChatModeStrategy strategy, @Nullable String parentMessageId, @Nullable String conversationId) {
      TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
      SwingUtilities.invokeLater(() -> {
         this.chatPanel.switchMode(ChatMode.AGENT);
         this.setStreamingState(true);
         this.chatPanel.addUserBubble("Continue", Collections.emptyList());
         this.chatPanel.showTypingIndicator();
         this.chatPanel.scrollToBottomLater();
      });
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         ProjectHistoryService hs = ProjectHistoryService.getInstance(this.project);
         int turn = hs.getNextTurnIndex(this.currentSession.id);
         hs.insertMessage(HistoryMessage.userMessage(this.currentSession.id, turn, "Continue", null));
      });
      String copilotAgentId = settings.getCopilotAgentId();
      boolean serverHasInstr = !copilotAgentId.isBlank();
      String injectedQuestion;
      if (serverHasInstr) {
         injectedQuestion = "[MODE: Agent]\nUser confirmed. Execute the plan now. Use <tool_call> to proceed.";
      } else {
         String sysPrompt = strategy.getSystemPrompt();
         String customInstr = InstructionsFileManager.readAll(this.project);
         String fullSys = InstructionsFileManager.buildInstructions(sysPrompt, customInstr != null ? customInstr : "").getInstructions();
         String tBlock = "\n\n" + ToolRegistry.getInstance().generateToolsPromptBlock();
         injectedQuestion = "<system_instructions>\n" + fullSys + tBlock + "\n</system_instructions>\n\ngo";
      }

      MessageMetadata metadata = new MessageMetadata(Collections.emptyList(), "high", copilotAgentId.isBlank() ? null : copilotAgentId);
      ChatRequest request = ChatRequest.builder()
         .question(injectedQuestion)
         .parentMessageId(parentMessageId)
         .conversationId(conversationId)
         .modelId(settings.getSelectedModelId())
         .metadata(metadata)
         .build();
      ChatModeStrategy execStrategy = ChatModeStrategyFactory.get(ChatMode.AGENT);
      ApplicationManager.getApplication().executeOnPooledThread(() -> this.runAgentLoop(request, execStrategy));
   }

   @NotNull
   private static String summarizeArgs(@NotNull ToolCall call) {
      Map<String, String> args = call.getArguments();
      if (args != null && !args.isEmpty()) {
         String name = call.getName();
         if ("read_file".equals(name) || "edit_file".equals(name) || "create_file".equals(name)) {
            String path = args.getOrDefault("path", "");
            if (!path.isEmpty()) {
               return path.length() > 40 ? "..." + path.substring(path.length() - 37) : path;
            }
         }

         if ("run_command".equals(name)) {
            String cmd = args.getOrDefault("command", "");
            if (!cmd.isEmpty()) {
               return cmd.length() > 50 ? cmd.substring(0, 47) + "..." : cmd;
            }
         }

         if ("search_code".equals(name)) {
            String pattern = args.getOrDefault("pattern", args.getOrDefault("query", ""));
            if (!pattern.isEmpty()) {
               return "\"" + (pattern.length() > 40 ? pattern.substring(0, 37) + "..." : pattern) + "\"";
            }
         }

         if ("list_files".equals(name)) {
            String path = args.getOrDefault("path", args.getOrDefault("directory", "."));
            return path.length() > 40 ? "..." + path.substring(path.length() - 37) : path;
         }

         if ("get_diagnostics".equals(name)) {
            String path = args.getOrDefault("path", args.getOrDefault("file", ""));
            if (!path.isEmpty()) {
               return path.length() > 40 ? "..." + path.substring(path.length() - 37) : path;
            }
         }

         String first = args.values().iterator().next();
         return first.length() <= 40 ? first : first.substring(0, 37) + "...";
      } else {
         return "";
      }
   }

   @NotNull
   private static String buildBundleAttachedInstruction(@NotNull MentionBundleService.BundleUploadResult bundle) {
      StringBuilder sb = new StringBuilder();
      sb.append("<context_files>\n");
      sb.append("The user referenced these files/folders. ");
      sb.append("Their content is attached in a single bundle file (tubi-bundle_ctx.md). ");
      sb.append("Use the attachment content directly:\n");

      for (String path : bundle.paths()) {
         sb.append("- @").append(path).append("\n");
      }

      sb.append("Total: ").append(bundle.entryCount()).append(" file(s)\n");
      sb.append("</context_files>\n\n");
      return sb.toString();
   }

   @NotNull
   private static List<String> extractContextRawTokens(@NotNull String rawText) {
      return MentionParser.extractTokens(rawText);
   }

   @NotNull
   private static String stripContextLines(@NotNull String rawText) {
      return MentionParser.stripMentions(rawText);
   }

   @Nullable
   private MentionBundleService.BundleUploadResult uploadMentionSingleBundle(
      @NotNull List<String> requestPaths, @NotNull List<String> contextPaths, @NotNull List<AttachmentRef> contextRefs
   ) {
      List<String> allPaths = new ArrayList<>();
      allPaths.addAll(requestPaths);
      allPaths.addAll(contextPaths);
      if (allPaths.isEmpty()) {
         LastSendSnapshotWriter.saveBundleContext(null, this.project);
         return null;
      }

      try {
         MentionBundleService.BundleUploadResult result = new MentionBundleService()
            .buildAndUploadSingleBundle(
               allPaths,
               this.project,
               this.chatPanel.getFileUploadApiClient(),
               this.chatPanel.getConversationApiClient(),
               this.currentSession.serverConversationId
            );
         if (result != null) {
            contextRefs.add(new AttachmentRef(result.fileId()));
            LOG.info("uploadMentionSingleBundle: uploaded 1 bundle (" + result.entryCount() + " file(s))");
         }

         return result;
      } catch (IOException e) {
         LOG.warn("uploadMentionSingleBundle: upload failed", e);
         LastSendSnapshotWriter.saveBundleContext(null, this.project);
         SwingUtilities.invokeLater(() -> this.showValidationError("Context upload failed: " + e.getMessage()));
         return null;
      }
   }

   @Nullable
   private MentionBundleService.BundleUploadResult uploadMentionSingleBundle(@NotNull String rawText, @NotNull List<AttachmentRef> contextRefs) {
      List<String> rawTokens = extractContextRawTokens(rawText);
      if (rawTokens.isEmpty()) {
         LastSendSnapshotWriter.saveBundleContext(null, this.project);
         return null;
      } else {
         List<String> contextPaths = rawTokens.stream().map(MentionParser::extractPath).filter(p -> !p.isBlank()).collect(Collectors.toList());
         return this.uploadMentionSingleBundle(Collections.emptyList(), contextPaths, contextRefs);
      }
   }

   @NotNull
   private StreamListener buildStreamListener() {
      return new StreamListener() {
         @Override
         public void onStart() {
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               ChatInputPanel.this.currentAssistantBubble = ChatInputPanel.this.chatPanel.addAssistantBubble();
            });
         }

         @Override
         public void onToken(@NotNull String delta) {
            SwingUtilities.invokeLater(() -> {
               if (ChatInputPanel.this.currentAssistantBubble != null) {
                  ChatInputPanel.this.currentAssistantBubble.appendToken(delta);
               }

               ChatInputPanel.this.chatPanel.scrollToBottomIfNear();
            });
         }

         @Override
         public void onComplete(@NotNull String fullContent, @Nullable String messageId, @Nullable String conversationId) {
            String normalizedContent = fullContent;
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
               ProjectHistoryService hs = ProjectHistoryService.getInstance(ChatInputPanel.this.project);
               int turn = hs.getNextTurnIndex(ChatInputPanel.this.currentSession.id);
               hs.insertMessage(HistoryMessage.assistantMessage(ChatInputPanel.this.currentSession.id, turn, normalizedContent, null, messageId));
               if (conversationId != null) {
                  hs.updateServerConversationId(ChatInputPanel.this.currentSession.id, conversationId);
                  ChatInputPanel.this.currentSession.serverConversationId = conversationId;
               }

               hs.updateSessionTimestamp(ChatInputPanel.this.currentSession.id, System.currentTimeMillis());
            });
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               ChatInputPanel.this.currentSession.addAssistantMessage(normalizedContent, messageId);
               ChatInputPanel.this.chatPanel.setLastMessageId(messageId);
               if (conversationId != null) {
                  ChatInputPanel.this.chatPanel.setCurrentConversationId(conversationId);
               }

               if (ChatInputPanel.this.currentAssistantBubble != null) {
                  ChatInputPanel.this.currentAssistantBubble.finalizeWithMarkdown(normalizedContent, ChatInputPanel.this.chatPanel.getProject());
               }

               ChatInputPanel.this.resetInput();
               ChatInputPanel.this.chatPanel.scrollToBottomLater();
            });
            ChatMode completionMode = TubiCopilotSettings.getInstance().getSelectedChatMode();
            if (completionMode == ChatMode.AGENT || completionMode == ChatMode.PLAN) {
               ApplicationManager.getApplication().executeOnPooledThread(() -> {
                  List<FileChangeProposal> proposals = FileProposalParser.parse(normalizedContent, ChatInputPanel.this.project);
                  if (!proposals.isEmpty()) {
                     SwingUtilities.invokeLater(() -> ChatInputPanel.this.chatPanel.showAgentChangesPanel(proposals));
                  }
               });
            }
         }

         @Override
         public void onError(@NotNull Throwable error) {
            ChatInputPanel.LOG.warn("Tubi Copilot: stream error", error);
            SwingUtilities.invokeLater(() -> {
               ChatInputPanel.this.chatPanel.hideTypingIndicator();
               String msg = error.getMessage() != null ? error.getMessage() : "An unexpected error occurred.";
               ChatInputPanel.this.chatPanel.showError(msg);
               ChatInputPanel.this.showSystemError(msg);
               ChatInputPanel.this.resetInput();
            });
         }
      };
   }

   public void setStreamingState(boolean streaming) {
      this.streaming.set(streaming);
      this.inputTextArea.setEnabled(!streaming);
      this.chatPanel.onStreamingStateChanged(streaming);
   }

   public void showNotice(@NotNull InlineNoticeBar.NoticeType type, @NotNull String message) {
      this.inlineNoticeBar.show(type, message);
   }

   private void resetInput() {
      this.setStreamingState(false);
      this.currentAssistantBubble = null;
      this.inputTextArea.requestFocusInWindow();
   }

   private static void collectFilesRecursively(@NotNull VirtualFile dir, @NotNull List<VirtualFile> out, int maxFiles) {
      if (out.size() < maxFiles) {
         for (VirtualFile child : dir.getChildren()) {
            if (out.size() >= maxFiles) {
               break;
            }

            if (child.isDirectory()) {
               collectFilesRecursively(child, out, maxFiles);
            } else {
               out.add(child);
            }
         }
      }
   }

   private DocumentListener mentionDocumentListener() {
      return new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(ChatInputPanel.this::scheduleMentionQuery);
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(ChatInputPanel.this::scheduleMentionQuery);
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(ChatInputPanel.this::scheduleMentionQuery);
         }
      };
   }

   private void scheduleMentionQuery() {
      ChatMode currentMode = TubiCopilotSettings.getInstance().getSelectedChatMode();
      if (currentMode != ChatMode.AGENT && currentMode != ChatMode.PLAN) {
         String query = this.extractMentionQuery();
         if (query == null) {
            this.mentionPopup.hide();
            this.mentionDebounceTimer.stop();
         } else {
            this.mentionDebounceTimer.restart();
         }
      } else {
         this.mentionPopup.hide();
         this.mentionDebounceTimer.stop();
      }
   }

   @Nullable
   private String extractMentionQuery() {
      String text = this.inputTextArea.getText();
      int caretPos = this.inputTextArea.getCaretPosition();
      int lineStart = caretPos;

      while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
         lineStart--;
      }

      String lineUpToCaret = text.substring(lineStart, caretPos);
      int lastAt = lineUpToCaret.lastIndexOf(64);
      if (lastAt < 0) {
         return null;
      } else {
         String afterAt = lineUpToCaret.substring(lastAt + 1);
         if (afterAt.startsWith("\"")) {
            return afterAt.indexOf(34, 1) >= 0 ? null : afterAt.substring(1);
         } else {
            return !afterAt.contains(" ") && !afterAt.contains("\t") ? afterAt : null;
         }
      }
   }

   private void fireMentionQuery() {
      String query = this.extractMentionQuery();
      if (query == null) {
         this.mentionPopup.hide();
      } else {
         int snapshotCaretPos = this.inputTextArea.getCaretPosition();
         ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<MentionSuggestion> results = (List<MentionSuggestion>)ReadAction.compute(() -> MentionSuggestionProvider.suggest(this.project, query));
            SwingUtilities.invokeLater(() -> {
               if (results.isEmpty()) {
                  this.mentionPopup.hide();
               } else if (this.mentionPopup.isVisible()) {
                  this.mentionPopup.update(results);
               } else {
                  try {
                     Rectangle2D caretRect = this.inputTextArea.modelToView2D(snapshotCaretPos);
                     if (caretRect != null) {
                        this.mentionPopup.show(results, (int)caretRect.getX(), (int)(caretRect.getY() + caretRect.getHeight()));
                     }
                  } catch (BadLocationException ex) {
                     LOG.warn("MentionAutocomplete: bad caret position", ex);
                  }
               }
            });
         });
      }
   }

   private KeyAdapter mentionKeyListener() {
      return new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (ChatInputPanel.this.mentionPopup.isVisible()) {
               switch (e.getKeyCode()) {
                  case 9:
                  case 10:
                     ChatInputPanel.this.mentionPopup.commitSelection();
                     e.consume();
                     break;
                  case 27:
                     ChatInputPanel.this.mentionPopup.hide();
                     e.consume();
                     break;
                  case 38:
                     ChatInputPanel.this.mentionPopup.moveSelectionUp();
                     e.consume();
                     break;
                  case 40:
                     ChatInputPanel.this.mentionPopup.moveSelectionDown();
                     e.consume();
               }
            }
         }
      };
   }

   private void onMentionSelected(@NotNull MentionSuggestion suggestion) {
      this.mentionPopup.hide();
      String text = this.inputTextArea.getText();
      int caretPos = this.inputTextArea.getCaretPosition();
      int atIndex = caretPos - 1;

      while (atIndex >= 0 && text.charAt(atIndex) != '@' && text.charAt(atIndex) != '\n') {
         atIndex--;
      }

      String path = suggestion.insertText();
      String insertionText = path.contains(" ") ? "@\"" + path + "\" " : "@" + path + " ";
      if (atIndex >= 0 && text.charAt(atIndex) == '@') {
         this.inputTextArea.select(atIndex, caretPos);
         this.inputTextArea.replaceSelection(insertionText);
         this.inputTextArea.setCaretPosition(atIndex + insertionText.length());
      } else {
         this.inputTextArea.insert(insertionText, caretPos);
         this.inputTextArea.setCaretPosition(caretPos + insertionText.length());
      }

      this.inputTextArea.requestFocusInWindow();
   }

   private static boolean isImageFile(@NotNull File file) {
      String name = file.getName().toLowerCase(Locale.ROOT);
      int dot = name.lastIndexOf(46);
      return dot >= 0 && IMAGE_EXTENSIONS.contains(name.substring(dot + 1));
   }

   private boolean tryPasteImageFromClipboard() {
      LOG.info("Tubi image paste: tryPasteImageFromClipboard() called");

      Clipboard clipboard;
      try {
         clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      } catch (HeadlessException e) {
         LOG.warn("Tubi image paste: headless environment", e);
         return false;
      }

      Transferable t = clipboard.getContents(null);
      if (t == null) {
         LOG.info("Tubi image paste: clipboard contents is null");
         return false;
      }

      DataFlavor[] flavors = t.getTransferDataFlavors();

      for (DataFlavor f : flavors) {
         LOG.info("Tubi image paste: available flavor: " + f.getMimeType());
      }

      try {
         if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            LOG.info("Tubi image paste: imageFlavor supported, reading...");
            Image img = (Image)t.getTransferData(DataFlavor.imageFlavor);
            if (img != null) {
               LOG.info("Tubi image paste: got image, handling paste");
               this.handleImagePaste(img);
               return true;
            }

            LOG.info("Tubi image paste: getTransferData returned null");
         }

         if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            LOG.info("Tubi image paste: javaFileListFlavor supported, reading...");
            List<File> files = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
            boolean handled = false;

            for (File file : files) {
               if (isImageFile(file)) {
                  LOG.info("Tubi image paste: image file found: " + file.getName());
                  this.handleImageFileAttach(file);
                  handled = true;
               }
            }

            if (handled) {
               return true;
            }
         }

         LOG.info("Tubi image paste: no image flavor found in clipboard");
      } catch (UnsupportedFlavorException | IOException ex) {
         LOG.warn("Tubi image paste: failed to read clipboard", ex);
      } catch (Exception ex) {
         LOG.warn("Tubi image paste: unexpected error reading clipboard", ex);
      }

      return false;
   }

   private void handleImagePaste(@NotNull Image image) {
      BufferedImage buffered;
      if (image instanceof BufferedImage bi) {
         buffered = bi;
      } else {
         buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
         Graphics2D g = buffered.createGraphics();
         g.drawImage(image, 0, 0, null);
         g.dispose();
      }

      int seq = this.pastedImageCounter.incrementAndGet();
      String filename = "pasted-image-" + seq + ".png";
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         File tempFile = null;

         try {
            tempFile = File.createTempFile("tubi-paste-", ".png");
            ImageIO.write(buffered, "png", tempFile);
            this.uploadImageFile(tempFile, filename);
         } catch (IOException e) {
            LOG.warn("Image paste: failed to save temp file", e);
            String msg = "Image paste failed: " + e.getMessage();
            SwingUtilities.invokeLater(() -> this.showValidationError(msg));
         } finally {
            if (tempFile != null) {
               tempFile.delete();
            }
         }
      });
   }

   private void handleImageFileAttach(@NotNull File imageFile) {
      String filename = imageFile.getName();
      ApplicationManager.getApplication().executeOnPooledThread(() -> this.uploadImageFile(imageFile, filename));
   }

   private void uploadImageFile(@NotNull File file, @NotNull final String displayName) {
      SwingUtilities.invokeLater(() -> {
         this.chatPanel.getAttachmentPreviewPanel().showUploading(displayName);
         this.chatPanel.getAttachmentPreviewPanel().setVisible(true);
      });
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<FileReference> resultRef = new AtomicReference<>();
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      this.chatPanel.getFileUploadApiClient().uploadFile(file, new FileUploadListener() {
         @Override
         public void onProgress(int percentage) {
            SwingUtilities.invokeLater(() -> ChatInputPanel.this.chatPanel.getAttachmentPreviewPanel().updateProgress(displayName, percentage));
         }

         @Override
         public void onSuccess(FileReference ref) {
            resultRef.set(ref);
            latch.countDown();
         }

         @Override
         public void onError(Throwable t) {
            errorRef.set(t);
            latch.countDown();
         }
      });

      try {
         latch.await(30L, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }

      if (errorRef.get() != null) {
         LOG.warn("Image upload failed: " + displayName, errorRef.get());
         SwingUtilities.invokeLater(() -> {
            this.chatPanel.getAttachmentPreviewPanel().showError(displayName, "Upload failed: " + errorRef.get().getMessage());
            this.showValidationError("Image upload failed: " + errorRef.get().getMessage());
         });
      } else if (resultRef.get() == null) {
         SwingUtilities.invokeLater(() -> {
            this.chatPanel.getAttachmentPreviewPanel().showError(displayName, "Upload timed out");
            this.showValidationError("Image upload timed out");
         });
      } else {
         AttachmentRef ref = new AttachmentRef(resultRef.get().getId());
         synchronized (this.pendingImageRefs) {
            this.pendingImageRefs.add(ref);
            this.pendingImageNames.add(displayName);
         }

         SwingUtilities.invokeLater(() -> {
            this.chatPanel.getAttachmentPreviewPanel().removeBadge(displayName);
            this.chatPanel.getAttachmentPreviewPanel().showImageBadge(displayName);
         });
         LOG.info("Image uploaded: " + displayName + " -> fileId=" + resultRef.get().getId());
      }
   }

   private static boolean isAbsolutePath(@NotNull String path) {
      return path.startsWith("/") ? true : path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':';
   }

   private static void collectBinaryFilesFromFolder(@NotNull VirtualFile dir, @NotNull List<MentionParser.ParsedMention> out) {
      for (VirtualFile child : dir.getChildren()) {
         if (child.isDirectory()) {
            String name = child.getName();
            if (!name.equals(".git")
               && !name.equals(".idea")
               && !name.equals("node_modules")
               && !name.equals("target")
               && !name.equals("build")
               && !name.equals("out")) {
               collectBinaryFilesFromFolder(child, out);
            }
         } else {
            String ext = child.getExtension();
            if (ext != null && MentionParser.UPLOAD_EXTENSIONS.contains(ext.toLowerCase())) {
               out.add(new MentionParser.ParsedMention("@" + child.getName(), child.getPath(), child, MentionParser.MentionType.UPLOAD_FILE));
            }
         }
      }
   }
}
