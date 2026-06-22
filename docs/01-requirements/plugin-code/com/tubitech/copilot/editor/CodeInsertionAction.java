package com.tubitech.copilot.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class CodeInsertionAction {
   private CodeInsertionAction() {
   }

   public static boolean insertAtCaret(@NotNull Project project, @NotNull String code) {
      assert ApplicationManager.getApplication().isDispatchThread() : "insertAtCaret must be called on the EDT";
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
         return false;
      }

      int offset = editor.getCaretModel().getOffset();
      WriteCommandAction.runWriteCommandAction(
         project, "Tubi Copilot: Insert Code", null, () -> editor.getDocument().insertString(offset, code), new PsiFile[0]
      );
      editor.getSelectionModel().setSelection(offset, offset + code.length());
      editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
      return true;
   }
}
