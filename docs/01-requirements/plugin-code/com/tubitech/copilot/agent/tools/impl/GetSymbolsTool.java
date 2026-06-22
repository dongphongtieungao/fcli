package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class GetSymbolsTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "get_symbols";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "get_symbols",
         "Get the outline/structure of a file: list all classes, methods, fields, and functions with their line numbers. Much faster than read_file when you only need to understand file structure without reading full code.",
         List.of(new ToolParameter("path", "string", "File path relative to project root", true)),
         "<tool_call>{\"id\":\"c1\",\"name\":\"get_symbols\",\"arguments\":{\"path\":\"src/main/java/com/example/UserService.java\"}}</tool_call>"
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

         String absPath = ToolUtils.resolveAndValidatePath(basePath, path);
         if (absPath == null) {
            return "Error: path must be within the project directory.";
         }

         VirtualFile vf = ToolUtils.refreshAndFind(absPath);
         if (vf == null) {
            String var7 = "Error: file not found: " + path;
            if ("Error: file not found: " + path == null) {
               $$$reportNull$$$0(2);
            }

            return var7;
         } else {
            return (String)ReadAction.compute(() -> {
               if (vf.getLength() > 1000000L) {
                  return "Error: file too large for symbol analysis (" + vf.getLength() / 1024L + " KB). Use read_file with line range instead.";
               }

               PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
               if (psiFile == null) {
                  return "Error: cannot parse file: " + path;
               }

               String fileText = psiFile.getText();
               List<String> symbols = new ArrayList<>();

               for (PsiNamedElement element : PsiTreeUtil.findChildrenOfType(psiFile, PsiNamedElement.class)) {
                  String name = element.getName();
                  if (name != null && !name.isBlank()) {
                     String kind = classifyElement(element);
                     if (kind != null) {
                        int line = getLineNumber(fileText, element.getTextOffset());
                        int depth = getDepth(element, psiFile);
                        String indent = "  ".repeat(Math.max(0, depth - 1));
                        symbols.add(indent + kind + " " + name + " (line " + line + ")");
                     }
                  }
               }

               if (symbols.isEmpty()) {
                  return "No symbols found in: " + path;
               }

               StringBuilder sb = new StringBuilder();
               sb.append("Symbols in ").append(path).append(":\n\n");

               for (String s : symbols) {
                  sb.append(s).append("\n");
               }

               return sb.toString().trim();
            });
         }
      } else {
         return "Error: 'path' argument is required.";
      }
   }

   private static String classifyElement(@NotNull PsiElement element) {
      String className = element.getClass().getSimpleName();
      if (!className.contains("Class") && !className.contains("Interface") && !className.contains("Enum")) {
         if (className.contains("Method") || className.contains("Function")) {
            return "[method]";
         } else if (className.contains("Field")) {
            return "[field]";
         } else if (className.contains("Variable") || className.contains("Parameter")) {
            return null;
         } else if (className.contains("Package")) {
            return null;
         } else {
            return className.contains("Property") ? "[property]" : null;
         }
      } else if (className.contains("Interface")) {
         return "[interface]";
      } else {
         return className.contains("Enum") ? "[enum]" : "[class]";
      }
   }

   private static int getLineNumber(@NotNull String text, int offset) {
      return ToolUtils.lineNumberAt(text, offset);
   }

   private static int getDepth(@NotNull PsiElement element, @NotNull PsiFile file) {
      int depth = 0;

      for (PsiElement parent = element.getParent(); parent != null && parent != file; parent = parent.getParent()) {
         String parentClass = parent.getClass().getSimpleName();
         if (parentClass.contains("Class") || parentClass.contains("Interface") || parentClass.contains("Enum")) {
            depth++;
         }
      }

      return depth;
   }
}
