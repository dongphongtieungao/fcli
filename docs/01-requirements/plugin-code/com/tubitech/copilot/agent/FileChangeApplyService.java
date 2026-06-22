package com.tubitech.copilot.agent;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public final class FileChangeApplyService {
   private static final Logger LOG = Logger.getInstance(FileChangeApplyService.class);
   static final String COMMAND_NAME = "Tubi Copilot: Apply Changes";
   static final String COMMAND_NAME_SINGLE = "Tubi Copilot: Apply Change";

   private FileChangeApplyService() {
   }

   public static void applySingle(@NotNull FileChangeProposal proposal, @NotNull Project project) {
      WriteCommandAction.runWriteCommandAction(project, "Tubi Copilot: Apply Change", null, () -> {
         try {
            applyInternal(proposal, project);
         } catch (IOException e) {
            String msg = "Failed to write '" + proposal.getRelativePath() + "': " + e.getMessage();
            LOG.warn("FileChangeApplyService: " + msg, e);
            Bus.notify(new Notification("TubiCopilot", "Agent Mode Error", msg, NotificationType.ERROR), project);
         }
      }, new PsiFile[0]);
      LOG.info("FileChangeApplyService: applySingle → " + proposal.getRelativePath());
   }

   @Deprecated
   public static void applyAll(@NotNull List<FileChangeProposal> proposals, @NotNull Project project) {
      int[] written = new int[]{0};
      WriteCommandAction.runWriteCommandAction(project, "Tubi Copilot: Apply Changes", null, () -> {
         for (FileChangeProposal proposal : proposals) {
            if (!proposal.isAccepted()) {
               LOG.debug("FileChangeApplyService: skipping rejected proposal: " + proposal.getRelativePath());
            } else {
               try {
                  applyInternal(proposal, project);
                  written[0]++;
               } catch (IOException e) {
                  String msg = "Failed to write '" + proposal.getRelativePath() + "': " + e.getMessage();
                  LOG.warn("FileChangeApplyService: " + msg, e);
                  Bus.notify(new Notification("TubiCopilot", "Agent Mode Error", msg, NotificationType.ERROR), project);
               }
            }
         }
      }, new PsiFile[0]);
      LOG.info("FileChangeApplyService: applyAll applied " + written[0] + " file(s) out of " + proposals.size() + " proposal(s)");
   }

   private static void applyInternal(@NotNull FileChangeProposal proposal, @NotNull Project project) throws IOException {
      VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
      if (baseDir == null) {
         throw new IOException("Cannot determine project base directory");
      }

      String relativePath = normalizeExtensionCase(proposal.getRelativePath());
      String absolutePath = baseDir.getPath() + "/" + relativePath;
      if (proposal.getOperation() == FileChangeProposal.Operation.DELETE) {
         VirtualFile existing = baseDir.findFileByRelativePath(relativePath);
         if (existing != null && existing.isValid()) {
            VirtualFile parent = existing.getParent();
            existing.delete(FileChangeApplyService.class);
            LOG.debug("FileChangeApplyService: deleted → " + relativePath);
            if (parent != null) {
               deleteEmptyAncestors(baseDir, parent);
            }
         } else {
            LOG.warn("FileChangeApplyService: DELETE target not found (already gone?): " + relativePath);
         }
      } else {
         VirtualFile vf;
         if (proposal.getOperation() == FileChangeProposal.Operation.MODIFY) {
            VirtualFile existing = baseDir.findFileByRelativePath(relativePath);
            if (existing != null && existing.isValid()) {
               vf = existing;
            } else {
               LOG.warn("FileChangeApplyService: MODIFY target not found, creating: " + relativePath);
               vf = createFile(absolutePath);
            }
         } else {
            vf = createFile(absolutePath);
         }

         VfsUtil.saveText(vf, proposal.getNewContent());
         vf.refresh(false, false);
         FileEditorManager.getInstance(project).openFile(vf, true);
         LOG.debug("FileChangeApplyService: wrote " + proposal.getOperation() + " → " + relativePath);
      }
   }

   @NotNull
   private static VirtualFile createFile(@NotNull String absolutePath) throws IOException {
      int lastSlash = absolutePath.lastIndexOf(47);
      String parentPath = absolutePath.substring(0, lastSlash);
      String fileName = absolutePath.substring(lastSlash + 1);
      VirtualFile parentVf = VfsUtil.createDirectories(parentPath);
      if (parentVf == null) {
         throw new IOException("Cannot create parent directory: " + parentPath);
      }

      VirtualFile existing = parentVf.findChild(fileName);
      return existing != null && existing.isValid() ? existing : parentVf.createChildData(FileChangeApplyService.class, fileName);
   }

   private static void deleteEmptyAncestors(@NotNull VirtualFile baseDir, @NotNull VirtualFile startDir) throws IOException {
      VirtualFile current = startDir;

      while (current != null && current.isValid() && !current.getPath().equals(baseDir.getPath())) {
         VirtualFile[] children = current.getChildren();
         if (children.length > 0) {
            break;
         }

         VirtualFile toDelete = current;
         current = current.getParent();
         toDelete.delete(FileChangeApplyService.class);
         LOG.debug("FileChangeApplyService: deleted empty folder → " + toDelete.getPath());
      }
   }

   @NotNull
   static String normalizeExtensionCase(@NotNull String relativePath) {
      int dot = relativePath.lastIndexOf(46);
      int slash = relativePath.lastIndexOf(47);
      return dot >= 0 && dot >= slash ? relativePath.substring(0, dot) + relativePath.substring(dot).toLowerCase(Locale.ROOT) : relativePath;
   }
}
