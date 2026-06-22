package com.tubitech.copilot.editor.bundle;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContextBundleWriter {
   private ContextBundleWriter() {
   }

   public static File writeTempBundle(String bundleContent) throws IOException {
      return writeTempBundle(bundleContent, null);
   }

   public static File writeTempBundle(String bundleContent, String label) throws IOException {
      String safeName;
      if (label != null && !label.isBlank()) {
         safeName = URLEncoder.encode(label, StandardCharsets.UTF_8);
      } else {
         safeName = "context";
      }

      String fileName = "tubi-" + safeName + ".md";
      Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
      Path tempFile = tempDir.resolve(fileName);
      Files.writeString(tempFile, bundleContent, StandardCharsets.UTF_8);
      return tempFile.toFile();
   }
}
