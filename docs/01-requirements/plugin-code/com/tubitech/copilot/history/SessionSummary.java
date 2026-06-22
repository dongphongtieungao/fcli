package com.tubitech.copilot.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SessionSummary {
   private final String id;
   private final String title;
   private final long updatedAt;
   private final int messageCount;

   public SessionSummary(@NotNull String id, @Nullable String title, long updatedAt, int messageCount) {
      this.id = id;
      this.title = title;
      this.updatedAt = updatedAt;
      this.messageCount = messageCount;
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @Nullable
   public String getTitle() {
      return this.title;
   }

   public long getUpdatedAt() {
      return this.updatedAt;
   }

   public int getMessageCount() {
      return this.messageCount;
   }

   @Override
   public String toString() {
      return "SessionSummary{id='" + this.id + "', title='" + this.title + "', messages=" + this.messageCount + "}";
   }
}
