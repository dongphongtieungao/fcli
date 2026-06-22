package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolCallRow extends JBPanel<ToolCallRow> {
   private static final String[] SPINNER = new String[]{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
   private static final Map<String, Icon> TOOL_ICONS = Map.ofEntries(
      Map.entry("read_file", Actions.Preview),
      Map.entry("edit_file", Actions.Edit),
      Map.entry("create_file", General.Add),
      Map.entry("delete_file", General.Remove),
      Map.entry("search_code", Actions.Find),
      Map.entry("list_files", Nodes.Folder),
      Map.entry("run_command", Actions.Execute),
      Map.entry("get_diagnostics", General.Information),
      Map.entry("find_references", Actions.Find),
      Map.entry("get_symbols", Nodes.Class),
      Map.entry("git_diff", Actions.Diff),
      Map.entry("task_done", Actions.Checked),
      Map.entry("plan_done", Actions.Checked)
   );
   private static final Icon DEFAULT_TOOL_ICON = Actions.Execute;
   private static final Map<String, String> TOOL_VERBS = Map.ofEntries(
      Map.entry("read_file", "Read"),
      Map.entry("edit_file", "Edited"),
      Map.entry("create_file", "Created"),
      Map.entry("delete_file", "Deleted"),
      Map.entry("search_code", "Searched"),
      Map.entry("list_files", "Read directory"),
      Map.entry("run_command", "Ran"),
      Map.entry("get_diagnostics", "Checked diagnostics"),
      Map.entry("find_references", "Found references"),
      Map.entry("get_symbols", "Loaded symbols"),
      Map.entry("git_diff", "Checked diff"),
      Map.entry("task_done", "Completed"),
      Map.entry("plan_done", "Plan ready")
   );
   private final String toolName;
   private final JBLabel statusIcon;
   private final JBLabel descriptionLabel;
   private final Timer spinnerTimer;
   private int spinnerIndex = 0;
   private boolean completed = false;

   public ToolCallRow(@NotNull String toolName, @NotNull String description) {
      super(new BorderLayout(4, 0));
      this.toolName = toolName;
      this.setOpaque(false);
      this.setBorder(Borders.empty(2, 4));
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
      Icon icon = TOOL_ICONS.getOrDefault(toolName, DEFAULT_TOOL_ICON);
      JBLabel toolIcon = new JBLabel(icon);
      toolIcon.setPreferredSize(JBUI.size(16, 16));
      this.statusIcon = new JBLabel(SPINNER[0]);
      this.statusIcon.setFont(Fonts.label(11.0F));
      this.statusIcon.setForeground(ChatStyles.PROGRESS_ACCENT);
      this.statusIcon.setPreferredSize(JBUI.size(14, 16));
      this.descriptionLabel = new JBLabel(description);
      this.descriptionLabel.setFont(Fonts.label(11.0F));
      this.descriptionLabel.setForeground(ChatStyles.PROGRESS_RUNNING_FG);
      JPanel leftPanel = new JPanel(new FlowLayout(0, 4, 0));
      leftPanel.setOpaque(false);
      leftPanel.add(this.statusIcon);
      leftPanel.add(toolIcon);
      leftPanel.add(this.descriptionLabel);
      this.add(leftPanel, "Center");
      this.spinnerTimer = new Timer(80, e -> {
         this.spinnerIndex = (this.spinnerIndex + 1) % SPINNER.length;
         this.statusIcon.setText(SPINNER[this.spinnerIndex]);
      });
      this.spinnerTimer.start();
   }

   public void markCompleted(long durationMs) {
      if (!this.completed) {
         this.completed = true;
         this.spinnerTimer.stop();
         this.statusIcon.setText("✓");
         this.statusIcon.setForeground(ChatStyles.PROGRESS_DONE_FG);
         this.descriptionLabel.setForeground(ChatStyles.PROGRESS_DONE_FG);
         String verb = TOOL_VERBS.getOrDefault(this.toolName, this.toolName);
         String currentText = this.descriptionLabel.getText();
         String target = extractTarget(currentText);
         if (target != null) {
            this.descriptionLabel.setText(verb + " " + target + " successfully.");
         } else {
            this.descriptionLabel.setText(verb + " successfully.");
         }
      }
   }

   public void markFailed(@NotNull String reason) {
      if (!this.completed) {
         this.completed = true;
         this.spinnerTimer.stop();
         this.statusIcon.setText("✗");
         this.statusIcon.setForeground(new JBColor(13574702, 14300723));
         this.descriptionLabel.setForeground(new JBColor(13574702, 14300723));
         String verb = TOOL_VERBS.getOrDefault(this.toolName, this.toolName);
         this.descriptionLabel.setText(verb + " failed.");
         this.descriptionLabel.setToolTipText(reason);
      }
   }

   public void dispose() {
      this.spinnerTimer.stop();
   }

   public void removeNotify() {
      this.dispose();
      super.removeNotify();
   }

   @Nullable
   private static String extractTarget(@NotNull String description) {
      int openParen = description.indexOf(40);
      int closeParen = description.lastIndexOf(41);
      if (openParen >= 0 && closeParen > openParen) {
         String inner = description.substring(openParen + 1, closeParen).strip();
         if (!inner.isEmpty()) {
            int lastSlash = Math.max(inner.lastIndexOf(47), inner.lastIndexOf(92));
            return lastSlash >= 0 ? inner.substring(lastSlash + 1) : inner;
         }
      }

      return null;
   }
}
