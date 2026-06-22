package com.tubitech.copilot.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.settings.ChatMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.jetbrains.annotations.NotNull;

public final class AgentNameResolver {
   public static final String COPILOT_AGENT_PREFIX = "Tubi-Copilot-";
   @Deprecated
   public static final String ASK_AGENT_PREFIX = "Ask-Agent-";
   @Deprecated
   public static final String PLAN_AGENT_PREFIX = "Plan-Agent-";
   @Deprecated
   public static final String EXECUTE_AGENT_PREFIX = "Execute-Agent-";
   private static final Logger LOG = Logger.getInstance(AgentNameResolver.class);

   private AgentNameResolver() {
   }

   @NotNull
   public static String projectHash(@NotNull Project project) {
      String basePath = project.getBasePath();
      return md5Hex8(basePath != null ? basePath : "");
   }

   @NotNull
   public static String copilotAgentName(@NotNull Project project) {
      return "Tubi-Copilot-" + projectHash(project);
   }

   @Deprecated
   @NotNull
   public static String askAgentName(@NotNull Project project) {
      return "Ask-Agent-" + projectHash(project);
   }

   @Deprecated
   @NotNull
   public static String planAgentName(@NotNull Project project) {
      return "Plan-Agent-" + projectHash(project);
   }

   @Deprecated
   @NotNull
   public static String executeAgentName(@NotNull Project project) {
      return "Execute-Agent-" + projectHash(project);
   }

   @Deprecated
   @NotNull
   public static String nameFor(@NotNull Project project, @NotNull ChatMode mode) {
      return copilotAgentName(project);
   }

   @NotNull
   private static String md5Hex8(@NotNull String input) {
      String var10000;
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
         StringBuilder sb = new StringBuilder(32);

         for (byte b : digest) {
            sb.append(String.format("%02x", b));
         }

         var10000 = sb.substring(0, 8);
      } catch (NoSuchAlgorithmException e) {
         LOG.warn("MD5 not available — falling back to empty hash", e);
         return "00000000";
      }

      return var10000;
   }
}
