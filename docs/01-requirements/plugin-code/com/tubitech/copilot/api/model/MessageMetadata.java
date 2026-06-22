package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class MessageMetadata {
   @SerializedName("attachments")
   private final List<AttachmentRef> attachments;
   @SerializedName("agent_id")
   @Nullable
   private final String agentId;
   @SerializedName("reasoning_effort")
   private final Object reasoningEffort;

   public MessageMetadata() {
      this(new ArrayList<>(), null, null);
   }

   public MessageMetadata(List<AttachmentRef> attachments, Object reasoningEffort) {
      this(attachments, reasoningEffort, null);
   }

   public MessageMetadata(List<AttachmentRef> attachments, Object reasoningEffort, @Nullable String agentId) {
      this.attachments = new ArrayList<>(attachments);
      this.reasoningEffort = reasoningEffort;
      this.agentId = agentId;
   }

   public List<AttachmentRef> getAttachments() {
      return Collections.unmodifiableList(this.attachments);
   }

   public void setAttachments(List<AttachmentRef> refs) {
      this.attachments.clear();
      this.attachments.addAll(refs);
   }

   public void addAttachment(AttachmentRef ref) {
      this.attachments.add(ref);
   }

   @Nullable
   public String getAgentId() {
      return this.agentId;
   }

   public Object getReasoningEffort() {
      return this.reasoningEffort;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof MessageMetadata that)
            ? false
            : Objects.equals(this.attachments, that.attachments)
               && Objects.equals(this.agentId, that.agentId)
               && Objects.equals(this.reasoningEffort, that.reasoningEffort);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.attachments, this.agentId, this.reasoningEffort);
   }

   @Override
   public String toString() {
      return "MessageMetadata{attachments=" + this.attachments + ", agentId='" + this.agentId + "', reasoningEffort=" + this.reasoningEffort + "}";
   }
}
