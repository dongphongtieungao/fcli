package com.tubitech.copilot.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class DeviceCodeGenerator {
   private static volatile String cachedCode;

   private DeviceCodeGenerator() {
   }

   @NotNull
   public static String getDeviceCode() {
      if (cachedCode != null) {
         return cachedCode;
      }

      String var10000;
      synchronized (DeviceCodeGenerator.class) {
         if (cachedCode != null) {
            return cachedCode;
         }

         cachedCode = generate();
         var10000 = cachedCode;
      }

      return var10000;
   }

   @NotNull
   private static String generate() {
      StringBuilder raw = new StringBuilder();
      raw.append(safeGetProperty("os.name"));
      raw.append("|");
      raw.append(safeGetProperty("os.arch"));
      raw.append("|");
      raw.append(safeGetProperty("user.name"));
      raw.append("|");
      raw.append(Runtime.getRuntime().availableProcessors());
      raw.append("|");
      String osName = System.getProperty("os.name", "").toLowerCase();
      if (osName.contains("win")) {
         raw.append(runCommand("wmic", "csproduct", "get", "UUID"));
         raw.append("|");
         raw.append(runCommand("wmic", "bios", "get", "serialnumber"));
      } else if (osName.contains("mac")) {
         raw.append(runCommand("ioreg", "-rd1", "-c", "IOPlatformExpertDevice"));
      } else {
         raw.append(readFileContent("/etc/machine-id"));
         if (raw.toString().endsWith("|")) {
            raw.append(readFileContent("/sys/class/dmi/id/product_uuid"));
         }
      }

      String var10000;
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         byte[] hash = md.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
         StringBuilder hex = new StringBuilder();

         for (int i = 0; i < 8; i++) {
            hex.append(String.format("%02X", hash[i]));
         }

         var10000 = hex.substring(0, 4) + "-" + hex.substring(4, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16);
      } catch (Exception e) {
         return "0000-0000-0000-0000";
      }

      return var10000;
   }

   @NotNull
   private static String safeGetProperty(@NotNull String key) {
      String var10000;
      try {
         var10000 = System.getProperty(key, "");
      } catch (SecurityException e) {
         return "";
      }

      return var10000;
   }

   @NotNull
   private static String runCommand(@NotNull String... command) {
      String trimmed;
      try {
         Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               trimmed = line.trim();
               if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("UUID") && !trimmed.equalsIgnoreCase("SerialNumber")) {
                  sb.append(trimmed);
               }
            }

            if (!process.waitFor(5L, TimeUnit.SECONDS)) {
               process.destroyForcibly();
            }

            trimmed = sb.toString().trim();
         }
      } catch (Exception e) {
         return "";
      }

      return trimmed;
   }

   @NotNull
   private static String readFileContent(@NotNull String path) {
      String var3;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
         String line = reader.readLine();
         var3 = line != null ? line.trim() : "";
      } catch (Exception e) {
         return "";
      }

      return var3;
   }
}
