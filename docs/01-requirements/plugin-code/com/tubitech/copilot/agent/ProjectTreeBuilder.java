package com.tubitech.copilot.agent;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Arrays;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

public final class ProjectTreeBuilder {
   private static final int MAX_DEPTH = 5;
   private static final int MAX_ENTRIES = 500;
   private static final int MAX_CHARS = 30000;

   private ProjectTreeBuilder() {
   }

   @NotNull
   public static String buildTree(@NotNull Project project) {
      String basePath = project.getBasePath();
      return basePath == null ? "" : (String)ReadAction.compute(() -> {
         VirtualFile root = LocalFileSystem.getInstance().findFileByPath(basePath);
         if (root != null && root.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            int[] count = new int[]{0};
            appendDir(root, "", true, 0, count, sb);
            if (count[0] >= 500 || sb.length() >= 30000) {
               sb.append("... (truncated — use list_files for deeper listing)\n");
            }

            return sb.toString();
         } else {
            return "";
         }
      });
   }

   private static void appendDir(@NotNull VirtualFile dir, @NotNull String indent, boolean isRoot, int depth, @NotNull int[] count, @NotNull StringBuilder sb) {
      if (count[0] < 500 && depth <= 5 && sb.length() < 30000) {
         VirtualFile[] children = dir.getChildren();
         if (children != null && children.length != 0) {
            Arrays.sort(
               children, Comparator.<VirtualFile, Boolean>comparing(f -> !f.isDirectory()).thenComparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER)
            );

            for (VirtualFile child : children) {
               if (count[0] >= 500) {
                  break;
               }

               String name = child.getName();
               if (child.isDirectory()) {
                  if (!FileFilterConstants.shouldSkipDir(name)) {
                     count[0]++;
                     sb.append(indent).append(name).append("/\n");
                     appendDir(child, indent + "  ", false, depth + 1, count, sb);
                  }
               } else if (!shouldSkipFile(name)) {
                  count[0]++;
                  sb.append(indent).append(name).append("\n");
               }
            }
         }
      }
   }

   private static boolean shouldSkipFile(@NotNull String name) {
      return FileFilterConstants.shouldSkipFile(name);
   }
}
