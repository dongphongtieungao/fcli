package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.tools.ToolRegistry;
import com.tubitech.copilot.api.AgentApiClient;
import com.tubitech.copilot.api.AgentApiClientImpl;
import com.tubitech.copilot.api.model.AgentDetail;
import com.tubitech.copilot.api.model.AgentSummary;
import com.tubitech.copilot.api.model.CreateAgentRequest;
import com.tubitech.copilot.api.model.SupportedModel;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UpdateSyncService {
   private static final Logger LOG = Logger.getInstance(UpdateSyncService.class);
   private static final String BASE_PROMPT_PATH = "/copilot-base-system.instructions.md";
   private static final String ASK_PROMPT_PATH = "/ask-system.instructions.md";
   private static final String AGENT_PROMPT_PATH = "/agent-system.instructions.md";
   private static final String PLAN_PROMPT_PATH = "/plan-system.instructions.md";

   private UpdateSyncService() {
   }

   public static void sync(@NotNull Project project, @NotNull UpdateSyncService.SyncCallback callback) {
      TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
      AgentApiClient agentClient = new AgentApiClientImpl(settings);

      try {
         String agentName = AgentNameResolver.copilotAgentName(project);
         String basePrompt = SystemPromptLoader.load("/copilot-base-system.instructions.md");
         String askPrompt = SystemPromptLoader.load("/ask-system.instructions.md");
         String agentPrompt = SystemPromptLoader.load("/agent-system.instructions.md");
         String planPrompt = SystemPromptLoader.load("/plan-system.instructions.md");
         String toolBlock = ToolRegistry.getInstance().generateToolsPromptBlock(false);
         String customContent = InstructionsFileManager.readAll(project);
         String fullBase = basePrompt + "\n\n---\n\n" + askPrompt + "\n\n---\n\n" + agentPrompt + "\n\n---\n\n" + planPrompt + "\n\n" + toolBlock;
         String customText = customContent != null ? customContent : "";
         String overflowError = InstructionsFileManager.validate(fullBase, customText);
         if (overflowError != null) {
            callback.onInstructionsOverflow(overflowError);
            return;
         }

         InstructionsFileManager.BuildResult buildResult = InstructionsFileManager.buildInstructions(fullBase, customText);
         String instructions = buildResult.getInstructions();
         List<AgentSummary> existing = agentClient.listMyAgents();
         String agentId = null;

         for (AgentSummary s : existing) {
            if (agentName.equalsIgnoreCase(s.getName())) {
               agentId = s.getId();
               break;
            }
         }

         if (agentId != null) {
            agentClient.updateAgent(agentId, new CreateAgentRequest(agentName, "", instructions, settings.getSelectedModelId()));
            LOG.debug("UpdateSyncService: updated agent id=" + agentId);
         } else {
            AgentDetail created = agentClient.createAgent(new CreateAgentRequest(agentName, "", instructions, settings.getSelectedModelId()));
            agentId = created.getId();
            LOG.debug("UpdateSyncService: created agent id=" + agentId);
         }

         settings.setCopilotAgentId(agentId);
         LocalSetupService.ensureTubiIgnore(project);
         callback.onSuccess(agentId, agentName, Collections.emptyList());
      } catch (IOException e) {
         LOG.warn("UpdateSyncService: sync failed", e);
         callback.onError(e);
      }
   }

   public interface SyncCallback {
      void onSuccess(@Nullable String var1, @Nullable String var2, @NotNull List<SupportedModel> var3);

      void onError(@NotNull IOException var1);

      default void onInstructionsOverflow(@NotNull String errorMessage) {
         UpdateSyncService.LOG.warn("UpdateSyncService: instructions overflow — " + errorMessage);
      }
   }
}
