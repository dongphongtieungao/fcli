package com.tubitech.copilot.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.tubitech.copilot.history.ConversationHistory;
import com.tubitech.copilot.history.ProjectHistoryService;
import com.tubitech.copilot.ui.ChatPanel;
import com.tubitech.copilot.ui.UiUtils;
import org.jetbrains.annotations.NotNull;

public final class ClearHistoryAction extends AnAction {
   public void actionPerformed(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      if (project != null) {
         int result = Messages.showYesNoDialog(
            project, "Are you sure you want to delete all conversation history?\nThis cannot be undone.", "Clear History", Messages.getWarningIcon()
         );
         if (result == 0) {
            ConversationHistory.getInstance().clearAll();
            ProjectHistoryService.getInstance(project).clearAll();
            ChatPanel chatPanel = UiUtils.getChatPanel(project);
            if (chatPanel != null) {
               chatPanel.clearMessages();
               chatPanel.startNewSession();
            }
         }
      }
   }
}
