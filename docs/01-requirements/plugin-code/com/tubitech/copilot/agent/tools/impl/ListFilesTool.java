package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.tubitech.copilot.agent.FileFilterConstants;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ListFilesTool implements ToolExecutor {
   private static final int MAX_ENTRIES = 200;
   private static final int MAX_DEPTH = 15;

   @NotNull
   @Override
   public String toolName() {
      return "list_files";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "list_files",
         "List files and directories at a given path. Defaults to project root. Skips common build/VCS directories.",
         List.of(
            new ToolParameter("path", "string", "Directory path relative to project root (default: root)", false),
            new ToolParameter("recursive", "boolean", "If true, list recursively (default: false)", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"list_files\",\"arguments\":{\"path\":\"src/main/java\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String basePath = project.getBasePath();
      if (basePath == null) {
         return "Error: project base path is not available.";
      }

      String subPath = arguments.getOrDefault("path", "");
      boolean recursive = "true".equalsIgnoreCase(arguments.getOrDefault("recursive", "false"));
      String dirPath;
      if (subPath.isBlank()) {
         dirPath = basePath;
      } else {
         String validated = ToolUtils.resolveAndValidatePath(basePath, subPath);
         if (validated == null) {
            return "Error: path must be within the project directory.";
         }

         dirPath = validated;
      }

      VirtualFile dir = ToolUtils.refreshAndFind(dirPath);
      return dir != null && dir.isDirectory()
         ? (String)ReadAction.compute(
            () -> {
               List<String> entries = new ArrayList<>();
               this.listDir(dir, basePath, recursive, entries, 0);
               if (entries.isEmpty()) {
                  return "(empty directory)";
               }

               String listing = String.join("\n", entries);
               return entries.size() >= 200
                  ? listing
                     + "\n\n... (truncated at 200 entries) To see more: call list_files on specific subdirectories shown above, or use recursive=false to list only the top level first."
                  : listing;
            }
         )
         : "Error: directory not found: " + (subPath.isBlank() ? "project root" : subPath);
   }

   private void listDir(@NotNull VirtualFile dir, @NotNull String basePath, boolean recursive, @NotNull List<String> entries, int depth) {
      if (entries.size() < 200) {
         if (depth <= 15) {
            VirtualFile[] children = dir.getChildren();
            if (children != null) {
               for (VirtualFile child : children) {
                  if (entries.size() >= 200) {
                     break;
                  }

                  if (!child.isDirectory() || !FileFilterConstants.shouldSkipDir(child.getName())) {
                     String rel = FileFilterConstants.relativePath(basePath, child);
                     entries.add(child.isDirectory() ? rel + "/" : rel);
                     if (recursive && child.isDirectory()) {
                        this.listDir(child, basePath, true, entries, depth + 1);
                     }
                  }
               }
            }
         }
      }
   }
}
