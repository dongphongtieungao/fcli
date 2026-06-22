package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public final class EditFileTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "edit_file";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "edit_file",
         "Edit an existing file using search-and-replace. Provide the exact text to find (old_text) and the replacement text (new_text). The old_text must match exactly (including whitespace and indentation). Only the first occurrence is replaced. For creating new files, use create_file instead.",
         List.of(
            new ToolParameter("path", "string", "File path relative to project root", true),
            new ToolParameter("old_text", "string", "Exact text to find in the file (must match exactly)", true),
            new ToolParameter("new_text", "string", "Replacement text", true),
            new ToolParameter("replace_all", "boolean", "If true, replace ALL occurrences (default: false, first only)", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"edit_file\",\"arguments\":{\"path\":\"src/Main.java\",\"old_text\":\"System.out.println(\\\"old\\\");\",\"new_text\":\"System.out.println(\\\"new\\\");\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String path = arguments.get("path");
      if (path != null && !path.isBlank()) {
         String oldText = arguments.get("old_text");
         String newText = arguments.get("new_text");
         if (oldText == null) {
            return "Error: 'old_text' argument is required.";
         }

         if (newText == null) {
            return "Error: 'new_text' argument is required.";
         }

         String basePath = project.getBasePath();
         if (basePath == null) {
            return "Error: project base path is not available.";
         }

         String absPath = ToolUtils.resolveAndValidatePath(basePath, path);
         if (absPath == null) {
            return "Error: path must be within the project directory.";
         }

         VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(absPath);
         if (vf != null && !vf.isDirectory()) {
            boolean replaceAll = "true".equalsIgnoreCase(arguments.getOrDefault("replace_all", "false"));
            String currentContent = VfsUtilCore.loadText(vf);
            int idx = currentContent.indexOf(oldText);
            if (idx < 0) {
               String preview = oldText.length() > 100 ? oldText.substring(0, 100) + "..." : oldText;
               String var22 = "Error: old_text not found in " + path + ". Make sure the text matches exactly (including whitespace). Searched for: " + preview;
               if ("Error: old_text not found in " + path + ". Make sure the text matches exactly (including whitespace). Searched for: " + preview == null) {
                  $$$reportNull$$$0(3);
               }

               return var22;
            } else {
               String updatedContent;
               int replacementCount;
               if (replaceAll) {
                  int count = 0;
                  StringBuilder sb = new StringBuilder();

                  int pos;
                  int found;
                  for (pos = 0; (found = currentContent.indexOf(oldText, pos)) >= 0; count++) {
                     sb.append(currentContent, pos, found).append(newText);
                     pos = found + oldText.length();
                  }

                  sb.append(currentContent, pos, currentContent.length());
                  updatedContent = sb.toString();
                  replacementCount = count;
               } else {
                  updatedContent = currentContent.substring(0, idx) + newText + currentContent.substring(idx + oldText.length());
                  replacementCount = 1;
               }

               AtomicReference<String> result = new AtomicReference<>("Error: edit operation did not complete.");
               Runnable editAction = () -> WriteCommandAction.runWriteCommandAction(
                  project,
                  "Tubi Copilot: Edit File",
                  null,
                  () -> {
                     try {
                        VfsUtil.saveText(vf, updatedContent);
                        int linesChanged = countLines(newText) - countLines(oldText);
                        String delta = linesChanged > 0
                           ? "(+" + linesChanged + " lines)"
                           : (linesChanged < 0 ? "(" + linesChanged + " lines)" : "(same line count)");
                        String countInfo = replacementCount > 1 ? " [" + replacementCount + " replacements]" : "";
                        result.set("File edited: " + path + " " + delta + countInfo);
                     } catch (IOException e) {
                        result.set("Error writing file: " + e.getMessage());
                     }
                  },
                  new PsiFile[0]
               );
               if (ApplicationManager.getApplication().isDispatchThread()) {
                  editAction.run();
               } else {
                  ApplicationManager.getApplication().invokeAndWait(editAction);
               }

               return result.get();
            }
         } else {
            String var10000 = "Error: file not found: " + path + ". Use create_file to create new files.";
            if ("Error: file not found: " + path + ". Use create_file to create new files." == null) {
               $$$reportNull$$$0(2);
            }

            return var10000;
         }
      } else {
         return "Error: 'path' argument is required.";
      }
   }

   private static int countLines(@NotNull String text) {
      if (text.isEmpty()) {
         return 0;
      }

      int count = 1;

      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == '\n') {
            count++;
         }
      }

      return count;
   }
}
