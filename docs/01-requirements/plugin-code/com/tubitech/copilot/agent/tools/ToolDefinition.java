package com.tubitech.copilot.agent.tools;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolDefinition {
   private final String name;
   private final String description;
   private final List<ToolParameter> parameters;
   private final String example;

   public ToolDefinition(@NotNull String name, @NotNull String description, @NotNull List<ToolParameter> parameters) {
      this(name, description, parameters, null);
   }

   public ToolDefinition(@NotNull String name, @NotNull String description, @NotNull List<ToolParameter> parameters, @Nullable String example) {
      this.name = name;
      this.description = description;
      this.parameters = List.copyOf(parameters);
      this.example = example;
   }

   @NotNull
   public String getName() {
      return this.name;
   }

   @NotNull
   public String getDescription() {
      return this.description;
   }

   @NotNull
   public List<ToolParameter> getParameters() {
      return this.parameters;
   }

   @NotNull
   public String toPromptBlock() {
      return this.toPromptBlock(true);
   }

   @NotNull
   public String toPromptBlock(boolean includeExample) {
      StringBuilder sb = new StringBuilder();
      if (includeExample) {
         sb.append("### ").append(this.name).append("\n");
         sb.append(this.description).append("\n");
         sb.append("Parameters:\n");

         for (ToolParameter p : this.parameters) {
            sb.append("- `").append(p.getName()).append("` (").append(p.getType());
            if (p.isRequired()) {
               sb.append(", required");
            }

            sb.append("): ").append(p.getDescription()).append("\n");
         }

         if (this.example != null) {
            sb.append("Example: `").append(this.example).append("`\n");
         }

         sb.append("\n");
      } else {
         sb.append("- **").append(this.name).append("**(");
         boolean first = true;

         for (ToolParameter p : this.parameters) {
            if (!first) {
               sb.append(", ");
            }

            first = false;
            sb.append(p.getName());
            if (p.isRequired()) {
               sb.append("*");
            }
         }

         sb.append("): ").append(this.description).append("\n");
      }

      return sb.toString();
   }
}
