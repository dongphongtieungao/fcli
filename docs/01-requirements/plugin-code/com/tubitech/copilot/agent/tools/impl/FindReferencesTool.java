package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class FindReferencesTool implements ToolExecutor {
   private static final int MAX_RESULTS = 50;

   @NotNull
   @Override
   public String toolName() {
      return "find_references";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "find_references",
         "Find all usages of a symbol (class, method, field, variable) across the project. Much more accurate than search_code for refactoring — uses IntelliJ's semantic analysis, not text matching.",
         List.of(
            new ToolParameter("path", "string", "File path relative to project root where the symbol is defined", true),
            new ToolParameter("symbol", "string", "Name of the symbol to find references for (class name, method name, field name)", true),
            new ToolParameter("line", "integer", "Line number where the symbol is defined (helps disambiguate overloaded names)", false)
         ),
         "<tool_call>{\"id\":\"c1\",\"name\":\"find_references\",\"arguments\":{\"path\":\"src/main/java/com/example/UserService.java\",\"symbol\":\"getUserById\"}}</tool_call>"
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String path = arguments.get("path");
      if (path != null && !path.isBlank()) {
         String symbol = arguments.get("symbol");
         if (symbol != null && !symbol.isBlank()) {
            String basePath = project.getBasePath();
            if (basePath == null) {
               return "Error: project base path is not available.";
            }

            int targetLine = ToolUtils.parseIntOr(arguments.get("line"), -1);
            String absPath = ToolUtils.resolveAndValidatePath(basePath, path);
            if (absPath == null) {
               return "Error: path must be within the project directory.";
            }

            VirtualFile vf = ToolUtils.refreshAndFind(absPath);
            if (vf == null) {
               String var9 = "Error: file not found: " + path;
               if ("Error: file not found: " + path == null) {
                  $$$reportNull$$$0(2);
               }

               return var9;
            } else {
               return (String)ReadAction.compute(() -> {
                  PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
                  if (psiFile == null) {
                     return "Error: cannot parse file: " + path;
                  }

                  Collection<PsiNamedElement> candidates = PsiTreeUtil.findChildrenOfType(psiFile, PsiNamedElement.class);
                  PsiNamedElement target = null;

                  for (PsiNamedElement element : candidates) {
                     if (symbol.equals(element.getName())) {
                        if (targetLine > 0) {
                           int elementLine = ToolUtils.lineNumberAt(psiFile.getText(), element.getTextOffset());
                           if (elementLine == targetLine) {
                              target = element;
                              break;
                           }
                        }

                        if (target == null) {
                           target = element;
                        }
                     }
                  }

                  if (target == null) {
                     return "Error: symbol '" + symbol + "' not found in " + path + (targetLine > 0 ? " at line " + targetLine : "");
                  }

                  Collection<PsiReference> refs = ReferencesSearch.search(target, GlobalSearchScope.projectScope(project)).findAll();
                  List<String> results = new ArrayList<>();
                  int count = 0;
                  Map<PsiFile, String> textCache = new HashMap<>();

                  for (PsiReference ref : refs) {
                     if (count >= 50) {
                        break;
                     }

                     PsiElement refElement = ref.getElement();
                     PsiFile refFile = refElement.getContainingFile();
                     if (refFile != null && refFile.getVirtualFile() != null) {
                        String refPath = relativePath(basePath, refFile.getVirtualFile());
                        String fileText = textCache.computeIfAbsent(refFile, PsiElement::getText);
                        int offset = refElement.getTextOffset();
                        int line = ToolUtils.lineNumberAt(fileText, offset);
                        int lineStart = fileText.lastIndexOf(10, offset - 1) + 1;
                        int lineEnd = fileText.indexOf(10, offset);
                        if (lineEnd < 0) {
                           lineEnd = fileText.length();
                        }

                        String lineText = fileText.substring(lineStart, lineEnd);
                        results.add(refPath + ":" + line + ": " + lineText.trim());
                        count++;
                     }
                  }

                  if (results.isEmpty()) {
                     return "No references found for '" + symbol + "' in the project.";
                  }

                  StringBuilder sb = new StringBuilder();
                  sb.append("Found ").append(results.size()).append(" reference(s) for '").append(symbol).append("':\n\n");

                  for (String r : results) {
                     sb.append(r).append("\n");
                  }

                  if (results.size() >= 50) {
                     sb.append("\n... (truncated at ").append(50).append(" results) Provide 'line' argument to disambiguate if needed.");
                  }

                  return sb.toString().trim();
               });
            }
         } else {
            return "Error: 'symbol' argument is required.";
         }
      } else {
         return "Error: 'path' argument is required.";
      }
   }

   @NotNull
   private static String relativePath(@NotNull String basePath, @NotNull VirtualFile file) {
      String filePath = file.getPath();
      String normalizedBase = basePath.replace('\\', '/');
      return filePath.startsWith(normalizedBase) ? filePath.substring(normalizedBase.length() + 1) : filePath;
   }
}
