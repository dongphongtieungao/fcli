package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class ConversationMessage {
   @SerializedName("id")
   private String id;
   @SerializedName("role")
   private String role;
   @SerializedName("content")
   private ConversationMessage.MessageContent content;
   @SerializedName("parent_id")
   @Nullable
   private String parentId;
   @SerializedName("model_id")
   @Nullable
   private String modelId;

   public String getId() {
      return this.id;
   }

   public String getRole() {
      return this.role;
   }

   public ConversationMessage.MessageContent getContent() {
      return this.content;
   }

   @Nullable
   public String getParentId() {
      return this.parentId;
   }

   @Nullable
   public String getModelId() {
      return this.modelId;
   }

   public static class MessageContent {
      @SerializedName("text")
      private String text;

      public String getText() {
         return this.text;
      }
   }
}
