package com.tubitech.copilot.agent.tools;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolUtils {
   private ToolUtils() {
   }

   @Nullable
   public static VirtualFile refreshAndFind(@NotNull String absPath) {
      return LocalFileSystem.getInstance().refreshAndFindFileByPath(absPath);
   }

   public static int parseIntOr(@Nullable String value, int defaultValue) {
      if (value != null && !value.isBlank()) {
         try {
            return Integer.parseInt(value.trim());
         } catch (NumberFormatException e) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   @Nullable
   public static String resolveAndValidatePath(@NotNull String basePath, @NotNull String relativePath) {
      Path base = Path.of(basePath).normalize();
      Path resolved = base.resolve(relativePath.replace('\\', '/')).normalize();
      return !resolved.startsWith(base) ? null : resolved.toString().replace('\\', '/');
   }

   @NotNull
   public static String runGit(@NotNull String workDir, @NotNull List<String> args, int timeoutMs) throws Exception {
      List<String> command = new ArrayList<>();
      command.add("git");
      command.addAll(args);
      GeneralCommandLine cmdLine = new GeneralCommandLine(command);
      cmdLine.setWorkDirectory(workDir);
      cmdLine.setCharset(StandardCharsets.UTF_8);
      CapturingProcessHandler handler = new CapturingProcessHandler(cmdLine);

      ProcessOutput output;
      try {
         output = handler.runProcess(timeoutMs);
      } catch (Exception e) {
         handler.destroyProcess();
         throw e;
      }

      if (output.isTimeout()) {
         handler.destroyProcess();
      }

      if (output.getExitCode() != 0) {
         String stderr = output.getStderr().trim();
         return "Error (exit " + output.getExitCode() + "): " + (stderr.isEmpty() ? "unknown" : stderr);
      } else {
         return output.getStdout();
      }
   }

   public static int lineNumberAt(@NotNull String text, int offset) {
      int line = 1;
      int end = Math.min(offset, text.length());

      for (int i = 0; i < end; i++) {
         if (text.charAt(i) == '\n') {
            line++;
         }
      }

      return line;
   }
}
