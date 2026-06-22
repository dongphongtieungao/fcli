package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FileProposalParser {
   private static final Logger LOG = Logger.getInstance(FileProposalParser.class);
   private static final Pattern HEADING_LINE = Pattern.compile("###\\s+File:\\s*(.+)", 2);
   private static final Pattern DELETE_LINE = Pattern.compile("###\\s+Delete:\\s*(.+)", 2);
   private static final Pattern FILEPATH_COMMENT = Pattern.compile("(?://|#)\\s*filepath:\\s*(.+)", 2);

   private FileProposalParser() {
   }

   @NotNull
   public static List<FileChangeProposal> parse(@NotNull String responseText, @Nullable Project project) {
      List<FileChangeProposal> results = new ArrayList<>();
      Set<String> seenPaths = new LinkedHashSet<>();
      String[] lines = responseText.split("\n", -1);
      int i = 0;

      label107:
      while (i < lines.length) {
         String line = lines[i];
         Matcher deleteMatcher = DELETE_LINE.matcher(line.trim());
         if (deleteMatcher.matches()) {
            String path = deleteMatcher.group(1).trim();
            i++;
            FileChangeProposal proposal = buildDeleteProposal(path, project);
            if (seenPaths.add(path)) {
               results.add(proposal);
            } else {
               for (int k = 0; k < results.size(); k++) {
                  if (results.get(k).getRelativePath().equals(path)) {
                     results.set(k, proposal);
                     LOG.debug("FileProposalParser: replaced duplicate proposal for '" + path + "' with DELETE");
                     break;
                  }
               }
            }
         } else {
            Matcher headingMatcher = HEADING_LINE.matcher(line.trim());
            if (!headingMatcher.matches()) {
               if (line.startsWith("```")) {
                  int nextNonEmpty = firstNonEmpty(lines, i + 1);
                  if (nextNonEmpty >= 0) {
                     Matcher commentMatcher = FILEPATH_COMMENT.matcher(lines[nextNonEmpty].trim());
                     if (commentMatcher.matches()) {
                        String path = commentMatcher.group(1).trim();
                        int contentStart = nextNonEmpty + 1;
                        String content = captureBlock(lines, contentStart);
                        i = advancePastBlock(lines, contentStart);
                        FileChangeProposal proposal2 = buildProposal(path, content, project);
                        if (seenPaths.add(path)) {
                           results.add(proposal2);
                           continue;
                        }

                        for (int k = 0; k < results.size(); k++) {
                           if (results.get(k).getRelativePath().equals(path)) {
                              results.set(k, proposal2);
                              LOG.debug("FileProposalParser: replaced duplicate proposal for '" + path + "' with last occurrence");
                              continue label107;
                           }
                        }
                        continue;
                     }
                  }
               }

               i++;
            } else {
               String path = headingMatcher.group(1).trim();
               i++;

               while (i < lines.length && lines[i].trim().isEmpty()) {
                  i++;
               }

               if (i < lines.length && lines[i].startsWith("```")) {
                  String content = captureBlock(lines, ++i);
                  i = advancePastBlock(lines, i);
                  FileChangeProposal proposal = buildProposal(path, content, project);
                  if (seenPaths.add(path)) {
                     results.add(proposal);
                  } else {
                     for (int k = 0; k < results.size(); k++) {
                        if (results.get(k).getRelativePath().equals(path)) {
                           results.set(k, proposal);
                           LOG.debug("FileProposalParser: replaced duplicate proposal for '" + path + "' with last occurrence");
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      if (!results.isEmpty()) {
         LOG.info("FileProposalParser: detected " + results.size() + " proposal(s)");
      }

      return results;
   }

   @NotNull
   private static String captureBlock(@NotNull String[] lines, int startIndex) {
      StringBuilder sb = new StringBuilder();
      int depth = 1;

      for (int j = startIndex; j < lines.length; j++) {
         String cl = lines[j];
         if (cl.startsWith("```")) {
            boolean hasLang = !cl.substring(3).trim().isEmpty();
            if (hasLang) {
               depth++;
               sb.append(cl).append('\n');
            } else if (depth > 1) {
               depth--;
               sb.append(cl).append('\n');
            } else {
               if (!isInnerOpen(lines, j)) {
                  break;
               }

               depth++;
               sb.append(cl).append('\n');
            }
         } else {
            sb.append(cl).append('\n');
         }
      }

      String result = sb.toString();
      return result.endsWith("\n") ? result.substring(0, result.length() - 1) : result;
   }

   private static int advancePastBlock(@NotNull String[] lines, int startIndex) {
      int depth = 1;

      for (int j = startIndex; j < lines.length; j++) {
         String cl = lines[j];
         if (cl.startsWith("```")) {
            boolean hasLang = !cl.substring(3).trim().isEmpty();
            if (hasLang) {
               depth++;
            } else if (depth > 1) {
               depth--;
            } else {
               if (!isInnerOpen(lines, j)) {
                  return j + 1;
               }

               depth++;
            }
         }
      }

      return lines.length;
   }

   private static int firstNonEmpty(@NotNull String[] lines, int from) {
      for (int j = from; j < lines.length; j++) {
         if (!lines[j].trim().isEmpty()) {
            return j;
         }
      }

      return -1;
   }

   private static boolean isInnerOpen(@NotNull String[] lines, int fenceIndex) {
      int next = fenceIndex + 1;

      while (next < lines.length && lines[next].trim().isEmpty()) {
         next++;
      }

      if (next >= lines.length) {
         return false;
      }

      String nextLine = lines[next];
      return nextLine.startsWith("```") ? false : !nextLine.trim().startsWith("###");
   }

   @NotNull
   private static FileChangeProposal buildProposal(@NotNull String relativePath, @NotNull String newContent, @Nullable Project project) {
      if (project != null) {
         VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
         if (baseDir != null) {
            VirtualFile existing = baseDir.findFileByRelativePath(relativePath);
            if (existing != null && existing.isValid()) {
               try {
                  String existingContent = VfsUtil.loadText(existing);
                  LOG.debug("Detected proposal: " + relativePath + " [MODIFY]");
                  return new FileChangeProposal(relativePath, FileChangeProposal.Operation.MODIFY, newContent, existingContent);
               } catch (IOException e) {
                  LOG.warn("FileProposalParser: could not read existing file '" + relativePath + "' — treating as CREATE", e);
               }
            }
         }
      }

      LOG.debug("Detected proposal: " + relativePath + " [CREATE]");
      return new FileChangeProposal(relativePath, FileChangeProposal.Operation.CREATE, newContent, null);
   }

   @NotNull
   private static FileChangeProposal buildDeleteProposal(@NotNull String relativePath, @Nullable Project project) {
      String existingContent = null;
      if (project != null) {
         VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
         if (baseDir != null) {
            VirtualFile existing = baseDir.findFileByRelativePath(relativePath);
            if (existing != null && existing.isValid()) {
               try {
                  existingContent = VfsUtil.loadText(existing);
               } catch (IOException e) {
                  LOG.warn("FileProposalParser: could not read file for DELETE proposal '" + relativePath + "'", e);
               }
            }
         }
      }

      LOG.debug("Detected proposal: " + relativePath + " [DELETE]");
      return FileChangeProposal.forDelete(relativePath, existingContent);
   }
}
