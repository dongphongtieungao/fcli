package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.editor.bundle.FileContentReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class AgentPathReader {
   private static final Logger LOG = Logger.getInstance(AgentPathReader.class);

   private AgentPathReader() {
   }

   @NotNull
   public static Path resolveProjectPath(@NotNull String rawPath, @NotNull Project project) {
      Path p = Paths.get(rawPath);
      if (p.isAbsolute()) {
         return p;
      }

      String base = project.getBasePath();
      return base != null ? Paths.get(base).resolve(rawPath) : p;
   }

   @NotNull
   public static String readPath(@NotNull Path path) {
      if (Files.isRegularFile(path)) {
         return readFile(path);
      } else {
         return Files.isDirectory(path) ? readDirectory(path, null) : "";
      }
   }

   @NotNull
   public static String readDirectoryFiltered(@NotNull Path path, @NotNull String extensionFilter) {
      if (Files.isRegularFile(path)) {
         return readFile(path);
      } else {
         return Files.isDirectory(path) ? readDirectory(path, extensionFilter) : "";
      }
   }

   @NotNull
   static String readFile(@NotNull Path path) {
      String name = path.getFileName() != null ? path.getFileName().toString() : "";
      int dot = name.lastIndexOf(46);
      String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";

      String var10000;
      try {
         String content = FileContentReader.read(path.toFile(), ext);
         var10000 = content == null ? "" : content;
      } catch (IOException e) {
         LOG.warn("AgentPathReader: could not read " + path + " — " + e.getMessage());
         return "";
      }

      return var10000;
   }

   @NotNull
   private static String readDirectory(@NotNull Path dir, String extensionFilter) {
      StringBuilder sb = new StringBuilder();

      try (Stream<Path> stream = Files.walk(dir).sorted(Comparator.naturalOrder())) {
         stream.filter(x$0 -> Files.isRegularFile(x$0))
            .filter(f -> extensionFilter == null || f.getFileName().toString().toLowerCase().endsWith(extensionFilter.toLowerCase()))
            .forEach(file -> {
               String content = readFile(file);
               if (!content.isBlank()) {
                  sb.append("--- ").append(dir.relativize(file)).append(" ---\n");
                  sb.append(content).append("\n\n");
               }
            });
      } catch (IOException e) {
         LOG.warn("AgentPathReader: error walking directory " + dir, e);
      }

      return sb.toString();
   }
}
