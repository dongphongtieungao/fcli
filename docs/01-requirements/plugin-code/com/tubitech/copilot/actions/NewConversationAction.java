package com.tubitech.copilot.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.ui.ChatPanel;
import com.tubitech.copilot.ui.UiUtils;
import org.jetbrains.annotations.NotNull;

public final class NewConversationAction extends AnAction {
   public void actionPerformed(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      if (project != null) {
         ChatPanel chatPanel = UiUtils.getChatPanel(project);
         if (chatPanel != null) {
            chatPanel.saveCurrentSession();
            chatPanel.startNewSession();
            chatPanel.clearMessages();
            chatPanel.getAttachmentPreviewPanel().clear();
         }
      }
   }
}
