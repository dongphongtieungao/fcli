package com.tubitech.copilot.agent.tools.impl;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.tools.ToolDefinition;
import com.tubitech.copilot.agent.tools.ToolExecutor;
import com.tubitech.copilot.agent.tools.ToolParameter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class RunCommandTool implements ToolExecutor {
   private static final int TIMEOUT_MS = 30000;
   private static final int MAX_OUTPUT_CHARS = 100000;
   private static final Set<String> BLOCKED_COMMANDS = Set.of(
      "rm -rf /",
      "rm -rf /*",
      "rmdir /s /q c:\\",
      "format c:",
      "format d:",
      "mkfs",
      "dd if=",
      ":(){ :|:& };:",
      "shutdown",
      "reboot",
      "del /f /s /q c:\\",
      "rd /s /q c:\\"
   );
   private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
      "\\brm\\s+-r[f]?\\s+/(?!\\S)|\\bformat\\s+[a-z]:|\\bmkfs\\b|\\bdd\\s+if=|\\bshutdown\\b|\\breboot\\b|\\bdel\\s+/[fq].*[a-z]:\\\\|\\brd\\s+/s\\s+/q\\s+[a-z]:\\\\",
      2
   );

   @NotNull
   @Override
   public String toolName() {
      return "run_command";
   }

   @NotNull
   @Override
   public ToolDefinition definition() {
      return new ToolDefinition(
         "run_command",
         "Run a shell command in the project directory. Use for builds, tests, or other CLI operations. Timeout: 30s.",
         List.of(new ToolParameter("command", "string", "The shell command to execute", true))
      );
   }

   @NotNull
   @Override
   public String execute(@NotNull Map<String, String> arguments, @NotNull Project project) throws Exception {
      String command = arguments.get("command");
      if (command != null && !command.isBlank()) {
         String cmdLower = command.toLowerCase().trim();

         for (String blocked : BLOCKED_COMMANDS) {
            if (cmdLower.contains(blocked)) {
               String var10000 = "Error: command blocked for safety — contains dangerous operation: " + blocked;
               if ("Error: command blocked for safety — contains dangerous operation: " + blocked == null) {
                  $$$reportNull$$$0(2);
               }

               return var10000;
            }
         }

         if (DANGEROUS_PATTERN.matcher(command).find()) {
            return "Error: command blocked for safety — matches a dangerous pattern.";
         }

         String basePath = project.getBasePath();
         if (basePath == null) {
            return "Error: project base path is not available.";
         }

         boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
         GeneralCommandLine cmdLine;
         if (isWindows) {
            cmdLine = new GeneralCommandLine(new String[]{"cmd.exe", "/c", command});
         } else {
            cmdLine = new GeneralCommandLine(new String[]{"/bin/sh", "-c", command});
         }

         cmdLine.setWorkDirectory(basePath);
         cmdLine.setCharset(StandardCharsets.UTF_8);
         CapturingProcessHandler handler = new CapturingProcessHandler(cmdLine);

         ProcessOutput output;
         try {
            output = handler.runProcess(30000);
         } catch (Exception e) {
            handler.destroyProcess();
            throw e;
         }

         if (output.isTimeout()) {
            handler.destroyProcess();
         }

         StringBuilder sb = new StringBuilder();
         if (output.isTimeout()) {
            sb.append("[TIMEOUT after ").append(30).append("s — process killed]\n");
         }

         sb.append("Exit code: ").append(output.getExitCode()).append("\n");
         String stdout = output.getStdout();
         if (!stdout.isBlank()) {
            sb.append("--- stdout ---\n");
            sb.append(stdout.length() > 100000 ? stdout.substring(0, 100000) + "\n[truncated at 100000 chars]" : stdout);
         }

         String stderr = output.getStderr();
         if (!stderr.isBlank()) {
            sb.append("--- stderr ---\n");
            sb.append(stderr.length() > 100000 ? stderr.substring(0, 100000) + "\n[truncated at 100000 chars]" : stderr);
         }

         return sb.toString();
      } else {
         return "Error: 'command' argument is required.";
      }
   }
}
