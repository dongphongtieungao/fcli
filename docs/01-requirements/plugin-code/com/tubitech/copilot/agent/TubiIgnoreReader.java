package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class TubiIgnoreReader {
   private static final Logger LOG = Logger.getInstance(TubiIgnoreReader.class);
   public static final String TUBI_DIR = ".tubi";
   public static final String TUBIIGNORE_FILE = ".tubiignore";

   private TubiIgnoreReader() {
   }

   @NotNull
   public static Set<String> readIgnorePatterns(@NotNull Project project) {
      String base = project.getBasePath();
      if (base == null) {
         return Collections.emptySet();
      }

      Path ignorePath = Paths.get(base, ".tubi", ".tubiignore");
      if (!Files.exists(ignorePath)) {
         return Collections.emptySet();
      }

      Set var9;
      try {
         List<String> lines = Files.readAllLines(ignorePath, StandardCharsets.UTF_8);
         Set<String> patterns = new HashSet<>();

         for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
               patterns.add(trimmed);
            }
         }

         LOG.debug("TubiIgnoreReader: loaded " + patterns.size() + " patterns from " + ignorePath);
         var9 = Collections.unmodifiableSet(patterns);
      } catch (IOException e) {
         LOG.warn("TubiIgnoreReader: cannot read " + ignorePath + " — ignoring", e);
         return Collections.emptySet();
      }

      return var9;
   }

   public static boolean isIgnored(@NotNull String relativePath, @NotNull Set<String> patterns) {
      if (patterns.isEmpty()) {
         return false;
      }

      for (String pattern : patterns) {
         if (pattern.equals(relativePath)) {
            return true;
         }

         if (pattern.contains("*") || pattern.contains("?")) {
            try {
               PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
               if (matcher.matches(Paths.get(relativePath))) {
                  return true;
               }
            } catch (Exception var5) {
            }
         } else if (pattern.endsWith("/")) {
            String dir = pattern.substring(0, pattern.length() - 1);
            if (relativePath.startsWith(dir + "/") || relativePath.equals(dir)) {
               return true;
            }
         }
      }

      return false;
   }
}
