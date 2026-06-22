package com.tubitech.copilot.ui.renderer;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.JBUI.Fonts;
import com.tubitech.copilot.ui.ChatStyles;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MarkdownRenderer extends JBPanel<MarkdownRenderer> {
   private static final Pattern BOLD_STAR = Pattern.compile("\\*\\*(.+?)\\*\\*");
   private static final Pattern BOLD_UNDER = Pattern.compile("__(.+?)__");
   private static final Pattern ITALIC_STAR = Pattern.compile("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)");
   private static final Pattern ITALIC_UNDER = Pattern.compile("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)");
   private static final Pattern STRIKE = Pattern.compile("~~(.+?)~~");
   private static final Pattern INLINE_CODE = Pattern.compile("`([^`]+)`");
   private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");
   @Nullable
   private final Project project;

   public MarkdownRenderer(@NotNull String markdownText, @Nullable Project project) {
      this.project = project;
      this.setLayout(new BoxLayout(this, 1));
      this.setOpaque(false);
      List<MarkdownRenderer.RawSegment> rawSegments = splitIntoSections(markdownText);

      for (int i = rawSegments.size() - 1; i >= 0; i--) {
         if (rawSegments.get(i) instanceof MarkdownRenderer.SectionSegment s) {
            rawSegments.set(i, new MarkdownRenderer.SectionSegment(s.title(), s.body(), true));
            break;
         }
      }

      for (MarkdownRenderer.RawSegment seg : rawSegments) {
         if (seg instanceof MarkdownRenderer.TextSegment plain) {
            this.renderCodeBlocksOnly(plain.text(), this);
         } else if (seg instanceof MarkdownRenderer.SectionSegment section) {
            if (section.body().isBlank()) {
               this.add(buildPlainTextPanel("**" + section.title() + "**"));
            } else {
               this.add(this.buildCollapsibleSection(section.title(), section.body(), section.expanded()));
            }

            this.add(Box.createVerticalStrut(JBUI.scale(3)));
         }
      }
   }

   private void renderRichContent(@NotNull String text, @NotNull JComponent container) {
      List<MarkdownRenderer.RawSegment> rawSegments = splitIntoSections(text);
      boolean hasSections = rawSegments.stream().anyMatch(s -> s instanceof MarkdownRenderer.SectionSegment);
      if (hasSections) {
         for (MarkdownRenderer.RawSegment seg : rawSegments) {
            if (seg instanceof MarkdownRenderer.TextSegment plain) {
               this.renderCodeBlocksOnly(plain.text(), container);
            } else if (seg instanceof MarkdownRenderer.SectionSegment section) {
               if (section.body().isBlank()) {
                  container.add(buildPlainTextPanel("**" + section.title() + "**"));
               } else {
                  JBPanel<?> cs = this.buildCollapsibleSection(section.title(), section.body(), section.expanded());
                  cs.setAlignmentX(0.0F);
                  container.add(cs);
               }

               container.add(Box.createVerticalStrut(JBUI.scale(3)));
            }
         }
      } else {
         this.renderCodeBlocksOnly(text, container);
      }
   }

   private void renderCodeBlocksOnly(@NotNull String text, @NotNull JComponent container) {
      for (MarkdownRenderer.ParsedSegment seg : parseCodeBlocks(text)) {
         if (seg instanceof MarkdownRenderer.PlainBlock plain) {
            String t = plain.text().stripTrailing();
            if (!t.isBlank()) {
               JBPanel<?> p = buildPlainTextPanel(t);
               p.setAlignmentX(0.0F);
               container.add(p);
            }
         } else if (seg instanceof MarkdownRenderer.CodeBlock cs) {
            CodeBlockRenderer renderer = new CodeBlockRenderer(cs.language(), cs.code(), this.project);
            renderer.setAlignmentX(0.0F);
            container.add(renderer);
         }

         container.add(Box.createVerticalStrut(JBUI.scale(3)));
      }
   }

   @NotNull
   private static List<MarkdownRenderer.RawSegment> splitIntoSections(@NotNull String text) {
      String[] lines = text.split("\n", -1);
      int topLevel = findTopHeadingLevel(lines);
      if (topLevel == 0) {
         return List.of(new MarkdownRenderer.TextSegment(text));
      }

      List<MarkdownRenderer.RawSegment> sections = new ArrayList<>();
      StringBuilder currentBody = new StringBuilder();
      String currentTitle = null;
      int headingCount = 0;
      boolean inCodeFence = false;

      for (String line : lines) {
         if (line.trim().startsWith("```")) {
            inCodeFence = !inCodeFence;
            currentBody.append(line).append("\n");
         } else {
            int level = inCodeFence ? 0 : headingLevel(line);
            String heading = level == topLevel ? extractHeadingText(line) : null;
            if (heading != null) {
               headingCount++;
               if (currentTitle != null) {
                  sections.add(new MarkdownRenderer.SectionSegment(currentTitle, currentBody.toString().trim(), false));
                  currentBody.setLength(0);
               } else if (currentBody.length() > 0 && !currentBody.toString().isBlank()) {
                  sections.add(new MarkdownRenderer.TextSegment(currentBody.toString()));
                  currentBody.setLength(0);
               }

               currentTitle = heading;
            } else {
               currentBody.append(line).append("\n");
            }
         }
      }

      if (currentTitle != null) {
         sections.add(new MarkdownRenderer.SectionSegment(currentTitle, currentBody.toString().trim(), false));
      } else if (!currentBody.toString().isBlank()) {
         sections.add(new MarkdownRenderer.TextSegment(currentBody.toString()));
      }

      return headingCount < 2 ? List.of(new MarkdownRenderer.TextSegment(text)) : sections;
   }

   private static int findTopHeadingLevel(@NotNull String[] lines) {
      int top = 0;
      boolean inCodeFence = false;

      for (String line : lines) {
         if (line.trim().startsWith("```")) {
            inCodeFence = !inCodeFence;
         } else if (!inCodeFence) {
            int level = headingLevel(line);
            if (level > 0 && (top == 0 || level < top)) {
               top = level;
            }
         }
      }

      return top;
   }

   private static int headingLevel(@NotNull String line) {
      String t = line.trim();
      if (t.startsWith("# ")) {
         return 1;
      }

      if (t.startsWith("## ")) {
         return 2;
      }

      if (t.startsWith("### ")) {
         return 3;
      }

      if (t.startsWith("#### ")) {
         return 4;
      }

      if (t.startsWith("**") && t.endsWith("**") && t.length() > 4) {
         String inner = t.substring(2, t.length() - 2);
         if (!inner.contains("**") && !inner.endsWith(":")) {
            if (inner.matches("^\\d+[.):]\\s.*")) {
               return 6;
            }

            return 5;
         }
      }

      return 0;
   }

   @Nullable
   private static String extractHeadingText(@NotNull String line) {
      String t = line.trim();
      if (t.startsWith("#### ")) {
         return t.substring(5);
      }

      if (t.startsWith("### ")) {
         return t.substring(4);
      }

      if (t.startsWith("## ")) {
         return t.substring(3);
      }

      if (t.startsWith("# ")) {
         return t.substring(2);
      }

      if (t.startsWith("**") && t.endsWith("**") && t.length() > 4) {
         String inner = t.substring(2, t.length() - 2);
         if (!inner.contains("**") && !inner.endsWith(":")) {
            return inner;
         }
      }

      return null;
   }

   @NotNull
   private JBPanel<?> buildCollapsibleSection(@NotNull String title, @NotNull String body, boolean startExpanded) {
      final JBPanel<?> panel = new JBPanel<JBPanel>(new BorderLayout(0, 0)) {
         public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
         }
      };
      panel.setOpaque(false);
      panel.setAlignmentX(0.0F);
      final JBLabel toggleIcon = new JBLabel(startExpanded ? "▼" : "▶");
      toggleIcon.setFont(Fonts.label(10.0F));
      toggleIcon.setForeground(ChatStyles.TIMESTAMP_FG);
      toggleIcon.setPreferredSize(JBUI.size(14, 14));
      JBLabel titleLabel = new JBLabel(title);
      titleLabel.setFont(Fonts.label(11.0F).asBold());
      titleLabel.setForeground(ChatStyles.TIMESTAMP_FG);
      JPanel header = new JPanel(new FlowLayout(0, 3, 1)) {
         @Override
         public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
         }
      };
      header.setOpaque(false);
      header.setCursor(Cursor.getPredefinedCursor(12));
      header.add(toggleIcon);
      header.add(titleLabel);
      final JBPanel<?> bodyPanel = new JBPanel<JBPanel>() {
         public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
         }
      };
      bodyPanel.setLayout(new BoxLayout(bodyPanel, 1));
      bodyPanel.setOpaque(false);
      bodyPanel.setBorder(Borders.emptyLeft(6));
      bodyPanel.setVisible(startExpanded);
      this.renderRichContent(body, bodyPanel);
      header.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            boolean vis = !bodyPanel.isVisible();
            bodyPanel.setVisible(vis);
            toggleIcon.setText(vis ? "▼" : "▶");

            for (Container c = panel; c != null; c = c.getParent()) {
               c.revalidate();
               if (c instanceof JScrollPane) {
                  break;
               }
            }

            panel.repaint();
         }
      });
      panel.add(header, "North");
      panel.add(bodyPanel, "Center");
      return panel;
   }

   @NotNull
   private static List<MarkdownRenderer.ParsedSegment> parseCodeBlocks(@NotNull String text) {
      List<MarkdownRenderer.ParsedSegment> segments = new ArrayList<>();
      String[] lines = text.split("\n", -1);
      boolean inCode = false;
      String currentLang = "";
      StringBuilder buffer = new StringBuilder();

      for (int i = 0; i < lines.length; i++) {
         String line = lines[i];
         String trimmed = line.trim();
         if (!inCode) {
            if (trimmed.startsWith("```")) {
               if (buffer.length() > 0) {
                  segments.add(new MarkdownRenderer.PlainBlock(buffer.toString()));
                  buffer.setLength(0);
               }

               currentLang = trimmed.substring(3).trim();
               inCode = true;
            } else {
               buffer.append(line).append('\n');
            }
         } else if (trimmed.startsWith("```") && !trimmed.equals("```" + currentLang)) {
            segments.add(new MarkdownRenderer.CodeBlock(currentLang, buffer.toString()));
            buffer.setLength(0);
            String afterFence = trimmed.substring(3).trim();
            if (!afterFence.isEmpty()) {
               currentLang = afterFence;
            } else {
               currentLang = "";
               inCode = false;
            }
         } else {
            buffer.append(line).append('\n');
         }
      }

      if (buffer.length() > 0) {
         if (inCode) {
            segments.add(new MarkdownRenderer.CodeBlock(currentLang, buffer.toString()));
         } else {
            segments.add(new MarkdownRenderer.PlainBlock(buffer.toString()));
         }
      }

      return segments;
   }

   @NotNull
   private static JBPanel<?> buildPlainTextPanel(@NotNull String text) {
      Color fg = UIUtil.getLabelForeground();
      String fgCss = String.format("rgb(%d,%d,%d)", fg.getRed(), fg.getGreen(), fg.getBlue());
      String family = Fonts.label().getFamily();
      int size = Fonts.label().getSize();
      String mutedCss = JBColor.isBright() ? "rgb(100,100,100)" : "rgb(150,150,150)";
      String linkColor = JBColor.isBright() ? "#0366D6" : "#4DABF7";
      String codeBg = JBColor.isBright() ? "#f0f0f0" : "#3c3f41";
      String tableBorder = JBColor.isBright() ? "#d0d7de" : "#3d3f43";
      String tableHeaderBg = JBColor.isBright() ? "#f6f8fa" : "#2d2d30";
      String css = "body { font-family: "
         + family
         + "; font-size: "
         + size
         + "pt; color: "
         + fgCss
         + "; margin: 0; padding: 0; line-height: 1.5; }h1,h2,h3,h4 { margin: 8px 0 4px 0; }ul,ol { margin: 4px 0 4px 18px; padding: 0; }li { margin: 2px 0; }p { margin: 0 0 6px 0; }hr { border: none; border-top: 1px solid #666; margin: 8px 0; }code { font-family: monospace; background: "
         + codeBg
         + "; padding: 1px 4px; border-radius: 3px; font-size: "
         + (size - 1)
         + "pt; }blockquote { border-left: 3px solid #555; padding-left: 8px; margin: 4px 0; color: "
         + mutedCss
         + "; }a { color: "
         + linkColor
         + "; text-decoration: none; }table { border-collapse: collapse; margin: 6px 0; width: 100%; }th, td { border: 1px solid "
         + tableBorder
         + "; padding: 6px 10px; text-align: left; font-size: "
         + size
         + "pt; }th { background: "
         + tableHeaderBg
         + "; font-weight: bold; }";
      String html = "<html><head><style>" + css + "</style></head><body>" + markdownToHtml(text.stripTrailing()) + "</body></html>";
      JEditorPane pane = new JEditorPane("text/html", html) {
         @Override
         public Dimension getPreferredSize() {
            int w = this.getWidth() > 0 ? this.getWidth() : 8191;

            try {
               View rootView = this.getUI().getRootView(this);
               rootView.setSize(w, 0.0F);
               int h = (int)Math.ceil(rootView.getPreferredSpan(1));
               return new Dimension(w, Math.max(h, JBUI.scale(16)));
            } catch (Exception ignored) {
               this.setSize(w, 16383);
               Dimension d = super.getPreferredSize();
               return new Dimension(w, Math.max(d.height, JBUI.scale(16)));
            }
         }
      };
      pane.setEditable(false);
      pane.setOpaque(false);
      pane.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);
      pane.setFont(Fonts.label());
      pane.setForeground(fg);
      pane.setBorder(null);
      JBPanel<?> wrapper = new JBPanel(new BorderLayout());
      wrapper.setOpaque(false);
      wrapper.setAlignmentX(0.0F);
      wrapper.add(pane, "Center");
      return wrapper;
   }

   @NotNull
   private static String markdownToHtml(@NotNull String md) {
      String[] lines = md.split("\n", -1);
      StringBuilder html = new StringBuilder();
      boolean inUl = false;
      boolean inOl = false;
      boolean inBlockquote = false;
      boolean inTable = false;
      boolean lastWasBlank = false;

      for (int li = 0; li < lines.length; li++) {
         String line = lines[li];
         String trimmed = line.trim();
         if (isTableRow(trimmed)) {
            if (!isSeparatorRow(trimmed)) {
               closeList(html, inUl, inOl);
               inUl = false;
               inOl = false;
               if (inBlockquote) {
                  html.append("</blockquote>");
                  inBlockquote = false;
               }

               if (!inTable) {
                  html.append("<table>");
                  inTable = true;
                  if (li + 1 < lines.length && isSeparatorRow(lines[li + 1].trim())) {
                     html.append("<tr>");

                     for (String cell : splitTableCells(trimmed)) {
                        html.append("<th>").append(inlineFormat(cell.trim())).append("</th>");
                     }

                     html.append("</tr>");
                     li++;
                     lastWasBlank = false;
                     continue;
                  }
               }

               html.append("<tr>");

               for (String cell : splitTableCells(trimmed)) {
                  html.append("<td>").append(inlineFormat(cell.trim())).append("</td>");
               }

               html.append("</tr>");
               lastWasBlank = false;
            }
         } else {
            if (inTable) {
               html.append("</table>");
               inTable = false;
            }

            if (trimmed.startsWith("#### ")) {
               closeAll(html, inUl, inOl, inBlockquote);
               inUl = false;
               inOl = false;
               inBlockquote = false;
               html.append("<h4>").append(inlineFormat(trimmed.substring(5))).append("</h4>");
               lastWasBlank = false;
            } else if (trimmed.startsWith("### ")) {
               closeAll(html, inUl, inOl, inBlockquote);
               inUl = false;
               inOl = false;
               inBlockquote = false;
               html.append("<h3>").append(inlineFormat(trimmed.substring(4))).append("</h3>");
               lastWasBlank = false;
            } else if (trimmed.startsWith("## ")) {
               closeAll(html, inUl, inOl, inBlockquote);
               inUl = false;
               inOl = false;
               inBlockquote = false;
               html.append("<h2>").append(inlineFormat(trimmed.substring(3))).append("</h2>");
               lastWasBlank = false;
            } else if (trimmed.startsWith("# ")) {
               closeAll(html, inUl, inOl, inBlockquote);
               inUl = false;
               inOl = false;
               inBlockquote = false;
               html.append("<h1>").append(inlineFormat(trimmed.substring(2))).append("</h1>");
               lastWasBlank = false;
            } else if (trimmed.equals("---") || trimmed.equals("***") || trimmed.equals("___")) {
               closeAll(html, inUl, inOl, inBlockquote);
               inUl = false;
               inOl = false;
               inBlockquote = false;
               html.append("<hr>");
               lastWasBlank = false;
            } else if (trimmed.startsWith("> ")) {
               if (inUl) {
                  html.append("</ul>");
                  inUl = false;
               }

               if (inOl) {
                  html.append("</ol>");
                  inOl = false;
               }

               if (!inBlockquote) {
                  html.append("<blockquote>");
                  inBlockquote = true;
               }

               html.append(inlineFormat(trimmed.substring(2))).append("<br>");
               lastWasBlank = false;
            } else {
               if (inBlockquote) {
                  html.append("</blockquote>");
                  inBlockquote = false;
               }

               if (!trimmed.startsWith("- ") && !trimmed.startsWith("* ")) {
                  if ((line.startsWith("  - ") || line.startsWith("  * ") || line.startsWith("    - ") || line.startsWith("    * ")) && inUl) {
                     String content = trimmed.substring(2);
                     html.append("<li style='margin-left:16px'>").append(inlineFormat(content)).append("</li>");
                     lastWasBlank = false;
                  } else if (trimmed.matches("^\\d+\\.\\s.*")) {
                     if (inUl) {
                        html.append("</ul>");
                        inUl = false;
                     }

                     if (!inOl) {
                        html.append("<ol>");
                        inOl = true;
                     }

                     String content = trimmed.replaceFirst("^\\d+\\.\\s", "");
                     html.append("<li>").append(inlineFormat(content)).append("</li>");
                     lastWasBlank = false;
                  } else if (trimmed.isEmpty()) {
                     closeList(html, inUl, inOl);
                     inUl = false;
                     inOl = false;
                     if (!lastWasBlank) {
                        html.append("<br>");
                        lastWasBlank = true;
                     }
                  } else {
                     closeList(html, inUl, inOl);
                     inUl = false;
                     inOl = false;
                     html.append(inlineFormat(line)).append("<br>");
                     lastWasBlank = false;
                  }
               } else {
                  if (inOl) {
                     html.append("</ol>");
                     inOl = false;
                  }

                  if (!inUl) {
                     html.append("<ul>");
                     inUl = true;
                  }

                  String content = trimmed.substring(2);
                  if (content.startsWith("[ ] ")) {
                     html.append("<li>&#9744; ").append(inlineFormat(content.substring(4))).append("</li>");
                  } else if (!content.startsWith("[x] ") && !content.startsWith("[X] ")) {
                     html.append("<li>").append(inlineFormat(content)).append("</li>");
                  } else {
                     html.append("<li>&#9745; ").append(inlineFormat(content.substring(4))).append("</li>");
                  }

                  lastWasBlank = false;
               }
            }
         }
      }

      if (inTable) {
         html.append("</table>");
      }

      closeAll(html, inUl, inOl, inBlockquote);
      return html.toString();
   }

   private static void closeList(@NotNull StringBuilder sb, boolean inUl, boolean inOl) {
      if (inUl) {
         sb.append("</ul>");
      }

      if (inOl) {
         sb.append("</ol>");
      }
   }

   private static void closeAll(@NotNull StringBuilder sb, boolean inUl, boolean inOl, boolean inBlockquote) {
      if (inUl) {
         sb.append("</ul>");
      }

      if (inOl) {
         sb.append("</ol>");
      }

      if (inBlockquote) {
         sb.append("</blockquote>");
      }
   }

   private static boolean isTableRow(@NotNull String trimmed) {
      return trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length() > 2;
   }

   private static boolean isSeparatorRow(@NotNull String trimmed) {
      if (!isTableRow(trimmed)) {
         return false;
      }

      String inner = trimmed.substring(1, trimmed.length() - 1);
      return inner.matches("[\\s|:-]+");
   }

   @NotNull
   private static String[] splitTableCells(@NotNull String row) {
      String inner = row;
      if (inner.startsWith("|")) {
         inner = inner.substring(1);
      }

      if (inner.endsWith("|")) {
         inner = inner.substring(0, inner.length() - 1);
      }

      return inner.split("\\|", -1);
   }

   @NotNull
   private static String inlineFormat(@NotNull String text) {
      String s = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      s = BOLD_STAR.matcher(s).replaceAll("<b>$1</b>");
      s = BOLD_UNDER.matcher(s).replaceAll("<b>$1</b>");
      s = ITALIC_STAR.matcher(s).replaceAll("<i>$1</i>");
      s = ITALIC_UNDER.matcher(s).replaceAll("<i>$1</i>");
      s = STRIKE.matcher(s).replaceAll("<s>$1</s>");
      s = INLINE_CODE.matcher(s).replaceAll("<code>$1</code>");
      return LINK.matcher(s).replaceAll("<a href='$2'>$1</a>");
   }

   private record CodeBlock(@NotNull String language, @NotNull String code) implements MarkdownRenderer.ParsedSegment {
   }

   private sealed interface ParsedSegment permits MarkdownRenderer.PlainBlock, MarkdownRenderer.CodeBlock {
   }

   private record PlainBlock(@NotNull String text) implements MarkdownRenderer.ParsedSegment {
   }

   private sealed interface RawSegment permits MarkdownRenderer.TextSegment, MarkdownRenderer.SectionSegment {
   }

   private record SectionSegment(@NotNull String title, @NotNull String body, boolean expanded) implements MarkdownRenderer.RawSegment {
   }

   private record TextSegment(@NotNull String text) implements MarkdownRenderer.RawSegment {
   }
}
