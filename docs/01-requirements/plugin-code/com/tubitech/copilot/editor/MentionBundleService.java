package com.tubitech.copilot.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tubitech.copilot.agent.ConversationAttachmentResolver;
import com.tubitech.copilot.agent.FileFilterConstants;
import com.tubitech.copilot.api.ConversationApiClient;
import com.tubitech.copilot.api.FileUploadApiClient;
import com.tubitech.copilot.api.FileUploadListener;
import com.tubitech.copilot.api.model.FileReference;
import com.tubitech.copilot.editor.bundle.ContextBundleBuilder;
import com.tubitech.copilot.editor.bundle.ContextBundleEntry;
import com.tubitech.copilot.editor.bundle.ContextBundleSection;
import com.tubitech.copilot.editor.bundle.ContextBundleWriter;
import com.tubitech.copilot.editor.bundle.FileContentReader;
import com.tubitech.copilot.editor.bundle.LastSendSnapshotWriter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MentionBundleService {
   private static final Logger LOG = Logger.getInstance(MentionBundleService.class);
   private static final int UPLOAD_TIMEOUT_SECONDS = 30;

   private void buildEntriesForPaths(
      @NotNull List<String> paths, @Nullable VirtualFile projectBaseDir, @Nullable String basePath, @NotNull List<ContextBundleEntry> result
   ) {
      StringBuilder tokenText = new StringBuilder();

      for (String path : paths) {
         if (path.contains(" ")) {
            tokenText.append("@\"").append(path).append("\"\n");
         } else {
            tokenText.append('@').append(path).append('\n');
         }
      }

      for (MentionParser.ParsedMention mention : MentionParser.resolve(tokenText.toString(), projectBaseDir)) {
         if (mention.type() == MentionParser.MentionType.UNRESOLVED) {
            LOG.warn("MentionBundleService: unresolved path: " + mention.path());
         } else {
            VirtualFile vf = mention.resolved();
            if (vf != null) {
               if (mention.type() == MentionParser.MentionType.FOLDER) {
                  List<VirtualFile> folderFiles = new ArrayList<>();
                  collectFiles(vf, folderFiles);

                  for (VirtualFile file : folderFiles) {
                     String relPath = getRelativePath(basePath, file);
                     String ext = file.getExtension() != null ? file.getExtension().toLowerCase() : "";
                     String language = EditorContextExtractor.detectLanguage(file.getName());

                     try {
                        String content = FileContentReader.read(new File(file.getPath()), ext);
                        result.add(new ContextBundleEntry(relPath, language, content));
                     } catch (IOException e) {
                        LOG.warn("MentionBundleService: cannot read " + file.getPath(), e);
                     }
                  }
               } else {
                  String relPath = getRelativePath(basePath, vf);
                  String ext = vf.getExtension() != null ? vf.getExtension().toLowerCase() : "";
                  String language = EditorContextExtractor.detectLanguage(vf.getName());

                  try {
                     String content = FileContentReader.read(new File(vf.getPath()), ext);
                     result.add(new ContextBundleEntry(relPath, language, content));
                  } catch (IOException e) {
                     LOG.warn("MentionBundleService: cannot read " + vf.getPath(), e);
                  }
               }
            }
         }
      }
   }

   @Nullable
   public MentionBundleService.BundleUploadResult buildAndUploadSingleBundle(
      @NotNull List<String> paths,
      @NotNull Project project,
      @NotNull FileUploadApiClient uploadApiClient,
      @Nullable ConversationApiClient conversationApiClient,
      @Nullable String serverConversationId
   ) throws IOException {
      if (paths.isEmpty()) {
         return null;
      }

      Runnable saveAll = () -> FileDocumentManager.getInstance().saveAllDocuments();
      if (ApplicationManager.getApplication().isDispatchThread()) {
         saveAll.run();
      } else {
         ApplicationManager.getApplication().invokeAndWait(saveAll);
      }

      String basePath = project.getBasePath();
      VirtualFile projectBaseDir = basePath != null ? LocalFileSystem.getInstance().findFileByPath(basePath) : null;

      for (String path : paths) {
         String normalized = path.replace('\\', '/');
         String abs = isAbsolutePath(normalized) ? normalized : (basePath != null ? basePath + "/" + normalized : null);
         if (abs != null) {
            try {
               LocalFileSystem.getInstance().refreshAndFindFileByPath(abs);
            } catch (RuntimeException ex) {
               LOG.warn("MentionBundleService: VFS pre-refresh failed for " + abs, ex);
            }
         }
      }

      List<ContextBundleEntry> allEntries = new ArrayList<>();
      ApplicationManager.getApplication().runReadAction(() -> this.buildEntriesForPaths(paths, projectBaseDir, basePath, allEntries));
      if (allEntries.isEmpty()) {
         LOG.warn("MentionBundleService: no readable content for any @mention path");
         return null;
      }

      String dateStr = LocalDate.now().toString();
      List<ContextBundleSection> sections = List.of(
         new ContextBundleSection("CONTEXT_FILES", "Content of user-mentioned files/folders.", List.copyOf(allEntries))
      );
      String markdown = ContextBundleBuilder.buildSectioned(sections, dateStr);
      String label = "bundle_ctx";
      LastSendSnapshotWriter.saveDebugBundle("bundle_ctx.md", markdown, project);
      if (conversationApiClient != null && serverConversationId != null && !serverConversationId.isEmpty()) {
         for (String path : paths) {
            String normalized = path.replace('\\', '/');
            ConversationAttachmentResolver.cascadeDeleteRelated(conversationApiClient, uploadApiClient, serverConversationId, normalized);
         }
      }

      File tempFile = ContextBundleWriter.writeTempBundle(markdown, "bundle_ctx");

      try {
         final CountDownLatch latch = new CountDownLatch(1);
         final String[] fileIdHolder = new String[]{null};
         final Throwable[] errorHolder = new Throwable[]{null};
         uploadApiClient.uploadFile(tempFile, new FileUploadListener() {
            @Override
            public void onProgress(int pct) {
            }

            @Override
            public void onSuccess(@NotNull FileReference ref) {
               fileIdHolder[0] = ref.getId();
               latch.countDown();
            }

            @Override
            public void onError(@NotNull Throwable error) {
               errorHolder[0] = error;
               latch.countDown();
            }
         });
         if (!latch.await(30L, TimeUnit.SECONDS)) {
            throw new IOException("Bundle upload timed out");
         } else if (errorHolder[0] != null) {
            throw new IOException("Bundle upload failed", errorHolder[0]);
         } else if (fileIdHolder[0] != null) {
            LOG.info("MentionBundleService: uploaded single bundle_ctx.md (" + allEntries.size() + " file(s) from " + paths.size() + " @mention(s))");
            return new MentionBundleService.BundleUploadResult(fileIdHolder[0], allEntries.size(), List.copyOf(paths));
         } else {
            return null;
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new IOException("Bundle upload interrupted", e);
      } finally {
         if (tempFile.exists() && !tempFile.delete()) {
            LOG.warn("MentionBundleService: could not delete temp file: " + tempFile);
         }
      }
   }

   @NotNull
   public List<MentionBundleService.PerPathUploadResult> buildAndUploadPerPath(
      @NotNull List<String> paths,
      @NotNull Project project,
      @NotNull FileUploadApiClient uploadApiClient,
      @Nullable ConversationApiClient conversationApiClient,
      @Nullable String serverConversationId
   ) throws IOException {
      if (paths.isEmpty()) {
         return List.of();
      }

      Runnable saveAll = () -> FileDocumentManager.getInstance().saveAllDocuments();
      if (ApplicationManager.getApplication().isDispatchThread()) {
         saveAll.run();
      } else {
         ApplicationManager.getApplication().invokeAndWait(saveAll);
      }

      String basePath = project.getBasePath();
      VirtualFile projectBaseDir = basePath != null ? LocalFileSystem.getInstance().findFileByPath(basePath) : null;

      for (String path : paths) {
         String normalized = path.replace('\\', '/');
         String abs = isAbsolutePath(normalized) ? normalized : (basePath != null ? basePath + "/" + normalized : null);
         if (abs != null) {
            try {
               LocalFileSystem.getInstance().refreshAndFindFileByPath(abs);
            } catch (RuntimeException ex) {
               LOG.warn("MentionBundleService: VFS pre-refresh failed for " + abs, ex);
            }
         }
      }

      List<MentionBundleService.PerPathUploadResult> results = new ArrayList<>();

      for (String path : paths) {
         List<ContextBundleEntry> entries = new ArrayList<>();
         ApplicationManager.getApplication().runReadAction(() -> this.buildEntriesForPaths(List.of(path), projectBaseDir, basePath, entries));
         if (entries.isEmpty()) {
            LOG.warn("MentionBundleService: no readable content for @" + path);
         } else {
            String dateStr = LocalDate.now().toString();
            List<ContextBundleSection> sections = List.of(
               new ContextBundleSection("CONTEXT_FILES", "Content of @" + path + " as referenced by the user.", List.copyOf(entries))
            );
            String markdown = ContextBundleBuilder.buildSectioned(sections, dateStr);
            String normalized = path.replace('\\', '/');
            String label = "ctx-" + normalized;
            String safeFile = "bundle_" + label.replaceAll("[^a-zA-Z0-9._-]", "_") + ".md";
            LastSendSnapshotWriter.saveDebugBundle(safeFile, markdown, project);
            if (conversationApiClient != null && serverConversationId != null && !serverConversationId.isEmpty()) {
               ConversationAttachmentResolver.cascadeDeleteRelated(conversationApiClient, uploadApiClient, serverConversationId, normalized);
            }

            File tempFile = ContextBundleWriter.writeTempBundle(markdown, label);

            try {
               final CountDownLatch latch = new CountDownLatch(1);
               final String[] fileIdHolder = new String[]{null};
               final Throwable[] errorHolder = new Throwable[]{null};
               uploadApiClient.uploadFile(tempFile, new FileUploadListener() {
                  @Override
                  public void onProgress(int pct) {
                  }

                  @Override
                  public void onSuccess(@NotNull FileReference ref) {
                     fileIdHolder[0] = ref.getId();
                     latch.countDown();
                  }

                  @Override
                  public void onError(@NotNull Throwable error) {
                     errorHolder[0] = error;
                     latch.countDown();
                  }
               });
               if (!latch.await(30L, TimeUnit.SECONDS)) {
                  LOG.warn("MentionBundleService: upload timed out for @" + path);
               } else if (errorHolder[0] != null) {
                  LOG.warn("MentionBundleService: upload failed for @" + path, errorHolder[0]);
               } else if (fileIdHolder[0] != null) {
                  results.add(new MentionBundleService.PerPathUploadResult(path, fileIdHolder[0], label, entries.size()));
                  LOG.info("MentionBundleService: uploaded @" + path + " as " + label + " (" + entries.size() + " file(s))");
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               throw new IOException("Upload interrupted for @" + path, e);
            } finally {
               if (tempFile.exists() && !tempFile.delete()) {
                  LOG.warn("MentionBundleService: could not delete temp file: " + tempFile);
               }
            }
         }
      }

      return results;
   }

   private static void collectFiles(@NotNull VirtualFile dir, @NotNull List<VirtualFile> result) {
      VirtualFile[] children = dir.getChildren();
      if (children != null) {
         for (VirtualFile child : children) {
            if (child.isDirectory()) {
               if (!FileFilterConstants.SKIP_DIRS.contains(child.getName())) {
                  collectFiles(child, result);
               }
            } else {
               result.add(child);
            }
         }
      }
   }

   @NotNull
   private static String getRelativePath(@Nullable String basePath, @NotNull VirtualFile file) {
      if (basePath == null) {
         return file.getName();
      }

      String filePath = file.getPath().replace('\\', '/');
      String base = basePath.replace('\\', '/');
      return filePath.startsWith(base + "/") ? filePath.substring(base.length() + 1) : file.getName();
   }

   private static boolean isAbsolutePath(@NotNull String path) {
      return path.startsWith("/") ? true : path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':';
   }

   public record BundleUploadResult(@NotNull String fileId, int entryCount, @NotNull List<String> paths) {
   }

   public record PerPathUploadResult(@NotNull String path, @NotNull String fileId, @NotNull String label, int entryCount) {
   }
}
