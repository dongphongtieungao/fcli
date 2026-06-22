package com.tubitech.copilot.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class MentionSuggestionProvider {
   private static final Logger LOG = Logger.getInstance(MentionSuggestionProvider.class);
   public static final int MAX_RESULTS = 50;

   private MentionSuggestionProvider() {
   }

   @NotNull
   public static List<MentionSuggestion> suggest(@NotNull Project project, @NotNull String query) {
      List var8;
      try {
         VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
         if (baseDir != null) {
            if (query.isBlank()) {
               return rootChildren(baseDir);
            }

            int lastSlash = query.lastIndexOf(47);
            if (lastSlash >= 0) {
               String dirPath = query.substring(0, lastSlash);
               String fragment = query.substring(lastSlash + 1);
               VirtualFile dir = baseDir.findFileByRelativePath(dirPath);
               if (dir != null && dir.isDirectory()) {
                  return childrenMatching(baseDir, dir, fragment.toLowerCase());
               }
            }

            return deepSearch(baseDir, baseDir, query.toLowerCase());
         }

         var8 = List.of();
      } catch (Exception e) {
         LOG.warn("MentionSuggestionProvider: unexpected error for query='" + query + "'", e);
         return List.of();
      }

      return var8;
   }

   @NotNull
   private static List<MentionSuggestion> rootChildren(@NotNull VirtualFile baseDir) {
      List<MentionSuggestion> dirs = new ArrayList<>();
      List<MentionSuggestion> files = new ArrayList<>();
      VirtualFile[] children = baseDir.getChildren();
      if (children == null) {
         return List.of();
      }

      for (VirtualFile child : children) {
         String name = child.getName();
         if (!FolderContextExtractor.SKIP_DIRS.contains(name)) {
            MentionSuggestion s = toSuggestion(baseDir, child);
            if (child.isDirectory()) {
               dirs.add(s);
            } else {
               files.add(s);
            }
         }
      }

      dirs.sort(Comparator.comparing(MentionSuggestion::displayText));
      files.sort(Comparator.comparing(MentionSuggestion::displayText));
      List<MentionSuggestion> result = new ArrayList<>(dirs.size() + files.size());
      result.addAll(dirs);
      result.addAll(files);
      return result.size() > 50 ? result.subList(0, 50) : result;
   }

   @NotNull
   private static List<MentionSuggestion> childrenMatching(@NotNull VirtualFile baseDir, @NotNull VirtualFile dir, @NotNull String lowerFragment) {
      List<MentionSuggestion> dirs = new ArrayList<>();
      List<MentionSuggestion> files = new ArrayList<>();
      VirtualFile[] children = dir.getChildren();
      if (children == null) {
         return List.of();
      }

      for (VirtualFile child : children) {
         String lowerName = child.getName().toLowerCase();
         if (lowerFragment.isEmpty() || lowerName.startsWith(lowerFragment)) {
            if (child.isDirectory()) {
               dirs.add(toSuggestion(baseDir, child));
            } else {
               files.add(toSuggestion(baseDir, child));
            }
         }
      }

      dirs.sort(Comparator.comparing(MentionSuggestion::displayText));
      files.sort(Comparator.comparing(MentionSuggestion::displayText));
      List<MentionSuggestion> result = new ArrayList<>(dirs.size() + files.size());
      result.addAll(dirs);
      result.addAll(files);
      return result.size() > 50 ? result.subList(0, 50) : result;
   }

   @NotNull
   private static List<MentionSuggestion> deepSearch(@NotNull VirtualFile baseDir, @NotNull VirtualFile dir, @NotNull String lowerQuery) {
      List<MentionSuggestion> matchDirs = new ArrayList<>();
      List<MentionSuggestion> matchFiles = new ArrayList<>();
      collectDeep(baseDir, dir, lowerQuery, matchDirs, matchFiles);
      matchDirs.sort(Comparator.comparing(MentionSuggestion::displayText));
      matchFiles.sort(Comparator.comparing(MentionSuggestion::displayText));
      List<MentionSuggestion> result = new ArrayList<>(matchDirs.size() + matchFiles.size());
      result.addAll(matchDirs);
      result.addAll(matchFiles);
      return result.size() > 50 ? result.subList(0, 50) : result;
   }

   private static void collectDeep(
      @NotNull VirtualFile baseDir,
      @NotNull VirtualFile dir,
      @NotNull String lowerQuery,
      @NotNull List<MentionSuggestion> matchDirs,
      @NotNull List<MentionSuggestion> matchFiles
   ) {
      if (matchDirs.size() + matchFiles.size() < 100) {
         VirtualFile[] children = dir.getChildren();
         if (children != null) {
            VirtualFile[] sorted = (VirtualFile[])children.clone();
            Arrays.sort(sorted, Comparator.<VirtualFile>comparingInt(f -> f.isDirectory() ? 0 : 1).thenComparing(VirtualFile::getName));

            for (VirtualFile child : sorted) {
               String name = child.getName();
               if (child.isDirectory()) {
                  if (!FolderContextExtractor.SKIP_DIRS.contains(name)) {
                     String relativePath = getRelativePath(baseDir, child);
                     if (relativePath.toLowerCase().contains(lowerQuery)) {
                        matchDirs.add(toSuggestion(baseDir, child));
                     }

                     collectDeep(baseDir, child, lowerQuery, matchDirs, matchFiles);
                  }
               } else {
                  String relativePath = getRelativePath(baseDir, child);
                  if (relativePath.toLowerCase().contains(lowerQuery)) {
                     matchFiles.add(toSuggestion(baseDir, child));
                  }
               }
            }
         }
      }
   }

   @NotNull
   private static MentionSuggestion toSuggestion(@NotNull VirtualFile baseDir, @NotNull VirtualFile vf) {
      String relativePath = getRelativePath(baseDir, vf);
      return new MentionSuggestion(relativePath, relativePath, vf.isDirectory(), vf);
   }

   @NotNull
   private static String getRelativePath(@NotNull VirtualFile baseDir, @NotNull VirtualFile file) {
      String basePath = baseDir.getPath();
      String filePath = file.getPath();
      return filePath.startsWith(basePath + "/") ? filePath.substring(basePath.length() + 1) : file.getName();
   }
}
