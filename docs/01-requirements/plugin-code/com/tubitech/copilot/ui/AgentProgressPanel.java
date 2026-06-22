package com.tubitech.copilot.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AgentProgressPanel extends JBPanel<AgentProgressPanel> {
   private static final JBColor LEFT_ACCENT = new JBColor(1733608, 4886745);
   private static final JBColor WAITING_FG = new JBColor(10395294, 9145227);
   private static final int SHORT_TEXT_THRESHOLD = 200;
   private final JBLabel headerLabel;
   private final JBLabel toggleLabel;
   private final JBPanel<?> contentPanel;
   private final List<ToolCallRow> rows = new ArrayList<>();
   private boolean expanded = true;
   private boolean finished = false;
   private int completedCount = 0;
   private JPanel waitingIndicator;

   public AgentProgressPanel() {
      super(new BorderLayout());
      this.setOpaque(false);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, LEFT_ACCENT), Borders.empty(4, 10, 4, 4)));
      JPanel header = new JPanel(new BorderLayout());
      header.setOpaque(false);
      header.setCursor(Cursor.getPredefinedCursor(12));
      header.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            AgentProgressPanel.this.toggleExpanded();
         }
      });
      this.headerLabel = new JBLabel("Agent working…");
      this.headerLabel.setFont(Fonts.label(11.0F).asBold());
      this.headerLabel.setForeground(ChatStyles.PROGRESS_ACCENT);
      this.toggleLabel = new JBLabel("▼");
      this.toggleLabel.setFont(Fonts.label(10.0F));
      this.toggleLabel.setForeground(ChatStyles.TIMESTAMP_FG);
      header.add(this.headerLabel, "West");
      header.add(this.toggleLabel, "East");
      this.contentPanel = new JBPanel();
      this.contentPanel.setLayout(new BoxLayout(this.contentPanel, 1));
      this.contentPanel.setOpaque(false);
      this.add(header, "North");
      this.add(this.contentPanel, "Center");
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
   }

   @NotNull
   public ToolCallRow addToolCall(@NotNull String toolName, @NotNull String description) {
      ToolCallRow row = new ToolCallRow(toolName, description);
      row.setAlignmentX(0.0F);
      this.rows.add(row);
      runOnEdt(() -> {
         this.removeWaitingIndicator();
         this.contentPanel.add(row);
         this.updateHeader();
         this.propagateResize();
      });
      return row;
   }

   public void markToolComplete(@NotNull ToolCallRow row, long durationMs) {
      this.completedCount++;
      runOnEdt(() -> {
         row.markCompleted(durationMs);
         this.updateHeader();
         this.propagateResize();
      });
   }

   public void markToolFailed(@NotNull ToolCallRow row, @NotNull String reason) {
      this.completedCount++;
      runOnEdt(() -> {
         row.markFailed(reason);
         this.updateHeader();
         this.propagateResize();
      });
   }

   public void showWaitingForResponse() {
      if (!this.finished) {
         runOnEdt(() -> {
            this.headerLabel.setText("Waiting for AI response…");
            this.removeWaitingIndicator();
            this.waitingIndicator = this.createWaitingIndicator();
            this.waitingIndicator.setAlignmentX(0.0F);
            this.contentPanel.add(this.waitingIndicator);
            this.propagateResize();
         });
      }
   }

   public void markLoopFinished() {
      if (!this.finished) {
         this.finished = true;
         runOnEdt(() -> {
            this.removeWaitingIndicator();
            this.headerLabel.setText("Agent completed (" + this.rows.size() + " tool calls)");
            this.headerLabel.setForeground(ChatStyles.PROGRESS_DONE_FG);
            if (this.expanded) {
               this.toggleExpanded();
            }

            this.propagateResize();
         });
      }
   }

   public void markLoopError(@NotNull String message) {
      this.finished = true;
      runOnEdt(() -> {
         this.removeWaitingIndicator();
         this.headerLabel.setText("Agent error: " + message);
         this.headerLabel.setForeground(new JBColor(15352629, 14300723));
         if (this.expanded) {
            this.toggleExpanded();
         }

         this.propagateResize();
      });
   }

   public int getToolCallCount() {
      return this.rows.size();
   }

   public void addIntermediateText(@NotNull String text) {
      String cleanText = stripMarkdown(text);
      if (!cleanText.isBlank()) {
         JPanel entry;
         if (cleanText.length() <= 200 && !cleanText.contains("\n")) {
            entry = this.createInlineTextEntry(cleanText);
         } else {
            entry = this.createCollapsibleTextEntry(cleanText);
         }

         entry.setAlignmentX(0.0F);
         runOnEdt(() -> {
            this.removeWaitingIndicator();
            this.contentPanel.add(entry);
            this.propagateResize();
         });
      }
   }

   @Nullable
   public ToolCallRow getLastRow() {
      return this.rows.isEmpty() ? null : this.rows.get(this.rows.size() - 1);
   }

   public void dispose() {
      for (ToolCallRow row : this.rows) {
         row.dispose();
      }
   }

   public void removeNotify() {
      this.dispose();
      super.removeNotify();
   }

   @NotNull
   private JPanel createInlineTextEntry(@NotNull String text) {
      JBTextArea textArea = new JBTextArea(text);
      textArea.setFont(Fonts.label(11.0F));
      textArea.setForeground(ChatStyles.PROGRESS_RUNNING_FG);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setEditable(false);
      textArea.setBorder(Borders.empty(3, 4, 3, 4));
      JPanel wrapper = new JPanel(new BorderLayout());
      wrapper.setOpaque(false);
      wrapper.add(textArea, "Center");
      wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      return wrapper;
   }

   @NotNull
   private JPanel createCollapsibleTextEntry(@NotNull String text) {
      String title = extractTitle(text);
      final JBLabel toggle = new JBLabel("▷");
      toggle.setFont(Fonts.label(10.0F));
      toggle.setForeground(ChatStyles.TIMESTAMP_FG);
      toggle.setVerticalAlignment(1);
      toggle.setBorder(Borders.emptyTop(2));
      JBTextArea titleArea = new JBTextArea(title);
      titleArea.setFont(Fonts.label(11.0F));
      titleArea.setForeground(ChatStyles.PROGRESS_RUNNING_FG);
      titleArea.setOpaque(false);
      titleArea.setLineWrap(true);
      titleArea.setWrapStyleWord(true);
      titleArea.setEditable(false);
      titleArea.setBorder(Borders.empty());
      titleArea.setCursor(Cursor.getPredefinedCursor(12));
      JPanel titleRow = new JPanel(new BorderLayout(4, 0));
      titleRow.setOpaque(false);
      titleRow.setCursor(Cursor.getPredefinedCursor(12));
      titleRow.add(toggle, "West");
      titleRow.add(titleArea, "Center");
      titleRow.setAlignmentX(0.0F);
      String remaining = extractBody(text);
      String bodyText = remaining.length() > 2000 ? remaining.substring(0, 1997) + "…" : remaining;
      final JBTextArea bodyArea = new JBTextArea(bodyText);
      bodyArea.setFont(Fonts.label(11.0F));
      bodyArea.setForeground(ChatStyles.TIMESTAMP_FG);
      bodyArea.setOpaque(false);
      bodyArea.setLineWrap(true);
      bodyArea.setWrapStyleWord(true);
      bodyArea.setEditable(false);
      bodyArea.setBorder(Borders.empty(2, 18, 4, 4));
      bodyArea.setVisible(false);
      bodyArea.setAlignmentX(0.0F);
      JPanel wrapper = new JPanel();
      wrapper.setLayout(new BoxLayout(wrapper, 1));
      wrapper.setOpaque(false);
      wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      wrapper.add(titleRow);
      wrapper.add(bodyArea);
      MouseAdapter clickAdapter = new MouseAdapter() {
         private boolean open = false;

         @Override
         public void mouseClicked(MouseEvent e) {
            this.open = !this.open;
            bodyArea.setVisible(this.open);
            toggle.setText(this.open ? "▽" : "▷");
            AgentProgressPanel.this.propagateResize();
         }
      };
      titleRow.addMouseListener(clickAdapter);
      titleArea.addMouseListener(clickAdapter);
      return wrapper;
   }

   @NotNull
   static String stripMarkdown(@NotNull String text) {
      String result = text;
      result = result.replaceAll("(?m)^#{1,6}\\s+", "");
      result = result.replaceAll("\\*{1,3}([^*]+)\\*{1,3}", "$1");
      result = result.replaceAll("_{1,3}([^_]+)_{1,3}", "$1");
      result = result.replaceAll("`([^`]+)`", "$1");
      result = result.replaceAll("(?m)^(\\s*)[*\\-+]\\s+", "$1• ");
      result = result.replaceAll("(?m)^(\\s*)(\\d+)\\.\\s+", "$1$2) ");
      result = result.replaceAll("\n{3,}", "\n\n");
      return result.strip();
   }

   @NotNull
   static String extractTitle(@NotNull String text) {
      String trimmed = text.strip();
      int nlIdx = trimmed.indexOf(10);
      String firstLine = nlIdx > 0 ? trimmed.substring(0, nlIdx).strip() : trimmed;
      firstLine = firstLine.replaceAll("^•\\s*", "");
      firstLine = firstLine.strip();
      return firstLine.isBlank() ? "AI reasoning" : firstLine;
   }

   @NotNull
   static String extractBody(@NotNull String text) {
      String trimmed = text.strip();
      int nlIdx = trimmed.indexOf(10);
      return nlIdx <= 0 ? trimmed : trimmed.substring(nlIdx + 1).strip();
   }

   private void removeWaitingIndicator() {
      if (this.waitingIndicator != null) {
         this.contentPanel.remove(this.waitingIndicator);
         this.waitingIndicator = null;
      }
   }

   @NotNull
   private JPanel createWaitingIndicator() {
      JPanel row = new JPanel(new FlowLayout(0, 6, 2));
      row.setOpaque(false);
      row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
      JBLabel spinner = new JBLabel("⏳");
      spinner.setFont(Fonts.label(11.0F));
      spinner.setForeground(WAITING_FG);
      JBLabel label = new JBLabel("Sending request to AI…");
      label.setFont(Fonts.label(11.0F).asItalic());
      label.setForeground(WAITING_FG);
      row.add(spinner);
      row.add(label);
      return row;
   }

   private void toggleExpanded() {
      this.expanded = !this.expanded;
      this.contentPanel.setVisible(this.expanded);
      this.toggleLabel.setText(this.expanded ? "▼" : "▶");
      this.revalidate();
      this.repaint();
      this.propagateResize();
   }

   private void propagateResize() {
      this.revalidate();
      this.repaint();

      for (Container c = this.getParent(); c != null; c = c.getParent()) {
         if (c instanceof JComponent) {
            ((JComponent)c).revalidate();
         }

         c.repaint();
      }
   }

   private void updateHeader() {
      if (!this.finished) {
         int total = this.rows.size();
         this.headerLabel.setText("Agent working… (" + this.completedCount + "/" + total + " tools done)");
      }
   }

   private static void runOnEdt(@NotNull Runnable action) {
      if (SwingUtilities.isEventDispatchThread()) {
         action.run();
      } else {
         SwingUtilities.invokeLater(action);
      }
   }
}
