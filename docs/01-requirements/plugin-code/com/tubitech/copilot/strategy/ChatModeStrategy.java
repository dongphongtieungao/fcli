package com.tubitech.copilot.strategy;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.settings.ChatMode;
import org.jetbrains.annotations.NotNull;

public interface ChatModeStrategy {
   @NotNull
   ChatMode getMode();

   @NotNull
   String getSystemPrompt();

   @NotNull
   String validate(@NotNull String var1);

   @NotNull
   SendPayload buildPayload(@NotNull String var1, @NotNull Project var2) throws Exception;

   boolean canUseTools();

   boolean requiresConfirmation();

   @NotNull
   String getPlaceholderText();

   boolean shouldParseFileProposals();

   @NotNull
   default String getModeTag() {
      return "[MODE: " + this.getMode().name().charAt(0) + this.getMode().name().substring(1).toLowerCase() + "]";
   }
}
