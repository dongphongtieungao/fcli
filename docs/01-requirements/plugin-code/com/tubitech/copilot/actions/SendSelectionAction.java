package com.tubitech.copilot.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.tubitech.copilot.editor.EditorContextExtractor;
import com.tubitech.copilot.ui.ChatPanel;
import com.tubitech.copilot.ui.UiUtils;
import org.jetbrains.annotations.NotNull;

public final class SendSelectionAction extends AnAction {
   public void actionPerformed(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      if (project != null) {
         EditorContextExtractor.CodeContext ctx = EditorContextExtractor.getSelectedContext(project);
         if (ctx == null) {
            ctx = EditorContextExtractor.getActiveFileContext(project);
         }

         if (ctx == null) {
            ToolWindowManager.getInstance(project).notifyByBalloon("Tubi Copilot", MessageType.WARNING, "No code selected and no active file open.");
         } else {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Tubi Copilot");
            if (toolWindow != null) {
               toolWindow.activate(null);
            }

            ChatPanel chatPanel = UiUtils.getChatPanel(project);
            if (chatPanel != null) {
               chatPanel.setCodeContext(ctx);
            }
         }
      }
   }

   public void update(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      boolean enabled = project != null && FileEditorManager.getInstance(project).getSelectedTextEditor() != null;
      e.getPresentation().setEnabledAndVisible(enabled);
   }
}
