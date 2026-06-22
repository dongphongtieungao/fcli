package com.tubitech.copilot.history;

import com.tubitech.copilot.api.model.FileReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConversationSession {
   public String id;
   public String title;
   public List<ConversationSession.ChatMessage> messages = new ArrayList<>();
   public String lastMessageId;
   public String serverConversationId;
   public long createdAt;
   public long updatedAt;

   @NotNull
   public static ConversationSession newSession() {
      ConversationSession session = new ConversationSession();
      session.id = UUID.randomUUID().toString();
      session.createdAt = System.currentTimeMillis();
      session.updatedAt = session.createdAt;
      return session;
   }

   public void addUserMessage(@NotNull String content, @Nullable List<FileReference> attachments) {
      ConversationSession.ChatMessage msg = new ConversationSession.ChatMessage();
      msg.role = ConversationSession.Role.USER;
      msg.content = content;
      msg.timestamp = System.currentTimeMillis();
      if (attachments != null && !attachments.isEmpty()) {
         msg.attachmentNames = attachments.stream().map(FileReference::getName).collect(Collectors.toList());
      }

      this.messages.add(msg);
      this.updatedAt = msg.timestamp;
      if (this.title == null || this.title.isEmpty()) {
         this.title = content.length() > 50 ? content.substring(0, 50) : content;
      }
   }

   public void addAssistantMessage(@NotNull String content, @Nullable String messageId) {
      ConversationSession.ChatMessage msg = new ConversationSession.ChatMessage();
      msg.role = ConversationSession.Role.ASSISTANT;
      msg.content = content;
      msg.timestamp = System.currentTimeMillis();
      this.messages.add(msg);
      this.lastMessageId = messageId;
      this.updatedAt = msg.timestamp;
   }

   @Nullable
   public String getTitle() {
      return this.title;
   }

   @NotNull
   public List<ConversationSession.ChatMessage> getMessages() {
      return Collections.unmodifiableList(this.messages);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof ConversationSession) ? false : Objects.equals(this.id, ((ConversationSession)o).id);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

   @Override
   public String toString() {
      return "ConversationSession{id='" + this.id + "', title='" + this.title + "', messages=" + this.messages.size() + "}";
   }

   public static class ChatMessage {
      public ConversationSession.Role role;
      public String content;
      public long timestamp;
      public List<String> attachmentNames = new ArrayList<>();

      @Override
      public String toString() {
         return "ChatMessage{role=" + this.role + ", contentLength=" + (this.content == null ? 0 : this.content.length()) + "}";
      }
   }

   public enum Role {
      USER,
      ASSISTANT;
   }
}
