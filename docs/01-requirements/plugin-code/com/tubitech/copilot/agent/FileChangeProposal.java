package com.tubitech.copilot.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FileChangeProposal {
   private final String relativePath;
   private final FileChangeProposal.Operation operation;
   private final String newContent;
   @Nullable
   private final String existingContent;
   private boolean accepted;

   public FileChangeProposal(
      @NotNull String relativePath, @NotNull FileChangeProposal.Operation operation, @NotNull String newContent, @Nullable String existingContent
   ) {
      this.relativePath = relativePath;
      this.operation = operation;
      this.newContent = newContent;
      this.existingContent = existingContent;
      this.accepted = true;
   }

   @NotNull
   public String getRelativePath() {
      return this.relativePath;
   }

   @NotNull
   public FileChangeProposal.Operation getOperation() {
      return this.operation;
   }

   @NotNull
   public String getNewContent() {
      return this.newContent;
   }

   @Nullable
   public String getExistingContent() {
      return this.existingContent;
   }

   public boolean isAccepted() {
      return this.accepted;
   }

   public void setAccepted(boolean accepted) {
      this.accepted = accepted;
   }

   @NotNull
   public static FileChangeProposal forDelete(@NotNull String relativePath, @Nullable String existingContent) {
      return new FileChangeProposal(relativePath, FileChangeProposal.Operation.DELETE, "", existingContent);
   }

   @Override
   public String toString() {
      return "FileChangeProposal{path='" + this.relativePath + "', op=" + this.operation + ", accepted=" + this.accepted + "}";
   }

   public enum Operation {
      CREATE,
      MODIFY,
      DELETE;
   }
}
