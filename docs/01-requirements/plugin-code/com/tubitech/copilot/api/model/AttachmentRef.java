package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public final class AttachmentRef {
   @SerializedName("file_id")
   private final String fileId;

   public AttachmentRef(String fileId) {
      this.fileId = fileId;
   }

   public String getFileId() {
      return this.fileId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof AttachmentRef) ? false : Objects.equals(this.fileId, ((AttachmentRef)o).fileId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fileId);
   }

   @Override
   public String toString() {
      return "AttachmentRef{fileId='" + this.fileId + "'}";
   }
}
