package com.tubitech.copilot.agent.loop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.agent.tools.ToolCall;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolCallParser {
   private static final Logger LOG = Logger.getInstance(ToolCallParser.class);
   private static final String OPEN_CALL = "<tool_call>";
   private static final String CLOSE_CALL = "</tool_call>";
   private static final Pattern FENCED_TOOL_CALL = Pattern.compile("```(?:xml|json)?\\s*<tool_call>(.*?)</tool_call>\\s*```", 32);
   private static final Pattern STRIP_FENCED = Pattern.compile("```(?:xml|json)?\\s*<tool_call>.*?</tool_call>\\s*```", 32);
   private static final Gson GSON = new Gson();
   private static final int MAX_TOOL_CALLS_PER_RESPONSE = 50;

   private ToolCallParser() {
   }

   @NotNull
   public static List<ToolCall> parse(@NotNull String response) {
      List<ToolCall> calls = new ArrayList<>();
      extractFromTags(response, "<tool_call>", "</tool_call>", calls);
      if (calls.isEmpty()) {
         Matcher matcher = FENCED_TOOL_CALL.matcher(response);

         while (matcher.find()) {
            String inner = matcher.group(1).trim();
            String json = extractJsonByBraceCounting(inner);
            if (json != null && json.contains("{")) {
               parseJson(json, calls);
            }
         }
      }

      return calls;
   }

   private static void extractFromTags(@NotNull String response, @NotNull String openTag, @NotNull String closeTag, @NotNull List<ToolCall> calls) {
      int searchFrom = 0;
      int iterations = 0;

      while (iterations++ < 50) {
         int openIdx = response.indexOf(openTag, searchFrom);
         if (openIdx < 0) {
            break;
         }

         int contentStart = openIdx + openTag.length();
         int closeIdx = response.indexOf(closeTag, contentStart);
         if (closeIdx < 0) {
            break;
         }

         String inner = response.substring(contentStart, closeIdx).trim();
         int callsBefore = calls.size();
         String json = extractJsonByBraceCounting(inner);
         if (json != null) {
            parseJson(json, calls);
         }

         boolean onlyMalformed = calls.size() > callsBefore
            && calls.subList(callsBefore, calls.size()).stream().allMatch(c -> "_malformed_tool_call".equals(c.getName()));
         if (onlyMalformed) {
            calls.subList(callsBefore, calls.size()).clear();
         }

         if (calls.size() == callsBefore) {
            LOG.info("ToolCallParser: brace-counted parse failed, trying boundary fallback");
            parseFallback(inner, calls);
         }

         searchFrom = closeIdx + closeTag.length();
      }
   }

   @Nullable
   private static String extractJsonByBraceCounting(@NotNull String text) {
      int start = text.indexOf(123);
      if (start < 0) {
         return null;
      }

      int depth = 0;
      boolean inString = false;
      boolean escaped = false;

      for (int i = start; i < text.length(); i++) {
         char c = text.charAt(i);
         if (escaped) {
            escaped = false;
         } else if (c == '\\' && inString) {
            escaped = true;
         } else if (c == '"') {
            inString = !inString;
         } else if (!inString) {
            if (c == '{') {
               depth++;
            } else if (c == '}') {
               if (--depth == 0) {
                  return text.substring(start, i + 1);
               }
            }
         }
      }

      if (depth > 0 && depth <= 3 && !inString) {
         LOG.info("ToolCallParser: auto-closing " + depth + " missing brace(s)");
         return text.substring(start) + "}".repeat(depth);
      } else {
         return null;
      }
   }

   private static void parseJson(@NotNull String json, @NotNull List<ToolCall> calls) {
      JsonObject obj = tryParseJson(json);
      if (obj == null) {
         String sanitized = sanitizeJsonStrings(json);
         obj = tryParseJson(sanitized);
      }

      if (obj == null) {
         LOG.warn("ToolCallParser: unparseable JSON, raw (" + json.length() + " chars): " + (json.length() > 300 ? json.substring(0, 300) + "..." : json));
         calls.add(new ToolCall("malformed_" + calls.size(), "_malformed_tool_call", Map.of()));
      } else {
         String id = getStringOrDefault(obj, "id", "auto_" + calls.size());
         String name = getStringOrDefault(obj, "name", getStringOrDefault(obj, "toolname", ""));
         if (!name.isBlank()) {
            Map<String, String> args = new LinkedHashMap<>();
            String argsKey = obj.has("arguments") ? "arguments" : "params";
            if (obj.has(argsKey) && obj.get(argsKey).isJsonObject()) {
               for (Entry<String, JsonElement> entry : obj.getAsJsonObject(argsKey).entrySet()) {
                  JsonElement val = entry.getValue();
                  args.put(entry.getKey(), val.isJsonPrimitive() ? val.getAsString() : val.toString());
               }
            }

            calls.add(new ToolCall(id, name, args));
         }
      }
   }

   @Nullable
   private static JsonObject tryParseJson(@NotNull String json) {
      try {
         return (JsonObject)GSON.fromJson(json, JsonObject.class);
      } catch (JsonSyntaxException e) {
         return null;
      }
   }

   @NotNull
   private static String sanitizeJsonStrings(@NotNull String json) {
      StringBuilder sb = new StringBuilder(json.length());
      boolean inString = false;
      boolean escaped = false;

      for (int i = 0; i < json.length(); i++) {
         char c = json.charAt(i);
         if (escaped) {
            escaped = false;
            sb.append(c);
         } else if (c == '\\' && inString) {
            escaped = true;
            sb.append(c);
         } else if (c == '"') {
            inString = !inString;
            sb.append(c);
         } else {
            if (inString) {
               if (c == '\n') {
                  sb.append("\\n");
                  continue;
               }

               if (c == '\r') {
                  sb.append("\\r");
                  continue;
               }

               if (c == '\t') {
                  sb.append("\\t");
                  continue;
               }
            }

            sb.append(c);
         }
      }

      return sb.toString();
   }

   @NotNull
   public static String stripToolCalls(@NotNull String response) {
      String s = STRIP_FENCED.matcher(response).replaceAll("");
      s = stripTagBlocks(s, "<tool_call>", "</tool_call>");
      s = stripTagBlocks(s, "<tool_result>", "</tool_result>");
      s = s.replaceAll("</?tool_(?:call|result)>", "");
      return s.replaceAll("\n{3,}", "\n\n").trim();
   }

   private static String stripTagBlocks(@NotNull String text, @NotNull String openTag, @NotNull String closeTag) {
      StringBuilder result = new StringBuilder();
      int searchFrom = 0;

      while (true) {
         int openIdx = text.indexOf(openTag, searchFrom);
         if (openIdx < 0) {
            result.append(text, searchFrom, text.length());
            break;
         }

         result.append(text, searchFrom, openIdx);
         int closeIdx = text.indexOf(closeTag, openIdx + openTag.length());
         if (closeIdx < 0) {
            result.append(text, openIdx, text.length());
            break;
         }

         searchFrom = closeIdx + closeTag.length();
      }

      return result.toString();
   }

   private static void parseFallback(@NotNull String inner, @NotNull List<ToolCall> calls) {
      Matcher nameMatcher = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(inner);
      if (nameMatcher.find()) {
         String name = nameMatcher.group(1);
         Matcher idMatcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(inner);
         String id = idMatcher.find() ? idMatcher.group(1) : "auto_fallback";
         int argsKeyIdx = inner.indexOf("\"arguments\"");
         if (argsKeyIdx < 0) {
            calls.add(new ToolCall(id, name, Map.of()));
         } else {
            int argsBraceStart = inner.indexOf(123, argsKeyIdx + 11);
            if (argsBraceStart < 0) {
               calls.add(new ToolCall(id, name, Map.of()));
            } else {
               int argsEnd = inner.lastIndexOf("}}");
               if (argsEnd < argsBraceStart) {
                  argsEnd = inner.lastIndexOf("}");
                  if (argsEnd <= argsBraceStart) {
                     calls.add(new ToolCall(id, name, Map.of()));
                     return;
                  }
               }

               String argsContent = inner.substring(argsBraceStart + 1, argsEnd);
               Map<String, String> args = extractArgsByBoundary(argsContent);
               LOG.info("ToolCallParser: fallback parsed tool '" + name + "' with " + args.size() + " argument(s)");
               calls.add(new ToolCall(id, name, args));
            }
         }
      }
   }

   @NotNull
   private static Map<String, String> extractArgsByBoundary(@NotNull String content) {
      Map<String, String> args = new LinkedHashMap<>();
      Pattern keyPattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"");
      Matcher keyMatcher = keyPattern.matcher(content);
      List<int[]> keyPositions = new ArrayList<>();

      while (keyMatcher.find()) {
         keyPositions.add(new int[]{keyMatcher.start(), keyMatcher.end(), keyMatcher.start(1), keyMatcher.end(1)});
      }

      for (int i = 0; i < keyPositions.size(); i++) {
         int[] pos = keyPositions.get(i);
         String key = content.substring(pos[2], pos[3]);
         int valueStart = pos[1];
         int valueEnd;
         if (i + 1 < keyPositions.size()) {
            valueEnd = keyPositions.get(i + 1)[0];

            while (
               valueEnd > valueStart
                  && (content.charAt(valueEnd - 1) == ',' || content.charAt(valueEnd - 1) == '"' || Character.isWhitespace(content.charAt(valueEnd - 1)))
            ) {
               valueEnd--;
            }
         } else {
            valueEnd = content.length();

            while (valueEnd > valueStart && (content.charAt(valueEnd - 1) == '"' || Character.isWhitespace(content.charAt(valueEnd - 1)))) {
               valueEnd--;
            }
         }

         if (valueEnd > valueStart) {
            args.put(key, content.substring(valueStart, valueEnd));
         }
      }

      return args;
   }

   private static String getStringOrDefault(@NotNull JsonObject obj, @NotNull String key, @NotNull String defaultValue) {
      return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : defaultValue;
   }
}
