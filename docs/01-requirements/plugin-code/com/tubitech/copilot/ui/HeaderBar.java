package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.Vcs;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.util.DeviceCodeGenerator;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;

public final class HeaderBar extends JBPanel<HeaderBar> {
   private static final JBColor VERSION_FG = new JBColor(9147550, 9147550);
   private static final JBColor DEVICE_CODE_FG = new JBColor(5809919, 5809919);
   private static final JBColor DEVICE_CODE_HOVER = new JBColor(7979263, 7979263);
   private Timer copyResetTimer;

   public HeaderBar(@NotNull JButton newButton, @NotNull JButton historyButton, @NotNull JButton clearButton, @NotNull AuthToolbarPanel authPanel) {
      super(new BorderLayout());
      this.setBackground(UIUtil.getPanelBackground());
      this.setBorder(Borders.customLine(JBColor.border(), 0, 0, 1, 0));
      JPanel leftGroup = new JPanel(new FlowLayout(0, 0, 0));
      leftGroup.setOpaque(false);
      final String deviceCode = DeviceCodeGenerator.getDeviceCode();
      final String infoText = "v2.0.0 | " + deviceCode;
      final JBLabel infoLabel = new JBLabel(infoText);
      infoLabel.setFont(Fonts.smallFont());
      infoLabel.setForeground(VERSION_FG);
      infoLabel.setBorder(Borders.empty(8, 10, 6, 0));
      infoLabel.setCursor(Cursor.getPredefinedCursor(12));
      infoLabel.setToolTipText("Device Code — click to copy");
      infoLabel.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(deviceCode), null);
            infoLabel.setText("Copied!");
            if (HeaderBar.this.copyResetTimer != null) {
               HeaderBar.this.copyResetTimer.stop();
            }

            HeaderBar.this.copyResetTimer = new Timer(1500, evt -> infoLabel.setText(infoText));
            HeaderBar.this.copyResetTimer.setRepeats(false);
            HeaderBar.this.copyResetTimer.start();
         }

         @Override
         public void mouseEntered(MouseEvent e) {
            infoLabel.setForeground(HeaderBar.DEVICE_CODE_HOVER);
         }

         @Override
         public void mouseExited(MouseEvent e) {
            infoLabel.setForeground(HeaderBar.VERSION_FG);
         }
      });
      leftGroup.add(infoLabel);
      JButton homeButton = new JButton();
      styleHeaderButton(homeButton, Actions.Preview, "Home page");
      homeButton.addActionListener(e -> BrowserUtil.browse("https://nguyenlinh1993.github.io/tubi-copilot-page/"));
      styleHeaderButton(newButton, General.Add, "New conversation");
      styleHeaderButton(historyButton, Vcs.History, "History");
      styleHeaderButton(clearButton, Actions.GC, "Delete conversation");
      JPanel rightGroup = new JPanel(new FlowLayout(2, 2, 4));
      rightGroup.setOpaque(false);
      rightGroup.add(homeButton);
      rightGroup.add(newButton);
      rightGroup.add(historyButton);
      rightGroup.add(clearButton);
      rightGroup.add(authPanel);
      this.add(leftGroup, "West");
      this.add(rightGroup, "East");
   }

   public void removeNotify() {
      if (this.copyResetTimer != null) {
         this.copyResetTimer.stop();
      }

      super.removeNotify();
   }

   private static void styleHeaderButton(@NotNull JButton btn, @NotNull Icon icon, @NotNull String tooltip) {
      btn.setText(null);
      btn.setIcon(icon);
      btn.setToolTipText(tooltip);
      btn.setBorderPainted(false);
      btn.setContentAreaFilled(false);
      btn.setFocusPainted(false);
      btn.setMargin(JBUI.emptyInsets());
      Dimension size = JBUI.size(28, 28);
      btn.setPreferredSize(size);
      btn.setMinimumSize(size);
      btn.setMaximumSize(size);
      btn.setCursor(Cursor.getPredefinedCursor(12));
   }
}
