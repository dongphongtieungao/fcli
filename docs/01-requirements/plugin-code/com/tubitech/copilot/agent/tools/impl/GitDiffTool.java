package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class GitDiffTool implements ToolExecutor {
   private static final int TIMEOUT_MS = 15000;
   private static final int MAX_OUTPUT_CHARS = 16000;

   @NotNull
   @Override
   public String toolName() {
      return "git_diff";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "git_diff",
         "Show git diff — unstaged changes, staged changes, or diff between branches/commits. Use to understand what has been modified before making edits.",
         List.of(
            new ToolParameter(
               "mode",
               "string",
               "One of: 'unstaged' (default), 'staged', 'HEAD' (all uncommitted), or a git ref like 'main', 'HEAD~3', 'abc123' to compare against",
               false
            ),
            new ToolParameter("path", "string", "Limit diff to a specific file or directory (relative to project root)", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"git_diff\",\"arguments\":{\"mode\":\"HEAD\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String basePath = project.getBasePath();
      if (basePath == null) {
         return "Error: project base path is not available.";
      }

      if (!Files.exists(Paths.get(basePath, ".git"))) {
         return "Error: project is not a git repository.";
      }

      String mode = arguments.getOrDefault("mode", "unstaged").trim();
      String path = arguments.getOrDefault("path", "").trim();
      List<String> gitArgs = new ArrayList<>();
      gitArgs.add("diff");
      switch (mode.toLowerCase()) {
         case "staged":
            gitArgs.add("--cached");
            break;
         case "head":
            gitArgs.add("HEAD");
         case "unstaged":
            break;
         default:
            gitArgs.add(mode);
      }

      gitArgs.add("--stat");
      gitArgs.add("--patch");
      if (!path.isBlank()) {
         gitArgs.add("--");
         gitArgs.add(path);
      }

      String result = ToolUtils.runGit(basePath, gitArgs, 15000);
      if (result.isBlank()) {
         return "No changes found.";
      } else {
         return result.length() > 16000
            ? result.substring(0, 16000)
               + "\n\n... [TRUNCATED at 16000 chars] Diff is too large. Use the 'path' argument to diff specific files or directories one at a time."
            : result;
      }
   }
}
