package com.tubitech.copilot.ui.renderer;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.ui.ChatStyles;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CodeBlockRenderer extends JBPanel<CodeBlockRenderer> {
   private static final Logger LOG = Logger.getInstance(CodeBlockRenderer.class);
   private static final int COLLAPSE_THRESHOLD = 8;
   private static final Map<String, String> SYNTAX_MAP = Map.of(
      "java",
      "text/java",
      "kotlin",
      "text/kotlin",
      "python",
      "text/python",
      "javascript",
      "text/javascript",
      "typescript",
      "text/typescript",
      "xml",
      "text/xml",
      "json",
      "text/json",
      "sql",
      "text/sql",
      "bash",
      "text/unix",
      "groovy",
      "text/groovy"
   );
   private final String code;
   private final int lineCount;
   private final JButton copyButton;
   private Timer copyResetTimer;
   @Nullable
   private final Project project;
   private boolean expanded;
   @Nullable
   private final JButton toggleButton;
   private final RTextScrollPane scrollPane;

   public CodeBlockRenderer(@NotNull String language, @NotNull String code) {
      this(language, code, null);
   }

   public CodeBlockRenderer(@NotNull String language, @NotNull String code, @Nullable Project project) {
      super(new BorderLayout(0, 0));
      this.code = code;
      this.project = project;
      this.lineCount = code.split("\n", -1).length;
      this.setBorder(new LineBorder(ChatStyles.CODE_BORDER, 1));
      this.setOpaque(false);
      boolean shouldCollapse = this.lineCount > 8;
      this.expanded = !shouldCollapse;
      RSyntaxTextArea textArea = this.buildTextArea(language);
      this.scrollPane = new RTextScrollPane(textArea, false);
      this.scrollPane.setHorizontalScrollBarPolicy(30);
      this.scrollPane.setVerticalScrollBarPolicy(21);
      this.scrollPane.setBorder(Borders.empty());
      this.scrollPane.setBackground(ChatStyles.CODE_BG);
      this.scrollPane.getViewport().setBackground(ChatStyles.CODE_BG);
      this.scrollPane.setVisible(this.expanded);
      this.copyButton = new JButton("\ud83d\udccb Copy");
      this.copyButton.setOpaque(false);
      this.copyButton.setContentAreaFilled(false);
      this.copyButton.setBorderPainted(false);
      this.copyButton.setForeground(ChatStyles.TIMESTAMP_FG);
      this.copyButton.setFont(Fonts.label(11.0F));
      this.copyButton.setFocusPainted(false);
      this.copyButton.setCursor(Cursor.getPredefinedCursor(12));
      this.copyButton.addActionListener(e -> this.onCopy());
      if (shouldCollapse) {
         this.toggleButton = new JButton("▶ " + this.lineCount + " lines");
         this.toggleButton.setOpaque(false);
         this.toggleButton.setContentAreaFilled(false);
         this.toggleButton.setBorderPainted(false);
         this.toggleButton.setForeground(ChatStyles.TIMESTAMP_FG);
         this.toggleButton.setFont(Fonts.label(11.0F));
         this.toggleButton.setFocusPainted(false);
         this.toggleButton.setCursor(Cursor.getPredefinedCursor(12));
         this.toggleButton.addActionListener(e -> this.toggleExpanded());
      } else {
         this.toggleButton = null;
      }

      JPanel headerBar = new JPanel(new BorderLayout(8, 0));
      headerBar.setOpaque(true);
      headerBar.setBackground(ChatStyles.CODE_HEADER_BG);
      headerBar.setBorder(Borders.empty(4, 8, 4, 8));
      JBLabel langLabel = new JBLabel(language.isBlank() ? "code" : language);
      langLabel.setFont(Fonts.label(11.0F));
      langLabel.setForeground(ChatStyles.TIMESTAMP_FG);
      JPanel eastPanel = new JPanel(new FlowLayout(2, 4, 0));
      eastPanel.setOpaque(false);
      if (this.toggleButton != null) {
         eastPanel.add(this.toggleButton);
      }

      if (project != null) {
         JButton insertButton = makeHeaderButton("⎘ Insert");
         insertButton.setToolTipText("Insert at cursor position in editor");
         insertButton.addActionListener(e -> this.onInsertAtCursor());
         eastPanel.add(insertButton);
      }

      eastPanel.add(this.copyButton);
      headerBar.add(langLabel, "West");
      headerBar.add(eastPanel, "East");
      this.add(headerBar, "North");
      this.add(this.scrollPane, "Center");
   }

   private void toggleExpanded() {
      this.expanded = !this.expanded;
      this.scrollPane.setVisible(this.expanded);
      if (this.toggleButton != null) {
         this.toggleButton.setText(this.expanded ? "▼ Collapse" : "▶ " + this.lineCount + " lines");
      }

      this.revalidate();
      this.repaint();
   }

   @NotNull
   private RSyntaxTextArea buildTextArea(@NotNull String language) {
      RSyntaxTextArea area = new RSyntaxTextArea();
      area.setSyntaxEditingStyle(SYNTAX_MAP.getOrDefault(language.toLowerCase(), "text/plain"));
      area.setEditable(false);
      area.setLineWrap(false);
      area.setHighlightCurrentLine(false);
      area.setAntiAliasingEnabled(true);
      area.setFont(Fonts.label().deriveFont(0, 12.0F));
      area.setText(this.code);
      String themeFile = JBColor.isBright() ? "idea.xml" : "dark.xml";

      try (InputStream is = this.getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + themeFile)) {
         if (is != null) {
            Theme.load(is).apply(area);
         }
      } catch (IOException e) {
         LOG.warn("Could not load RSyntaxTextArea theme '" + themeFile + "'", e);
      }

      area.setBackground(ChatStyles.CODE_BG);
      area.setForeground(new Color(15134195));
      area.setCaretColor(new Color(15134195));
      area.setOpaque(true);
      return area;
   }

   private void onCopy() {
      CopyPasteManager.getInstance().setContents(new StringSelection(this.code));
      this.copyButton.setText("✓ Copied");
      if (this.copyResetTimer != null) {
         this.copyResetTimer.stop();
      }

      this.copyResetTimer = new Timer(2000, e -> this.copyButton.setText("\ud83d\udccb Copy"));
      this.copyResetTimer.setRepeats(false);
      this.copyResetTimer.start();
   }

   public void removeNotify() {
      if (this.copyResetTimer != null) {
         this.copyResetTimer.stop();
         this.copyResetTimer = null;
      }

      super.removeNotify();
   }

   private void onInsertAtCursor() {
      if (this.project != null) {
         Editor editor = FileEditorManager.getInstance(this.project).getSelectedTextEditor();
         if (editor != null) {
            int offset = editor.getCaretModel().getOffset();
            WriteCommandAction.runWriteCommandAction(this.project, () -> editor.getDocument().insertString(offset, this.code));
         }
      }
   }

   @NotNull
   private static JButton makeHeaderButton(@NotNull String label) {
      JButton btn = new JButton(label);
      btn.setOpaque(false);
      btn.setContentAreaFilled(false);
      btn.setBorderPainted(false);
      btn.setForeground(ChatStyles.TIMESTAMP_FG);
      btn.setFont(Fonts.label(11.0F));
      btn.setFocusPainted(false);
      btn.setCursor(Cursor.getPredefinedCursor(12));
      return btn;
   }
}
