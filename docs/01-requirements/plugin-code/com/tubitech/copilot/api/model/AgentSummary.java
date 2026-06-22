package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class AgentSummary {
   @SerializedName("id")
   private String id;
   @SerializedName("name")
   private String name;
   @SerializedName("description")
   @Nullable
   private String description;
   @SerializedName("status")
   private String status;
   @SerializedName("model_id")
   @Nullable
   private String modelId;

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   public String getStatus() {
      return this.status;
   }

   @Nullable
   public String getModelId() {
      return this.modelId;
   }
}
