package com.tubitech.copilot.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class CodeReplaceAction {
   private CodeReplaceAction() {
   }

   public static boolean replaceSelection(@NotNull Project project, @NotNull String code) {
      assert ApplicationManager.getApplication().isDispatchThread() : "replaceSelection must be called on the EDT";
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
         return false;
      }

      SelectionModel sel = editor.getSelectionModel();
      if (!sel.hasSelection()) {
         return false;
      }

      int start = sel.getSelectionStart();
      int end = sel.getSelectionEnd();
      WriteCommandAction.runWriteCommandAction(
         project, "Tubi Copilot: Replace Code", null, () -> editor.getDocument().replaceString(start, end, code), new PsiFile[0]
      );
      editor.getSelectionModel().setSelection(start, start + code.length());
      editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
      return true;
   }
}
