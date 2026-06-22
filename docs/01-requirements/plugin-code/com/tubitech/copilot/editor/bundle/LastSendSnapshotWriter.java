package com.tubitech.copilot.editor.bundle;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LastSendSnapshotWriter {
   private static final Logger LOG = Logger.getInstance(LastSendSnapshotWriter.class);
   private static final String DIR = ".tubi/temp";
   private static final String CHAT_FILE = "chat.md";
   private static final String BUNDLE_FILE = "bundle_context.md";
   private static final String CHAT_AFTER_FILE = "chat_after.md";

   private LastSendSnapshotWriter() {
   }

   public static void saveChatPrompt(@NotNull String chatPrompt, @NotNull Project project) {
      write("chat.md", chatPrompt, project);
   }

   public static void saveBundleContext(@Nullable String bundleMarkdown, @NotNull Project project) {
      write("bundle_context.md", bundleMarkdown != null ? bundleMarkdown : "", project);
   }

   public static void saveChatAfter(@NotNull String chatAfter, @NotNull Project project) {
      write("chat_after.md", chatAfter, project);
   }

   public static void clearDebugBundles(@NotNull Project project) {
      String basePath = project.getBasePath();
      if (basePath != null) {
         try {
            Path dir = Path.of(basePath, ".tubi/temp");
            if (!Files.isDirectory(dir)) {
               return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "bundle_*.md")) {
               for (Path p : stream) {
                  Files.deleteIfExists(p);
               }
            }
         } catch (IOException e) {
            LOG.warn("LastSendSnapshotWriter: failed to clear debug bundles", e);
         }
      }
   }

   public static void saveDebugBundle(@NotNull String filename, @NotNull String content, @NotNull Project project) {
      write(filename, content, project);
   }

   private static void write(@NotNull String filename, @NotNull String content, @NotNull Project project) {
      String basePath = project.getBasePath();
      if (basePath == null) {
         LOG.warn("LastSendSnapshotWriter: project has no base path, skipping " + filename);
      } else {
         try {
            Path dir = Path.of(basePath, ".tubi/temp");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(filename), content, StandardCharsets.UTF_8);
         } catch (IOException e) {
            LOG.warn("LastSendSnapshotWriter: failed to write " + filename, e);
         }
      }
   }
}
