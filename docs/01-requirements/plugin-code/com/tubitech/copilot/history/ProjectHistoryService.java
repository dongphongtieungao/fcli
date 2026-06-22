package com.tubitech.copilot.history;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tubitech.copilot.history.db.HistoryDatabase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProjectHistoryService implements Disposable {
   private static final Logger LOG = Logger.getInstance(ProjectHistoryService.class);
   private final Project project;
   private volatile HistoryDatabase db;
   private volatile boolean migrated;
   private volatile boolean disposed;

   public ProjectHistoryService(@NotNull Project project) {
      this.project = project;
   }

   @NotNull
   public static ProjectHistoryService getInstance(@NotNull Project project) {
      return (ProjectHistoryService)project.getService(ProjectHistoryService.class);
   }

   public synchronized void init() {
      if (!this.disposed && this.db == null) {
         try {
            Path dbPath = this.resolveDbPath();
            Files.createDirectories(dbPath.getParent());
            this.db = new HistoryDatabase(dbPath);
            LOG.info("ProjectHistoryService: opened " + dbPath);
            this.migrateFromXmlIfNeeded();
         } catch (Exception e) {
            LOG.error("ProjectHistoryService: failed to initialize database", e);
         }
      }
   }

   @NotNull
   private Path resolveDbPath() {
      String basePath = this.project.getBasePath();
      if (basePath != null) {
         Path newPath = Path.of(basePath, ".tubi", "data", "history.db");
         migrateDbIfNeeded(Path.of(basePath, ".tubi", "history.db"), newPath);
         return newPath;
      } else {
         return Path.of(System.getProperty("user.home"), ".tubi", "data", "history.db");
      }
   }

   private static void migrateDbIfNeeded(@NotNull Path oldPath, @NotNull Path newPath) {
      if (Files.exists(oldPath) && !Files.exists(newPath)) {
         try {
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath);
            Path oldShm = oldPath.resolveSibling("history.db-shm");
            Path oldWal = oldPath.resolveSibling("history.db-wal");
            if (Files.exists(oldShm)) {
               Files.move(oldShm, newPath.resolveSibling("history.db-shm"));
            }

            if (Files.exists(oldWal)) {
               Files.move(oldWal, newPath.resolveSibling("history.db-wal"));
            }

            LOG.info("ProjectHistoryService: migrated DB from .tubi/ to .tubi/data/");
         } catch (IOException e) {
            LOG.warn("ProjectHistoryService: DB migration failed, using new path", e);
         }
      }
   }

   private void migrateFromXmlIfNeeded() {
      if (!this.migrated && this.db != null) {
         this.migrated = true;

         try {
            if (this.db.getSchemaVersion() < 1) {
               return;
            }

            if (!this.db.listSessions(0, 1).isEmpty()) {
               return;
            }

            ConversationHistory xmlHistory = ConversationHistory.getInstance();
            List<ConversationSession> xmlSessions = xmlHistory.getSessions();
            if (xmlSessions.isEmpty()) {
               return;
            }

            LOG.info("ProjectHistoryService: migrating " + xmlSessions.size() + " sessions from XML");
            int count = 0;

            for (ConversationSession session : xmlSessions) {
               try {
                  List<HistoryMessage> messages = convertMessages(session);
                  this.db.migrateSession(session.id, session.title, session.createdAt, session.updatedAt, messages);
                  count++;
               } catch (SQLException e) {
                  LOG.warn("ProjectHistoryService: failed to migrate session " + session.id, e);
               }
            }

            LOG.info("ProjectHistoryService: migrated " + count + "/" + xmlSessions.size() + " sessions");
         } catch (Exception e) {
            LOG.warn("ProjectHistoryService: XML migration failed", e);
         }
      }
   }

   @NotNull
   private static List<HistoryMessage> convertMessages(@NotNull ConversationSession session) {
      List<ConversationSession.ChatMessage> xmlMessages = session.getMessages();
      List<HistoryMessage> result = new ArrayList<>(xmlMessages.size());

      for (int i = 0; i < xmlMessages.size(); i++) {
         ConversationSession.ChatMessage cm = xmlMessages.get(i);
         if (cm.role == ConversationSession.Role.USER) {
            result.add(HistoryMessage.userMessage(session.id, i, cm.content, null));
         } else {
            result.add(HistoryMessage.assistantMessage(session.id, i, cm.content, null));
         }
      }

      return result;
   }

   @Nullable
   private synchronized HistoryDatabase getDb() {
      if (this.db == null) {
         this.init();
      }

      return this.db;
   }

   public void insertSession(@NotNull String id, @Nullable String title, long createdAt, long updatedAt) {
      this.insertSession(id, title, createdAt, updatedAt, null);
   }

   public void insertSession(@NotNull String id, @Nullable String title, long createdAt, long updatedAt, @Nullable String serverConversationId) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.insertSession(id, title, createdAt, updatedAt, serverConversationId);
         } catch (SQLException e) {
            LOG.warn("insertSession failed", e);
         }
      }
   }

   public void updateServerConversationId(@NotNull String sessionId, @NotNull String serverConversationId) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.updateServerConversationId(sessionId, serverConversationId);
         } catch (SQLException e) {
            LOG.warn("updateServerConversationId failed", e);
         }
      }
   }

   public void updateMessageContent(@NotNull String messageId, @NotNull String content) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.updateMessageContent(messageId, content);
         } catch (SQLException e) {
            LOG.warn("updateMessageContent failed", e);
         }
      }
   }

   public void updateMessageServerIds(@NotNull String messageId, @Nullable String serverMessageId) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.updateMessageServerIds(messageId, serverMessageId);
         } catch (SQLException e) {
            LOG.warn("updateMessageServerIds failed", e);
         }
      }
   }

   @Nullable
   public String getLastServerMessageId(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? null : d.getLastServerMessageId(sessionId);
   }

   @Nullable
   public String getServerConversationId(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? null : d.getServerConversationId(sessionId);
   }

   @Nullable
   public SessionSummary getMostRecentSession() {
      HistoryDatabase d = this.getDb();
      return d == null ? null : d.getMostRecentSession();
   }

   public void updateSessionTitle(@NotNull String sessionId, @NotNull String title) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.updateSessionTitle(sessionId, title);
         } catch (SQLException e) {
            LOG.warn("updateSessionTitle failed", e);
         }
      }
   }

   public void updateSessionTimestamp(@NotNull String sessionId, long updatedAt) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.updateSessionTimestamp(sessionId, updatedAt);
         } catch (SQLException e) {
            LOG.warn("updateSessionTimestamp failed", e);
         }
      }
   }

   public void archiveSession(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.archiveSession(sessionId);
         } catch (SQLException e) {
            LOG.warn("archiveSession failed", e);
         }
      }
   }

   public void deleteSession(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.deleteSession(sessionId);
         } catch (SQLException e) {
            LOG.warn("deleteSession failed", e);
         }
      }
   }

   public void insertMessage(@NotNull HistoryMessage msg) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.insertMessage(msg);
         } catch (SQLException e) {
            LOG.warn("insertMessage failed", e);
         }
      }
   }

   public void insertMessages(@NotNull List<HistoryMessage> messages) {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.insertMessages(messages);
         } catch (SQLException e) {
            LOG.warn("insertMessages failed", e);
         }
      }
   }

   @NotNull
   public List<SessionSummary> listSessions(int offset, int limit) {
      HistoryDatabase d = this.getDb();
      return d == null ? List.of() : d.listSessions(offset, limit);
   }

   @NotNull
   public Optional<SessionSummary> findSession(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? Optional.empty() : d.findSession(sessionId);
   }

   @NotNull
   public List<HistoryMessage> loadVisibleMessages(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? List.of() : d.loadVisibleMessages(sessionId);
   }

   @NotNull
   public List<HistoryMessage> loadAllMessages(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? List.of() : d.loadAllMessages(sessionId);
   }

   public int getNextTurnIndex(@NotNull String sessionId) {
      HistoryDatabase d = this.getDb();
      return d == null ? 0 : d.getNextTurnIndex(sessionId);
   }

   @NotNull
   public List<SearchResult> search(@NotNull String query, int limit) {
      HistoryDatabase d = this.getDb();
      return d == null ? List.of() : d.search(query, limit);
   }

   public void clearAll() {
      HistoryDatabase d = this.getDb();
      if (d != null) {
         try {
            d.clearAll();
         } catch (SQLException e) {
            LOG.warn("clearAll failed", e);
         }
      }
   }

   public synchronized void dispose() {
      this.disposed = true;
      if (this.db != null) {
         this.db.close();
         this.db = null;
         LOG.info("ProjectHistoryService: closed database for " + this.project.getName());
      }
   }
}
