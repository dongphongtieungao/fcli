package com.tubitech.copilot.agent.tools;

import org.jetbrains.annotations.NotNull;

public final class ToolParameter {
   private final String name;
   private final String type;
   private final String description;
   private final boolean required;

   public ToolParameter(@NotNull String name, @NotNull String type, @NotNull String description, boolean required) {
      this.name = name;
      this.type = type;
      this.description = description;
      this.required = required;
   }

   @NotNull
   public String getName() {
      return this.name;
   }

   @NotNull
   public String getType() {
      return this.type;
   }

   @NotNull
   public String getDescription() {
      return this.description;
   }

   public boolean isRequired() {
      return this.required;
   }
}
