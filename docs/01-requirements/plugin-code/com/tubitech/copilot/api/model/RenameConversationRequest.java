package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;

public class RenameConversationRequest {
   @SerializedName("new_name")
   private final String newName;

   public RenameConversationRequest(String newName) {
      this.newName = newName;
   }
}
