package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public final class SystemPromptLoader {
   private static final Logger LOG = Logger.getInstance(SystemPromptLoader.class);
   private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();

   private SystemPromptLoader() {
   }

   @NotNull
   public static String load(@NotNull String resourcePath) {
      return CACHE.computeIfAbsent(resourcePath, SystemPromptLoader::doLoad);
   }

   @NotNull
   private static String doLoad(@NotNull String resourcePath) {
      String var7;
      try (InputStream is = SystemPromptLoader.class.getResourceAsStream(resourcePath)) {
         if (is == null) {
            LOG.warn("SystemPromptLoader: resource not found: " + resourcePath);
            return "";
         }

         var7 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
         LOG.warn("SystemPromptLoader: failed to read " + resourcePath, e);
         return "";
      }

      return var7;
   }
}
