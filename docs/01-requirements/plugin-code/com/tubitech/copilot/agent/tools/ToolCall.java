package com.tubitech.copilot.agent.tools;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ToolCall {
   private final String id;
   private final String name;
   private final Map<String, String> arguments;

   public ToolCall(@NotNull String id, @NotNull String name, @NotNull Map<String, String> arguments) {
      this.id = id;
      this.name = name;
      this.arguments = Map.copyOf(arguments);
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @NotNull
   public String getName() {
      return this.name;
   }

   @NotNull
   public Map<String, String> getArguments() {
      return this.arguments;
   }

   @Override
   public String toString() {
      return "ToolCall{id='" + this.id + "', name='" + this.name + "', args=" + this.arguments.keySet() + "}";
   }
}
