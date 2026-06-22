package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public final class PlanConfirmationCard extends JBPanel<PlanConfirmationCard> {
   private static final JBColor BG = new JBColor(15267305, 2767402);
   private static final JBColor BORDER_COLOR = new JBColor(10868391, 3824186);
   private static final JBColor DESC_FG = new JBColor(2369839, 13226457);
   private static final JBColor CONTINUE_BG = new JBColor(2991182, 2328118);
   private static final JBColor CONTINUE_HOVER = new JBColor(2197560, 3055683);
   private static final JBColor CANCEL_BG = new JBColor(7239553, 4738904);
   private static final JBColor CANCEL_HOVER = new JBColor(5726314, 4014408);
   private static final int BTN_ARC = 8;
   private static final int BTN_HEIGHT = JBUI.scale(28);
   private final JButton continueButton;
   private final JButton cancelButton;
   private Runnable onProceed;
   private Runnable onCancel;

   public PlanConfirmationCard() {
      super(new BorderLayout(8, 0));
      this.setBackground(BG);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), Borders.empty(8, 12)));
      JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
      leftPanel.setOpaque(false);
      JBLabel iconLabel = new JBLabel(Actions.Execute);
      iconLabel.setPreferredSize(JBUI.size(16, 16));
      JBLabel descLabel = new JBLabel("Plan ready. Click Continue to execute, or Cancel to revise.");
      descLabel.setFont(Fonts.label(12.0F));
      descLabel.setForeground(DESC_FG);
      leftPanel.add(iconLabel, "West");
      leftPanel.add(descLabel, "Center");
      JPanel btnPanel = new JPanel(new FlowLayout(2, 6, 0));
      btnPanel.setOpaque(false);
      this.continueButton = makeFilledButton("Continue", CONTINUE_BG, CONTINUE_HOVER);
      this.continueButton.setEnabled(false);
      this.continueButton.addActionListener(e -> {
         this.setButtonsEnabled(false);
         if (this.onProceed != null) {
            this.onProceed.run();
         }
      });
      btnPanel.add(this.continueButton);
      this.cancelButton = makeFilledButton("Cancel", CANCEL_BG, CANCEL_HOVER);
      this.cancelButton.setEnabled(false);
      this.cancelButton.addActionListener(e -> {
         this.setButtonsEnabled(false);
         if (this.onCancel != null) {
            this.onCancel.run();
         }
      });
      btnPanel.add(this.cancelButton);
      this.add(leftPanel, "Center");
      this.add(btnPanel, "East");
   }

   public void setOnProceed(@NotNull Runnable action) {
      this.onProceed = action;
      this.continueButton.setEnabled(true);
   }

   public void setOnCancel(@NotNull Runnable action) {
      this.onCancel = action;
      this.cancelButton.setEnabled(true);
   }

   public void setButtonsEnabled(boolean enabled) {
      this.continueButton.setEnabled(enabled);
      this.cancelButton.setEnabled(enabled);
   }

   public void markExecuting() {
      this.continueButton.setText("Executing...");
      this.setButtonsEnabled(false);
   }

   public void markCancelled() {
      this.cancelButton.setText("Cancelled");
      this.setButtonsEnabled(false);
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
            return new Dimension(d.width + JBUI.scale(16), PlanConfirmationCard.BTN_HEIGHT);
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
