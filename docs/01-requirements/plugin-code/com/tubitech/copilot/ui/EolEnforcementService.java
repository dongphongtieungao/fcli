package com.tubitech.copilot.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI.Borders;
import com.tubitech.copilot.ApplicationConstants;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public final class EolEnforcementService {
   private EolEnforcementService() {
   }

   public static boolean isExpired() {
      return LocalDate.now().isAfter(ApplicationConstants.EOL_DATE);
   }

   public static void showExpiryDialog(@Nullable Project project) {
      new EolEnforcementService.ExpiryDialog(project).show();
   }

   private static final class ExpiryDialog extends DialogWrapper {
      ExpiryDialog(@Nullable Project project) {
         super(project, false);
         this.setTitle("Tubi Copilot — Plugin Expired");
         this.setOKButtonText("Close");
         this.init();
      }

      @Nullable
      protected JComponent createCenterPanel() {
         JPanel panel = new JPanel(new BorderLayout(0, 12));
         panel.setBorder(Borders.empty(12));
         JBLabel msg = new JBLabel(
            "<html><b>Tubi Copilot v2.0.0 has reached its end of life.</b><br><br>This plugin expired on <b>"
               + ApplicationConstants.EOL_DATE
               + "</b>.<br>All features are now disabled. Please visit the home page for updates.</html>"
         );
         msg.setPreferredSize(new Dimension(420, -1));
         panel.add(msg, "Center");
         JLabel link = new JBLabel("<html><a href=''>https://nguyenlinh1993.github.io/tubi-copilot-page/</a></html>");
         link.setCursor(Cursor.getPredefinedCursor(12));
         link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               BrowserUtil.browse("https://nguyenlinh1993.github.io/tubi-copilot-page/");
            }
         });
         panel.add(link, "South");
         return panel;
      }

      protected Action[] createActions() {
         return new Action[]{this.getOKAction()};
      }
   }
}
