package com.tubitech.copilot.strategy;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.SystemPromptLoader;
import com.tubitech.copilot.settings.ChatMode;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

public final class AgentModeStrategy implements ChatModeStrategy {
   private static final String SYSTEM_PROMPT_PATH = "/agent-system.instructions.md";

   @NotNull
   @Override
   public ChatMode getMode() {
      return ChatMode.AGENT;
   }

   @NotNull
   @Override
   public String getSystemPrompt() {
      return SystemPromptLoader.load("/agent-system.instructions.md");
   }

   @NotNull
   @Override
   public String validate(@NotNull String rawText) {
      String agentId = TubiCopilotSettings.getInstance().getCopilotAgentId();
      if (agentId.isBlank()) {
         return "Agent not ready — please wait for initialisation.";
      }

      for (String line : rawText.split("\n")) {
         String trimmed = line.trim();
         if (trimmed.startsWith("/")) {
            return "Slash commands are not supported.";
         }
      }

      return "";
   }

   @NotNull
   @Override
   public SendPayload buildPayload(@NotNull String rawText, @NotNull Project project) {
      return new SendPayload(rawText, null, Collections.emptyList(), Collections.emptyList());
   }

   @Override
   public boolean canUseTools() {
      return true;
   }

   @Override
   public boolean requiresConfirmation() {
      return false;
   }

   @NotNull
   @Override
   public String getPlaceholderText() {
      return "Ask Tubi to do something…  (has tool access to read/edit files)";
   }

   @Override
   public boolean shouldParseFileProposals() {
      return false;
   }
}
