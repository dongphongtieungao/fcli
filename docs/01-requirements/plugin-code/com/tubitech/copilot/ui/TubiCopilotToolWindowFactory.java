package com.tubitech.copilot.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public final class TubiCopilotToolWindowFactory implements ToolWindowFactory, DumbAware {
   public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
      ChatPanel chatPanel = new ChatPanel(project);
      Disposer.register(project, chatPanel);
      ContentFactory contentFactory = ContentFactory.getInstance();
      Content content = contentFactory.createContent(chatPanel, "", false);
      toolWindow.getContentManager().addContent(content);
   }
}
