package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.VirtualFileVisitor.Option;
import com.tubitech.copilot.agent.FileFilterConstants;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;

public final class SearchCodeTool implements ToolExecutor {
   private static final int DEFAULT_MAX_RESULTS = 50;
   private static final int MAX_FILE_SIZE = 262144;
   private static final int DEFAULT_CONTEXT_LINES = 0;

   @NotNull
   @Override
   public String toolName() {
      return "search_code";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "search_code",
         "Search for a text pattern across project files. Returns matching lines with file path and line number.",
         List.of(
            new ToolParameter("pattern", "string", "Text or regex pattern to search for", true),
            new ToolParameter("path", "string", "Subdirectory to search in (default: project root)", false),
            new ToolParameter("file_pattern", "string", "Glob for file names, e.g. *.java (default: all files)", false),
            new ToolParameter("context_lines", "integer", "Number of lines before/after each match (default: 0)", false),
            new ToolParameter("max_results", "integer", "Maximum results to return (default: 50)", false),
            new ToolParameter("offset", "integer", "Number of matches to skip (default: 0). Use to paginate: first call offset=0, next call offset=50", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"search_code\",\"arguments\":{\"pattern\":\"TODO\",\"file_pattern\":\"*.java\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String pattern = arguments.get("pattern");
      if (pattern != null && !pattern.isBlank()) {
         String basePath = project.getBasePath();
         if (basePath == null) {
            return "Error: project base path is not available.";
         }

         String subPath = arguments.getOrDefault("path", "");
         String filePattern = arguments.getOrDefault("file_pattern", "");
         String searchRoot;
         if (subPath.isBlank()) {
            searchRoot = basePath;
         } else {
            String validated = ToolUtils.resolveAndValidatePath(basePath, subPath);
            if (validated == null) {
               return "Error: path must be within the project directory.";
            }

            searchRoot = validated;
         }

         int contextLines = ToolUtils.parseIntOr(arguments.get("context_lines"), 0);
         int maxResults = ToolUtils.parseIntOr(arguments.get("max_results"), 50);
         int offset = Math.max(0, ToolUtils.parseIntOr(arguments.get("offset"), 0));

         Pattern regex;
         try {
            regex = Pattern.compile(pattern, 2);
         } catch (PatternSyntaxException e) {
            return "Error: invalid regex pattern: " + e.getDescription();
         }

         VirtualFile root = ToolUtils.refreshAndFind(searchRoot);
         return root == null ? "Error: directory not found: " + (subPath.isBlank() ? "project root" : subPath) : (String)ReadAction.compute(() -> {
            final List<String> matches = new ArrayList<>();
            final int[] skipped = new int[]{0};
            final int collectLimit = offset + maxResults;
            final long deadline = System.currentTimeMillis() + 30000L;
            VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor<Void>(new Option[0]) {
               public boolean visitFile(@NotNull VirtualFile file) {
                  if (skipped[0] >= collectLimit) {
                     return false;
                  }

                  if (System.currentTimeMillis() > deadline) {
                     return false;
                  }

                  if (file.isDirectory()) {
                     return !FileFilterConstants.shouldSkipDir(file.getName());
                  }

                  if (file.getLength() > 262144L) {
                     return true;
                  }

                  if (!filePattern.isBlank() && !SearchCodeTool.matchGlob(file.getName(), filePattern)) {
                     return true;
                  }

                  try {
                     String content = VfsUtilCore.loadText(file);
                     String[] lines = content.split("\n");
                     String relativePath = FileFilterConstants.relativePath(basePath, file);

                     for (int i = 0; i < lines.length && skipped[0] < collectLimit; i++) {
                        if (regex.matcher(lines[i]).find()) {
                           if (skipped[0] < offset) {
                              skipped[0]++;
                           } else {
                              skipped[0]++;
                              if (contextLines > 0) {
                                 matches.add("── " + relativePath + ":" + (i + 1) + " ──");
                                 int from = Math.max(0, i - contextLines);
                                 int to = Math.min(lines.length - 1, i + contextLines);

                                 for (int j = from; j <= to; j++) {
                                    String marker = j == i ? ">" : " ";
                                    matches.add(String.format("%s %4d | %s", marker, j + 1, lines[j]));
                                 }
                              } else {
                                 matches.add(relativePath + ":" + (i + 1) + ": " + lines[i].trim());
                              }
                           }
                        }
                     }
                  } catch (IOException var10) {
                  }

                  return true;
               }
            });
            if (matches.isEmpty()) {
               return offset > 0 ? "No more matches found for: " + pattern + " (offset=" + offset + ")" : "No matches found for: " + pattern;
            } else {
               String result = String.join("\n", matches);
               if (matches.size() >= maxResults) {
                  int nextOffset = offset + maxResults;
                  return result + "\n\n... (truncated at " + maxResults + " results) To see more: call search_code again with offset=" + nextOffset;
               } else {
                  return result;
               }
            }
         });
      } else {
         return "Error: 'pattern' argument is required.";
      }
   }

   private static boolean matchGlob(@NotNull String name, @NotNull String glob) {
      String regex = glob.replace(".", "\\.").replace("*", ".*").replace("?", ".");
      return name.matches(regex);
   }
}
