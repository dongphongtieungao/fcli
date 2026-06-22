package com.tubitech.copilot.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.tubitech.copilot.agent.FileFilterConstants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FolderContextExtractor {
   private static final Logger LOG = Logger.getInstance(FolderContextExtractor.class);
   public static final int MAX_FILES = 50;
   public static final long MAX_FILE_BYTES = 512000L;
   public static final long MAX_TOTAL_BYTES = 2097152L;
   public static final Set<String> SKIP_DIRS = FileFilterConstants.SKIP_DIRS;
   private static final Set<String> TEXT_EXTENSIONS = FileFilterConstants.TEXT_EXTENSIONS;

   private FolderContextExtractor() {
   }

   @Nullable
   public static FolderContextExtractor.FolderContext extractFolderContext(@NotNull VirtualFile folder) {
      if (!folder.isDirectory()) {
         return null;
      }

      List<VirtualFile> candidates = new ArrayList<>();
      collectFiles(folder, candidates);
      if (candidates.isEmpty()) {
         return null;
      }

      StringBuilder body = new StringBuilder();
      int fileCount = 0;
      long totalBytes = 0L;
      boolean truncated = false;

      for (VirtualFile file : candidates) {
         if (fileCount >= 50) {
            truncated = true;
            break;
         }

         long fileSize = file.getLength();
         if (fileSize > 512000L) {
            LOG.debug("FolderContextExtractor: skipping large file " + file.getPath() + " (" + fileSize + " B)");
         } else {
            if (totalBytes + fileSize > 2097152L) {
               truncated = true;
               break;
            }

            try {
               String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
               String relativePath = getRelativePath(folder, file);
               String lang = EditorContextExtractor.detectLanguage(file.getName());
               body.append("### File: ").append(relativePath).append('\n');
               body.append("```").append(lang).append('\n');
               body.append(content);
               if (!content.isEmpty() && content.charAt(content.length() - 1) != '\n') {
                  body.append('\n');
               }

               body.append("```\n\n");
               fileCount++;
               totalBytes += fileSize;
            } catch (IOException e) {
               LOG.warn("FolderContextExtractor: cannot read " + file.getPath(), e);
            }
         }
      }

      if (fileCount == 0) {
         return null;
      }

      String countLabel = fileCount + (truncated ? "+" : "") + " files";
      String header = "[Folder Context: " + folder.getName() + "/ (" + countLabel + ")]";
      String formattedContext = header + "\n\n" + body;
      return new FolderContextExtractor.FolderContext(folder.getName(), folder.getPath(), formattedContext, fileCount, totalBytes, truncated);
   }

   private static void collectFiles(@NotNull VirtualFile dir, @NotNull List<VirtualFile> result) {
      VirtualFile[] children = dir.getChildren();
      if (children != null) {
         for (VirtualFile child : children) {
            if (child.isDirectory()) {
               if (!SKIP_DIRS.contains(child.getName())) {
                  collectFiles(child, result);
               }
            } else {
               String ext = child.getExtension();
               if (ext != null && TEXT_EXTENSIONS.contains(ext.toLowerCase())) {
                  result.add(child);
               }
            }
         }
      }
   }

   @NotNull
   static String getRelativePath(@NotNull VirtualFile base, @NotNull VirtualFile file) {
      String basePath = base.getPath();
      String filePath = file.getPath();
      return filePath.startsWith(basePath + "/") ? filePath.substring(basePath.length() + 1) : file.getName();
   }

   public record FolderContext(
      @NotNull String folderName, @NotNull String folderPath, @NotNull String formattedContext, int fileCount, long totalBytes, boolean truncated
   ) {
      @NotNull
      public String toInjectionFormat(@NotNull String userQuestion) {
         String var10000 = this.formattedContext + "\nUser question: " + userQuestion;
         if (this.formattedContext + "\nUser question: " + userQuestion == null) {
            $$$reportNull$$$0(4);
         }

         return var10000;
      }
   }
}
