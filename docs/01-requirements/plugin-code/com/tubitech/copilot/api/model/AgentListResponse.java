package com.tubitech.copilot.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AgentListResponse {
   @SerializedName("items")
   private List<AgentSummary> items;
   @SerializedName("total")
   private int total;
   @SerializedName("offset")
   private int offset;
   @SerializedName("limit")
   private int limit;

   public List<AgentSummary> getItems() {
      return this.items;
   }

   public int getTotal() {
      return this.total;
   }

   public int getOffset() {
      return this.offset;
   }

   public int getLimit() {
      return this.limit;
   }
}
