package com.tubitech.copilot.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.agent.AgentNameResolver;
import com.tubitech.copilot.api.ConversationApiClient;
import com.tubitech.copilot.api.model.ConversationSummary;
import com.tubitech.copilot.settings.ChatMode;
import java.io.IOException;
import java.util.Locale;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;

public final class ConversationSwitchService {
   private static final Logger LOG = Logger.getInstance(ConversationSwitchService.class);
   private static final int SEARCH_LIMIT = 100;

   private ConversationSwitchService() {
   }

   public static void switchToModeConversation(
      @NotNull Project project,
      @NotNull ChatPanel chatPanel,
      @NotNull ChatInputPanel chatInputPanel,
      @NotNull ConversationApiClient conversationApiClient,
      @NotNull ChatMode mode
   ) {
      String modeName = AgentNameResolver.nameFor(project, mode);
      String foundId = null;

      try {
         for (ConversationSummary summary : conversationApiClient.listConversations(0, 100)) {
            String summaryName = summary.getConversationName();
            if (summaryName != null) {
               String lowerSummary = summaryName.toLowerCase(Locale.ROOT);
               String lowerPrefix = (modeName + "-").toLowerCase(Locale.ROOT);
               if (lowerSummary.startsWith(lowerPrefix) || modeName.equalsIgnoreCase(summaryName)) {
                  foundId = summary.getId();
                  break;
               }
            }
         }
      } catch (IOException e) {
         LOG.warn("ConversationSwitchService: failed to list conversations for mode " + mode, e);
      }

      String matchedId = foundId;
      SwingUtilities.invokeLater(() -> {
         chatPanel.clearMessages();
         if (matchedId != null) {
            chatPanel.loadConversation(matchedId);
         } else {
            chatPanel.startNewSession();
            chatInputPanel.setPendingConversationName(modeName);
         }
      });
   }
}
