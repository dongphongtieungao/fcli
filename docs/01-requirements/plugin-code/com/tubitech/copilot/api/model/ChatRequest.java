package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class ChatRequest {
   @SerializedName("parent_message_id")
   private final String parentMessageId;
   @SerializedName("conversation_id")
   @Nullable
   private final String conversationId;
   @SerializedName("question")
   private final String question;
   @SerializedName("metadata")
   private final MessageMetadata metadata;
   @SerializedName("model_id")
   private final String modelId;
   @SerializedName("tools")
   private final List<Object> tools;

   private ChatRequest(ChatRequest.Builder builder) {
      this.parentMessageId = builder.parentMessageId;
      this.conversationId = builder.conversationId;
      this.question = builder.question;
      this.metadata = builder.metadata != null ? builder.metadata : new MessageMetadata();
      this.modelId = builder.modelId;
      this.tools = Collections.unmodifiableList(new ArrayList<>(builder.tools));
   }

   public static ChatRequest.Builder builder() {
      return new ChatRequest.Builder();
   }

   public String getParentMessageId() {
      return this.parentMessageId;
   }

   @Nullable
   public String getConversationId() {
      return this.conversationId;
   }

   public String getQuestion() {
      return this.question;
   }

   public MessageMetadata getMetadata() {
      return this.metadata;
   }

   public String getModelId() {
      return this.modelId;
   }

   public List<Object> getTools() {
      return this.tools;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof ChatRequest that)
            ? false
            : Objects.equals(this.parentMessageId, that.parentMessageId)
               && Objects.equals(this.question, that.question)
               && Objects.equals(this.metadata, that.metadata)
               && Objects.equals(this.modelId, that.modelId)
               && Objects.equals(this.tools, that.tools);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.parentMessageId, this.question, this.metadata, this.modelId, this.tools);
   }

   @Override
   public String toString() {
      return "ChatRequest{parentMessageId='"
         + this.parentMessageId
         + "', question='"
         + this.question
         + "', metadata="
         + this.metadata
         + ", modelId='"
         + this.modelId
         + "', tools="
         + this.tools
         + "}";
   }

   public static final class Builder {
      private String parentMessageId = null;
      private String conversationId = null;
      private String question = "";
      private MessageMetadata metadata = null;
      private String modelId = "azure-gpt-5.2";
      private List<Object> tools = new ArrayList<>();

      private Builder() {
      }

      public ChatRequest.Builder parentMessageId(String parentMessageId) {
         this.parentMessageId = parentMessageId;
         return this;
      }

      public ChatRequest.Builder conversationId(@Nullable String conversationId) {
         this.conversationId = conversationId;
         return this;
      }

      public ChatRequest.Builder question(String question) {
         this.question = Objects.requireNonNull(question, "question must not be null");
         return this;
      }

      public ChatRequest.Builder metadata(MessageMetadata metadata) {
         this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
         return this;
      }

      public ChatRequest.Builder modelId(String modelId) {
         this.modelId = Objects.requireNonNull(modelId, "modelId must not be null");
         return this;
      }

      public ChatRequest.Builder tools(List<Object> tools) {
         this.tools = Objects.requireNonNull(tools, "tools must not be null");
         return this;
      }

      public ChatRequest build() {
         return new ChatRequest(this);
      }
   }
}
