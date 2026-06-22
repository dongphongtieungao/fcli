package com.tubitech.copilot.agent.tools;

import com.tubitech.copilot.agent.tools.impl.CreateFileTool;
import com.tubitech.copilot.agent.tools.impl.DeleteFileTool;
import com.tubitech.copilot.agent.tools.impl.EditFileTool;
import com.tubitech.copilot.agent.tools.impl.FindReferencesTool;
import com.tubitech.copilot.agent.tools.impl.GetDiagnosticsTool;
import com.tubitech.copilot.agent.tools.impl.GetSymbolsTool;
import com.tubitech.copilot.agent.tools.impl.GitDiffTool;
import com.tubitech.copilot.agent.tools.impl.ListFilesTool;
import com.tubitech.copilot.agent.tools.impl.PlanDoneTool;
import com.tubitech.copilot.agent.tools.impl.ReadFileTool;
import com.tubitech.copilot.agent.tools.impl.RunCommandTool;
import com.tubitech.copilot.agent.tools.impl.SearchCodeTool;
import com.tubitech.copilot.agent.tools.impl.TaskDoneTool;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolRegistry {
   private static final ToolRegistry INSTANCE = new ToolRegistry();
   private final Map<String, ToolExecutor> tools = new LinkedHashMap<>();

   private ToolRegistry() {
      this.register(new ReadFileTool());
      this.register(new EditFileTool());
      this.register(new CreateFileTool());
      this.register(new DeleteFileTool());
      this.register(new SearchCodeTool());
      this.register(new ListFilesTool());
      this.register(new RunCommandTool());
      this.register(new GetDiagnosticsTool());
      this.register(new TaskDoneTool());
      this.register(new PlanDoneTool());
      this.register(new FindReferencesTool());
      this.register(new GetSymbolsTool());
      this.register(new GitDiffTool());
   }

   public static ToolRegistry getInstance() {
      return INSTANCE;
   }

   private void register(@NotNull ToolExecutor tool) {
      this.tools.put(tool.toolName(), tool);
   }

   @Nullable
   public ToolExecutor get(@NotNull String name) {
      return this.tools.get(name);
   }

   @NotNull
   public Collection<ToolExecutor> all() {
      return this.tools.values();
   }

   @NotNull
   public String generateToolsPromptBlock() {
      return this.generateToolsPromptBlock(true);
   }

   @NotNull
   public String generateToolsPromptBlock(boolean includeExamples) {
      StringBuilder sb = new StringBuilder();
      sb.append("## Available tools\n\n");

      for (ToolExecutor tool : this.tools.values()) {
         sb.append(tool.definition().toPromptBlock(includeExamples));
      }

      return sb.toString();
   }
}
