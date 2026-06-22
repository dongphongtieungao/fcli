package com.tubitech.copilot.agent.tools;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

public final class ToolResult {
   private static final Gson GSON = new Gson();
   private final String toolCallId;
   private final String toolName;
   private final boolean success;
   private final String output;
   private final long durationMs;

   public ToolResult(@NotNull String toolCallId, @NotNull String toolName, boolean success, @NotNull String output, long durationMs) {
      this.toolCallId = toolCallId;
      this.toolName = toolName;
      this.success = success;
      this.output = output;
      this.durationMs = durationMs;
   }

   @NotNull
   public String getToolCallId() {
      return this.toolCallId;
   }

   @NotNull
   public String getToolName() {
      return this.toolName;
   }

   public boolean isSuccess() {
      return this.success;
   }

   @NotNull
   public String getOutput() {
      return this.output;
   }

   public long getDurationMs() {
      return this.durationMs;
   }

   @NotNull
   public String toResponseBlock() {
      return "<tool_result>{\"id\":"
         + GSON.toJson(this.toolCallId)
         + ",\"status\":\""
         + (this.success ? "ok" : "error")
         + "\",\"output\":"
         + GSON.toJson(this.output)
         + "}</tool_result>";
   }
}
