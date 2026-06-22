package com.tubitech.copilot.settings;

import com.intellij.openapi.options.Configurable;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Nls.Capitalization;

public final class TubiCopilotSettingsConfigurable implements Configurable {
   @Nullable
   private TubiCopilotSettingsPanel panel;

   @Nls(capitalization = Capitalization.Title)
   public String getDisplayName() {
      return "Tubi Copilot";
   }

   public JComponent createComponent() {
      if (this.panel == null) {
         this.panel = new TubiCopilotSettingsPanel();
      }

      return this.panel.getPanel();
   }

   public void reset() {
      if (this.panel != null) {
         this.panel.reset(TubiCopilotSettings.getInstance());
      }
   }

   public boolean isModified() {
      return this.panel != null && this.panel.isModified(TubiCopilotSettings.getInstance());
   }

   public void apply() {
      if (this.panel != null) {
         this.panel.apply(TubiCopilotSettings.getInstance());
      }
   }

   public void disposeUIResources() {
      this.panel = null;
   }
}
