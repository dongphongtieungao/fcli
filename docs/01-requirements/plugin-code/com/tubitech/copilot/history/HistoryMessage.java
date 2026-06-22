package com.tubitech.copilot.history;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HistoryMessage {
   private final String id;
   private final String sessionId;
   private final MessageRole role;
   private final String content;
   private final boolean visible;
   private final int turnIndex;
   private final long createdAt;
   private final String metadata;
   private final String serverMessageId;

   private HistoryMessage(
      String id,
      String sessionId,
      MessageRole role,
      @Nullable String content,
      boolean visible,
      int turnIndex,
      long createdAt,
      @Nullable String metadata,
      @Nullable String serverMessageId
   ) {
      this.id = id;
      this.sessionId = sessionId;
      this.role = role;
      this.content = content;
      this.visible = visible;
      this.turnIndex = turnIndex;
      this.createdAt = createdAt;
      this.metadata = metadata;
      this.serverMessageId = serverMessageId;
   }

   public static HistoryMessage userMessage(@NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata) {
      return new HistoryMessage(UUID.randomUUID().toString(), sessionId, MessageRole.USER, content, true, turnIndex, System.currentTimeMillis(), metadata, null);
   }

   public static HistoryMessage assistantMessage(@NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata) {
      return new HistoryMessage(
         UUID.randomUUID().toString(), sessionId, MessageRole.ASSISTANT, content, true, turnIndex, System.currentTimeMillis(), metadata, null
      );
   }

   public static HistoryMessage assistantMessage(
      @NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata, @Nullable String serverMessageId
   ) {
      return new HistoryMessage(
         UUID.randomUUID().toString(), sessionId, MessageRole.ASSISTANT, content, true, turnIndex, System.currentTimeMillis(), metadata, serverMessageId
      );
   }

   public static HistoryMessage toolCallMessage(@NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata) {
      return new HistoryMessage(
         UUID.randomUUID().toString(), sessionId, MessageRole.TOOL_CALL, content, false, turnIndex, System.currentTimeMillis(), metadata, null
      );
   }

   public static HistoryMessage toolResultMessage(@NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata) {
      return new HistoryMessage(
         UUID.randomUUID().toString(), sessionId, MessageRole.TOOL_RESULT, content, false, turnIndex, System.currentTimeMillis(), metadata, null
      );
   }

   public static HistoryMessage systemMessage(@NotNull String sessionId, int turnIndex, @NotNull String content) {
      return new HistoryMessage(UUID.randomUUID().toString(), sessionId, MessageRole.SYSTEM, content, false, turnIndex, System.currentTimeMillis(), null, null);
   }

   public static HistoryMessage systemMessage(@NotNull String sessionId, int turnIndex, @NotNull String content, @Nullable String metadata) {
      return new HistoryMessage(
         UUID.randomUUID().toString(), sessionId, MessageRole.SYSTEM, content, false, turnIndex, System.currentTimeMillis(), metadata, null
      );
   }

   public static HistoryMessage fromDb(
      @NotNull String id,
      @NotNull String sessionId,
      @NotNull MessageRole role,
      @Nullable String content,
      boolean visible,
      int turnIndex,
      long createdAt,
      @Nullable String metadata
   ) {
      return new HistoryMessage(id, sessionId, role, content, visible, turnIndex, createdAt, metadata, null);
   }

   public static HistoryMessage fromDb(
      @NotNull String id,
      @NotNull String sessionId,
      @NotNull MessageRole role,
      @Nullable String content,
      boolean visible,
      int turnIndex,
      long createdAt,
      @Nullable String metadata,
      @Nullable String serverMessageId
   ) {
      return new HistoryMessage(id, sessionId, role, content, visible, turnIndex, createdAt, metadata, serverMessageId);
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @NotNull
   public String getSessionId() {
      return this.sessionId;
   }

   @NotNull
   public MessageRole getRole() {
      return this.role;
   }

   @Nullable
   public String getContent() {
      return this.content;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public int getTurnIndex() {
      return this.turnIndex;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   @Nullable
   public String getMetadata() {
      return this.metadata;
   }

   @Nullable
   public String getServerMessageId() {
      return this.serverMessageId;
   }

   @Override
   public String toString() {
      return "HistoryMessage{role="
         + this.role
         + ", turn="
         + this.turnIndex
         + ", visible="
         + this.visible
         + ", len="
         + (this.content == null ? 0 : this.content.length())
         + "}";
   }
}
