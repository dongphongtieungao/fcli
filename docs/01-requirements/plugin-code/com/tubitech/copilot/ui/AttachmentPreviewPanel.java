package com.tubitech.copilot.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.api.model.FileReference;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import org.jetbrains.annotations.NotNull;

public class AttachmentPreviewPanel extends JBPanel<AttachmentPreviewPanel> {
   private static final JBColor UPLOADING_COLOR = new JBColor(15267069, 2767434);
   private static final JBColor READY_COLOR = new JBColor(15398890, 2771498);
   private static final JBColor ERROR_COLOR = new JBColor(16773360, 4860458);
   private static final JBColor CODE_CONTEXT_COLOR = new JBColor(16776166, 4864512);
   private static final JBColor FOLDER_CONTEXT_COLOR = new JBColor(15266046, 1714746);
   private static final JBColor IMAGE_COLOR = new JBColor(15984893, 3811914);
   private final Map<String, AttachmentPreviewPanel.BadgePanel> badges = new LinkedHashMap<>();

   public AttachmentPreviewPanel() {
      this.setLayout(new BoxLayout(this, 1));
      this.setBorder(Borders.empty(2, 6));
   }

   public void showUploading(@NotNull String filename) {
      if (!this.badges.containsKey(filename)) {
         AttachmentPreviewPanel.BadgePanel badge = new AttachmentPreviewPanel.BadgePanel(filename);
         this.badges.put(filename, badge);
         this.add(badge);
         this.setVisible(true);
         this.revalidate();
         this.repaint();
      }
   }

   public void updateProgress(@NotNull String filename, int percentage) {
      AttachmentPreviewPanel.BadgePanel badge = this.badges.get(filename);
      if (badge != null) {
         badge.updateProgress(percentage);
      }
   }

   public void showReady(@NotNull FileReference fileReference) {
      AttachmentPreviewPanel.BadgePanel badge = this.badges.get(fileReference.getName());
      if (badge != null) {
         badge.transitionToReady();
      }
   }

   public void showError(@NotNull String filename, @NotNull String reason) {
      AttachmentPreviewPanel.BadgePanel badge = this.badges.get(filename);
      if (badge != null) {
         badge.transitionToError(reason);
      }
   }

   public void showCodeContextBadge(@NotNull String filename, @NotNull String language) {
      String key = "context:" + filename;
      if (!this.badges.containsKey(key)) {
         AttachmentPreviewPanel.BadgePanel badge = new AttachmentPreviewPanel.BadgePanel(key);
         badge.transitionToCodeContext(filename, language);
         this.badges.put(key, badge);
         this.add(badge);
         this.setVisible(true);
         this.revalidate();
         this.repaint();
      }
   }

   public void showFolderContextBadge(@NotNull String folderName, int fileCount, boolean truncated) {
      String key = "folder:" + folderName;
      if (!this.badges.containsKey(key)) {
         AttachmentPreviewPanel.BadgePanel badge = new AttachmentPreviewPanel.BadgePanel(key);
         String countLabel = fileCount + (truncated ? "+" : "") + " files";
         badge.transitionToFolderContext(folderName, countLabel);
         this.badges.put(key, badge);
         this.add(badge);
         this.setVisible(true);
         this.revalidate();
         this.repaint();
      }
   }

   public void showImageBadge(@NotNull String filename) {
      String key = "image:" + filename;
      if (!this.badges.containsKey(key)) {
         AttachmentPreviewPanel.BadgePanel badge = new AttachmentPreviewPanel.BadgePanel(key);
         badge.transitionToImage(filename);
         this.badges.put(key, badge);
         this.add(badge);
         this.setVisible(true);
         this.revalidate();
         this.repaint();
      }
   }

   public void removeBadge(@NotNull String filename) {
      AttachmentPreviewPanel.BadgePanel badge = this.badges.remove(filename);
      if (badge != null) {
         this.remove(badge);
         this.revalidate();
         this.repaint();
      }

      if (this.badges.isEmpty()) {
         this.setVisible(false);
      }
   }

   public void clear() {
      this.badges.clear();
      this.removeAll();
      this.revalidate();
      this.repaint();
      this.setVisible(false);
   }

   private enum AttachmentState {
      UPLOADING,
      READY,
      ERROR;
   }

   private final class BadgePanel extends JBPanel<AttachmentPreviewPanel.BadgePanel> {
      private final String filename;
      private AttachmentPreviewPanel.AttachmentState state;
      private final JBLabel statusLabel;
      private final JProgressBar progressBar;
      private final JButton dismissButton;
      private final JButton retryButton;

