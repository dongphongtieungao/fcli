package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;

public final class LocalSetupService {
   private static final Logger LOG = Logger.getInstance(LocalSetupService.class);
   private static final String TUBI_DIR = ".tubi";
   private static final String TUBIIGNORE_FILE = ".tubiignore";
   private static final String TUBIIGNORE_HEADER = "# Tubi Copilot ignore file\n# List files/folders to exclude from AI_CONTEXT_BUNDLE\n# One pattern per line. # lines are comments.\n";

   private LocalSetupService() {
   }

   public static void ensureLocalFiles(@NotNull Project project) {
      ensureDirectories(project);
      migrateInstructionsFile(project);
      ensureTubiIgnore(project);
   }

   private static void ensureDirectories(@NotNull Project project) {
      String base = project.getBasePath();
      if (base != null) {
         try {
            Files.createDirectories(Paths.get(base, ".tubi", "data"));
         } catch (IOException e) {
            LOG.warn("LocalSetupService: could not create .tubi/data/", e);
         }

         InstructionsFileManager.ensureDir(project);
      }
   }

   private static void migrateInstructionsFile(@NotNull Project project) {
      String agentName = AgentNameResolver.copilotAgentName(project);
      InstructionsFileManager.migrateIfNeeded(project, agentName);
   }

   public static void ensureTubiIgnore(@NotNull Project project) {
      String base = project.getBasePath();
      if (base != null) {
         Path tubiDir = Paths.get(base, ".tubi");
         Path ignoreFile = tubiDir.resolve(".tubiignore");
         if (!Files.exists(ignoreFile)) {
            try {
               Files.createDirectories(tubiDir);
               Files.writeString(
                  ignoreFile,
                  "# Tubi Copilot ignore file\n# List files/folders to exclude from AI_CONTEXT_BUNDLE\n# One pattern per line. # lines are comments.\n",
                  StandardCharsets.UTF_8
               );
               LOG.debug("LocalSetupService: created " + ignoreFile);
            } catch (IOException ex) {
               LOG.warn("LocalSetupService: could not create .tubiignore", ex);
            }
         }
      }
   }
}
