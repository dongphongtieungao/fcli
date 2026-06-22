package com.tubitech.copilot.strategy;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.SystemPromptLoader;
import com.tubitech.copilot.settings.ChatMode;
import org.jetbrains.annotations.NotNull;

public final class AskModeStrategy implements ChatModeStrategy {
   private static final String SYSTEM_PROMPT_PATH = "/ask-system.instructions.md";

   @NotNull
   @Override
   public ChatMode getMode() {
      return ChatMode.ASK;
   }

   @NotNull
   @Override
   public String getSystemPrompt() {
      return SystemPromptLoader.load("/ask-system.instructions.md");
   }

   @NotNull
   @Override
   public String validate(@NotNull String rawText) {
      for (String line : rawText.split("\n")) {
         String trimmed = line.trim();
         if (trimmed.startsWith("/")) {
            return "Slash commands are not supported. Use @file or @folder to attach context.";
         }
      }

      return "";
   }

   @NotNull
   @Override
   public SendPayload buildPayload(@NotNull String rawText, @NotNull Project project) {
      return SendPayload.parseFromRawText(rawText);
   }

   @Override
   public boolean canUseTools() {
      return false;
   }

   @Override
   public boolean requiresConfirmation() {
      return false;
   }

   @NotNull
   @Override
   public String getPlaceholderText() {
      return "Ask anything…  @file or @folder to attach";
   }

   @Override
   public boolean shouldParseFileProposals() {
      return false;
   }
}
