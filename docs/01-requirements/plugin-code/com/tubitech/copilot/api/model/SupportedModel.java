package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupportedModel {
   @SerializedName("model_id")
   private String modelId;
   @SerializedName("display_name")
   private String displayName;
   @SerializedName("description")
   private String description;
   @SerializedName("reasoning")
   @Nullable
   private List<String> reasoning;
   @SerializedName("default_reasoning")
   @Nullable
   private Object defaultReasoning;

   public SupportedModel() {
   }

   public SupportedModel(@NotNull String modelId, @NotNull String displayName) {
      this.modelId = modelId;
      this.displayName = displayName;
   }

   public String getModelId() {
      return this.modelId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   @Nullable
   public List<String> getReasoning() {
      return this.reasoning;
   }

   @Nullable
   public Object getDefaultReasoning() {
      return this.defaultReasoning;
   }

   @Override
   public String toString() {
      return this.displayName != null ? this.displayName : this.modelId;
   }
}
