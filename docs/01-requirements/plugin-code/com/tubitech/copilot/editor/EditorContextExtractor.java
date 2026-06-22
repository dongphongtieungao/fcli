package com.tubitech.copilot.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EditorContextExtractor {
   static final Map<String, String> LANGUAGE_MAP = Map.of(
      "java",
      "java",
      "kt",
      "kotlin",
      "py",
      "python",
      "js",
      "javascript",
      "ts",
      "typescript",
      "xml",
      "xml",
      "json",
      "json",
      "sql",
      "sql",
      "groovy",
      "groovy",
      "md",
      "markdown"
   );

   private EditorContextExtractor() {
   }

   @Nullable
   public static EditorContextExtractor.CodeContext getSelectedContext(@NotNull Project project) {
      assert ApplicationManager.getApplication().isDispatchThread() : "getSelectedContext must be called on the EDT";
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
         return null;
      } else {
         SelectionModel selection = editor.getSelectionModel();
         if (!selection.hasSelection()) {
            return null;
         } else {
            String code = selection.getSelectedText();
            if (code != null && !code.isBlank()) {
               String filename = resolveFilename(editor);
               return new EditorContextExtractor.CodeContext(code, filename, detectLanguage(filename));
            } else {
               return null;
            }
         }
      }
   }

   @Nullable
   public static EditorContextExtractor.CodeContext getActiveFileContext(@NotNull Project project) {
      assert ApplicationManager.getApplication().isDispatchThread() : "getActiveFileContext must be called on the EDT";
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
         return null;
      }

      String code = editor.getDocument().getText();
      String filename = resolveFilename(editor);
      return new EditorContextExtractor.CodeContext(code, filename, detectLanguage(filename));
   }

   @NotNull
   public static String detectLanguage(@NotNull String filename) {
      int dotIdx = filename.lastIndexOf(46);
      if (dotIdx >= 0 && dotIdx != filename.length() - 1) {
         String ext = filename.substring(dotIdx + 1).toLowerCase();
         return LANGUAGE_MAP.getOrDefault(ext, "text");
      } else {
         return "text";
      }
   }

   @NotNull
   private static String resolveFilename(@NotNull Editor editor) {
      VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
      return file != null ? file.getName() : "unknown";
   }

   public record CodeContext(@NotNull String code, @NotNull String filename, @NotNull String language) {
      @NotNull
      public String toInjectionFormat(@NotNull String userQuestion) {
         String var10000 = "[Code Context - "
            + this.filename
            + " ("
            + this.language
            + ")]\n```"
            + this.language
            + "\n"
            + this.code
            + "\n```\n\nUser question: "
            + userQuestion;
         if ("[Code Context - "
               + this.filename
               + " ("
               + this.language
               + ")]\n```"
               + this.language
               + "\n"
               + this.code
               + "\n```\n\nUser question: "
               + userQuestion
            == null) {
            $$$reportNull$$$0(4);
         }

         return var10000;
      }
   }
}
