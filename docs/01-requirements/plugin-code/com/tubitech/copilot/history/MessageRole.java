package com.tubitech.copilot.history;

public enum MessageRole {
   USER,
   ASSISTANT,
   TOOL_CALL,
   TOOL_RESULT,
   SYSTEM;

   public String toDbValue() {
      return this.name().toLowerCase();
   }

   public static MessageRole fromDbValue(String value) {
      if (value == null) {
         return USER;
      }

      try {
         return valueOf(value.toUpperCase());
      } catch (IllegalArgumentException e) {
         return USER;
      }
   }
}
