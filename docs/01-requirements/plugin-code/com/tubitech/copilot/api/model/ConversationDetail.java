package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ConversationDetail {
   @SerializedName("id")
   private String id;
   @SerializedName("conversation_name")
   private String conversationName;
   @SerializedName("messages_list")
   private List<ConversationMessage> messagesList;
   @SerializedName("agent_id")
   @Nullable
   private String agentId;
   @SerializedName("conversation_metadata")
   @Nullable
   private ConversationDetail.ConversationMetadata conversationMetadata;

   public String getId() {
      return this.id;
   }

   public String getConversationName() {
      return this.conversationName;
   }

   @Nullable
   public List<ConversationMessage> getMessagesList() {
      return this.messagesList;
   }

   @Nullable
   public String getAgentId() {
      return this.agentId;
   }

   @Nullable
   public ConversationDetail.ConversationMetadata getConversationMetadata() {
      return this.conversationMetadata;
   }

   public static class ConversationMetadata {
      @SerializedName("attachments")
      @Nullable
      private List<ConversationDetail.FileAttachment> attachments;

      @Nullable
      public List<ConversationDetail.FileAttachment> getAttachments() {
         return this.attachments;
      }
   }

   public static class FileAttachment {
      @SerializedName("file_id")
      private String fileId;
      @SerializedName("file_name")
      private String fileName;

      public String getFileId() {
         return this.fileId;
      }

      public String getFileName() {
         return this.fileName;
      }
   }
}
