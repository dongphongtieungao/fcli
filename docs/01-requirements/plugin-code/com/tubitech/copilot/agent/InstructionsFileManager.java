package com.tubitech.copilot.agent;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InstructionsFileManager {
   private static final Logger LOG = Logger.getInstance(InstructionsFileManager.class);
   public static final String TUBI_DIR = ".tubi";
   public static final String INSTRUCTIONS_DIR = "instructions";
   private static final int MAX_INSTRUCTIONS_CHARS = 19500;

   private InstructionsFileManager() {
   }

   @NotNull
   public static Path instructionsDir(@NotNull Project project) {
      String base = project.getBasePath();
      if (base == null) {
         throw new IllegalStateException("Project has no base path");
      } else {
         return Paths.get(base, ".tubi", "instructions");
      }
   }

   @Nullable
   public static String readAll(@NotNull Project project) {
      Path dir = instructionsDir(project);
      if (!Files.isDirectory(dir)) {
         return null;
      }

      List<Path> mdFiles = new ArrayList<>();

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.md")) {
         for (Path p : stream) {
            if (Files.isRegularFile(p)) {
               mdFiles.add(p);
            }
         }
      } catch (IOException e) {
         LOG.warn("InstructionsFileManager: failed to list " + dir, e);
         return null;
      }

      if (mdFiles.isEmpty()) {
         return null;
      }

      mdFiles.sort(Comparator.comparing(p -> p.getFileName().toString()));
      StringBuilder sb = new StringBuilder();

      for (Path mdFile : mdFiles) {
         String content = readSingleFile(mdFile);
         if (content != null && !content.isBlank()) {
            if (!sb.isEmpty()) {
               sb.append("\n\n");
            }

            sb.append(content.strip());
         }
      }

      return sb.isEmpty() ? null : sb.toString();
   }

   @Nullable
   public static String validate(@NotNull String systemInstruction, @NotNull String customInstruction) {
      String trimmed = customInstruction.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      int total = systemInstruction.length() + 2 + trimmed.length();
      if (total <= 19500) {
         return null;
      }

      int available = 19500 - systemInstruction.length() - 2;
      return available <= 0
         ? "System instructions already exceed the 19500 character limit (" + systemInstruction.length() + " chars). Custom instructions cannot be added."
         : "Custom instructions are too long: "
            + trimmed.length()
            + " chars, but only "
            + available
            + " chars available (limit: 19500). Please reduce by "
            + (trimmed.length() - available)
            + " chars.";
   }

   @NotNull
   public static InstructionsFileManager.BuildResult buildInstructions(@NotNull String systemInstruction, @NotNull String customInstruction) {
      String trimmed = customInstruction.trim();
      String combined = trimmed.isEmpty() ? systemInstruction : systemInstruction + "\n\n" + trimmed;
      if (combined.length() > 19500) {
         LOG.warn(
            "InstructionsFileManager: combined instructions too long (" + combined.length() + " chars, limit 19500). Custom instructions will be truncated."
         );
         int overflowChars = combined.length() - 19500;
         int available = 19500 - systemInstruction.length() - 2;
         return available <= 0
            ? new InstructionsFileManager.BuildResult(systemInstruction.substring(0, 19500), true, combined.length(), overflowChars)
            : new InstructionsFileManager.BuildResult(
               systemInstruction + "\n\n" + trimmed.substring(0, Math.min(trimmed.length(), available)), true, combined.length(), overflowChars
            );
      } else {
         return new InstructionsFileManager.BuildResult(combined, false, combined.length(), 0);
      }
   }

   public static void ensureDir(@NotNull Project project) {
      Path dir = instructionsDir(project);
      if (!Files.isDirectory(dir)) {
         try {
            Files.createDirectories(dir);
            LOG.debug("InstructionsFileManager: created " + dir);
         } catch (IOException e) {
            LOG.warn("InstructionsFileManager: could not create " + dir, e);
         }
      }
   }

   public static void openInEditor(@NotNull Project project) {
      Path dir = instructionsDir(project);
      VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(dir);
      if (vf != null) {
         FileEditorManager.getInstance(project).openFile(vf, true);
      }
   }

   public static void migrateIfNeeded(@NotNull Project project, @NotNull String legacyAgentName) {
      String base = project.getBasePath();
      if (base != null) {
         Path legacyFile = Paths.get(base, ".tubi", legacyAgentName + ".instructions.md");
         if (Files.exists(legacyFile)) {
            Path newDir = instructionsDir(project);

            try {
               Files.createDirectories(newDir);

               boolean hasFiles;
               try (DirectoryStream<Path> stream = Files.newDirectoryStream(newDir, "*.md")) {
                  hasFiles = stream.iterator().hasNext();
               }

               if (hasFiles) {
                  Files.deleteIfExists(legacyFile);
                  return;
               }

               String content = Files.readString(legacyFile, StandardCharsets.UTF_8);
               if (!content.isBlank() && !content.trim().equals("# Add your custom agent instructions below.")) {
                  Files.writeString(newDir.resolve("custom.md"), content, StandardCharsets.UTF_8);
                  LOG.info("InstructionsFileManager: migrated " + legacyFile.getFileName() + " → instructions/custom.md");
               }

               Files.deleteIfExists(legacyFile);
            } catch (IOException e) {
               LOG.warn("InstructionsFileManager: migration failed for " + legacyFile, e);
            }
         }
      }
   }

   @Nullable
   private static String readSingleFile(@NotNull Path path) {
      VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
      if (vf != null) {
         String docText = (String)ReadAction.compute(() -> {
            Document doc = FileDocumentManager.getInstance().getDocument(vf);
            return doc != null ? doc.getText() : null;
         });
         if (docText != null) {
            return docText;
         }

         try {
            return new String(vf.contentsToByteArray(), StandardCharsets.UTF_8);
         } catch (IOException e) {
            LOG.warn("InstructionsFileManager: VFS read failed for " + path, e);
         }
      }

      try {
         return Files.readString(path, StandardCharsets.UTF_8);
      } catch (IOException e) {
         LOG.warn("InstructionsFileManager: cannot read " + path, e);
         return null;
      }
   }

   public static final class BuildResult {
      private final String instructions;
      private final boolean overflow;
      private final int totalChars;
      private final int overflowChars;

      BuildResult(@NotNull String instructions, boolean overflow, int totalChars, int overflowChars) {
         this.instructions = instructions;
         this.overflow = overflow;
         this.totalChars = totalChars;
         this.overflowChars = overflowChars;
      }

      @NotNull
      public String getInstructions() {
         return this.instructions;
      }

      public boolean isOverflow() {
         return this.overflow;
      }

      public int getTotalChars() {
         return this.totalChars;
      }

      public int getOverflowChars() {
         return this.overflowChars;
      }

      public int getLimit() {
         return 19500;
      }
   }
}
