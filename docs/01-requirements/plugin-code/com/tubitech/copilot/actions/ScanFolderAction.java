package com.tubitech.copilot.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.tubitech.copilot.editor.FolderContextExtractor;
import com.tubitech.copilot.ui.ChatPanel;
import com.tubitech.copilot.ui.UiUtils;
import org.jetbrains.annotations.NotNull;

public final class ScanFolderAction extends AnAction {
   private static final Logger LOG = Logger.getInstance(ScanFolderAction.class);

   public void actionPerformed(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      if (project != null) {
         VirtualFile selected = (VirtualFile)e.getData(CommonDataKeys.VIRTUAL_FILE);
         if (selected != null && selected.isDirectory()) {
            ApplicationManager.getApplication()
               .executeOnPooledThread(
                  () -> {
                     FolderContextExtractor.FolderContext[] result = new FolderContextExtractor.FolderContext[]{null};
                     ApplicationManager.getApplication().runReadAction(() -> result[0] = FolderContextExtractor.extractFolderContext(selected));
                     FolderContextExtractor.FolderContext ctx = result[0];
                     ApplicationManager.getApplication()
                        .invokeLater(
                           () -> {
                              if (ctx == null) {
                                 ToolWindowManager.getInstance(project)
                                    .notifyByBalloon("Tubi Copilot", MessageType.WARNING, "No readable text/code files found in '" + selected.getName() + "'.");
                              } else {
                                 if (ctx.truncated()) {
                                    LOG.info(
                                       "ScanFolderAction: truncated scan of '"
                                          + selected.getName()
                                          + "' — included "
                                          + ctx.fileCount()
                                          + " files, "
                                          + ctx.totalBytes()
                                          + " B"
                                    );
                                 } else {
                                    LOG.info(
                                       "ScanFolderAction: scanned '" + selected.getName() + "' — " + ctx.fileCount() + " files, " + ctx.totalBytes() + " B"
                                    );
                                 }

                                 ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Tubi Copilot");
                                 if (toolWindow != null) {
                                    toolWindow.activate(null);
                                 }

                                 ChatPanel chatPanel = UiUtils.getChatPanel(project);
                                 if (chatPanel != null) {
                                    chatPanel.setFolderContext(ctx);
                                 }
                              }
                           }
                        );
                  }
               );
         }
      }
   }

   @NotNull
   public ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
   }

   public void update(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      VirtualFile file = (VirtualFile)e.getData(CommonDataKeys.VIRTUAL_FILE);
      boolean isDir = project != null && file != null && file.isDirectory();
      e.getPresentation().setEnabledAndVisible(isDir);
   }
}
