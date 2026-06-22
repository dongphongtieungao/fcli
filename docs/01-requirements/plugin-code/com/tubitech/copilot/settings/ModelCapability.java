package com.tubitech.copilot.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModelCapability {
   private ModelCapability() {
   }

   public static boolean supportsAgentMode(@Nullable String modelId) {
      if (modelId != null && !modelId.isBlank()) {
         String lower = modelId.toLowerCase();
         return lower.contains("gemini");
      } else {
         return false;
      }
   }

   public static boolean modeRequiresAgent(@NotNull ChatMode mode) {
      return mode == ChatMode.AGENT || mode == ChatMode.PLAN;
   }

   @NotNull
   public static ChatMode effectiveMode(@NotNull ChatMode requested, @Nullable String modelId) {
      if (modeRequiresAgent(requested) && !supportsAgentMode(modelId)) {
         return ChatMode.ASK;
      } else {
         return requested;
      }
   }
}
