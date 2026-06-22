package com.tubitech.copilot.agent;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResponseNormalizer {
   private static final String CITATION_PATTERN = "<citation>[^<]*</citation>";
   private static final Pattern THINKING_BLOCK_PATTERN = Pattern.compile("<(think|thinking)>.*?</\\1>", 32);

   private ResponseNormalizer() {
   }

   @NotNull
   public static String stripCitations(@Nullable String text) {
      return text == null ? "" : text.replaceAll("<citation>[^<]*</citation>", "");
   }

   @NotNull
   public static String stripThinkingBlocks(@NotNull String text) {
      return THINKING_BLOCK_PATTERN.matcher(text).replaceAll("");
   }

   @NotNull
   public static String normalize(@NotNull String rawResponse) {
      String result = stripCitations(rawResponse);
      result = stripThinkingBlocks(result);
      return result.strip();
   }
}
