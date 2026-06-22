package com.tubitech.copilot.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;

public final class ErrorBubble extends JBPanel<ErrorBubble> {
   public ErrorBubble(@NotNull String errorMessage) {
      super(new BorderLayout(8, 0));
      this.setOpaque(true);
      this.setBackground(new JBColor(16773360, 4860458));
      this.setBorder(BorderFactory.createCompoundBorder(new ErrorBubble.RoundedBorder(8), Borders.empty(8, 12)));
      this.setAlignmentX(0.0F);
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      JLabel icon = new JLabel("⚠️");
      icon.setFont(Fonts.label(16.0F));
      icon.setVerticalAlignment(1);
      JBTextArea text = new JBTextArea(errorMessage);
      text.setEditable(false);
      text.setOpaque(false);
      text.setWrapStyleWord(true);
      text.setLineWrap(true);
      text.setFont(Fonts.label());
      text.setForeground(new JBColor(13369344, 16737894));
      text.setBorder(null);
      this.add(icon, "West");
      this.add(text, "Center");
   }

   private static final class RoundedBorder implements Border {
      private final int radius;

      RoundedBorder(int radius) {
         this.radius = radius;
      }

      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
         Graphics2D g2 = (Graphics2D)g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(c.getBackground().darker());
         g2.drawRoundRect(x, y, width - 1, height - 1, this.radius, this.radius);
         g2.dispose();
      }

      @Override
      public Insets getBorderInsets(Component c) {
         return new Insets(0, 0, 0, 0);
      }

      @Override
      public boolean isBorderOpaque() {
         return false;
      }
   }
}
