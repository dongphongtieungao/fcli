package com.tubitech.copilot.agent.tools.impl;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.VirtualFileVisitor.Option;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.tubitech.copilot.agent.FileFilterConstants;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import com.tubitech.copilot.agent.tools.ToolUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public final class GetDiagnosticsTool implements ToolExecutor {
   private static final int MAX_DIAGNOSTICS = 50;
   private static final int MAX_FILES_TO_SCAN = 200;
   private static final int MAX_DEPTH = 10;

   @NotNull
   @Override
   public String toolName() {
      return "get_diagnostics";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "get_diagnostics",
         "Get compiler errors and warnings from the IDE's code analysis. Accepts a file path OR a directory path to scan multiple files at once. For full project build errors, prefer run_command with your build tool (mvn, gradle).",
         List.of(new ToolParameter("path", "string", "File or directory path relative to project root. Use \".\" for project root.", true))
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String rawPath = arguments.get("path");
      if (rawPath != null && !rawPath.isBlank()) {
         String basePath = project.getBasePath();
         if (basePath == null) {
            return "Error: project base path is not available.";
         }

         String path = rawPath.trim();
         String absPath;
         if (".".equals(path)) {
            absPath = basePath;
         } else {
            String validated = ToolUtils.resolveAndValidatePath(basePath, path);
            if (validated == null) {
               return "Error: path must be within the project directory.";
            }

            absPath = validated;
         }

         VirtualFile target = ToolUtils.refreshAndFind(absPath);
         if (target == null) {
            String var10000 = "Error: path not found: " + path;
            if ("Error: path not found: " + path == null) {
               $$$reportNull$$$0(2);
            }

            return var10000;
         } else {
            return !target.isDirectory() ? this.scanSingleFile(target, path, project) : this.scanDirectory(target, basePath, path, project);
         }
      } else {
         return "Error: 'path' argument is required.";
      }
   }

   @NotNull
   private String scanSingleFile(@NotNull VirtualFile vf, @NotNull String displayPath, @NotNull Project project) {
      List<GetDiagnosticsTool.DiagnosticEntry> entries = this.collectDiagnostics(vf, displayPath, project);
      if (entries.isEmpty()) {
         String var10000 = "No errors or warnings in: " + displayPath;
         if ("No errors or warnings in: " + displayPath == null) {
            $$$reportNull$$$0(6);
         }

         return var10000;
      } else {
         return formatEntries(entries, 1);
      }
   }

   @NotNull
   private String scanDirectory(@NotNull VirtualFile dir, @NotNull String basePath, @NotNull String displayPath, @NotNull Project project) {
      List<VirtualFile> files = (List<VirtualFile>)ReadAction.compute(() -> {
         final List<VirtualFile> result = new ArrayList<>();
         VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor<Void>(new Option[]{VirtualFileVisitor.limit(10)}) {
            public boolean visitFile(@NotNull VirtualFile file) {
               if (result.size() >= 200) {
                  return false;
               }

               if (file.isDirectory()) {
                  return !FileFilterConstants.shouldSkipDir(file.getName());
               }

               if (!FileFilterConstants.shouldSkipFile(file.getName())) {
                  result.add(file);
               }

               return true;
            }
         });
         return result;
      });
      List<GetDiagnosticsTool.DiagnosticEntry> allEntries = new ArrayList<>();
      int filesScanned = 0;

      for (VirtualFile file : files) {
         if (allEntries.size() >= 50) {
            break;
         }

         String relPath = FileFilterConstants.relativePath(basePath, file);
         List<GetDiagnosticsTool.DiagnosticEntry> entries = this.collectDiagnostics(file, relPath, project);
         allEntries.addAll(entries);
         filesScanned++;
      }

      if (allEntries.isEmpty()) {
         String var10000 = "No errors or warnings found in " + displayPath + " (" + filesScanned + " files scanned).";
         if ("No errors or warnings found in " + displayPath + " (" + filesScanned + " files scanned)." == null) {
            $$$reportNull$$$0(11);
         }

         return var10000;
      } else {
         return formatEntries(allEntries, filesScanned);
      }
   }

   @NotNull
   private List<GetDiagnosticsTool.DiagnosticEntry> collectDiagnostics(@NotNull VirtualFile vf, @NotNull String displayPath, @NotNull Project project) {
      Document doc = (Document)ReadAction.compute(() -> {
         PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
         return psiFile == null ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
      });
      if (doc == null) {
         return List.of();
      }

      AtomicReference<List<GetDiagnosticsTool.DiagnosticEntry>> resultRef = new AtomicReference<>(List.of());
      Runnable collectFromMarkup = () -> {
         MarkupModel markupModel = DocumentMarkupModel.forDocument(doc, project, false);
         if (markupModel != null) {
            List<GetDiagnosticsTool.DiagnosticEntry> entries = new ArrayList<>();

            for (RangeHighlighter highlighter : markupModel.getAllHighlighters()) {
               HighlightInfo info = HighlightInfo.fromRangeHighlighter(highlighter);
               if (info != null
                  && info.getSeverity().compareTo(HighlightSeverity.WARNING) >= 0
                  && info.getDescription() != null
                  && !info.getDescription().isBlank()) {
                  int line = doc.getLineNumber(info.getStartOffset()) + 1;
                  entries.add(new GetDiagnosticsTool.DiagnosticEntry(displayPath, info.getSeverity().getName(), line, info.getDescription()));
                  if (entries.size() >= 50) {
                     break;
                  }
               }
            }

            resultRef.set(entries);
         }
      };
      if (ApplicationManager.getApplication().isDispatchThread()) {
         collectFromMarkup.run();
      } else {
         ApplicationManager.getApplication().invokeAndWait(collectFromMarkup);
      }

      return resultRef.get();
   }

   @NotNull
   private static String formatEntries(@NotNull List<GetDiagnosticsTool.DiagnosticEntry> entries, int filesScanned) {
      StringBuilder sb = new StringBuilder();
      int errors = 0;
      int warnings = 0;

      for (GetDiagnosticsTool.DiagnosticEntry e : entries) {
         if ("ERROR".equalsIgnoreCase(e.severity)) {
            errors++;
         } else {
            warnings++;
         }
      }

      sb.append("Found ").append(entries.size()).append(" diagnostic(s)");
      if (errors > 0) {
         sb.append(" (").append(errors).append(" errors");
      }

      if (errors > 0 && warnings > 0) {
         sb.append(", ").append(warnings).append(" warnings");
      }

      if (errors > 0) {
         sb.append(")");
      } else if (warnings > 0) {
         sb.append(" (").append(warnings).append(" warnings)");
      }

      sb.append(" in ").append(filesScanned).append(" file(s):\n\n");

      for (GetDiagnosticsTool.DiagnosticEntry e : entries) {
         sb.append(e.path).append(":").append(e.line).append(" [").append(e.severity).append("] ").append(e.message).append("\n");
      }

      if (entries.size() >= 50) {
         sb.append("\n... (capped at ").append(50).append(" diagnostics. Use a more specific path to see all.)");
      }

      return sb.toString().trim();
   }

   private static final class DiagnosticEntry {
      final String path;
      final String severity;
      final int line;
      final String message;

      DiagnosticEntry(@NotNull String path, @NotNull String severity, int line, @NotNull String message) {
         this.path = path;
         this.severity = severity;
         this.line = line;
         this.message = message;
      }
   }
}
