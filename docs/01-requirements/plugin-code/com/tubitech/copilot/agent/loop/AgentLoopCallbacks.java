package com.tubitech.copilot.agent.loop;

import com.tubitech.copilot.agent.tools.ToolCall;
import com.tubitech.copilot.agent.tools.ToolResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AgentLoopCallbacks {
   void onToolExecuting(@NotNull ToolCall var1);

   void onToolComplete(@NotNull ToolResult var1);

   boolean onToolApprovalRequired(@NotNull ToolCall var1);

   void onAssistantText(@NotNull String var1);

   void onLoopIteration(int var1, int var2);

   default void onWaitingForResponse() {
   }

   boolean onIterationLimitReached(int var1);

   void onLoopFinished(@NotNull String var1, @Nullable String var2, @Nullable String var3);

   void onLoopError(@NotNull Throwable var1);
}
