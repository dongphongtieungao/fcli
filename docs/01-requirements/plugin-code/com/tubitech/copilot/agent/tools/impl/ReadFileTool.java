package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ReadFileTool implements ToolExecutor {
   private static final long MAX_FILE_SIZE = 2097152L;
   private static final int MAX_OUTPUT_CHARS = 16000;

   @NotNull
   @Override
   public String toolName() {
      return "read_file";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "read_file",
         "Read the contents of a file with line numbers. Returns as many lines as fit within the output limit (~16 000 chars). Small files are returned in full. For large files, use start_line/end_line to read specific sections.",
         List.of(
            new ToolParameter("path", "string", "File path relative to project root", true),
            new ToolParameter("start_line", "integer", "First line to read (1-based, default: 1)", false),
            new ToolParameter("end_line", "integer", "Last line to read (inclusive, default: as many as fit)", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"read_file\",\"arguments\":{\"path\":\"src/main/java/com/example/Main.java\"}}</tool_call>"
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

         int startLine = ToolUtils.parseIntOr(arguments.get("start_line"), 1);
         int requestedEnd = ToolUtils.parseIntOr(arguments.get("end_line"), -1);
         String absPath = ToolUtils.resolveAndValidatePath(basePath, path);
         if (absPath == null) {
            return "Error: path must be within the project directory.";
         }

         VirtualFile vf = ToolUtils.refreshAndFind(absPath);
         if (vf != null && !vf.isDirectory()) {
            return (String)ReadAction.compute(
               () -> {
                  if (vf.getLength() > 2097152L) {
                     return "Error: file too large (" + vf.getLength() / 1024L + " KB). Max 2048 KB.";
                  }

                  String content = VfsUtilCore.loadText(vf);
                  String[] lines = content.split("\\r?\\n", -1);
                  int totalLines = lines.length;
                  int from = Math.max(1, startLine);
                  int maxTo = requestedEnd > 0 ? Math.min(totalLines, requestedEnd) : totalLines;
                  if (from > totalLines) {
                     return "Error: start_line " + from + " exceeds file length (" + totalLines + " lines).";
                  }

                  StringBuilder sb = new StringBuilder();
                  int lastLine = from - 1;

                  for (int i = from - 1; i < maxTo; i++) {
                     String formatted = String.format("%4d | %s\n", i + 1, lines[i]);
                     if (sb.length() + formatted.length() > 16000) {
                        break;
                     }

                     sb.append(formatted);
                     lastLine = i + 1;
                  }

                  StringBuilder header = new StringBuilder();
                  header.append("[")
                     .append(path)
                     .append("] (")
                     .append(totalLines)
                     .append(" lines total, showing ")
                     .append(from)
                     .append("-")
                     .append(lastLine)
                     .append(")\n");
                  if (lastLine < totalLines) {
                     sb.append("... (")
                        .append(totalLines - lastLine)
                        .append(" more lines. ")
                        .append("Use start_line=")
                        .append(lastLine + 1)
                        .append(" to continue reading)");
                  }

                  return header.toString() + sb;
               }
            );
         }

         String var10000 = "Error: file not found: " + path;
         if ("Error: file not found: " + path == null) {
            $$$reportNull$$$0(2);
         }

         return var10000;
      } else {
         return "Error: 'path' argument is required.";
      }
   }
}
