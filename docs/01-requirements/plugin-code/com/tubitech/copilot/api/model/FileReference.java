package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public final class FileReference {
   @SerializedName("id")
   private final String id;
   @SerializedName("file_name")
   private final String fileName;
   @SerializedName("status")
   private final String status;
   @SerializedName("file_extension")
   private final String fileExtension;
   @SerializedName("created_at")
   private final String createdAt;
   @SerializedName("updated_at")
   private final String updatedAt;

   public FileReference(String id, String fileName, String status, String fileExtension, String createdAt, String updatedAt) {
      this.id = id;
      this.fileName = fileName;
      this.status = status;
      this.fileExtension = fileExtension;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.fileName;
   }

   public String getStatus() {
      return this.status;
   }

   public String getFileExtension() {
      return this.fileExtension;
   }

   public String getCreatedAt() {
      return this.createdAt;
   }

   public String getUpdatedAt() {
      return this.updatedAt;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof FileReference that)
            ? false
            : Objects.equals(this.id, that.id)
               && Objects.equals(this.fileName, that.fileName)
               && Objects.equals(this.status, that.status)
               && Objects.equals(this.fileExtension, that.fileExtension)
               && Objects.equals(this.createdAt, that.createdAt)
               && Objects.equals(this.updatedAt, that.updatedAt);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.fileName, this.status, this.fileExtension, this.createdAt, this.updatedAt);
   }

   @Override
   public String toString() {
      return "FileReference{id='"
         + this.id
         + "', fileName='"
         + this.fileName
         + "', status='"
         + this.status
         + "', fileExtension='"
         + this.fileExtension
         + "', createdAt='"
         + this.createdAt
         + "', updatedAt='"
         + this.updatedAt
         + "'}";
   }
}
