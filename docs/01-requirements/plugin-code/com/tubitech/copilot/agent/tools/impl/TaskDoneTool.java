package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class TaskDoneTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "task_done";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "task_done",
         "Signal that the task is complete. Call this when you have finished all changes and verification. The agent loop will stop after this call.",
         List.of(new ToolParameter("summary", "string", "Brief summary of what was done", true))
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) {
      return arguments.getOrDefault("summary", "Task completed.");
   }
}