      BadgePanel(@NotNull String filename) {
         super(new BorderLayout(6, 2));
         this.filename = filename;
         this.state = AttachmentPreviewPanel.AttachmentState.UPLOADING;
         this.statusLabel = new JBLabel("\ud83d\udcc4 " + filename + "  ⏫ Uploading…");
         this.statusLabel.setFont(Fonts.label());
         this.progressBar = new JProgressBar(0, 100);
         this.progressBar.setIndeterminate(true);
         this.progressBar.setPreferredSize(new Dimension(0, JBUI.scale(4)));
         this.progressBar.setBorderPainted(false);
         this.dismissButton = new JButton("×");
         this.dismissButton.setMargin(JBUI.insets(0, 4));
         this.dismissButton.setToolTipText("Remove attachment");
         this.dismissButton.setVisible(false);
         this.dismissButton.addActionListener(e -> AttachmentPreviewPanel.this.removeBadge(filename));
         this.retryButton = new JButton("Retry");
         this.retryButton.setMargin(JBUI.insets(0, 4));
         this.retryButton.setVisible(false);
         this.retryButton.addActionListener(e -> AttachmentPreviewPanel.this.removeBadge(filename));
         JBPanel<?> buttonPanel = new JBPanel(new FlowLayout(2, 2, 0));
         buttonPanel.setOpaque(false);
         buttonPanel.add(this.retryButton);
         buttonPanel.add(this.dismissButton);
         this.setBackground(AttachmentPreviewPanel.UPLOADING_COLOR);
         this.setBorder(Borders.empty(4, 6));
         this.setMaximumSize(new Dimension(Integer.MAX_VALUE, JBUI.scale(40)));
         this.setAlignmentX(0.0F);
         this.add(this.statusLabel, "Center");
         this.add(buttonPanel, "East");
         this.add(this.progressBar, "South");
      }

      void transitionToReady() {
         this.state = AttachmentPreviewPanel.AttachmentState.READY;
         this.statusLabel.setText("\ud83d\udcc4 " + this.filename + "  ✅ Uploaded");
         this.progressBar.setVisible(false);
         this.retryButton.setVisible(false);
         this.dismissButton.setVisible(true);
         this.setBackground(AttachmentPreviewPanel.READY_COLOR);
         this.revalidate();
         this.repaint();
      }

      void transitionToError(@NotNull String reason) {
         this.state = AttachmentPreviewPanel.AttachmentState.ERROR;
         this.statusLabel.setText("\ud83d\udcc4 " + this.filename + "  ⚠️ " + reason);
         this.progressBar.setVisible(false);
         this.retryButton.setVisible(true);
         this.dismissButton.setVisible(true);
         this.setBackground(AttachmentPreviewPanel.ERROR_COLOR);
         this.revalidate();
         this.repaint();
      }

      void transitionToCodeContext(@NotNull String displayFilename, @NotNull String language) {
         this.state = AttachmentPreviewPanel.AttachmentState.READY;
         this.statusLabel.setText("\ud83d\udcce " + displayFilename + " (" + language + ")  \ud83d\udcdd Code Context");
         this.progressBar.setVisible(false);
         this.retryButton.setVisible(false);
         this.dismissButton.setVisible(true);
         this.setBackground(AttachmentPreviewPanel.CODE_CONTEXT_COLOR);
         this.revalidate();
         this.repaint();
      }

      void transitionToFolderContext(@NotNull String folderName, @NotNull String countLabel) {
         this.state = AttachmentPreviewPanel.AttachmentState.READY;
         this.statusLabel.setText("\ud83d\udcc2 " + folderName + "/ (" + countLabel + ")  \ud83d\udd0d Folder Context");
         this.progressBar.setVisible(false);
         this.retryButton.setVisible(false);
         this.dismissButton.setVisible(true);
         this.setBackground(AttachmentPreviewPanel.FOLDER_CONTEXT_COLOR);
         this.revalidate();
         this.repaint();
      }

      void transitionToImage(@NotNull String displayFilename) {
         this.state = AttachmentPreviewPanel.AttachmentState.READY;
         this.statusLabel.setText("\ud83d\uddbc️ " + displayFilename + "  ✅ Attached");
         this.progressBar.setVisible(false);
         this.retryButton.setVisible(false);
         this.dismissButton.setVisible(true);
         this.setBackground(AttachmentPreviewPanel.IMAGE_COLOR);
         this.revalidate();
         this.repaint();
      }

      void updateProgress(int percentage) {
         if (this.state == AttachmentPreviewPanel.AttachmentState.UPLOADING) {
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(percentage);
            this.statusLabel.setText("\ud83d\udcc4 " + this.filename + "  ⏫ " + percentage + "%");
         }
      }
   }
}
