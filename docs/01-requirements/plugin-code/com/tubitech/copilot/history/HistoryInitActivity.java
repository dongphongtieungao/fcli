package com.tubitech.copilot.history;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity.DumbAware;
import org.jetbrains.annotations.NotNull;

public final class HistoryInitActivity implements DumbAware {
   public void runActivity(@NotNull Project project) {
      ApplicationManager.getApplication().executeOnPooledThread(() -> ProjectHistoryService.getInstance(project).init());
   }
}
