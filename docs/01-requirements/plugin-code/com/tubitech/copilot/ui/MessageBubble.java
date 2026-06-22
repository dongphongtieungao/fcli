package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.ui.renderer.MarkdownRenderer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageBubble extends JBPanel<MessageBubble> {
   private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
   private final MessageBubble.Role role;
   private final StringBuilder contentBuilder;
   private final JBTextArea textArea;
   private final JBPanel<?> attachmentRow;
   @Nullable
   private AgentProgressPanel embeddedProgress;
   private final JBPanel<?> contentColumn;
   private int bubbleMaxWidth = 400;
   @Nullable
   private ComponentListener viewportResizeListener;
   @Nullable
   private JViewport trackedViewport;
   private final JPanel bubbleContent;

   public MessageBubble(@NotNull MessageBubble.Role role, @NotNull String initialContent) {
      super(new BorderLayout());
      this.role = role;
      this.contentBuilder = new StringBuilder(initialContent);
      this.setOpaque(false);
      this.setAlignmentX(0.0F);
      this.contentColumn = new JBPanel<JBPanel>() {
         public Dimension getPreferredSize() {
            int cap = Math.max(MessageBubble.this.bubbleMaxWidth, 1);
            if (this.getWidth() == 0) {
               this.setSize(cap, 16383);
            }

            Dimension d = super.getPreferredSize();
            int w = MessageBubble.this.embeddedProgress != null ? cap : Math.min(d.width, cap);
            return new Dimension(w, Math.max(d.height, 1));
         }

         public Dimension getMaximumSize() {
            return new Dimension(Math.max(MessageBubble.this.bubbleMaxWidth, 1), Integer.MAX_VALUE);
         }
      };
      this.contentColumn.setLayout(new BoxLayout(this.contentColumn, 1));
      this.contentColumn.setOpaque(false);
      JPanel headerRow = this.buildHeaderRow();
      headerRow.setAlignmentX(0.0F);
      this.attachmentRow = new JBPanel(new FlowLayout(0, 4, 0));
      this.attachmentRow.setOpaque(false);
      this.attachmentRow.setVisible(false);
      this.attachmentRow.setAlignmentX(0.0F);
      this.textArea = new JBTextArea(initialContent) {
         public Dimension getPreferredSize() {
            int w = this.getWidth() > 0 ? this.getWidth() : (MessageBubble.this.bubbleMaxWidth > 0 ? MessageBubble.this.bubbleMaxWidth : 400);
            this.setSize(w, 16383);
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, Math.max(d.height, 1));
         }
      };
      this.textArea.setEditable(false);
      this.textArea.setLineWrap(true);
      this.textArea.setWrapStyleWord(true);
      this.textArea.setOpaque(false);
      this.textArea.setFont(Fonts.label(13.0F));
      this.textArea.setBorder(null);
      if (role == MessageBubble.Role.USER) {
         this.textArea.setForeground(ChatStyles.BUBBLE_USER_FG);
         RoundedBorderPanel userBubble = new RoundedBorderPanel(ChatStyles.BUBBLE_USER_BG, 14, new BorderLayout());
         userBubble.setBorder(Borders.empty(10, 14));
         userBubble.add(this.textArea, "Center");
         this.bubbleContent = userBubble;
      } else {
         JBPanel<?> aiBubble = new JBPanel(new BorderLayout());
         aiBubble.setOpaque(false);
         aiBubble.add(this.textArea, "Center");
         this.bubbleContent = aiBubble;
      }

      this.bubbleContent.setAlignmentX(0.0F);
      this.contentColumn.add(headerRow);
      this.contentColumn.add(Box.createRigidArea(new Dimension(0, 2)));
      this.contentColumn.add(this.attachmentRow);
      this.contentColumn.add(this.bubbleContent);
      JPanel outerRow = new JPanel();
      outerRow.setLayout(new BoxLayout(outerRow, 0));
      outerRow.setOpaque(false);
      if (role == MessageBubble.Role.USER) {
         outerRow.add(Box.createHorizontalGlue());
         outerRow.add(this.contentColumn);
      } else {
         outerRow.add(this.contentColumn);
         outerRow.add(Box.createHorizontalGlue());
      }

      this.add(outerRow, "Center");
   }

   public void addNotify() {
      super.addNotify();
      Container c = this.getParent();

      while (c != null && !(c instanceof JViewport)) {
         c = c.getParent();
      }

      if (c instanceof JViewport vp) {
         this.viewportResizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
               MessageBubble.this.updateMaxWidth(vp.getWidth());
            }
         };
         this.trackedViewport = vp;
         vp.addComponentListener(this.viewportResizeListener);
         this.updateMaxWidth(vp.getWidth());
      }
   }

   public void removeNotify() {
      if (this.trackedViewport != null && this.viewportResizeListener != null) {
         this.trackedViewport.removeComponentListener(this.viewportResizeListener);
         this.trackedViewport = null;
         this.viewportResizeListener = null;
      }

      super.removeNotify();
   }

   private void updateMaxWidth(int viewportWidth) {
      float ratio = this.role == MessageBubble.Role.USER ? 0.75F : 0.98F;
      this.bubbleMaxWidth = Math.max(1, (int)(viewportWidth * ratio));
      this.revalidate();
      this.repaint();
   }

   public void appendToken(@NotNull String delta) {
      assert EventQueue.isDispatchThread() : "appendToken must be called on the EDT";
      this.contentBuilder.append(delta);
      this.textArea.setText(this.contentBuilder.toString());
      this.revalidate();
   }

   public void renderMarkdown(@NotNull String fullContent, @Nullable Project project) {
      if (this.role == MessageBubble.Role.ASSISTANT) {
         this.contentBuilder.setLength(0);
         this.contentBuilder.append(fullContent);
         if (this.embeddedProgress != null) {
            JSeparator sep = new JSeparator(0);
            sep.setAlignmentX(0.0F);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setForeground(new JBColor(13686750, 4013891));
            this.bubbleContent.add(Box.createRigidArea(new Dimension(0, 4)));
            this.bubbleContent.add(sep);
            this.bubbleContent.add(Box.createRigidArea(new Dimension(0, 4)));
            MarkdownRenderer renderer = new MarkdownRenderer(fullContent, project);
            renderer.setAlignmentX(0.0F);
            this.bubbleContent.add(renderer);
         } else {
            this.bubbleContent.remove(this.textArea);
            MarkdownRenderer renderer = new MarkdownRenderer(fullContent, project);
            this.bubbleContent.add(renderer, "Center");
         }

         this.bubbleContent.revalidate();
         this.bubbleContent.repaint();
         this.revalidate();
         this.repaint();
         SwingUtilities.invokeLater(() -> {
            this.bubbleContent.revalidate();
            this.bubbleContent.repaint();
            this.revalidate();
            this.repaint();
         });
      }
   }

   public void finalizeWithMarkdown(@NotNull String fullContent, @Nullable Project project) {
      this.renderMarkdown(fullContent, project);
   }

   public void setContent(@NotNull String fullText) {
      this.contentBuilder.setLength(0);
      this.contentBuilder.append(fullText);
      this.textArea.setText(fullText);
      this.revalidate();
   }

   public void setAttachmentNames(@NotNull List<String> names) {
      this.attachmentRow.removeAll();
      if (!names.isEmpty()) {
         for (String name : names) {
            JBLabel badge = new JBLabel("\ud83d\udcce " + name);
            badge.setFont(Fonts.label(11.0F));
            badge.setForeground(this.role == MessageBubble.Role.USER ? ChatStyles.BUBBLE_USER_FG : ChatStyles.TIMESTAMP_FG);
            this.attachmentRow.add(badge);
         }

         this.attachmentRow.setVisible(true);
      } else {
         this.attachmentRow.setVisible(false);
      }

      this.revalidate();
   }

   public void embedAgentProgress(@NotNull AgentProgressPanel panel) {
      if (this.role == MessageBubble.Role.ASSISTANT) {
         this.embeddedProgress = panel;
         panel.setAlignmentX(0.0F);
         this.bubbleContent.remove(this.textArea);
         this.bubbleContent.setLayout(new BoxLayout(this.bubbleContent, 1));
         this.bubbleContent.add(panel);
         this.bubbleContent.revalidate();
         this.bubbleContent.repaint();
      }
   }

   @Nullable
   public AgentProgressPanel getEmbeddedProgress() {
      return this.embeddedProgress;
   }

   @NotNull
   public String getContent() {
      return this.contentBuilder.toString();
   }

   @NotNull
   public MessageBubble.Role getRole() {
      return this.role;
   }

   @NotNull
   private JPanel buildHeaderRow() {
      JPanel row = new JPanel(new BorderLayout(4, 0)) {
         @Override
         public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
         }
      };
      row.setOpaque(false);
      JPanel leftPanel = new JPanel(new FlowLayout(0, 4, 0));
      leftPanel.setOpaque(false);
      JBLabel nameLabel = new JBLabel(this.role == MessageBubble.Role.USER ? "You" : "Tubi Copilot");
      nameLabel.setFont(Fonts.label(11.0F).asBold());
      nameLabel.setForeground(this.role == MessageBubble.Role.USER ? ChatStyles.ROLE_USER_FG : ChatStyles.ROLE_AI_FG);
      leftPanel.add(nameLabel);
      JPanel rightPanel = new JPanel(new FlowLayout(2, 4, 0));
      rightPanel.setOpaque(false);
      if (this.role == MessageBubble.Role.USER) {
         JButton copyButton = new JButton(Actions.Copy);
         copyButton.setBorderPainted(false);
         copyButton.setContentAreaFilled(false);
         copyButton.setFocusPainted(false);
         copyButton.setMargin(JBUI.emptyInsets());
         copyButton.setToolTipText("Copy message");
         copyButton.setCursor(Cursor.getPredefinedCursor(12));
         Dimension copySize = JBUI.size(18, 18);
         copyButton.setPreferredSize(copySize);
         copyButton.setMinimumSize(copySize);
         copyButton.setMaximumSize(copySize);
         copyButton.addActionListener(e -> this.copyContentToClipboard());
         rightPanel.add(copyButton);
      }

      JBLabel timeLabel = new JBLabel(LocalTime.now().format(TIME_FMT));
      timeLabel.setFont(Fonts.label(11.0F));
      timeLabel.setForeground(ChatStyles.TIMESTAMP_FG);
      rightPanel.add(timeLabel);
      row.add(leftPanel, "West");
      row.add(rightPanel, "East");
      return row;
   }

   private void copyContentToClipboard() {
      StringSelection selection = new StringSelection(this.contentBuilder.toString());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
   }

   public enum Role {
      USER,
      ASSISTANT;
   }
}
