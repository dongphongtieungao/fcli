package com.tubitech.copilot.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "TubiCopilotSettings", storages = @Storage("TubiCopilot.xml"))
public class TubiCopilotSettings implements PersistentStateComponent<TubiCopilotSettings> {
   private static final String BEARER_TOKEN_CREDENTIAL_KEY = "TubiCopilotBearerToken";
   public String baseUrl = "https://privategpt.fptconsulting.co.jp";
   @Deprecated
   public String modelId = "azure-gpt-5.2";
   public String selectedModelId = "azure-gpt-5.2";
   public int connectTimeoutSeconds = 10;
   public int maxUploadSizeMb = 20;
   public boolean showTimestamps = true;
   public boolean autoScroll = true;
   public boolean persistHistory = true;
   public int maxSavedSessions = 50;
   @Deprecated
   public boolean useMentionBundle = true;
   public boolean autoApproveWriteTools = false;
   public String selectedChatMode = ChatMode.ASK.name();
   @Deprecated
   public String selectedAgentId = "";
   @Deprecated
   public String selectedAgentName = "";
   public String planAgentId = "";
   public String executeAgentId = "";
   public String askAgentId = "";
   public String askAgentName = "";
   public String copilotAgentId = "";

   @NotNull
   public static TubiCopilotSettings getInstance() {
      return (TubiCopilotSettings)ApplicationManager.getApplication().getService(TubiCopilotSettings.class);
   }

   @Nullable
   public TubiCopilotSettings getState() {
      return this;
   }

   public void loadState(@NotNull TubiCopilotSettings state) {
      XmlSerializerUtil.copyBean(state, this);
   }

   @NotNull
   public String getSelectedModelId() {
      if (this.selectedModelId != null && !this.selectedModelId.isBlank()) {
         return this.selectedModelId;
      } else {
         return this.modelId != null && !this.modelId.isBlank() ? this.modelId : "azure-gpt-5.2";
      }
   }

   public void setSelectedModelId(@Nullable String modelId) {
      if (modelId != null && !modelId.isBlank()) {
         this.selectedModelId = modelId;
      }
   }

   @NotNull
   public ChatMode getSelectedChatMode() {
      ChatMode var10000;
      try {
         ChatMode mode = ChatMode.valueOf(this.selectedChatMode);
         if (mode == ChatMode.PLAN_AGENT) {
            return ChatMode.PLAN;
         }

         if (mode == ChatMode.EXECUTE_AGENT) {
            return ChatMode.AGENT;
         }

         var10000 = mode;
      } catch (IllegalArgumentException | NullPointerException e) {
         return ChatMode.ASK;
      }

      return var10000;
   }

   public void setSelectedChatMode(@NotNull ChatMode mode) {
      this.selectedChatMode = mode.name();
   }

   @Deprecated
   @NotNull
   public String getSelectedAgentId() {
      return this.selectedAgentId != null ? this.selectedAgentId : "";
   }

   @Deprecated
   public void setSelectedAgentId(@NotNull String id) {
      this.selectedAgentId = id;
   }

   @Deprecated
   @NotNull
   public String getSelectedAgentName() {
      return this.selectedAgentName != null ? this.selectedAgentName : "";
   }

   @Deprecated
   public void setSelectedAgentName(@NotNull String name) {
      this.selectedAgentName = name;
   }

   @NotNull
   public String getPlanAgentId() {
      return this.planAgentId != null ? this.planAgentId : "";
   }

   public void setPlanAgentId(@NotNull String id) {
      this.planAgentId = id;
   }

   @NotNull
   public String getExecuteAgentId() {
      return this.executeAgentId != null ? this.executeAgentId : "";
   }

   public void setExecuteAgentId(@NotNull String id) {
      this.executeAgentId = id;
   }

   @NotNull
   public String getAskAgentId() {
      return this.askAgentId != null ? this.askAgentId : "";
   }

   public void setAskAgentId(@NotNull String id) {
      this.askAgentId = id;
   }

   @NotNull
   public String getAskAgentName() {
      return this.askAgentName != null ? this.askAgentName : "";
   }

   public void setAskAgentName(@NotNull String name) {
      this.askAgentName = name;
   }

   @NotNull
   public String getCopilotAgentId() {
      return this.copilotAgentId != null ? this.copilotAgentId : "";
   }

   public void setCopilotAgentId(@NotNull String id) {
      this.copilotAgentId = id;
   }

   @NotNull
   public static CredentialAttributes getRefreshTokenCredentialAttributes() {
      return new CredentialAttributes(CredentialAttributesKt.generateServiceName("TubiCopilot", "TubiCopilot.RefreshToken"));
   }

   @Deprecated
   @Nullable
   public String getBearerToken() {
      CredentialAttributes attrs = buildLegacyBearerTokenAttributes();
      Credentials credentials = PasswordSafe.getInstance().get(attrs);
      return credentials == null ? null : credentials.getPasswordAsString();
   }

   @Deprecated
   public void setBearerToken(@Nullable String token) {
      CredentialAttributes attrs = buildLegacyBearerTokenAttributes();
      if (token != null && !token.isBlank()) {
         PasswordSafe.getInstance().set(attrs, new Credentials("token", token));
      } else {
         PasswordSafe.getInstance().set(attrs, null);
      }
   }

   @NotNull
   private static CredentialAttributes buildLegacyBearerTokenAttributes() {
      return new CredentialAttributes(CredentialAttributesKt.generateServiceName("TubiCopilot", "TubiCopilotBearerToken"));
   }
}
