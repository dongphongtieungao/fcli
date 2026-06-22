package com.tubitech.copilot.agent.tools;

import com.intellij.openapi.project.Project;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ToolExecutor {
   @NotNull
   String toolName();

   @NotNull
   ToolDefinition definition();

   @NotNull
   String execute(@NotNull Map<String, String> var1, @NotNull Project var2) throws Exception;
}
