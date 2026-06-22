package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;

public final class TypingIndicator extends JBPanel<TypingIndicator> {
   private static final String[] FRAMES = new String[]{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
   private static final Map<String, Icon> STATUS_ICONS = Map.of(
      "thinking",
      Actions.Execute,
      "read_file",
      Actions.Preview,
      "edit_file",
      Actions.Edit,
      "create_file",
      General.Add,
      "search_code",
      Actions.Find,
      "list_files",
      Nodes.Folder,
      "run_command",
      Actions.Execute,
      "get_diagnostics",
      General.Information
   );
   private int frameIndex = 0;
   private final JLabel statusIconLabel;
   private final JLabel spinnerLabel;
   private final JLabel textLabel;
   private final Timer animationTimer;

   public TypingIndicator() {
      super(new FlowLayout(0, 6, 4));
      this.setBackground(UIUtil.getPanelBackground());
      this.setBorder(Borders.empty(6, 12));
      this.setAlignmentX(0.0F);
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      this.setVisible(false);
      this.statusIconLabel = new JLabel(Actions.Execute);
      this.statusIconLabel.setPreferredSize(JBUI.size(16, 16));
      this.spinnerLabel = new JLabel(FRAMES[0]);
      this.spinnerLabel.setFont(Fonts.label(13.0F));
      this.spinnerLabel.setForeground(ChatStyles.PROGRESS_ACCENT);
      this.textLabel = new JLabel("Tubi Copilot is thinking…");
      this.textLabel.setForeground(UIUtil.getContextHelpForeground());
      this.textLabel.setFont(Fonts.label(12.0F).asItalic());
      this.add(this.statusIconLabel);
      this.add(this.spinnerLabel);
      this.add(this.textLabel);
      this.animationTimer = new Timer(80, e -> {
         this.frameIndex = (this.frameIndex + 1) % FRAMES.length;
         this.spinnerLabel.setText(FRAMES[this.frameIndex]);
      });
   }

   public void start() {
      this.frameIndex = 0;
      this.spinnerLabel.setText(FRAMES[0]);
      this.statusIconLabel.setIcon(Actions.Execute);
      this.textLabel.setText("Tubi Copilot is thinking…");
      this.setVisible(true);
      this.animationTimer.start();
   }

   public void stop() {
      this.animationTimer.stop();
      this.setVisible(false);
   }

   public void removeNotify() {
      this.animationTimer.stop();
      super.removeNotify();
   }

   public void setStatusText(@NotNull String text) {
      this.textLabel.setText(text);

      for (Entry<String, Icon> entry : STATUS_ICONS.entrySet()) {
         if (text.toLowerCase().contains(entry.getKey())) {
            this.statusIconLabel.setIcon(entry.getValue());
            return;
         }
      }

      this.statusIconLabel.setIcon(Actions.Execute);
   }
}
