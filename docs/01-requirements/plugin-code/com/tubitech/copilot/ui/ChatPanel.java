package com.tubitech.copilot.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.agent.AgentNameResolver;
import com.tubitech.copilot.agent.FileChangeProposal;
import com.tubitech.copilot.agent.LocalSetupService;
import com.tubitech.copilot.agent.loop.AgentLoopRunner;
import com.tubitech.copilot.agent.tools.ToolCall;
import com.tubitech.copilot.api.AgentApiClient;
import com.tubitech.copilot.api.AgentApiClientImpl;
import com.tubitech.copilot.api.ConversationApiClient;
import com.tubitech.copilot.api.ConversationApiClientImpl;
import com.tubitech.copilot.api.FileUploadApiClient;
import com.tubitech.copilot.api.FileUploadApiClientImpl;
import com.tubitech.copilot.api.ModelApiClientImpl;
import com.tubitech.copilot.api.model.ConversationMessage;
import com.tubitech.copilot.api.model.ConversationSummary;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.editor.EditorContextExtractor;
import com.tubitech.copilot.editor.FolderContextExtractor;
import com.tubitech.copilot.history.ConversationHistory;
import com.tubitech.copilot.history.ConversationSession;
import com.tubitech.copilot.history.HistoryMessage;
import com.tubitech.copilot.history.MessageRole;
import com.tubitech.copilot.history.ProjectHistoryService;
import com.tubitech.copilot.history.SessionSummary;
import com.tubitech.copilot.settings.ChatMode;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChatPanel extends JBPanel<ChatPanel> implements Disposable {
   private static final Logger LOG = Logger.getInstance(ChatPanel.class);
   private static final int AUTO_SCROLL_THRESHOLD_PX = 50;
   private static final int HISTORY_PAGE_SIZE = 20;
   private final Project project;
   private final ConversationApiClient conversationApiClient;
   private final FileUploadApiClient fileUploadApiClient;
   private final AgentApiClient agentApiClient;
   private final JBPanel<?> messagesPanel;
   private final JBScrollPane messageScrollPane;
   private final AttachmentPreviewPanel attachmentPreviewPanel;
   private final ChatInputPanel chatInputPanel;
   private final TypingIndicator typingIndicator;
   private final JButton newConversationButton = new JButton();
   private final JButton historyButton = new JButton();
   private final JButton clearHistoryButton = new JButton();
   private final AuthToolbarPanel authToolbarPanel;
   @Nullable
   private JBPopup currentHistoryPopup;
   @Nullable
   private String pendingCodeContext;
   private final JBPanel<?> southWrapper;
   private final JBPanel<?> aboveInputStack;
   @Nullable
   private AgentChangesPanel currentAgentPanel;
   private ModelSelectorPanel modelSelectorPanel;
   private HeaderBar headerBar;
   private boolean eolDialogShown = false;
   @Nullable
   private final AtomicReference<AgentLoopRunner> activeAgentLoopRunner = new AtomicReference<>();
   @Nullable
   private MessageBubble.Role lastAddedRole = null;
   @Nullable
   private volatile String currentConversationId = null;
   @Nullable
   private volatile String lastMessageId = null;
   @Nullable
   private List<ConversationMessage> pendingEarlierMessages = null;
   @Nullable
   private JButton loadEarlierButton = null;
   @Nullable
   private ToolApprovalPanel currentApprovalPanel;
   @Nullable
   private ContinuePromptPanel currentContinuePanel;
   @Nullable
   private PlanConfirmationCard currentPlanCard;

   public ChatPanel(@NotNull Project project) {
      super(new BorderLayout());
      this.project = project;
      TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
      this.conversationApiClient = new ConversationApiClientImpl(settings);
      this.fileUploadApiClient = new FileUploadApiClientImpl(settings);
      this.agentApiClient = new AgentApiClientImpl(settings);
      this.messagesPanel = new JBPanel();
      this.messagesPanel.setLayout(new BoxLayout(this.messagesPanel, 1));
      this.messagesPanel.setBackground(UIUtil.getPanelBackground());
      this.messagesPanel.setBorder(Borders.empty(8, 12, 8, 12));
      this.typingIndicator = new TypingIndicator();
      this.messagesPanel.add(this.typingIndicator);
      JBPanel<?> messagesWrapper = new JBPanel(new BorderLayout());
      messagesWrapper.setOpaque(false);
      messagesWrapper.setBackground(UIUtil.getPanelBackground());
      messagesWrapper.add(this.messagesPanel, "North");
      this.messageScrollPane = new JBScrollPane(messagesWrapper);
      this.messageScrollPane.setVerticalScrollBarPolicy(20);
      this.messageScrollPane.setHorizontalScrollBarPolicy(31);
      this.messageScrollPane.setBorder(null);
      this.messageScrollPane.getViewport().setBackground(UIUtil.getPanelBackground());
      this.attachmentPreviewPanel = new AttachmentPreviewPanel();
      this.attachmentPreviewPanel.setVisible(false);
      this.chatInputPanel = new ChatInputPanel(project, this);
      this.authToolbarPanel = new AuthToolbarPanel(msg -> this.chatInputPanel.showNotice(InlineNoticeBar.NoticeType.SYSTEM, msg));
      this.modelSelectorPanel = new ModelSelectorPanel(
         project, new ModelApiClientImpl((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class), settings), this, this.chatInputPanel
      );
      JBPanel<?> unifiedInputPanel = new JBPanel(new BorderLayout());
      unifiedInputPanel.setOpaque(false);
      unifiedInputPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ChatStyles.INPUT_BORDER, 1, true), Borders.empty(6, 4, 6, 8)));
      unifiedInputPanel.add(this.chatInputPanel, "Center");
      unifiedInputPanel.add(this.modelSelectorPanel, "South");
      JBPanel<?> inputWrapper = new JBPanel(new BorderLayout());
      inputWrapper.setOpaque(false);
      inputWrapper.setBorder(Borders.empty(6, 8, 8, 8));
      inputWrapper.add(unifiedInputPanel, "Center");
      JBPanel<?> inputSection = new JBPanel(new BorderLayout());
      inputSection.add(this.attachmentPreviewPanel, "North");
      inputSection.add(inputWrapper, "Center");
      inputSection.setBorder(Borders.customLine(JBColor.border(), 1, 0, 0, 0));
      this.aboveInputStack = new JBPanel();
      this.aboveInputStack.setLayout(new BoxLayout(this.aboveInputStack, 1));
      this.aboveInputStack.setOpaque(false);
      this.southWrapper = new JBPanel(new BorderLayout());
      this.southWrapper.add(this.aboveInputStack, "North");
      this.southWrapper.add(inputSection, "Center");
      this.headerBar = new HeaderBar(this.newConversationButton, this.historyButton, this.clearHistoryButton, this.authToolbarPanel);
      this.wireToolbarButtons();
      this.add(this.headerBar, "North");
      this.add(this.messageScrollPane, "Center");
      this.add(this.southWrapper, "South");
      ApplicationManager.getApplication().executeOnPooledThread(this::restoreOnStartup);
      if (EolEnforcementService.isExpired()) {
         this.disableForEol();
         if (!this.eolDialogShown) {
            this.eolDialogShown = true;
            EolEnforcementService.showExpiryDialog(project);
         }
      }
   }

   public void dispose() {
      AgentLoopRunner runner = this.activeAgentLoopRunner.getAndSet(null);
      if (runner != null) {
         runner.cancel();
      }

      this.conversationApiClient.cancel();
      this.authToolbarPanel.dispose();
      LOG.debug("Tubi Copilot: ChatPanel disposed — in-flight API call cancelled.");
   }

   private void disableForEol() {
      this.chatInputPanel.setEnabled(false);
      this.chatInputPanel.getInputTextArea().setEnabled(false);
      this.modelSelectorPanel.getSendButton().setEnabled(false);
      this.modelSelectorPanel.setEnabled(false);
      this.newConversationButton.setEnabled(false);
      this.historyButton.setEnabled(false);
      this.clearHistoryButton.setEnabled(false);
   }

   public void onStreamingStateChanged(boolean streaming) {
      this.modelSelectorPanel.setSendStreaming(streaming);
   }

   public void switchMode(@NotNull ChatMode mode) {
      this.modelSelectorPanel.switchMode(mode);
   }

   public void showSyncResult(@NotNull InlineNoticeBar.NoticeType type, @NotNull String message) {
      this.chatInputPanel.showNotice(type, message);
   }

   private void restoreOnStartup() {
      LocalSetupService.ensureLocalFiles(this.project);
      ProjectHistoryService historyService = ProjectHistoryService.getInstance(this.project);
      SessionSummary recent = historyService.getMostRecentSession();
      if (recent != null && recent.getMessageCount() > 0) {
         SwingUtilities.invokeLater(() -> this.loadConversation(recent.getId()));
      } else {
         SwingUtilities.invokeLater(this::startNewSession);
      }
   }

   public void addUserBubble(@NotNull String text, @NotNull List<String> attachmentNames) {
      MessageBubble bubble = new MessageBubble(MessageBubble.Role.USER, text);
      if (!attachmentNames.isEmpty()) {
         bubble.setAttachmentNames(attachmentNames);
      }

      this.addGapIfNeeded(MessageBubble.Role.USER);
      this.messagesPanel.add(bubble, Math.max(0, this.messagesPanel.getComponentCount() - 1));
      this.lastAddedRole = MessageBubble.Role.USER;
      this.messagesPanel.revalidate();
      this.scrollToBottomLater();
   }

   @NotNull
   public MessageBubble addAssistantBubble() {
      MessageBubble bubble = new MessageBubble(MessageBubble.Role.ASSISTANT, "");
      this.addGapIfNeeded(MessageBubble.Role.ASSISTANT);
      this.messagesPanel.add(bubble, Math.max(0, this.messagesPanel.getComponentCount() - 1));
      this.lastAddedRole = MessageBubble.Role.ASSISTANT;
      this.messagesPanel.revalidate();
      this.scrollToBottomLater();
      return bubble;
   }

   public void showTypingIndicator() {
      assert SwingUtilities.isEventDispatchThread() : "showTypingIndicator must be on EDT";
      this.typingIndicator.start();
      this.messagesPanel.revalidate();
      this.scrollToBottomLater();
   }

   public void hideTypingIndicator() {
      assert SwingUtilities.isEventDispatchThread() : "hideTypingIndicator must be on EDT";
      this.typingIndicator.stop();
   }

   public void setActiveAgentLoopRunner(@Nullable AgentLoopRunner runner) {
      this.activeAgentLoopRunner.set(runner);
   }

   @Nullable
   public AgentLoopRunner getActiveAgentLoopRunner() {
      return this.activeAgentLoopRunner.get();
   }

   public void updateTypingStatus(@NotNull String message) {
      SwingUtilities.invokeLater(() -> this.typingIndicator.setStatusText(message));
   }

   @NotNull
   public HeaderBar getHeaderBar() {
      return this.headerBar;
   }

   @NotNull
   public CompletableFuture<Boolean> showToolApproval(@NotNull ToolCall toolCall) {
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      SwingUtilities.invokeLater(() -> {
         this.dismissCurrentApproval();
         this.currentApprovalPanel = new ToolApprovalPanel(toolCall, this.project);
         this.currentApprovalPanel.setAlignmentX(0.0F);
         this.aboveInputStack.add(this.currentApprovalPanel);
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
         this.currentApprovalPanel.getDecision().thenAccept(approved -> {
            result.complete(approved);
            SwingUtilities.invokeLater(this::dismissCurrentApproval);
         });
      });
      return result;
   }

   private void dismissCurrentApproval() {
      if (this.currentApprovalPanel != null) {
         this.aboveInputStack.remove(this.currentApprovalPanel);
         this.currentApprovalPanel = null;
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
      }
   }

   @NotNull
   public CompletableFuture<Boolean> showContinuePrompt(int iterationsUsed) {
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      SwingUtilities.invokeLater(() -> {
         this.dismissCurrentContinuePrompt();
         this.currentContinuePanel = new ContinuePromptPanel(iterationsUsed);
         this.currentContinuePanel.setAlignmentX(0.0F);
         this.aboveInputStack.add(this.currentContinuePanel);
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
         this.scrollToBottomLater();
         this.currentContinuePanel.getDecision().thenAccept(shouldContinue -> {
            result.complete(shouldContinue);
            SwingUtilities.invokeLater(this::dismissCurrentContinuePrompt);
         });
      });
      return result;
   }

   private void dismissCurrentContinuePrompt() {
      if (this.currentContinuePanel != null) {
         this.aboveInputStack.remove(this.currentContinuePanel);
         this.currentContinuePanel = null;
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
      }
   }

   public void showPlanConfirmation(@NotNull Runnable onProceed, @NotNull Runnable onCancel) {
      SwingUtilities.invokeLater(() -> {
         this.dismissCurrentPlanConfirmation();
         this.currentPlanCard = new PlanConfirmationCard();
         this.currentPlanCard.setOnProceed(() -> {
            this.currentPlanCard.markExecuting();
            onProceed.run();
            this.dismissCurrentPlanConfirmation();
         });
         this.currentPlanCard.setOnCancel(() -> {
            this.currentPlanCard.markCancelled();
            onCancel.run();
            this.dismissCurrentPlanConfirmation();
         });
         this.currentPlanCard.setAlignmentX(0.0F);
         this.aboveInputStack.add(this.currentPlanCard);
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
         this.scrollToBottomLater();
      });
   }

   private void dismissCurrentPlanConfirmation() {
      if (this.currentPlanCard != null) {
         this.aboveInputStack.remove(this.currentPlanCard);
         this.currentPlanCard = null;
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
      }
   }

   public void clearMessages() {
      this.typingIndicator.stop();
      this.dismissCurrentApproval();
      this.dismissCurrentContinuePrompt();
      this.dismissCurrentPlanConfirmation();
      if (this.currentAgentPanel != null) {
         this.aboveInputStack.remove(this.currentAgentPanel);
         this.currentAgentPanel = null;
      }

      this.aboveInputStack.revalidate();
      this.aboveInputStack.repaint();
      this.messagesPanel.removeAll();
      this.messagesPanel.add(this.typingIndicator);
      this.lastAddedRole = null;
      this.pendingEarlierMessages = null;
      this.loadEarlierButton = null;
      this.messagesPanel.revalidate();
      this.messagesPanel.repaint();
   }

   public void setCodeContext(@NotNull String codeContext) {
      this.pendingCodeContext = codeContext;
   }

   @Nullable
   public String getCodeContext() {
      return this.pendingCodeContext;
   }

   public void setCodeContext(@NotNull EditorContextExtractor.CodeContext ctx) {
      this.pendingCodeContext = "[Code Context - " + ctx.filename() + " (" + ctx.language() + ")]\n```" + ctx.language() + "\n" + ctx.code() + "\n```";
      this.attachmentPreviewPanel.showCodeContextBadge(ctx.filename(), ctx.language());
      this.attachmentPreviewPanel.setVisible(true);
   }

   public void setFolderContext(@NotNull FolderContextExtractor.FolderContext ctx) {
      this.pendingCodeContext = ctx.formattedContext();
      this.attachmentPreviewPanel.showFolderContextBadge(ctx.folderName(), ctx.fileCount(), ctx.truncated());
      this.attachmentPreviewPanel.setVisible(true);
   }

   public void clearCodeContext() {
      this.pendingCodeContext = null;
   }

   public void saveCurrentSession() {
      ConversationSession session = this.chatInputPanel.getCurrentSession();
      if (!session.getMessages().isEmpty() && TubiCopilotSettings.getInstance().persistHistory) {
         ConversationHistory.getInstance().addSession(session);
         ProjectHistoryService historyService = ProjectHistoryService.getInstance(this.project);
         historyService.updateSessionTimestamp(session.id, session.updatedAt);
         if (session.title != null) {
            historyService.updateSessionTitle(session.id, session.title);
         }
      }
   }

   public void startNewSession() {
      this.conversationApiClient.cancel();
      this.chatInputPanel.startNewSession();
      this.currentConversationId = null;
      this.lastMessageId = null;
   }

   public void loadSession(@NotNull ConversationSession session) {
      assert SwingUtilities.isEventDispatchThread() : "loadSession must be on EDT";
      this.conversationApiClient.cancel();
      this.clearMessages();
      this.chatInputPanel.loadSession(session);
      this.currentConversationId = session.serverConversationId;
      this.lastMessageId = session.lastMessageId;

      for (ConversationSession.ChatMessage msg : session.getMessages()) {
         if (msg.content != null) {
            if (msg.role == ConversationSession.Role.USER) {
               List<String> names = msg.attachmentNames != null ? msg.attachmentNames : List.of();
               this.addUserBubble(msg.content, names);
            } else if (msg.role == ConversationSession.Role.ASSISTANT) {
               MessageBubble bubble = this.addAssistantBubble();
               bubble.finalizeWithMarkdown(msg.content, this.project);
            }
         }
      }
   }

   public void loadConversation(@NotNull String sessionId) {
      SwingUtilities.invokeLater(this::showLoadingIndicator);
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         ProjectHistoryService historyService = ProjectHistoryService.getInstance(this.project);
         List<HistoryMessage> allMessages = historyService.loadAllMessages(sessionId);
         String serverConvId = historyService.getServerConversationId(sessionId);
         String serverMsgId = historyService.getLastServerMessageId(sessionId);
         SwingUtilities.invokeLater(() -> {
            this.clearMessages();
            this.currentConversationId = serverConvId;
            this.lastMessageId = serverMsgId;
            ConversationSession session = new ConversationSession();
            session.id = sessionId;
            session.serverConversationId = serverConvId;
            session.lastMessageId = serverMsgId;
            session.createdAt = allMessages.isEmpty() ? System.currentTimeMillis() : allMessages.get(0).getCreatedAt();
            session.updatedAt = allMessages.isEmpty() ? session.createdAt : allMessages.get(allMessages.size() - 1).getCreatedAt();

            for (HistoryMessage msg : allMessages) {
               if (msg.getRole() == MessageRole.USER) {
                  session.addUserMessage(msg.getContent() != null ? msg.getContent() : "", null);
               } else if (msg.getRole() == MessageRole.ASSISTANT) {
                  session.addAssistantMessage(msg.getContent() != null ? msg.getContent() : "", msg.getServerMessageId());
               }
            }

            this.chatInputPanel.loadSession(session);
            int startIdx = Math.max(0, findVisibleStartIdx(allMessages, 20));
            List<Object> pendingItems = new ArrayList<>();

            for (int i = startIdx; i < allMessages.size(); i++) {
               HistoryMessage msg = allMessages.get(i);
               MessageRole role = msg.getRole();
               if (role == MessageRole.TOOL_CALL) {
                  pendingItems.add(parseToolCallInfo(msg.getContent()));
               } else if (role == MessageRole.SYSTEM && isIntermediateText(msg.getMetadata())) {
                  pendingItems.add(msg.getContent() != null ? msg.getContent() : "");
               } else if (role == MessageRole.USER) {
                  pendingItems.clear();
                  List<String> attachNames = parseAttachmentNames(msg.getMetadata());
                  this.addUserBubble(msg.getContent() != null ? msg.getContent() : "", attachNames);
               } else if (role == MessageRole.ASSISTANT) {
                  String content = msg.getContent() != null ? msg.getContent() : "";
                  if (!pendingItems.isEmpty()) {
                     this.addAssistantBubbleWithProgress(content, pendingItems);
                     pendingItems.clear();
                  } else {
                     this.addMessageBubble(MessageBubble.Role.ASSISTANT, content);
                  }
               }
            }

            pendingItems.clear();
            ChatMode mode = TubiCopilotSettings.getInstance().getSelectedChatMode();
            this.modelSelectorPanel.restoreConversationContext(mode, null);
            this.hideLoadingIndicator();
            this.scrollToBottomLater();
         });
      });
   }

   public void setCurrentConversationId(@Nullable String id) {
      this.currentConversationId = id;
   }

   @Nullable
   public String getCurrentConversationId() {
      return this.currentConversationId;
   }

   public void setLastMessageId(@Nullable String id) {
      this.lastMessageId = id;
   }

   @Nullable
   public String getLastMessageId() {
      return this.lastMessageId;
   }

   private void showLoadingIndicator() {
      this.showTypingIndicator();
   }

   private void hideLoadingIndicator() {
      this.hideTypingIndicator();
   }

   @NotNull
   private static List<String> parseAttachmentNames(@Nullable String metadata) {
      if (metadata != null && !metadata.isBlank()) {
         try {
            JsonObject obj = JsonParser.parseString(metadata).getAsJsonObject();
            if (obj.has("attachments")) {
               JsonArray arr = obj.getAsJsonArray("attachments");
               List<String> names = new ArrayList<>(arr.size());

               for (JsonElement el : arr) {
                  names.add(el.getAsString());
               }

               return names;
            }
         } catch (Exception var6) {
         }

         return Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   @NotNull
   private static ChatPanel.ToolCallInfo parseToolCallInfo(@Nullable String content) {
      if (content == null) {
         return new ChatPanel.ToolCallInfo("unknown", "unknown");
      }

      try {
         String json = content;
         int start = json.indexOf(123);
         int end = json.lastIndexOf(125);
         if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
         }

         JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
         String name = obj.has("name") ? obj.get("name").getAsString() : "unknown";
         String desc = name;
         if (obj.has("arguments") && obj.get("arguments").isJsonObject()) {
            JsonObject args = obj.getAsJsonObject("arguments");
            if (args.has("path")) {
               desc = desc + "(" + args.get("path").getAsString() + ")";
            } else if (args.has("command")) {
               desc = desc + "(" + args.get("command").getAsString() + ")";
            } else if (args.has("pattern")) {
               desc = desc + "(" + args.get("pattern").getAsString() + ")";
            }
         }

         return new ChatPanel.ToolCallInfo(name, desc);
      } catch (Exception e) {
         return new ChatPanel.ToolCallInfo("unknown", content.length() > 60 ? content.substring(0, 57) + "…" : content);
      }
   }

   private void addAssistantBubbleWithProgress(@NotNull String content, @NotNull List<Object> items) {
      MessageBubble bubble = this.addAssistantBubble();
      AgentProgressPanel panel = new AgentProgressPanel();

      for (Object item : items) {
         if (item instanceof ChatPanel.ToolCallInfo tc) {
            ToolCallRow row = panel.addToolCall(tc.name, tc.description);
            row.markCompleted(0L);
         } else if (item instanceof String) {
            panel.addIntermediateText((String)item);
         }
      }

      panel.markLoopFinished();
      bubble.embedAgentProgress(panel);
      if (!content.isBlank()) {
         bubble.finalizeWithMarkdown(content, this.project);
      }
   }

   private static boolean isIntermediateText(@Nullable String metadata) {
      if (metadata == null) {
         return false;
      }

      try {
         JsonObject obj = JsonParser.parseString(metadata).getAsJsonObject();
         return "intermediate".equals(obj.has("type") ? obj.get("type").getAsString() : null);
      } catch (Exception e) {
         return false;
      }
   }

   private static int findVisibleStartIdx(@NotNull List<HistoryMessage> messages, int pageSize) {
      int visibleCount = 0;

      for (int i = messages.size() - 1; i >= 0; i--) {
         if (messages.get(i).isVisible()) {
            if (++visibleCount >= pageSize) {
               return i;
            }
         }
      }

      return 0;
   }

   private void addHistorySeparatorLabel(@NotNull String text) {
      JBLabel label = new JBLabel(text);
      label.setForeground(ChatStyles.TIMESTAMP_FG);
      label.setFont(Fonts.miniFont());
      label.setHorizontalAlignment(0);
      label.setBorder(Borders.empty(6, 0));
      label.setAlignmentX(0.0F);
      label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height + JBUI.scale(12)));
      this.messagesPanel.add(label, Math.max(0, this.messagesPanel.getComponentCount() - 1));
   }

   private void addMessageBubble(@NotNull MessageBubble.Role role, @NotNull String text) {
      if (role == MessageBubble.Role.USER) {
         this.addUserBubble(text, Collections.emptyList());
      } else {
         MessageBubble bubble = this.addAssistantBubble();
         bubble.finalizeWithMarkdown(text, this.project);
      }
   }

   private void insertLoadEarlierButton() {
      this.loadEarlierButton = new JButton("↑ Load earlier messages");
      this.loadEarlierButton.setFont(Fonts.miniFont());
      this.loadEarlierButton.setBorderPainted(false);
      this.loadEarlierButton.setContentAreaFilled(false);
      this.loadEarlierButton.setForeground(ChatStyles.ROLE_AI_FG);
      this.loadEarlierButton.setCursor(Cursor.getPredefinedCursor(12));
      this.loadEarlierButton.setAlignmentX(0.0F);
      this.loadEarlierButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.loadEarlierButton.getPreferredSize().height + JBUI.scale(8)));
      this.loadEarlierButton.addActionListener(e -> this.prependEarlierMessages());
      this.messagesPanel.add(this.loadEarlierButton, 0);
      this.messagesPanel.revalidate();
      this.messagesPanel.repaint();
   }

   private void prependEarlierMessages() {
      if (this.pendingEarlierMessages != null && !this.pendingEarlierMessages.isEmpty()) {
         JScrollBar bar = this.messageScrollPane.getVerticalScrollBar();
         int scrollVal = bar.getValue();
         int oldMax = bar.getMaximum();
         int total = this.pendingEarlierMessages.size();
         int startIdx = Math.max(0, total - 20);
         List<ConversationMessage> page = new ArrayList<>(this.pendingEarlierMessages.subList(startIdx, total));
         this.pendingEarlierMessages = startIdx > 0 ? new ArrayList<>(this.pendingEarlierMessages.subList(0, startIdx)) : null;
         int insertAt = this.loadEarlierButton != null ? 1 : 0;

         for (ConversationMessage msg : page) {
            MessageBubble.Role role = "USER".equalsIgnoreCase(msg.getRole()) ? MessageBubble.Role.USER : MessageBubble.Role.ASSISTANT;
            String rawText = msg.getContent() != null ? (msg.getContent().getText() != null ? msg.getContent().getText() : "") : "";
            MessageBubble bubble = new MessageBubble(role, "");
            if (role == MessageBubble.Role.ASSISTANT) {
               bubble.finalizeWithMarkdown(rawText, this.project);
            } else {
               bubble.appendToken(rawText);
            }

            this.messagesPanel.add(bubble, insertAt++);
         }

         if (this.pendingEarlierMessages == null && this.loadEarlierButton != null) {
            this.messagesPanel.remove(this.loadEarlierButton);
            this.loadEarlierButton = null;
         }

         this.messagesPanel.revalidate();
         this.messagesPanel.repaint();
         SwingUtilities.invokeLater(() -> {
            this.messageScrollPane.validate();
            int newMax = bar.getMaximum();
            int addedPx = newMax - oldMax;
            bar.setValue(scrollVal + addedPx);
         });
      }
   }

   public void showError(@NotNull String message) {
      assert SwingUtilities.isEventDispatchThread() : "showError must be on EDT";
      this.hideTypingIndicator();
      ErrorBubble errorBubble = new ErrorBubble(message);
      this.messagesPanel.add(errorBubble, Math.max(0, this.messagesPanel.getComponentCount() - 1));
      this.messagesPanel.revalidate();
      this.messagesPanel.repaint();
      this.scrollToBottomLater();
   }

   public void showAgentChangesPanel(@NotNull List<FileChangeProposal> proposals) {
      assert SwingUtilities.isEventDispatchThread() : "showAgentChangesPanel must be on EDT";
      this.dismissCurrentApproval();
      if (this.currentAgentPanel != null) {
         this.aboveInputStack.remove(this.currentAgentPanel);
      }

      this.currentAgentPanel = new AgentChangesPanel(proposals, this.project, () -> {
         this.aboveInputStack.remove(this.currentAgentPanel);
         this.currentAgentPanel = null;
         this.aboveInputStack.revalidate();
         this.aboveInputStack.repaint();
      });
      this.currentAgentPanel.setAlignmentX(0.0F);
      this.aboveInputStack.add(this.currentAgentPanel);
      this.aboveInputStack.revalidate();
      this.aboveInputStack.repaint();
   }

   @NotNull
   public AttachmentPreviewPanel getAttachmentPreviewPanel() {
      return this.attachmentPreviewPanel;
   }

   @NotNull
   public ConversationApiClient getConversationApiClient() {
      return this.conversationApiClient;
   }

   @NotNull
   public ChatInputPanel getChatInputPanel() {
      return this.chatInputPanel;
   }

   @NotNull
   public FileUploadApiClient getFileUploadApiClient() {
      return this.fileUploadApiClient;
   }

   @NotNull
   public Project getProject() {
      return this.project;
   }

   private void addGapIfNeeded(@NotNull MessageBubble.Role incoming) {
      if (this.lastAddedRole != null) {
         int gap = this.lastAddedRole == incoming ? 4 : 12;
         this.messagesPanel.add(Box.createRigidArea(new Dimension(0, gap)), Math.max(0, this.messagesPanel.getComponentCount() - 1));
      }
   }

   private void wireToolbarButtons() {
      this.newConversationButton.addActionListener(e -> this.handleNewConversation());
      this.historyButton.addActionListener(e -> this.showHistoryPopup());
      this.clearHistoryButton.addActionListener(e -> this.handleClear());
   }

   private void handleNewConversation() {
      this.conversationApiClient.cancel();
      this.clearMessages();
      this.currentConversationId = null;
      this.lastMessageId = null;
      this.chatInputPanel.startNewSession();
      this.attachmentPreviewPanel.clear();
      String modeName = AgentNameResolver.nameFor(this.project, TubiCopilotSettings.getInstance().getSelectedChatMode());
      this.chatInputPanel.setPendingConversationName(modeName);
   }

   private void handleClear() {
      String sessionId = this.chatInputPanel.getCurrentSession().id;
      if (this.currentConversationId == null) {
         this.clearMessages();
         this.lastMessageId = null;
         this.deleteLocalSession(sessionId);
         this.chatInputPanel.startNewSession();
      } else {
         int choice = Messages.showYesNoDialog(
            this.project, "Delete this conversation?\nThis cannot be undone.", "Delete Conversation", Messages.getWarningIcon()
         );
         if (choice == 0) {
            String idToDelete = this.currentConversationId;
            this.clearMessages();
            this.currentConversationId = null;
            this.lastMessageId = null;
            this.chatInputPanel.startNewSession();
            ApplicationManager.getApplication()
               .executeOnPooledThread(
                  () -> {
                     this.deleteLocalSession(sessionId);

                     try {
                        this.conversationApiClient.deleteConversation(idToDelete);
                        LOG.debug("Tubi Copilot: conversation deleted — " + idToDelete);
                     } catch (IOException e) {
                        LOG.warn("Tubi Copilot: failed to delete conversation " + idToDelete, e);
                        SwingUtilities.invokeLater(
                           () -> this.showError("Failed to delete conversation: " + (e.getMessage() != null ? e.getMessage() : "unknown error"))
                        );
                     }
                  }
               );
         }
      }
   }

   private void deleteLocalSession(@NotNull String sessionId) {
      ProjectHistoryService.getInstance(this.project).deleteSession(sessionId);
   }

   private boolean isProjectConversation(@NotNull ConversationSummary summary) {
      String hash = AgentNameResolver.projectHash(this.project);
      String name = summary.getConversationName();
      if (name == null) {
         return false;
      }

      String lower = name.toLowerCase();
      return lower.startsWith("ask-agent-" + hash) || lower.startsWith("plan-agent-" + hash) || lower.startsWith("execute-agent-" + hash);
   }

   static ChatMode inferModeFromName(@Nullable String name, @NotNull Project project) {
      if (name == null) {
         return TubiCopilotSettings.getInstance().getSelectedChatMode();
      } else {
         String lower = name.toLowerCase(Locale.ROOT);
         String hash = AgentNameResolver.projectHash(project);
         if (lower.startsWith("ask-agent-" + hash)) {
            return ChatMode.ASK;
         } else if (lower.startsWith("plan-agent-" + hash)) {
            return ChatMode.PLAN;
         } else {
            return lower.startsWith("execute-agent-" + hash) ? ChatMode.AGENT : TubiCopilotSettings.getInstance().getSelectedChatMode();
         }
      }
   }

   private void showHistoryPopup() {
      DefaultListModel<SessionSummary> model = new DefaultListModel<>();
      JBList<SessionSummary> list = new JBList(model);
      list.setSelectionMode(0);
      list.setBackground(UIUtil.getPanelBackground());
      list.setCellRenderer(new ChatPanel.SessionSummaryListCellRenderer());
      JBScrollPane listScroll = new JBScrollPane(list);
      listScroll.setBorder(null);
      listScroll.getViewport().setBackground(UIUtil.getPanelBackground());
      JButton loadMoreBtn = new JButton("Load more");
      loadMoreBtn.setVisible(false);
      JBLabel header = new JBLabel("Conversations");
      header.setBorder(Borders.empty(6, 10, 4, 10));
      header.setFont(Fonts.label().asBold());
      JBPanel<?> centerPanel = new JBPanel(new BorderLayout());
      JBPanel<?> bottomPanel = new JBPanel(new BorderLayout());
      bottomPanel.setBorder(Borders.empty(4, 8));
      bottomPanel.add(loadMoreBtn, "Center");
      JBPanel<?> content = new JBPanel(new BorderLayout());
      content.setPreferredSize(new Dimension(JBUI.scale(300), JBUI.scale(340)));
      content.add(header, "North");
      content.add(centerPanel, "Center");
      content.add(bottomPanel, "South");
      int[] pageOffset = new int[]{50};
      list.addListSelectionListener(e -> {
         if (!e.getValueIsAdjusting()) {
            SessionSummary selected = (SessionSummary)list.getSelectedValue();
            if (selected != null) {
               if (this.currentHistoryPopup != null) {
                  this.currentHistoryPopup.cancel();
                  this.currentHistoryPopup = null;
               }

               this.loadConversation(selected.getId());
            }
         }
      });
      loadMoreBtn.addActionListener(e -> {
         loadMoreBtn.setEnabled(false);
         int fetchOffset = pageOffset[0];
         ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<SessionSummary> more = ProjectHistoryService.getInstance(this.project).listSessions(fetchOffset, 50);
            SwingUtilities.invokeLater(() -> {
               for (SessionSummary s : more) {
                  model.addElement(s);
               }

               pageOffset[0] += more.size();
               loadMoreBtn.setVisible(more.size() == 50);
               loadMoreBtn.setEnabled(true);
            });
         });
      });
      this.currentHistoryPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(content, list).setRequestFocus(true).setResizable(true).createPopup();
      this.currentHistoryPopup.showUnderneathOf(this.historyButton);
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         List<SessionSummary> items = ProjectHistoryService.getInstance(this.project).listSessions(0, 50);
         SwingUtilities.invokeLater(() -> {
            centerPanel.removeAll();
            if (items.isEmpty()) {
               JBLabel emptyLabel = new JBLabel("No conversations found");
               emptyLabel.setBorder(Borders.empty(8, 10));
               emptyLabel.setHorizontalAlignment(0);
               emptyLabel.setForeground(ChatStyles.TIMESTAMP_FG);
               centerPanel.add(emptyLabel, "Center");
            } else {
               for (SessionSummary s : items) {
                  model.addElement(s);
               }

               centerPanel.add(listScroll, "Center");
               loadMoreBtn.setVisible(items.size() == 50);
            }

            centerPanel.revalidate();
            centerPanel.repaint();
         });
      });
   }

   public void scrollToBottomLater() {
      SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
         this.messageScrollPane.validate();
         JScrollBar bar = this.messageScrollPane.getVerticalScrollBar();
         bar.setValue(bar.getMaximum());
      }));
   }

   public void scrollToBottomIfNear() {
      SwingUtilities.invokeLater(() -> {
         this.messageScrollPane.validate();
         JScrollBar bar = this.messageScrollPane.getVerticalScrollBar();
         int maximum = bar.getMaximum();
         int visible = bar.getVisibleAmount();
         int current = bar.getValue();
         if (maximum - (current + visible) <= 50) {
            bar.setValue(maximum);
         }
      });
   }

   private static final class ConversationSummaryListCellRenderer extends DefaultListCellRenderer {
      private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         if (value instanceof ConversationSummary s) {
            String rawName = s.getConversationName();
            String displayName = rawName != null && rawName.length() > 48
               ? rawName.substring(0, 48) + "…"
               : (rawName != null && !rawName.isBlank() ? rawName : "Untitled conversation");
            String date = formatUpdatedAt(s.getUpdatedAt());
            this.setText("<html><b>" + escapeHtml(displayName) + "</b><br><font size='-2' color='gray'>" + date + "</font></html>");
            this.setBorder(Borders.empty(4, 8));
         }

         return this;
      }

      private static String formatUpdatedAt(@Nullable String isoString) {
         if (isoString != null && !isoString.isBlank()) {
            try {
               return OffsetDateTime.parse(isoString).atZoneSameInstant(ZoneId.systemDefault()).format(DATE_FMT);
            } catch (Exception e) {
               return isoString.length() > 16 ? isoString.substring(0, 16) : isoString;
            }
         } else {
            return "";
         }
      }

      private static String escapeHtml(@NotNull String text) {
         return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      }
   }

   private static final class SessionSummaryListCellRenderer extends SimpleListCellRenderer<SessionSummary> {
      private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM d, HH:mm");

      public void customize(@NotNull JList<? extends SessionSummary> list, SessionSummary value, int index, boolean selected, boolean hasFocus) {
         if (value == null) {
            this.setText("");
         } else {
            String title = value.getTitle() != null ? value.getTitle() : "Untitled";
            String date = DATE_FMT.format(new Date(value.getUpdatedAt()));
            this.setText(title + "  (" + date + ")");
         }
      }
   }

   private static final class ToolCallInfo {
      final String name;
      final String description;

      ToolCallInfo(String name, String description) {
         this.name = name;
         this.description = description;
      }
   }
}
