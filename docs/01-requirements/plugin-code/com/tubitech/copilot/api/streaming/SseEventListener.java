package com.tubitech.copilot.api.streaming;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.agent.ResponseNormalizer;
import com.tubitech.copilot.api.StreamListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SseEventListener {
   private static final Logger LOG = Logger.getInstance(SseEventListener.class);
   private static final String EVENT_PREFIX = "event: ";
   private static final String DATA_PREFIX = "data: ";
   private static final String EVENT_DATA = "data";
   private static final String EVENT_MES_INFO = "mes_info";
   private static final String EVENT_CONV = "conv";
   private static final String EVENT_HINT = "hint";
   private static final String EVENT_DONE = "DONE";
   private static final int MAX_CONTENT_LENGTH = 10000000;
   private final StreamListener listener;
   private final StringBuilder fullContent = new StringBuilder();
   private boolean contentOverflow = false;
   private final List<String> suggestedQuestions = new ArrayList<>();
   @Nullable
   private String lastMessageId = null;
   @Nullable
   private String conversationId = null;
   @Nullable
   private String currentEventType = null;
   private boolean started = false;
   private boolean completed = false;
   private static final Set<String> THINKING_MESSAGE_TYPES = Set.of("thinking", "thought", "reasoning");

   public SseEventListener(@NotNull StreamListener listener) {
      this.listener = listener;
   }

   public void processLine(@Nullable String line) {
      if (line == null) {
         this.processEof();
      } else if (line.isBlank()) {
         if ("DONE".equals(this.currentEventType) && !this.completed) {
            this.completed = true;
            this.listener.onComplete(ResponseNormalizer.normalize(this.fullContent.toString()), this.lastMessageId, this.conversationId);
         }

         this.currentEventType = null;
      } else if (line.startsWith("event: ")) {
         this.currentEventType = line.substring("event: ".length()).trim();
      } else {
         if (line.startsWith("data: ") && this.currentEventType != null) {
            this.handleDataLine(line.substring("data: ".length()).trim());
         }
      }
   }

   public void processEof() {
      if (!this.completed) {
         this.completed = true;
         this.listener.onComplete(ResponseNormalizer.normalize(this.fullContent.toString()), this.lastMessageId, this.conversationId);
      }
   }

   @NotNull
   public List<String> getSuggestedQuestions() {
      return Collections.unmodifiableList(this.suggestedQuestions);
   }

   private void handleDataLine(@NotNull String data) {
      if (!data.isEmpty() && this.currentEventType != null) {
         switch (this.currentEventType) {
            case "data":
               this.handleTokenData(data);
               break;
            case "mes_info":
               this.handleMesInfoData(data);
               break;
            case "conv":
               this.handleConvData(data);
               break;
            case "hint":
               this.handleHintData(data);
               break;
            default:
               LOG.debug("Tubi Copilot: unhandled SSE event type '" + this.currentEventType + "': " + data);
         }
      }
   }

   private void handleTokenData(@NotNull String data) {
      try {
         JsonElement el = JsonParser.parseString(data);
         if (!el.isJsonObject()) {
            LOG.debug("Tubi Copilot: token event is not a JSON object: " + data);
            return;
         }

         JsonObject obj = el.getAsJsonObject();
         if (!obj.has("v") || obj.get("v").isJsonNull()) {
            return;
         }

         if (obj.has("m") && !obj.get("m").isJsonNull()) {
            String msgType = obj.get("m").getAsString();
            if (THINKING_MESSAGE_TYPES.contains(msgType)) {
               LOG.debug("Tubi Copilot: skipping thinking token (m=" + msgType + ")");
               return;
            }

            LOG.debug("Tubi Copilot: token m=" + msgType);
         }

         JsonElement vEl = obj.get("v");
         if (!vEl.isJsonPrimitive()) {
            return;
         }

         String v = vEl.getAsString();
         if (v.isEmpty()) {
            return;
         }

         if (!this.started) {
            this.started = true;
            this.listener.onStart();
         }

         if (!this.contentOverflow) {
            if (this.fullContent.length() + v.length() > 10000000) {
               this.contentOverflow = true;
               LOG.warn("Tubi Copilot: response exceeded 10000000 chars, stopped accumulating");
            } else {
               this.fullContent.append(v);
            }
         }

         this.listener.onToken(v);
      } catch (JsonSyntaxException e) {
         LOG.warn("Tubi Copilot: failed to parse token event: " + data, e);
      }
   }

   private void handleMesInfoData(@NotNull String data) {
      try {
         JsonElement el = JsonParser.parseString(data);
         if (!el.isJsonObject()) {
            return;
         }

         JsonObject obj = el.getAsJsonObject();
         if (obj.has("id") && obj.get("id").isJsonPrimitive()) {
            this.lastMessageId = obj.get("id").getAsString();
            LOG.debug("Tubi Copilot: lastMessageId = " + this.lastMessageId);
         }
      } catch (JsonSyntaxException e) {
         LOG.warn("Tubi Copilot: failed to parse mes_info event: " + data, e);
      }
   }

   private void handleConvData(@NotNull String data) {
      try {
         JsonElement el = JsonParser.parseString(data);
         if (!el.isJsonObject()) {
            return;
         }

         JsonObject obj = el.getAsJsonObject();
         if (obj.has("conversation_id") && obj.get("conversation_id").isJsonPrimitive()) {
            this.conversationId = obj.get("conversation_id").getAsString();
            LOG.debug("Tubi Copilot: conversationId = " + this.conversationId);
         }
      } catch (JsonSyntaxException e) {
         LOG.warn("Tubi Copilot: failed to parse conv event: " + data, e);
      }
   }

   private void handleHintData(@NotNull String data) {
      try {
         JsonElement el = JsonParser.parseString(data);
         if (!el.isJsonArray()) {
            return;
         }

         for (JsonElement item : el.getAsJsonArray()) {
            if (item.isJsonPrimitive()) {
               this.suggestedQuestions.add(item.getAsString());
            }
         }

         LOG.debug("Tubi Copilot: suggested questions: " + this.suggestedQuestions);
      } catch (JsonSyntaxException e) {
         LOG.warn("Tubi Copilot: failed to parse hint event: " + data, e);
      }
   }
}
