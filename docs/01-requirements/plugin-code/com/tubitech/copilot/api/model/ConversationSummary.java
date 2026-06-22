package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class ConversationSummary {
   @SerializedName("id")
   private String id;
   @SerializedName("created_at")
   private String createdAt;
   @SerializedName("updated_at")
   private String updatedAt;
   @SerializedName("conversation_name")
   private String conversationName;
   @SerializedName("conversation_description")
   @Nullable
   private String conversationDescription;
   @SerializedName("read_only")
   private boolean readOnly;

   public String getId() {
      return this.id;
   }

   public String getCreatedAt() {
      return this.createdAt;
   }

   public String getUpdatedAt() {
      return this.updatedAt;
   }

   public String getConversationName() {
      return this.conversationName;
   }

   @Nullable
   public String getConversationDescription() {
      return this.conversationDescription;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }
}
