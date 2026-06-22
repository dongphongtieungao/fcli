package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.ConversationApiClient;
import com.tubitech.copilot.api.FileUploadApiClient;
import com.tubitech.copilot.api.model.ConversationDetail;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConversationAttachmentResolver {
   private static final Logger LOG = Logger.getInstance(ConversationAttachmentResolver.class);
   private static final String TUBI_PREFIX = "tubi-";
   private static final String MD_SUFFIX = ".md";

   private ConversationAttachmentResolver() {
   }

   @NotNull
   public static List<ConversationDetail.FileAttachment> findRelatedAttachments(
      @NotNull ConversationApiClient apiClient, @Nullable String conversationId, @NotNull String targetPath
   ) {
      if (conversationId != null && !conversationId.isBlank() && !targetPath.isBlank()) {
         List<ConversationDetail.FileAttachment> allAttachments = fetchAllAttachments(apiClient, conversationId);
         if (allAttachments.isEmpty()) {
            return Collections.emptyList();
         }

         String normalizedTarget = targetPath.replace('\\', '/');
         List<ConversationDetail.FileAttachment> related = new ArrayList<>();

         for (ConversationDetail.FileAttachment att : allAttachments) {
            String decoded = extractPathFromFileName(att.getFileName());
            if (decoded != null && isRelatedPath(decoded, normalizedTarget)) {
               related.add(att);
            }
         }

         return related;
      } else {
         return Collections.emptyList();
      }
   }

   @NotNull
   public static List<String> cascadeDeleteRelated(
      @NotNull ConversationApiClient apiClient, @NotNull FileUploadApiClient uploadClient, @Nullable String conversationId, @NotNull String targetPath
   ) {
      List<ConversationDetail.FileAttachment> related = findRelatedAttachments(apiClient, conversationId, targetPath);
      if (related.isEmpty()) {
         return Collections.emptyList();
      }

      List<String> deletedIds = new ArrayList<>();

      for (ConversationDetail.FileAttachment att : related) {
         LOG.info("cascadeDeleteRelated: deleting " + att.getFileName() + " (fileId=" + att.getFileId() + ")");
         uploadClient.deleteFile(att.getFileId());
         deletedIds.add(att.getFileId());
      }

      return deletedIds;
   }

   @NotNull
   static List<ConversationDetail.FileAttachment> fetchAllAttachments(@NotNull ConversationApiClient apiClient, @NotNull String conversationId) {
      List var5;
      try {
         ConversationDetail detail = apiClient.getConversation(conversationId);
         if (detail == null) {
            return Collections.emptyList();
         }

         ConversationDetail.ConversationMetadata meta = detail.getConversationMetadata();
         if (meta != null && meta.getAttachments() != null) {
            return meta.getAttachments();
         }

         var5 = Collections.emptyList();
      } catch (Exception e) {
         LOG.warn("fetchAllAttachments: failed for conversation " + conversationId, e);
         return Collections.emptyList();
      }

      return var5;
   }

   @Nullable
   static String extractPathFromFileName(@Nullable String fileName) {
      if (fileName != null && fileName.startsWith("tubi-")) {
         String label = fileName;
         if (label.endsWith(".md")) {
            label = label.substring(0, label.length() - ".md".length());
         }

         label = label.substring("tubi-".length());

         try {
            label = URLDecoder.decode(label, StandardCharsets.UTF_8);
         } catch (IllegalArgumentException e) {
            return null;
         }

         int dashIndex = label.indexOf(45);
         return dashIndex >= 0 && dashIndex < label.length() - 1 ? label.substring(dashIndex + 1) : label;
      } else {
         return null;
      }
   }

   static boolean isRelatedPath(@NotNull String path1, @NotNull String path2) {
      if (path1.equals(path2)) {
         return true;
      }

      String p1 = path1.endsWith("/") ? path1 : path1 + "/";
      String p2 = path2.endsWith("/") ? path2 : path2 + "/";
      return p1.startsWith(p2) || p2.startsWith(p1);
   }
}
