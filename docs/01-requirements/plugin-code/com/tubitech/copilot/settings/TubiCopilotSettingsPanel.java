package com.tubitech.copilot.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.tubitech.copilot.auth.TokenManager;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TubiCopilotSettingsPanel {
   private static final Logger LOG = Logger.getInstance(TubiCopilotSettingsPanel.class);
   private final JPasswordField refreshTokenField = new JPasswordField(40);
   private final JButton testConnectionButton = new JButton("Test Connection \ud83d\udd17");
   private final JBLabel connectionStatusLabel = new JBLabel(" ");
   private final JBTextField baseUrlField = new JBTextField(40);
   private final JBTextField connectTimeoutField = new JBTextField(5);
   private final JBTextField maxUploadSizeField = new JBTextField(5);
   private final JBCheckBox showTimestampsCheckbox = new JBCheckBox("Show timestamps on messages");
   private final JBCheckBox autoScrollCheckbox = new JBCheckBox("Auto-scroll to latest message");
   private final JBCheckBox persistHistoryCheckbox = new JBCheckBox("Persist conversation history across restarts");
   private final JBTextField maxSavedSessionsField = new JBTextField(5);
   private final JBCheckBox autoApproveCheckbox = new JBCheckBox("Auto-approve file modifications (skip manual confirmation in Agent mode)");
   private final JPanel root = this.buildForm();

   public TubiCopilotSettingsPanel() {
      this.testConnectionButton.addActionListener(e -> this.onTestConnection());
   }

   @NotNull
   public JPanel getPanel() {
      return this.root;
   }

   public void apply(@NotNull TubiCopilotSettings settings) {
      String refreshToken = new String(this.refreshTokenField.getPassword());
      saveRefreshToken(refreshToken);
      settings.baseUrl = this.baseUrlField.getText().trim();
      settings.connectTimeoutSeconds = parseIntSafe(this.connectTimeoutField.getText(), 10);
      settings.maxUploadSizeMb = parseIntSafe(this.maxUploadSizeField.getText(), 20);
      settings.showTimestamps = this.showTimestampsCheckbox.isSelected();
      settings.autoScroll = this.autoScrollCheckbox.isSelected();
      settings.persistHistory = this.persistHistoryCheckbox.isSelected();
      settings.maxSavedSessions = parseIntSafe(this.maxSavedSessionsField.getText(), 50);
      settings.autoApproveWriteTools = this.autoApproveCheckbox.isSelected();
   }

   public void reset(@NotNull TubiCopilotSettings settings) {
      String rt = loadRefreshToken();
      this.refreshTokenField.setText(rt == null ? "" : rt);
      this.baseUrlField.setText(settings.baseUrl);
      this.connectTimeoutField.setText(String.valueOf(settings.connectTimeoutSeconds));
      this.maxUploadSizeField.setText(String.valueOf(settings.maxUploadSizeMb));
      this.showTimestampsCheckbox.setSelected(settings.showTimestamps);
      this.autoScrollCheckbox.setSelected(settings.autoScroll);
      this.persistHistoryCheckbox.setSelected(settings.persistHistory);
      this.maxSavedSessionsField.setText(String.valueOf(settings.maxSavedSessions));
      this.autoApproveCheckbox.setSelected(settings.autoApproveWriteTools);
      this.connectionStatusLabel.setText(" ");
   }

   public boolean isModified(@NotNull TubiCopilotSettings settings) {
      String storedToken = loadRefreshToken();
      String fieldToken = new String(this.refreshTokenField.getPassword());
      if (!fieldToken.equals(storedToken == null ? "" : storedToken)) {
         return true;
      } else if (!this.baseUrlField.getText().trim().equals(settings.baseUrl)) {
         return true;
      } else if (parseIntSafe(this.connectTimeoutField.getText(), 10) != settings.connectTimeoutSeconds) {
         return true;
      } else if (parseIntSafe(this.maxUploadSizeField.getText(), 20) != settings.maxUploadSizeMb) {
         return true;
      } else if (this.showTimestampsCheckbox.isSelected() != settings.showTimestamps) {
         return true;
      } else if (this.autoScrollCheckbox.isSelected() != settings.autoScroll) {
         return true;
      } else if (this.persistHistoryCheckbox.isSelected() != settings.persistHistory) {
         return true;
      } else {
         return parseIntSafe(this.maxSavedSessionsField.getText(), 50) != settings.maxSavedSessions
            ? true
            : this.autoApproveCheckbox.isSelected() != settings.autoApproveWriteTools;
      }
   }

   @NotNull
   private JPanel buildForm() {
      return FormBuilder.createFormBuilder()
         .addLabeledComponent(new JBLabel("Refresh Token:"), this.refreshTokenField, 1, false)
         .addComponentToRightColumn(this.testConnectionButton, 0)
         .addComponentToRightColumn(this.connectionStatusLabel, 0)
         .addSeparator(JBUI.scale(6))
         .addLabeledComponent(new JBLabel("Base URL:"), this.baseUrlField, 1, false)
         .addLabeledComponent(new JBLabel("Connect Timeout (s):"), this.connectTimeoutField, 1, false)
         .addLabeledComponent(new JBLabel("Max Upload Size (MB):"), this.maxUploadSizeField, 1, false)
         .addSeparator(JBUI.scale(6))
         .addComponent(this.showTimestampsCheckbox, JBUI.scale(4))
         .addComponent(this.autoScrollCheckbox, JBUI.scale(4))
         .addComponent(this.persistHistoryCheckbox, JBUI.scale(4))
         .addLabeledComponent(new JBLabel("Max Saved Sessions:"), this.maxSavedSessionsField, 1, false)
         .addSeparator(JBUI.scale(6))
         .addComponent(this.autoApproveCheckbox, JBUI.scale(4))
         .addComponentFillVertically(new JBPanel(), 0)
         .getPanel();
   }

   private void onTestConnection() {
      String refreshToken = new String(this.refreshTokenField.getPassword());
      if (refreshToken.isBlank()) {
         this.connectionStatusLabel.setText("⚠ Enter a Refresh Token first.");
      } else {
         saveRefreshToken(refreshToken);
         this.testConnectionButton.setEnabled(false);
         this.connectionStatusLabel.setText("Testing…");
         ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String result;
            try {
               TokenManager tm = (TokenManager)ApplicationManager.getApplication().getService(TokenManager.class);
               String accessToken = tm.forceRefresh();
               String preview = accessToken.length() > 12 ? accessToken.substring(0, 12) + "…" : "***";
               result = "✅ Connected — access token obtained (" + preview + ")";
            } catch (Exception ex) {
               LOG.warn("Tubi Copilot: test connection failed", ex);
               result = "❌ Error: " + ex.getMessage();
            }

            String finalResult = result;
            SwingUtilities.invokeLater(() -> {
               this.connectionStatusLabel.setText(finalResult);
               this.testConnectionButton.setEnabled(true);
            });
         });
      }
   }

   @Nullable
   private static String loadRefreshToken() {
      CredentialAttributes attrs = TubiCopilotSettings.getRefreshTokenCredentialAttributes();
      Credentials credentials = PasswordSafe.getInstance().get(attrs);
      return credentials == null ? null : credentials.getPasswordAsString();
   }

   private static void saveRefreshToken(@Nullable String token) {
      CredentialAttributes attrs = TubiCopilotSettings.getRefreshTokenCredentialAttributes();
      if (token != null && !token.isBlank()) {
         PasswordSafe.getInstance().set(attrs, new Credentials("refresh_token", token));
      } else {
         PasswordSafe.getInstance().set(attrs, null);
      }
   }

   private static int parseIntSafe(@NotNull String text, int defaultValue) {
      try {
         return Integer.parseInt(text.trim());
      } catch (NumberFormatException e) {
         return defaultValue;
      }
   }
}
