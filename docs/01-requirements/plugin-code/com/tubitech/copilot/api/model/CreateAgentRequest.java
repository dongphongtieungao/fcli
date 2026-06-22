package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class CreateAgentRequest {
   @SerializedName("name")
   private final String name;
   @SerializedName("description")
   private final String description;
   @SerializedName("instructions")
   private final String instructions;
   @SerializedName("model_id")
   private final String modelId;
   @SerializedName("allow_download")
   private final boolean allowDownload;
   @SerializedName("show_instructions")
   private final boolean showInstructions;
   @SerializedName("sample_questions")
   private final List<String> sampleQuestions;
   @SerializedName("category")
   @Nullable
   private final String category;

   public CreateAgentRequest(String name, String description, String instructions, String modelId) {
      this.name = name;
      this.description = description;
      this.instructions = instructions;
      this.modelId = modelId;
      this.allowDownload = true;
      this.showInstructions = true;
      this.sampleQuestions = new ArrayList<>();
      this.category = null;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getInstructions() {
      return this.instructions;
   }

   public String getModelId() {
      return this.modelId;
   }
}
