package com.tubitech.copilot.ui;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.agent.tools.ToolCall;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D.Float;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ToolApprovalPanel extends JBPanel<ToolApprovalPanel> {
   private static final JBColor BG = new JBColor(15791352, 2829616);
   private static final JBColor BORDER_COLOR = new JBColor(13686750, 4013891);
   private static final JBColor DESC_FG = new JBColor(2369839, 13226457);
   private static final JBColor ALLOW_BG = new JBColor(2991182, 2328118);
   private static final JBColor ALLOW_HOVER = new JBColor(2197560, 3055683);
   private static final JBColor DENY_BG = new JBColor(13574702, 14300723);
   private static final JBColor DENY_HOVER = new JBColor(12000284, 16273737);
   private static final JBColor OUTLINE_BG = new JBColor(16777215, 3159613);
   private static final JBColor OUTLINE_HOVER = new JBColor(15987958, 4014408);
   private static final JBColor OUTLINE_FG = new JBColor(2369839, 13226457);
   private static final JBColor OUTLINE_BORDER = new JBColor(13686750, 4738904);
   private static final int BTN_ARC = 8;
   private static final int BTN_HEIGHT = JBUI.scale(28);
   private final CompletableFuture<Boolean> decision = new CompletableFuture<>();

   public ToolApprovalPanel(@NotNull ToolCall toolCall, @NotNull Project project) {
      super(new BorderLayout(8, 0));
      this.setBackground(BG);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), Borders.empty(8, 12)));
      String toolName = toolCall.getName();
      Map<String, String> args = toolCall.getArguments();
      JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
      leftPanel.setOpaque(false);
      JBLabel iconLabel;
      String descText;
      if ("edit_file".equals(toolName)) {
         iconLabel = new JBLabel(Actions.Edit);
         String path = args.getOrDefault("path", "unknown");
         String newText = args.getOrDefault("new_text", "");
         int lines = newText.isEmpty() ? 0 : newText.split("\n", -1).length;
         descText = "edit_file  " + truncatePath(path) + "  (" + lines + " lines changed)";
      } else if ("create_file".equals(toolName)) {
         iconLabel = new JBLabel(Actions.Edit);
         String path = args.getOrDefault("path", "unknown");
         String content = args.getOrDefault("content", "");
         int lines = content.isEmpty() ? 0 : content.split("\n", -1).length;
         descText = "create_file  " + truncatePath(path) + "  (" + lines + " lines)";
      } else if ("delete_file".equals(toolName)) {
         iconLabel = new JBLabel(General.Remove);
         String path = args.getOrDefault("path", "unknown");
         descText = "delete_file  " + truncatePath(path);
      } else if ("run_command".equals(toolName)) {
         iconLabel = new JBLabel(Actions.Execute);
         String cmd = args.getOrDefault("command", "unknown");
         descText = "run_command  " + (cmd.length() > 50 ? cmd.substring(0, 47) + "..." : cmd);
      } else {
         iconLabel = new JBLabel(General.Information);
         descText = toolName;
      }

      JBLabel descLabel = new JBLabel(descText) {
         public Dimension getMinimumSize() {
            return new Dimension(0, super.getMinimumSize().height);
         }
      };
      descLabel.setFont(Fonts.label(12.0F));
      descLabel.setForeground(DESC_FG);
      iconLabel.setPreferredSize(JBUI.size(16, 16));
      leftPanel.add(iconLabel, "West");
      leftPanel.add(descLabel, "Center");
      JPanel btnPanel = new JPanel(new FlowLayout(2, 6, 0));
      btnPanel.setOpaque(false);
      if ("edit_file".equals(toolName) || "create_file".equals(toolName)) {
         JButton viewBtn = makeOutlineButton("View diff", Actions.Diff);
         viewBtn.addActionListener(e -> showDiff(toolCall, project));
         btnPanel.add(viewBtn);
      }

      JButton allowBtn = makeFilledButton("Allow", ALLOW_BG, ALLOW_HOVER);
      allowBtn.addActionListener(e -> this.resolve(true));
      btnPanel.add(allowBtn);
      JButton denyBtn = makeFilledButton("Deny", DENY_BG, DENY_HOVER);
      denyBtn.addActionListener(e -> this.resolve(false));
      btnPanel.add(denyBtn);
      this.add(leftPanel, "Center");
      this.add(btnPanel, "East");
   }

   @NotNull
   public CompletableFuture<Boolean> getDecision() {
      return this.decision;
   }

   private void resolve(boolean approved) {
      if (!this.decision.isDone()) {
         this.decision.complete(approved);
      }
   }

   @NotNull
   private static String truncatePath(@NotNull String path) {
      if (path.length() <= 35) {
         return path;
      } else {
         int lastSlash = Math.max(path.lastIndexOf(47), path.lastIndexOf(92));
         if (lastSlash >= 0) {
            String fileName = path.substring(lastSlash + 1);
            String dir = path.substring(0, lastSlash);
            return dir.length() > 15 ? "..." + dir.substring(dir.length() - 12) + "/" + fileName : path;
         } else {
            return "..." + path.substring(path.length() - 32);
         }
      }
   }

   private static void showDiff(@NotNull ToolCall call, @NotNull Project project) {
      String toolName = call.getName();
      String path = call.getArguments().getOrDefault("path", "");
      String basePath = project.getBasePath();
      String oldContent = "";
      if (basePath != null && !path.isBlank()) {
         String fullPath = basePath + "/" + path.replace('\\', '/');
         VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(fullPath);
         if (vf != null) {
            try {
               oldContent = new String(vf.contentsToByteArray(), StandardCharsets.UTF_8);
            } catch (Exception var11) {
            }
         }
      }

      String newContent;
      if ("edit_file".equals(toolName)) {
         String oldText = call.getArguments().getOrDefault("old_text", "");
         String newText = call.getArguments().getOrDefault("new_text", "");
         int idx = oldContent.indexOf(oldText);
         if (idx >= 0) {
            newContent = oldContent.substring(0, idx) + newText + oldContent.substring(idx + oldText.length());
         } else {
            newContent = oldContent + "\n// [old_text not found — showing new_text]\n" + newText;
         }
      } else {
         newContent = call.getArguments().getOrDefault("content", "");
      }

      DiffContentFactory factory = DiffContentFactory.getInstance();
      DocumentContent oldDiff = factory.create(project, oldContent);
      DocumentContent newDiff = factory.create(project, newContent);
      SimpleDiffRequest request = new SimpleDiffRequest("Tubi Copilot — " + path, oldDiff, newDiff, "Current", "Proposed");
      DiffManager.getInstance().showDiff(project, request);
   }

   @NotNull
   private static JButton makeFilledButton(@NotNull String label, @NotNull final Color bg, @NotNull final Color hoverBg) {
      JButton btn = new JButton(label) {
         private boolean hovered = false;

         {
            this.addMouseListener(new MouseAdapter() {
               @Override
               public void mouseEntered(MouseEvent e) {
                  hovered = true;
                  repaint();
               }

               @Override
               public void mouseExited(MouseEvent e) {
                  hovered = false;
                  repaint();
               }
            });
         }

         @Override
         protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.hovered ? hoverBg : bg);
            g2.fill(new Float(0.0F, 0.0F, this.getWidth(), this.getHeight(), 8.0F, 8.0F));
            g2.dispose();
            super.paintComponent(g);
         }

         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + JBUI.scale(16), ToolApprovalPanel.BTN_HEIGHT);
         }
      };
      btn.setFont(Fonts.label(12.0F).asBold());
      btn.setForeground(Color.WHITE);
      btn.setFocusPainted(false);
      btn.setBorderPainted(false);
      btn.setContentAreaFilled(false);
      btn.setOpaque(false);
      btn.setCursor(Cursor.getPredefinedCursor(12));
      btn.setMargin(JBUI.insets(0, 12));
      return btn;
   }

   @NotNull
   private static JButton makeOutlineButton(@NotNull String label, @Nullable Icon icon) {
      JButton btn = new JButton(label, icon) {
         private boolean hovered = false;

         {
            this.addMouseListener(new MouseAdapter() {
               @Override
               public void mouseEntered(MouseEvent e) {
                  hovered = true;
                  repaint();
               }

               @Override
               public void mouseExited(MouseEvent e) {
                  hovered = false;
                  repaint();
               }
            });
         }

         @Override
         protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.hovered ? ToolApprovalPanel.OUTLINE_HOVER : ToolApprovalPanel.OUTLINE_BG);
            g2.fill(new Float(0.0F, 0.0F, this.getWidth(), this.getHeight(), 8.0F, 8.0F));
            g2.setColor(ToolApprovalPanel.OUTLINE_BORDER);
            g2.draw(new Float(0.5F, 0.5F, this.getWidth() - 1, this.getHeight() - 1, 8.0F, 8.0F));
            g2.dispose();
            super.paintComponent(g);
         }

         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + JBUI.scale(16), ToolApprovalPanel.BTN_HEIGHT);
         }
      };
      btn.setFont(Fonts.label(12.0F));
      btn.setForeground(OUTLINE_FG);
      btn.setFocusPainted(false);
      btn.setBorderPainted(false);
      btn.setContentAreaFilled(false);
      btn.setOpaque(false);
      btn.setCursor(Cursor.getPredefinedCursor(12));
      btn.setMargin(JBUI.insets(0, 12));
      btn.setIconTextGap(JBUI.scale(4));
      return btn;
   }
}
