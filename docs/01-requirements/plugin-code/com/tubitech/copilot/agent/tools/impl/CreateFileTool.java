package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class CreateFileTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "create_file";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "create_file",
         "Create a new file with the given content. Parent directories are created automatically. Fails if the file already exists — use edit_file for existing files.",
         List.of(
            new ToolParameter("path", "string", "File path relative to project root", true),
            new ToolParameter("content", "string", "Full file content to write", true)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"create_file\",\"arguments\":{\"path\":\"src/Hello.java\",\"content\":\"public class Hello {\\n    public static void main(String[] args) {\\n        System.out.println(\\\"Hello\\\");\\n    }\\n}\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String path = arguments.get("path");
      String content = arguments.get("content");
      if (path == null || path.isBlank()) {
         return "Error: 'path' argument is required.";
      }

      if (content == null) {
         return "Error: 'content' argument is required.";
      }

      String basePath = project.getBasePath();
      if (basePath == null) {
         return "Error: project base path is not available.";
      }

      String validated = ToolUtils.resolveAndValidatePath(basePath, path);
      if (validated == null) {
         return "Error: path must be within the project directory.";
      }

      Path filePath = Path.of(validated);
      if (Files.exists(filePath)) {
         String var9 = "Error: file already exists: " + path + ". Use edit_file to modify existing files.";
         if ("Error: file already exists: " + path + ". Use edit_file to modify existing files." == null) {
            $$$reportNull$$$0(2);
         }

         return var9;
      } else {
         Files.createDirectories(filePath.getParent());
         Files.writeString(filePath, content, StandardCharsets.UTF_8);
         LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath.toString().replace('\\', '/'));
         int lines = content.split("\n", -1).length;
         String var10000 = "File created: " + path + " (" + lines + " lines)";
         if ("File created: " + path + " (" + lines + " lines)" == null) {
            $$$reportNull$$$0(3);
         }

         return var10000;
      }
   }
}
