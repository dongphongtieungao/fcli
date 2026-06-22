package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public final class StreamEvent {
   @SerializedName("id")
   private final String id;
   @SerializedName("content")
   private final String content;
   @SerializedName("delta")
   private final String delta;
   @SerializedName("done")
   private final boolean done;
   @SerializedName("message_id")
   private final String messageId;

   public StreamEvent(String id, String content, String delta, boolean done, String messageId) {
      this.id = id;
      this.content = content;
      this.delta = delta;
      this.done = done;
      this.messageId = messageId;
   }

   public String getId() {
      return this.id;
   }

   public String getContent() {
      return this.content;
   }

   public String getDelta() {
      return this.delta;
   }

   public boolean isDone() {
      return this.done;
   }

   public String getMessageId() {
      return this.messageId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof StreamEvent that)
            ? false
            : this.done == that.done
               && Objects.equals(this.id, that.id)
               && Objects.equals(this.content, that.content)
               && Objects.equals(this.delta, that.delta)
               && Objects.equals(this.messageId, that.messageId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.content, this.delta, this.done, this.messageId);
   }

   @Override
   public String toString() {
      return "StreamEvent{id='" + this.id + "', delta='" + this.delta + "', done=" + this.done + ", messageId='" + this.messageId + "'}";
   }
}
