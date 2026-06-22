package com.tubitech.copilot.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SearchResult {
   private final String sessionId;
   private final String sessionTitle;
   private final String messageId;
   private final String snippet;
   private final long messageTimestamp;

   public SearchResult(@NotNull String sessionId, @Nullable String sessionTitle, @NotNull String messageId, @NotNull String snippet, long messageTimestamp) {
      this.sessionId = sessionId;
      this.sessionTitle = sessionTitle;
      this.messageId = messageId;
      this.snippet = snippet;
      this.messageTimestamp = messageTimestamp;
   }

   @NotNull
   public String getSessionId() {
      return this.sessionId;
   }

   @Nullable
   public String getSessionTitle() {
      return this.sessionTitle;
   }

   @NotNull
   public String getMessageId() {
      return this.messageId;
   }

   @NotNull
   public String getSnippet() {
      return this.snippet;
   }

   public long getMessageTimestamp() {
      return this.messageTimestamp;
   }
}
