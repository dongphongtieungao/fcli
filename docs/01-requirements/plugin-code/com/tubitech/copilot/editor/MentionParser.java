package com.tubitech.copilot.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MentionParser {
   private static final Pattern MENTION_PATTERN = Pattern.compile("@\"([^\"]+)\"|@([^\\s\"@]+)");
   public static final Set<String> UPLOAD_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf", "epub");

   private MentionParser() {
   }

   public static boolean hasMentions(@NotNull String text) {
      return MENTION_PATTERN.matcher(text).find();
   }

   @NotNull
   public static String stripLeadingMentions(@Nullable String text) {
      if (text != null && !text.isBlank()) {
         String[] lines = text.split("\n", -1);
         int start = 0;

         while (start < lines.length && lines[start].trim().isEmpty()) {
            start++;
         }

         while (start < lines.length && lines[start].trim().startsWith("@")) {
            start++;
         }

         while (start < lines.length && lines[start].trim().isEmpty()) {
            start++;
         }

         if (start >= lines.length) {
            return "";
         }

         StringBuilder sb = new StringBuilder();

         for (int i = start; i < lines.length; i++) {
            if (i > start) {
               sb.append('\n');
            }

            sb.append(lines[i]);
         }

         return sb.toString().trim();
      } else {
         return "";
      }
   }

   @NotNull
   public static List<String> extractTokens(@NotNull String text) {
      List<String> tokens = new ArrayList<>();
      Matcher m = MENTION_PATTERN.matcher(text);

      while (m.find()) {
         tokens.add(m.group());
      }

      return tokens;
   }

   @NotNull
   public static String extractPath(@NotNull String rawToken) {
      return rawToken.startsWith("@\"") && rawToken.endsWith("\"") && rawToken.length() > 3
         ? rawToken.substring(2, rawToken.length() - 1)
         : rawToken.substring(1);
   }

   @NotNull
   public static String stripMentions(@NotNull String text) {
      return MENTION_PATTERN.matcher(text).replaceAll("").replaceAll("\\s{2,}", " ").trim();
   }

   @NotNull
   public static List<MentionParser.ParsedMention> resolve(@NotNull String text, @Nullable VirtualFile projectBaseDir) {
      List<MentionParser.ParsedMention> result = new ArrayList<>();
      Matcher m = MENTION_PATTERN.matcher(text);

      while (m.find()) {
         String rawToken = m.group();
         String path = extractPath(rawToken);
         String normalised = path.replace('\\', '/');
         VirtualFile resolved = null;
         if (projectBaseDir != null) {
            resolved = projectBaseDir.findFileByRelativePath(normalised);
            if (resolved == null && normalised.endsWith("/")) {
               resolved = projectBaseDir.findFileByRelativePath(normalised.substring(0, normalised.length() - 1));
            }

            if (resolved == null && ApplicationManager.getApplication() != null) {
               String cleanPath = normalised.endsWith("/") ? normalised.substring(0, normalised.length() - 1) : normalised;
               resolved = LocalFileSystem.getInstance().findFileByPath(projectBaseDir.getPath() + "/" + cleanPath);
            }
         }

         MentionParser.MentionType type;
         if (resolved == null) {
            type = MentionParser.MentionType.UNRESOLVED;
         } else if (resolved.isDirectory()) {
            type = MentionParser.MentionType.FOLDER;
         } else {
            String ext = resolved.getExtension();
            if (ext != null && UPLOAD_EXTENSIONS.contains(ext.toLowerCase())) {
               type = MentionParser.MentionType.UPLOAD_FILE;
            } else {
               type = MentionParser.MentionType.TEXT_FILE;
            }
         }

         result.add(new MentionParser.ParsedMention(rawToken, path, resolved, type));
      }

      return result;
   }

   public enum MentionType {
      FOLDER,
      TEXT_FILE,
      UPLOAD_FILE,
      UNRESOLVED;
   }

   public record ParsedMention(@NotNull String rawToken, @NotNull String path, @Nullable VirtualFile resolved, @NotNull MentionParser.MentionType type) {
   }
}
