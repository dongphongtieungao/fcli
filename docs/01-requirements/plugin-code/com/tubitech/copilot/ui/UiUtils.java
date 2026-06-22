package com.tubitech.copilot.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UiUtils {
   private UiUtils() {
   }

   @Nullable
   public static ChatPanel getChatPanel(@NotNull Project project) {
      ToolWindow tw = ToolWindowManager.getInstance(project).getToolWindow("Tubi Copilot");
      if (tw == null) {
         return null;
      } else {
         ContentManager cm = tw.getContentManager();
         if (cm.getContentCount() == 0) {
            return null;
         } else {
            Content content = cm.getContent(0);
            if (content == null) {
               return null;
            } else {
               return content.getComponent() instanceof ChatPanel chatPanel ? chatPanel : null;
            }
         }
      }
   }
}
