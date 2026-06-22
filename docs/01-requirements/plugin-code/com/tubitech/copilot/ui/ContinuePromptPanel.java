package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.General;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
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
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public final class ContinuePromptPanel extends JBPanel<ContinuePromptPanel> {
   private static final JBColor BG = new JBColor(16775393, 3354666);
   private static final JBColor BORDER_COLOR = new JBColor(16769154, 5918208);
   private static final JBColor DESC_FG = new JBColor(2369839, 13226457);
   private static final JBColor CONTINUE_BG = new JBColor(2991182, 2328118);
   private static final JBColor CONTINUE_HOVER = new JBColor(2197560, 3055683);
   private static final JBColor STOP_BG = new JBColor(7239553, 4738904);
   private static final JBColor STOP_HOVER = new JBColor(5726314, 4014408);
   private static final int BTN_ARC = 8;
   private static final int BTN_HEIGHT = JBUI.scale(28);
   private final CompletableFuture<Boolean> decision = new CompletableFuture<>();

   public ContinuePromptPanel(int iterationsUsed) {
      super(new BorderLayout(8, 0));
      this.setBackground(BG);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), Borders.empty(8, 12)));
      JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
      leftPanel.setOpaque(false);
      JBLabel iconLabel = new JBLabel(General.Warning);
      iconLabel.setPreferredSize(JBUI.size(16, 16));
      JBLabel descLabel = new JBLabel("Agent reached iteration limit (" + iterationsUsed + " iterations). Continue working?");
      descLabel.setFont(Fonts.label(12.0F));
      descLabel.setForeground(DESC_FG);
      leftPanel.add(iconLabel, "West");
      leftPanel.add(descLabel, "Center");
      JPanel btnPanel = new JPanel(new FlowLayout(2, 6, 0));
      btnPanel.setOpaque(false);
      JButton continueBtn = makeFilledButton("Continue", CONTINUE_BG, CONTINUE_HOVER);
      continueBtn.addActionListener(e -> this.resolve(true));
      btnPanel.add(continueBtn);
      JButton stopBtn = makeFilledButton("Stop", STOP_BG, STOP_HOVER);
      stopBtn.addActionListener(e -> this.resolve(false));
      btnPanel.add(stopBtn);
      this.add(leftPanel, "Center");
      this.add(btnPanel, "East");
   }

   @NotNull
   public CompletableFuture<Boolean> getDecision() {
      return this.decision;
   }

   private void resolve(boolean shouldContinue) {
      if (!this.decision.isDone()) {
         this.decision.complete(shouldContinue);
      }
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
            return new Dimension(d.width + JBUI.scale(16), ContinuePromptPanel.BTN_HEIGHT);
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
}
