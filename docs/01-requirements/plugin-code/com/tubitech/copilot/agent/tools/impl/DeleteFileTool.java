package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DeleteFileTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "delete_file";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "delete_file",
         "Delete a file from the project. The file must exist. Use this instead of run_command for file deletion.",
         List.of(new ToolParameter("path", "string", "File path relative to project root", true)),
         "<tool_call>{\"id\":\"c1\",\"name\":\"delete_file\",\"arguments\":{\"path\":\"src/OldFile.java\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String path = arguments.get("path");
      if (path != null && !path.isBlank()) {
         String basePath = project.getBasePath();
         if (basePath == null) {
            return "Error: project base path is not available.";
         }

         String fullPath = ToolUtils.resolveAndValidatePath(basePath, path);
         if (fullPath == null) {
            return "Error: path must be within the project directory.";
         }

         Path filePath = Path.of(fullPath);
         if (!Files.exists(filePath)) {
            String var9 = "Error: file not found: " + path;
            if ("Error: file not found: " + path == null) {
               $$$reportNull$$$0(2);
            }

            return var9;
         } else if (Files.isDirectory(filePath)) {
            String var8 = "Error: path is a directory, not a file: " + path;
            if ("Error: path is a directory, not a file: " + path == null) {
               $$$reportNull$$$0(3);
            }

            return var8;
         } else {
            Files.delete(filePath);
            VirtualFile parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(Path.of(fullPath).getParent().toString().replace('\\', '/'));
            if (parent != null) {
               parent.refresh(false, false);
            }

            String var10000 = "File deleted successfully: " + path;
            if ("File deleted successfully: " + path == null) {
               $$$reportNull$$$0(4);
            }

            return var10000;
         }
      } else {
         return "Error: 'path' argument is required.";
      }
   }
}
