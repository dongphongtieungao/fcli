package com.tubitech.copilot.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InlineNoticeBar extends JBPanel<InlineNoticeBar> {
   private static final int AUTO_HIDE_MS = 5000;
   private static final String ICON_SUCCESS = "✅ ";
   private static final String ICON_VALIDATION = "⚠ ";
   private static final String ICON_SYSTEM = "\ud83d\udd0c ";
   private static final JBColor SUCCESS_BG = new JBColor(new Color(15267305), new Color(1784351));
   private static final JBColor SUCCESS_FG = new JBColor(new Color(3046706), new Color(8505220));
   private static final JBColor VALIDATION_BG = new JBColor(new Color(16772078), new Color(3807770));
   private static final JBColor VALIDATION_FG = new JBColor(new Color(12986408), new Color(15702682));
   private static final JBColor SYSTEM_BG = new JBColor(new Color(16776679), new Color(3812864));
   private static final JBColor SYSTEM_FG = new JBColor(new Color(16088855), new Color(16763906));
   private final JBLabel label;
   @Nullable
   private Timer autoHideTimer;

   public InlineNoticeBar() {
      super(new BorderLayout(4, 0));
      this.setOpaque(true);
      this.setBorder(Borders.empty(0, 6));
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE, JBUI.scale(22)));
      this.label = new JBLabel("");
      this.label.setFont(Fonts.miniFont());
      this.add(this.label, "Center");
      this.setVisible(false);
   }

   public void show(@NotNull InlineNoticeBar.NoticeType type, @NotNull String message) {
      JBColor bg;
      JBColor fg;
      String icon;
      switch (type) {
         case SUCCESS:
            bg = SUCCESS_BG;
            fg = SUCCESS_FG;
            icon = "✅ ";
            break;
         case VALIDATION:
            bg = VALIDATION_BG;
            fg = VALIDATION_FG;
            icon = "⚠ ";
            break;
         default:
            bg = SYSTEM_BG;
            fg = SYSTEM_FG;
            icon = "\ud83d\udd0c ";
      }

      this.setBackground(bg);
      this.label.setForeground(fg);
      this.label.setText(icon + message);
      this.setVisible(true);
      this.revalidate();
      this.repaint();
      if (this.autoHideTimer != null && this.autoHideTimer.isRunning()) {
         this.autoHideTimer.restart();
      } else {
         this.autoHideTimer = new Timer(5000, e -> this.dismiss());
         this.autoHideTimer.setRepeats(false);
         this.autoHideTimer.start();
      }
   }

   public void dismiss() {
      if (this.autoHideTimer != null) {
         this.autoHideTimer.stop();
         this.autoHideTimer = null;
      }

      this.setVisible(false);
      this.label.setText("");
   }

   public void dispose() {
      if (this.autoHideTimer != null) {
         this.autoHideTimer.stop();
         this.autoHideTimer = null;
      }
   }

   public enum NoticeType {
      SUCCESS,
      VALIDATION,
      SYSTEM;
   }
}
