package com.tubitech.copilot.agent.loop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolCallStreamDetector {
   private static final String[] OPEN_TAGS = new String[]{"<tool_call>", "<tool_result>"};
   private static final String[] CLOSE_TAGS = new String[]{"</tool_call>", "</tool_result>"};
   private static final int MAX_BUFFER_SIZE = 100000;
   private final StringBuilder buffer = new StringBuilder();
   private boolean insideTag = false;
   private int activeTagIndex = -1;

   @Nullable
   public String process(@NotNull String token) {
      this.buffer.append(token);
      if (this.buffer.length() > 100000) {
         if (this.insideTag) {
            this.buffer.setLength(0);
            this.insideTag = false;
            this.activeTagIndex = -1;
            return null;
         } else {
            String flushed = this.buffer.toString();
            this.buffer.setLength(0);
            return flushed;
         }
      } else {
         StringBuilder visible = new StringBuilder();
         boolean changed = true;

         while (changed) {
            changed = false;
            if (this.insideTag) {
               String closeTag = CLOSE_TAGS[this.activeTagIndex];
               int closeIdx = this.buffer.indexOf(closeTag);
               if (closeIdx >= 0) {
                  int end = closeIdx + closeTag.length();
                  String remainder = this.buffer.substring(end);
                  this.buffer.setLength(0);
                  this.buffer.append(remainder);
                  this.insideTag = false;
                  this.activeTagIndex = -1;
                  changed = !this.buffer.isEmpty();
               }
            } else {
               String current = this.buffer.toString();
               boolean foundTag = false;

               for (int t = 0; t < OPEN_TAGS.length; t++) {
                  int idx = current.indexOf(OPEN_TAGS[t]);
                  if (idx >= 0) {
                     visible.append(current, 0, idx);
                     this.insideTag = true;
                     this.activeTagIndex = t;
                     this.buffer.setLength(0);
                     this.buffer.append(current.substring(idx + OPEN_TAGS[t].length()));
                     changed = true;
                     foundTag = true;
                     break;
                  }
               }

               if (!foundTag) {
                  for (int t = 0; t < OPEN_TAGS.length; t++) {
                     if (couldBePartialMatch(current, OPEN_TAGS[t])) {
                        return visible.isEmpty() ? null : visible.toString();
                     }
                  }

                  visible.append(current);
                  this.buffer.setLength(0);
               }
            }
         }

         return visible.isEmpty() ? null : visible.toString();
      }
   }

   private static boolean couldBePartialMatch(@NotNull String text, @NotNull String tag) {
      int len = text.length();

      for (int i = Math.max(0, len - tag.length()); i < len; i++) {
         int suffixLen = len - i;
         if (tag.regionMatches(0, text, i, suffixLen)) {
            return true;
         }
      }

      return false;
   }

   public boolean isInsideToolCall() {
      return this.insideTag;
   }

   public void reset() {
      this.buffer.setLength(0);
      this.insideTag = false;
      this.activeTagIndex = -1;
   }
}
