package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI.Borders;
import com.tubitech.copilot.editor.MentionSuggestion;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jetbrains.annotations.NotNull;

public class MentionAutocompletePopup {
   private static final int POPUP_WIDTH = 400;
   private static final int POPUP_MAX_HEIGHT = 220;
   private static final int ROW_HEIGHT = 24;
   private final JTextArea ownerTextArea;
   private final Consumer<MentionSuggestion> onSelect;
   private final JWindow window;
   private final DefaultListModel<MentionSuggestion> listModel;
   private final JBList<MentionSuggestion> list;
   private int lastCaretX = 0;

   public MentionAutocompletePopup(@NotNull Project project, @NotNull JTextArea ownerTextArea, @NotNull Consumer<MentionSuggestion> onSelect) {
      this.ownerTextArea = ownerTextArea;
      this.onSelect = onSelect;
      Window ownerWindow = SwingUtilities.getWindowAncestor(ownerTextArea);
      this.window = new JWindow(ownerWindow);
      this.window.setFocusableWindowState(false);
      this.listModel = new DefaultListModel<>();
      this.list = new JBList(this.listModel);
      this.list.setFixedCellHeight(24);
      this.list.setCellRenderer(new MentionAutocompletePopup.MentionCellRenderer());
      this.list.setSelectionMode(0);
      this.list.setFocusable(false);
      this.list.addMouseMotionListener(new MouseAdapter() {
         @Override
         public void mouseMoved(MouseEvent e) {
            int idx = MentionAutocompletePopup.this.list.locationToIndex(e.getPoint());
            if (idx >= 0) {
               MentionAutocompletePopup.this.list.setSelectedIndex(idx);
            }
         }
      });
      this.list.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            int idx = MentionAutocompletePopup.this.list.locationToIndex(e.getPoint());
            if (idx >= 0) {
               MentionAutocompletePopup.this.commitIndex(idx);
            }
         }
      });
      JBScrollPane scrollPane = new JBScrollPane(this.list);
      scrollPane.setVerticalScrollBarPolicy(20);
      scrollPane.setHorizontalScrollBarPolicy(31);
      scrollPane.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
      this.window.setContentPane(scrollPane);
   }

   public void show(@NotNull List<MentionSuggestion> suggestions, int caretX, int caretY) {
      this.setModel(suggestions);
      if (!this.listModel.isEmpty()) {
         this.list.setSelectedIndex(0);
         this.list.ensureIndexIsVisible(0);
         this.lastCaretX = caretX;
         this.resizeWindow();
         this.positionWindow(caretX, caretY);
         this.window.setVisible(true);
      }
   }

   public void update(@NotNull List<MentionSuggestion> suggestions) {
      this.setModel(suggestions);
      if (this.listModel.isEmpty()) {
         this.hide();
      } else {
         int sel = this.list.getSelectedIndex();
         if (sel < 0 || sel >= this.listModel.size()) {
            this.list.setSelectedIndex(0);
         }

         this.resizeWindow();
         this.positionWindow(this.lastCaretX, 0);
         this.window.revalidate();
         this.window.repaint();
      }
   }

   public void hide() {
      if (this.window.isVisible()) {
         this.window.setVisible(false);
      }
   }

   public boolean isVisible() {
      return this.window.isVisible();
   }

   public void moveSelectionDown() {
      int size = this.listModel.size();
      if (size != 0) {
         int next = (this.list.getSelectedIndex() + 1) % size;
         this.list.setSelectedIndex(next);
         this.list.ensureIndexIsVisible(next);
      }
   }

   public void moveSelectionUp() {
      int size = this.listModel.size();
      if (size != 0) {
         int prev = this.list.getSelectedIndex() - 1;
         if (prev < 0) {
            prev = size - 1;
         }

         this.list.setSelectedIndex(prev);
         this.list.ensureIndexIsVisible(prev);
      }
   }

   public void commitSelection() {
      int idx = this.list.getSelectedIndex();
      if (idx >= 0 && idx < this.listModel.size()) {
         this.commitIndex(idx);
      }
   }

   private void commitIndex(int index) {
      MentionSuggestion selected = this.listModel.get(index);
      this.hide();
      this.onSelect.accept(selected);
   }

   private void setModel(@NotNull List<MentionSuggestion> suggestions) {
      this.listModel.clear();

      for (MentionSuggestion s : suggestions) {
         this.listModel.addElement(s);
      }
   }

   private void resizeWindow() {
      int itemCount = this.listModel.size();
      int naturalHeight = itemCount * 24 + 2;
      int height = Math.min(naturalHeight, 220);
      this.window.setSize(400, height);
   }

   private void positionWindow(int caretX, int caretY) {
      Point ownerOnScreen;
      try {
         ownerOnScreen = this.ownerTextArea.getLocationOnScreen();
      } catch (IllegalComponentStateException e) {
         return;
      }

      Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
      int screenX = ownerOnScreen.x + caretX;
      screenX = Math.min(screenX, screenBounds.x + screenBounds.width - 400);
      screenX = Math.max(screenX, screenBounds.x);
      int anchorY = ownerOnScreen.y - 2;
      int screenY = anchorY - this.window.getHeight();
      if (screenY < screenBounds.y) {
         screenY = screenBounds.y;
      }

      this.window.setLocation(screenX, screenY);
   }

   private static final class MentionCellRenderer extends DefaultListCellRenderer {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         if (value instanceof MentionSuggestion suggestion) {
            label.setText(suggestion.displayText());
            label.setIcon(suggestion.isDirectory() ? Nodes.Folder : FileTypes.Text);
            Font base = UIManager.getFont("Label.font");
            label.setFont(base != null ? base.deriveFont(12.0F) : label.getFont().deriveFont(12.0F));
            label.setBorder(Borders.empty(0, 4));
            if (isSelected) {
               label.setBackground(UIManager.getColor("List.selectionBackground"));
               label.setForeground(UIManager.getColor("List.selectionForeground"));
            } else {
               label.setBackground(UIManager.getColor("List.background"));
               label.setForeground(UIManager.getColor("List.foreground"));
            }

            label.setOpaque(true);
         }

         return label;
      }
   }
}
