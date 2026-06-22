package com.tubitech.copilot.agent.tools.impl;

import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class PlanDoneTool implements ToolExecutor {
   @NotNull
   @Override
   public String toolName() {
      return "plan_done";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "plan_done",
         "Signal that the plan is ready for user review. Call this AFTER outputting your numbered plan. The user will review and confirm before execution begins.",
         List.of(new ToolParameter("summary", "string", "Brief summary of the plan", true))
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) {
      return arguments.getOrDefault("summary", "Plan ready for review.");
   }
}
