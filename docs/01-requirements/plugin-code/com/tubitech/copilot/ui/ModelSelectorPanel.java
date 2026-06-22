package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.tubitech.copilot.agent.UpdateSyncService;
import com.tubitech.copilot.api.ModelApiClient;
import com.tubitech.copilot.api.model.SupportedModel;
import com.tubitech.copilot.settings.ChatMode;
import com.tubitech.copilot.settings.ModelCapability;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import com.tubitech.copilot.settings.TubiCopilotSettingsConfigurable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModelSelectorPanel extends JBPanel<ModelSelectorPanel> {
   private static final String PLACEHOLDER_DISPLAY = "Loading…";
   private final Project project;
   private final ModelApiClient modelApiClient;
   private final ChatPanel chatPanel;
   private final ChatInputPanel chatInputPanel;
   private boolean suppressModelEvent = false;
   private boolean suppressModeEvent = false;
   private final JComboBox<SupportedModel> modelComboBox;
   private final JComboBox<ChatMode> modeComboBox;
   private final JButton reloadBtn;
   private final JButton sendButton;
   private static final Icon SEND_ICON = Actions.Execute;
   private static final Icon STOP_ICON = Actions.Suspend;

   public JButton getSendButton() {
      return this.sendButton;
   }

   public ModelSelectorPanel(
      @NotNull Project project, @NotNull ModelApiClient modelApiClient, @NotNull ChatPanel chatPanel, @NotNull ChatInputPanel chatInputPanel
   ) {
      super(new BorderLayout());
      this.project = project;
      this.modelApiClient = modelApiClient;
      this.chatPanel = chatPanel;
      this.chatInputPanel = chatInputPanel;
      this.setBorder(Borders.empty(8, 0, 4, 0));
      this.modeComboBox = new JComboBox<>();
      this.modeComboBox.addItem(ChatMode.ASK);
      this.modeComboBox.addItem(ChatMode.AGENT);
      this.modeComboBox.addItem(ChatMode.PLAN);
      this.modeComboBox.setRenderer(new ModelSelectorPanel.ModeRenderer());
      this.suppressModeEvent = true;
      this.modeComboBox.setSelectedItem(TubiCopilotSettings.getInstance().getSelectedChatMode());
      this.suppressModeEvent = false;
      this.modeComboBox
         .addActionListener(
            e -> {
               if (!this.suppressModeEvent) {
                  ChatMode selected = (ChatMode)this.modeComboBox.getSelectedItem();
                  if (selected != null) {
                     String currentModelId = this.getSelectedModelId();
                     if (ModelCapability.modeRequiresAgent(selected) && !ModelCapability.supportsAgentMode(currentModelId)) {
                        this.suppressModeEvent = true;
                        this.modeComboBox.setSelectedItem(ChatMode.ASK);
                        this.suppressModeEvent = false;
                        chatPanel.showSyncResult(
                           InlineNoticeBar.NoticeType.SYSTEM, "This model does not support " + selected.name() + " mode. Please switch to a Gemini model."
                        );
                     } else {
                        this.handleModeSwitch(selected);
                     }
                  }
               }
            }
         );
      this.modelComboBox = new JComboBox<>();
      this.modelComboBox.setRenderer(new ModelSelectorPanel.ModelRenderer());
      this.modelComboBox.setEnabled(false);
      this.suppressModelEvent = true;
      this.modelComboBox.addItem(new SupportedModel("", "Loading…"));
      this.suppressModelEvent = false;
      this.modelComboBox.addActionListener(e -> {
         if (!this.suppressModelEvent) {
            SupportedModel sel = (SupportedModel)this.modelComboBox.getSelectedItem();
            if (sel != null && sel.getModelId() != null && !sel.getModelId().isBlank()) {
               TubiCopilotSettings.getInstance().setSelectedModelId(sel.getModelId());
               this.enforceModelModeConstraints(sel.getModelId());
            }
         }
      });
      this.reloadBtn = makeIconButton(Actions.Refresh, "Reload model list");
      this.reloadBtn.addActionListener(e -> this.handleReload());
      JButton settingsBtn = makeIconButton(General.Settings, "Tubi Copilot Settings");
      settingsBtn.addActionListener(e -> ShowSettingsUtil.getInstance().showSettingsDialog(project, TubiCopilotSettingsConfigurable.class));
      this.sendButton = new JButton(SEND_ICON);
      this.sendButton.setToolTipText("Send (Enter)");
      this.sendButton.setContentAreaFilled(false);
      this.sendButton.setFocusPainted(false);
      this.sendButton.setMargin(new Insets(0, 0, 0, 0));
      this.sendButton.setBorder(Borders.customLine(ChatStyles.INPUT_BORDER, 1));
      Dimension sendSize = JBUI.size(28, 28);
      this.sendButton.setPreferredSize(sendSize);
      this.sendButton.setMinimumSize(sendSize);
      this.sendButton.setMaximumSize(sendSize);
      this.sendButton.addActionListener(e -> chatInputPanel.handleSendOrStop());
      int gap = JBUI.scale(4);
      Box leftGroup = Box.createHorizontalBox();
      leftGroup.add(this.modeComboBox);
      leftGroup.add(Box.createHorizontalStrut(gap));
      leftGroup.add(this.modelComboBox);
      leftGroup.add(Box.createHorizontalStrut(gap));
      leftGroup.add(this.reloadBtn);
      leftGroup.add(Box.createHorizontalStrut(gap));
      leftGroup.add(settingsBtn);
      leftGroup.add(Box.createHorizontalGlue());
      this.add(leftGroup, "Center");
      this.add(this.sendButton, "East");
      this.loadModels(modelApiClient);
      this.ensureAgentsInitialized();
   }

   @NotNull
   public String getSelectedModelId() {
      SupportedModel sel = (SupportedModel)this.modelComboBox.getSelectedItem();
      return sel != null && sel.getModelId() != null && !sel.getModelId().isBlank() ? sel.getModelId() : TubiCopilotSettings.getInstance().getSelectedModelId();
   }

   public void setSendStreaming(boolean streaming) {
      this.sendButton.setIcon(streaming ? STOP_ICON : SEND_ICON);
      this.sendButton.setToolTipText(streaming ? "Stop (Enter)" : "Send (Enter)");
   }

   public void switchMode(@NotNull ChatMode mode) {
      this.suppressModeEvent = true;
      this.modeComboBox.setSelectedItem(mode);
      this.suppressModeEvent = false;
      TubiCopilotSettings.getInstance().setSelectedChatMode(mode);
      this.chatInputPanel.updatePlaceholder(mode);
   }

   public void restoreConversationContext(@NotNull ChatMode mode, @Nullable String modelId) {
      this.switchMode(mode);
      if (modelId != null && !modelId.isBlank()) {
         this.selectModelById(modelId);
      }
   }

   private void enforceModelModeConstraints(@NotNull String modelId) {
      boolean supportsAgent = ModelCapability.supportsAgentMode(modelId);
      ChatMode current = (ChatMode)this.modeComboBox.getSelectedItem();
      if (current != null && ModelCapability.modeRequiresAgent(current) && !supportsAgent) {
         this.switchMode(ChatMode.ASK);
         this.chatPanel.showSyncResult(InlineNoticeBar.NoticeType.SYSTEM, "Switched to Ask mode — this model does not support Agent/Plan.");
      }

      this.modeComboBox.repaint();
   }

   private void handleModeSwitch(@NotNull ChatMode mode) {
      TubiCopilotSettings.getInstance().setSelectedChatMode(mode);
      this.chatInputPanel.updatePlaceholder(mode);
      this.ensureAgentsInitialized();
   }

   private void ensureAgentsInitialized() {
      TubiCopilotSettings settings = TubiCopilotSettings.getInstance();
      if (ModelCapability.supportsAgentMode(settings.getSelectedModelId())) {
         if (settings.getCopilotAgentId().isBlank()) {
            ApplicationManager.getApplication()
               .executeOnPooledThread(
                  () -> UpdateSyncService.sync(
                     this.project,
                     new UpdateSyncService.SyncCallback() {
                        @Override
                        public void onSuccess(@Nullable String agentId, @Nullable String agentName, @NotNull List<SupportedModel> models) {
                           SwingUtilities.invokeLater(
                              () -> ModelSelectorPanel.this.chatPanel.showSyncResult(InlineNoticeBar.NoticeType.SUCCESS, "Agents ready.")
                           );
                        }

                        @Override
                        public void onError(@NotNull IOException e) {
                           SwingUtilities.invokeLater(
                              () -> ModelSelectorPanel.this.chatPanel.showSyncResult(InlineNoticeBar.NoticeType.SYSTEM, "Agent init failed: " + e.getMessage())
                           );
                        }

                        @Override
                        public void onInstructionsOverflow(@NotNull String errorMessage) {
                           SwingUtilities.invokeLater(
                              () -> ModelSelectorPanel.this.chatPanel
                                 .showSyncResult(InlineNoticeBar.NoticeType.VALIDATION, "Instructions overflow: " + errorMessage)
                           );
                        }
                     }
                  )
               );
         }
      }
   }

   private void handleReload() {
      this.reloadBtn.setEnabled(false);
      ApplicationManager.getApplication()
         .executeOnPooledThread(
            () -> {
               List<SupportedModel> models = null;
               IOException fetchError = null;

               try {
                  models = this.modelApiClient.fetchSupportedModels();
               } catch (IOException e) {
                  fetchError = e;
               }

               List<SupportedModel> finalModels = models;
               IOException finalError = fetchError;
               SwingUtilities.invokeLater(
                  () -> {
                     this.reloadBtn.setEnabled(true);
                     if (finalError == null && finalModels != null && !finalModels.isEmpty()) {
                        this.populateModels(finalModels);
                        this.chatPanel.showSyncResult(InlineNoticeBar.NoticeType.SUCCESS, "Models reloaded successfully.");
                     } else {
                        this.suppressModelEvent = true;
                        this.modelComboBox.removeAllItems();
                        this.suppressModelEvent = false;
                        this.modelComboBox.setEnabled(false);
                        this.chatPanel
                           .showSyncResult(
                              InlineNoticeBar.NoticeType.SYSTEM, "Reload failed: " + (finalError != null ? finalError.getMessage() : "empty model list")
                           );
                     }
                  }
               );
            }
         );
   }

   @NotNull
   private static JButton makeIconButton(@NotNull Icon icon, @NotNull String tooltip) {
      JButton btn = new JButton(icon);
      btn.setToolTipText(tooltip);
      btn.setBorderPainted(false);
      btn.setContentAreaFilled(false);
      btn.setFocusPainted(false);
      btn.setMargin(new Insets(0, 0, 0, 0));
      Dimension size = JBUI.size(24, 24);
      btn.setPreferredSize(size);
      btn.setMinimumSize(size);
      btn.setMaximumSize(size);
      return btn;
   }

   private void loadModels(@NotNull ModelApiClient client) {
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         List<SupportedModel> models = null;

         try {
            models = client.fetchSupportedModels();
         } catch (IOException var4) {
         }

         List<SupportedModel> finalModels = models;
         SwingUtilities.invokeLater(() -> {
            this.suppressModelEvent = true;
            this.modelComboBox.removeAllItems();
            if (finalModels != null && !finalModels.isEmpty()) {
               for (SupportedModel m : finalModels) {
                  this.modelComboBox.addItem(m);
               }

               this.restoreModelSelection();
               this.modelComboBox.setEnabled(true);
            } else {
               this.modelComboBox.setEnabled(false);
            }

            this.suppressModelEvent = false;
            this.reloadBtn.setEnabled(true);
         });
      });
   }

   private void populateModels(@NotNull List<SupportedModel> models) {
      this.suppressModelEvent = true;
      this.modelComboBox.removeAllItems();

      for (SupportedModel m : models) {
         this.modelComboBox.addItem(m);
      }

      this.restoreModelSelection();
      this.suppressModelEvent = false;
      this.modelComboBox.setEnabled(true);
   }

   private void restoreModelSelection() {
      String saved = TubiCopilotSettings.getInstance().getSelectedModelId();

      for (int i = 0; i < this.modelComboBox.getItemCount(); i++) {
         if (saved.equals(this.modelComboBox.getItemAt(i).getModelId())) {
            this.modelComboBox.setSelectedIndex(i);
            return;
         }
      }

      if (this.modelComboBox.getItemCount() > 0) {
         this.modelComboBox.setSelectedIndex(0);
         SupportedModel first = this.modelComboBox.getItemAt(0);
         if (first.getModelId() != null) {
            TubiCopilotSettings.getInstance().setSelectedModelId(first.getModelId());
         }
      }
   }

   private void selectModelById(@NotNull String modelId) {
      this.suppressModelEvent = true;

      for (int i = 0; i < this.modelComboBox.getItemCount(); i++) {
         if (modelId.equals(this.modelComboBox.getItemAt(i).getModelId())) {
            this.modelComboBox.setSelectedIndex(i);
            TubiCopilotSettings.getInstance().setSelectedModelId(modelId);
            this.suppressModelEvent = false;
            return;
         }
      }

      this.suppressModelEvent = false;
   }

   private final class ModeRenderer extends SimpleListCellRenderer<ChatMode> {
      public void customize(@NotNull JList<? extends ChatMode> list, @Nullable ChatMode value, int index, boolean selected, boolean hasFocus) {
         if (value == null) {
            this.setText("");
         } else {
            String label = switch (value) {
               case ASK -> "Ask";
               case AGENT -> "Agent";
               case PLAN -> "Plan";
               default -> value.name();
            };
            String currentModelId = ModelSelectorPanel.this.modelComboBox != null ? ModelSelectorPanel.this.getSelectedModelId() : "";
            boolean disabled = ModelCapability.modeRequiresAgent(value) && !ModelCapability.supportsAgentMode(currentModelId);
            if (disabled) {
               this.setText(label + " ✗");
               this.setForeground(Color.GRAY);
            } else {
               this.setText(label);
            }
         }
      }
   }

   private static final class ModelRenderer extends SimpleListCellRenderer<SupportedModel> {
      public void customize(@NotNull JList<? extends SupportedModel> list, @Nullable SupportedModel value, int index, boolean selected, boolean hasFocus) {
         if (value == null) {
            this.setText("");
         } else {
            String name = value.getDisplayName() != null && !value.getDisplayName().isBlank()
               ? value.getDisplayName()
               : (value.getModelId() != null ? value.getModelId() : "");
            this.setText(name);
         }
      }
   }
}
