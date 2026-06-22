package com.tubitech.copilot.strategy;

import com.tubitech.copilot.editor.MentionParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SendPayload {
   private final String question;
   private final String agentId;
   private final List<String> requestPaths;
   private final List<String> contextPaths;

   public SendPayload(@NotNull String question, @Nullable String agentId, @NotNull List<String> requestPaths, @NotNull List<String> contextPaths) {
      this.question = question;
      this.agentId = agentId;
      this.requestPaths = requestPaths;
      this.contextPaths = contextPaths;
   }

   @NotNull
   public String getQuestion() {
      return this.question;
   }

   @Nullable
   public String getAgentId() {
      return this.agentId;
   }

   @NotNull
   public List<String> getRequestPaths() {
      return this.requestPaths;
   }

   @NotNull
   public List<String> getContextPaths() {
      return this.contextPaths;
   }

   @NotNull
   public static SendPayload parseFromRawText(@NotNull String rawText) {
      List<String> contextPaths = new ArrayList<>();

      for (String token : MentionParser.extractTokens(rawText)) {
         String path = MentionParser.extractPath(token);
         if (!path.isBlank()) {
            contextPaths.add(path);
         }
      }

      String question = MentionParser.stripMentions(rawText);
      return new SendPayload(question, null, Collections.emptyList(), contextPaths);
   }
}
