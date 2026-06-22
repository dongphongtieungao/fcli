package com.tubitech.copilot.ui;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI.Borders;
import com.tubitech.copilot.agent.FileChangeApplyService;
import com.tubitech.copilot.agent.FileChangeProposal;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public final class AgentChangesPanel extends JBPanel<AgentChangesPanel> {
   private static final JBColor PANEL_BG = new JBColor(16316922, 2829616);
   private static final JBColor HEADER_BG = new JBColor(15330543, 3948353);
   private static final JBColor BADGE_NEW = new JBColor(12838603, 2972187);
   private static final JBColor BADGE_MOD = new JBColor(16774093, 5917184);
   private static final JBColor BADGE_DEL = new JBColor(16308186, 5904922);
   private final List<FileChangeProposal> proposals;
   private final Project project;
   private final Runnable onDismiss;
   private int currentIndex = 0;
   private final JBLabel titleLabel = new JBLabel();
   private final JPanel centerRow = new JPanel(new FlowLayout(0, 8, 4));
   private final JButton viewBtn = new JButton("View");

   public AgentChangesPanel(@NotNull List<FileChangeProposal> proposals, @NotNull Project project, @NotNull Runnable onDismiss) {
      super(new BorderLayout());
      this.proposals = proposals;
      this.project = project;
      this.onDismiss = onDismiss;
      this.setBackground(PANEL_BG);
      this.setBorder(Borders.customLine(JBColor.border(), 1, 0, 0, 0));
      this.add(this.buildHeader(), "North");
      this.add(this.centerRow, "Center");
      this.centerRow.setBackground(PANEL_BG);
      this.showProposal(0);
   }

   @NotNull
   private JPanel buildHeader() {
      JPanel header = new JPanel(new BorderLayout());
      header.setBackground(HEADER_BG);
      header.setBorder(Borders.empty(4, 8));
      this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1));
      this.titleLabel.setBorder(Borders.emptyRight(8));
      header.add(this.titleLabel, "Center");
      JPanel btnBar = new JPanel(new FlowLayout(2, 6, 0));
      btnBar.setBackground(HEADER_BG);
      this.viewBtn.addActionListener(e -> this.viewDiff(this.proposals.get(this.currentIndex)));
      JButton keepBtn = new JButton("Keep");
      keepBtn.addActionListener(e -> this.keepAndAdvance());
      JButton skipBtn = new JButton("Skip");
      skipBtn.addActionListener(e -> this.skipAndAdvance());
      btnBar.add(this.viewBtn);
      btnBar.add(keepBtn);
      btnBar.add(skipBtn);
      header.add(btnBar, "East");
      return header;
   }

   private void showProposal(int index) {
      int total = this.proposals.size();
      this.titleLabel.setText(String.format("\ud83e\udd16 Proposed Changes (%d file%s) — File %d of %d", total, total == 1 ? "" : "s", index + 1, total));
      FileChangeProposal p = this.proposals.get(index);
      this.viewBtn.setEnabled(p.getOperation() != FileChangeProposal.Operation.DELETE || p.getExistingContent() != null);
      this.centerRow.removeAll();
      this.centerRow.setBackground(PANEL_BG);
      boolean isCreate = p.getOperation() == FileChangeProposal.Operation.CREATE;
      boolean isDelete = p.getOperation() == FileChangeProposal.Operation.DELETE;
      JBLabel fileIcon = new JBLabel(isDelete ? Actions.GC : (isCreate ? FileTypes.Text : Actions.Edit));
      this.centerRow.add(fileIcon);
      JBLabel pathLabel = new JBLabel(p.getRelativePath());
      this.centerRow.add(pathLabel);
      JBLabel badge = new JBLabel(isDelete ? " DEL " : (isCreate ? " NEW " : " MOD "));
      badge.setOpaque(true);
      badge.setBackground(isDelete ? BADGE_DEL : (isCreate ? BADGE_NEW : BADGE_MOD));
      badge.setBorder(Borders.empty(1, 4));
      this.centerRow.add(badge);
      this.centerRow.revalidate();
      this.centerRow.repaint();
      this.revalidate();
      this.repaint();
   }

   private void viewDiff(@NotNull FileChangeProposal proposal) {
      String leftText;
      String rightText;
      String leftTitle;
      String rightTitle;
      switch (proposal.getOperation()) {
         case CREATE:
            leftText = "";
            rightText = proposal.getNewContent();
            leftTitle = "(new file)";
            rightTitle = proposal.getRelativePath() + " [proposed]";
            break;
         case DELETE:
            leftText = proposal.getExistingContent() != null ? proposal.getExistingContent() : "";
            rightText = "";
            leftTitle = proposal.getRelativePath() + " [current]";
            rightTitle = "(deleted)";
            break;
         default:
            leftText = proposal.getExistingContent() != null ? proposal.getExistingContent() : "";
            rightText = proposal.getNewContent();
            leftTitle = proposal.getRelativePath() + " [current]";
            rightTitle = proposal.getRelativePath() + " [proposed]";
      }

      FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(proposal.getRelativePath());
      DiffContent leftContent = (DiffContent)(leftText.isEmpty()
         ? DiffContentFactory.getInstance().createEmpty()
         : DiffContentFactory.getInstance().create(this.project, leftText, fileType));
      DiffContent rightContent = (DiffContent)(rightText.isEmpty()
         ? DiffContentFactory.getInstance().createEmpty()
         : DiffContentFactory.getInstance().create(this.project, rightText, fileType));
      DiffManager.getInstance()
         .showDiff(this.project, new SimpleDiffRequest("Diff: " + proposal.getRelativePath(), leftContent, rightContent, leftTitle, rightTitle));
   }

   private void keepAndAdvance() {
      FileChangeApplyService.applySingle(this.proposals.get(this.currentIndex), this.project);
      this.advance();
   }

   private void skipAndAdvance() {
      this.advance();
   }

   private void advance() {
      this.currentIndex++;
      if (this.currentIndex >= this.proposals.size()) {
         this.onDismiss.run();
      } else {
         this.showProposal(this.currentIndex);
      }
   }
}
