package com.tubitech.copilot.ui;

import com.intellij.ui.components.JBPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

public class RoundedBorderPanel extends JBPanel<RoundedBorderPanel> {
   private final Color bgColor;
   private final int radius;

   public RoundedBorderPanel(Color bgColor, int radius, LayoutManager layout) {
      super(layout);
      this.bgColor = bgColor;
      this.radius = radius;
      this.setOpaque(false);
   }

   protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D)g.create();

      try {
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(this.bgColor);
         g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.radius * 2, this.radius * 2);
      } finally {
         g2.dispose();
      }
   }
}
