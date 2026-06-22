package com.tubitech.copilot.history.db;

import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.history.HistoryMessage;
import com.tubitech.copilot.history.MessageRole;
import com.tubitech.copilot.history.SearchResult;
import com.tubitech.copilot.history.SessionSummary;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HistoryDatabase {
   private static final Logger LOG = Logger.getInstance(HistoryDatabase.class);
   private static final int CURRENT_SCHEMA_VERSION = 3;
   private final Connection connection;
   private static volatile boolean driverLoaded;

   public HistoryDatabase(@NotNull Path dbPath) throws SQLException {
      ensureDriverLoaded();
      Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());

      try {
         this.connection = conn;
         this.configurePragmas();
         this.createSchema();
      } catch (SQLException | RuntimeException e) {
         conn.close();
         throw e;
      }
   }

   private static synchronized void ensureDriverLoaded() throws SQLException {
      if (!driverLoaded) {
         try {
            Class.forName("org.sqlite.JDBC");
            driverLoaded = true;
         } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
         }
      }
   }

   private void configurePragmas() throws SQLException {
      try (Statement stmt = this.connection.createStatement()) {
         stmt.execute("PRAGMA journal_mode = WAL");
         stmt.execute("PRAGMA foreign_keys = ON");
         stmt.execute("PRAGMA busy_timeout = 5000");
      }
   }

   private synchronized void createSchema() throws SQLException {
      try (Statement stmt = this.connection.createStatement()) {
         stmt.execute(
            "CREATE TABLE IF NOT EXISTS schema_version (\n    version    INTEGER NOT NULL,\n    applied_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)\n)"
         );
         int currentVersion = this.getSchemaVersion();
         if (currentVersion >= 3) {
            return;
         }

         if (currentVersion < 1) {
            stmt.execute(
               "CREATE TABLE IF NOT EXISTS sessions (\n    id                     TEXT    PRIMARY KEY,\n    title                  TEXT,\n    created_at             INTEGER NOT NULL,\n    updated_at             INTEGER NOT NULL,\n    archived               INTEGER NOT NULL DEFAULT 0,\n    server_conversation_id TEXT\n)"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sessions_updated ON sessions(archived, updated_at DESC)");
            stmt.execute(
               "CREATE TABLE IF NOT EXISTS messages (\n    id                TEXT    PRIMARY KEY,\n    session_id        TEXT    NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,\n    role              TEXT    NOT NULL,\n    content           TEXT,\n    visible           INTEGER NOT NULL DEFAULT 1,\n    turn_index        INTEGER NOT NULL,\n    created_at        INTEGER NOT NULL,\n    metadata          TEXT,\n    server_message_id TEXT\n)"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_session_turn ON messages(session_id, turn_index)");
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS messages_fts USING fts5(\n    content, content=messages, content_rowid=rowid\n)");
            stmt.execute(
               "CREATE TRIGGER IF NOT EXISTS messages_ai AFTER INSERT ON messages BEGIN\n    INSERT INTO messages_fts(rowid, content) VALUES (new.rowid, new.content);\nEND"
            );
            stmt.execute(
               "CREATE TRIGGER IF NOT EXISTS messages_ad AFTER DELETE ON messages BEGIN\n    INSERT INTO messages_fts(messages_fts, rowid, content)\n        VALUES('delete', old.rowid, old.content);\nEND"
            );
            stmt.execute(
               "CREATE TRIGGER IF NOT EXISTS messages_au AFTER UPDATE ON messages BEGIN\n    INSERT INTO messages_fts(messages_fts, rowid, content)\n        VALUES('delete', old.rowid, old.content);\n    INSERT INTO messages_fts(rowid, content) VALUES (new.rowid, new.content);\nEND"
            );
         }

         if (currentVersion >= 1 && currentVersion < 2) {
            try {
               stmt.execute("ALTER TABLE sessions ADD COLUMN server_conversation_id TEXT");
            } catch (SQLException e) {
               LOG.debug("Column sessions.server_conversation_id may already exist", e);
            }

            try {
               stmt.execute("ALTER TABLE messages ADD COLUMN server_message_id TEXT");
            } catch (SQLException e) {
               LOG.debug("Column messages.server_message_id may already exist", e);
            }
         }

         if (currentVersion < 3) {
            stmt.execute(
               "CREATE TABLE IF NOT EXISTS bundle_files (\n    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n    session_id      TEXT    NOT NULL,\n    local_path      TEXT    NOT NULL,\n    server_file_id  TEXT    NOT NULL,\n    label           TEXT    NOT NULL,\n    created_at      INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),\n    UNIQUE(session_id, label)\n)"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_bundle_files_session ON bundle_files(session_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_bundle_files_path ON bundle_files(session_id, local_path)");
         }

         try (PreparedStatement ps = this.connection.prepareStatement("INSERT INTO schema_version(version, applied_at) VALUES (?, ?)")) {
            ps.setInt(1, 3);
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
         }
      }
   }

   public synchronized int getSchemaVersion() {
      try (
         Statement stmt = this.connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_version");
      ) {
         return rs.next() ? rs.getInt(1) : 0;
      } catch (SQLException e) {
         return 0;
      }
   }

   public synchronized void insertSession(@NotNull String id, @Nullable String title, long createdAt, long updatedAt) throws SQLException {
      this.insertSession(id, title, createdAt, updatedAt, null);
   }

   public synchronized void insertSession(@NotNull String id, @Nullable String title, long createdAt, long updatedAt, @Nullable String serverConversationId) throws SQLException {
      try (PreparedStatement ps = this.connection
            .prepareStatement("INSERT OR REPLACE INTO sessions(id, title, created_at, updated_at, server_conversation_id) VALUES (?, ?, ?, ?, ?)")) {
         ps.setString(1, id);
         ps.setString(2, title);
         ps.setLong(3, createdAt);
         ps.setLong(4, updatedAt);
         ps.setString(5, serverConversationId);
         ps.executeUpdate();
      }
   }

   public synchronized void updateServerConversationId(@NotNull String sessionId, @NotNull String serverConversationId) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE sessions SET server_conversation_id = ? WHERE id = ?")) {
         ps.setString(1, serverConversationId);
         ps.setString(2, sessionId);
         ps.executeUpdate();
      }
   }

   public synchronized void updateSessionTitle(@NotNull String sessionId, @NotNull String title) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE sessions SET title = ? WHERE id = ?")) {
         ps.setString(1, title);
         ps.setString(2, sessionId);
         ps.executeUpdate();
      }
   }

   public synchronized void updateSessionTimestamp(@NotNull String sessionId, long updatedAt) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE sessions SET updated_at = ? WHERE id = ?")) {
         ps.setLong(1, updatedAt);
         ps.setString(2, sessionId);
         ps.executeUpdate();
      }
   }

   public synchronized void archiveSession(@NotNull String sessionId) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE sessions SET archived = 1 WHERE id = ?")) {
         ps.setString(1, sessionId);
         ps.executeUpdate();
      }
   }

   public synchronized void deleteSession(@NotNull String sessionId) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("DELETE FROM sessions WHERE id = ?")) {
         ps.setString(1, sessionId);
         ps.executeUpdate();
      }
   }

   public synchronized void insertMessage(@NotNull HistoryMessage msg) throws SQLException {
      try (PreparedStatement ps = this.connection
            .prepareStatement(
               "INSERT OR IGNORE INTO messages(id, session_id, role, content, visible, turn_index, created_at, metadata, server_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
         this.bindMessage(ps, msg);
         ps.executeUpdate();
      }
   }

   public synchronized void insertMessages(@NotNull List<HistoryMessage> messages) throws SQLException {
      if (!messages.isEmpty()) {
         this.connection.setAutoCommit(false);

         try (PreparedStatement ps = this.connection
               .prepareStatement(
                  "INSERT OR IGNORE INTO messages(id, session_id, role, content, visible, turn_index, created_at, metadata, server_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
               )) {
            for (HistoryMessage msg : messages) {
               this.bindMessage(ps, msg);
               ps.addBatch();
            }

            ps.executeBatch();
            this.connection.commit();
         } catch (SQLException e) {
            this.connection.rollback();
            throw e;
         } finally {
            this.connection.setAutoCommit(true);
         }
      }
   }

   public synchronized void updateMessageContent(@NotNull String messageId, @NotNull String content) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE messages SET content = ? WHERE id = ?")) {
         ps.setString(1, content);
         ps.setString(2, messageId);
         ps.executeUpdate();
      }
   }

   public synchronized void updateMessageServerIds(@NotNull String messageId, @Nullable String serverMessageId) throws SQLException {
      try (PreparedStatement ps = this.connection.prepareStatement("UPDATE messages SET server_message_id = ? WHERE id = ?")) {
         ps.setString(1, serverMessageId);
         ps.setString(2, messageId);
         ps.executeUpdate();
      }
   }

   @Nullable
   public synchronized String getLastServerMessageId(@NotNull String sessionId) {
      String sql = "SELECT server_message_id FROM messages WHERE session_id = ? AND server_message_id IS NOT NULL ORDER BY turn_index DESC LIMIT 1";

      try (PreparedStatement ps = this.connection
            .prepareStatement("SELECT server_message_id FROM messages WHERE session_id = ? AND server_message_id IS NOT NULL ORDER BY turn_index DESC LIMIT 1")) {
         ps.setString(1, sessionId);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: getLastServerMessageId failed", e);
         return null;
      }
   }

   @Nullable
   public synchronized String getServerConversationId(@NotNull String sessionId) {
      try (PreparedStatement ps = this.connection.prepareStatement("SELECT server_conversation_id FROM sessions WHERE id = ?")) {
         ps.setString(1, sessionId);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: getServerConversationId failed", e);
         return null;
      }
   }

   @Nullable
   public synchronized SessionSummary getMostRecentSession() {
      String sql = "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s\nWHERE s.archived = 0\nORDER BY s.updated_at DESC\nLIMIT 1";

      try (
         PreparedStatement ps = this.connection
            .prepareStatement(
               "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s\nWHERE s.archived = 0\nORDER BY s.updated_at DESC\nLIMIT 1"
            );
         ResultSet rs = ps.executeQuery();
      ) {
         if (rs.next()) {
            return new SessionSummary(rs.getString("id"), rs.getString("title"), rs.getLong("updated_at"), rs.getInt("msg_count"));
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: getMostRecentSession failed", e);
      }

      return null;
   }

   private void bindMessage(PreparedStatement ps, HistoryMessage msg) throws SQLException {
      ps.setString(1, msg.getId());
      ps.setString(2, msg.getSessionId());
      ps.setString(3, msg.getRole().toDbValue());
      ps.setString(4, msg.getContent());
      ps.setInt(5, msg.isVisible() ? 1 : 0);
      ps.setInt(6, msg.getTurnIndex());
      ps.setLong(7, msg.getCreatedAt());
      ps.setString(8, msg.getMetadata());
      ps.setString(9, msg.getServerMessageId());
   }

   @NotNull
   public synchronized List<SessionSummary> listSessions(int offset, int limit) {
      String sql = "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s\nWHERE s.archived = 0\nORDER BY s.updated_at DESC\nLIMIT ? OFFSET ?";

      List var14;
      try (PreparedStatement ps = this.connection
            .prepareStatement(
               "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s\nWHERE s.archived = 0\nORDER BY s.updated_at DESC\nLIMIT ? OFFSET ?"
            )) {
         ps.setInt(1, limit);
         ps.setInt(2, offset);
         List<SessionSummary> results = new ArrayList<>();

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               results.add(new SessionSummary(rs.getString("id"), rs.getString("title"), rs.getLong("updated_at"), rs.getInt("msg_count")));
            }
         }

         var14 = results;
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: listSessions failed", e);
         return Collections.emptyList();
      }

      return var14;
   }

   @NotNull
   public synchronized List<HistoryMessage> loadVisibleMessages(@NotNull String sessionId) {
      return this.loadMessages(sessionId, true);
   }

   @NotNull
   public synchronized List<HistoryMessage> loadAllMessages(@NotNull String sessionId) {
      return this.loadMessages(sessionId, false);
   }

   private List<HistoryMessage> loadMessages(String sessionId, boolean visibleOnly) {
      String sql = "SELECT * FROM messages WHERE session_id = ?" + (visibleOnly ? " AND visible = 1" : "") + " ORDER BY turn_index";

      try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
         ps.setString(1, sessionId);
         List<HistoryMessage> results = new ArrayList<>();

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               results.add(readMessage(rs));
            }
         }

         return results;
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: loadMessages failed for session " + sessionId, e);
         return Collections.emptyList();
      }
   }

   @NotNull
   public synchronized Optional<SessionSummary> findSession(@NotNull String sessionId) {
      String sql = "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s WHERE s.id = ?";

      try (PreparedStatement ps = this.connection
            .prepareStatement(
               "SELECT s.id, s.title, s.updated_at,\n       (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.id AND m.visible = 1) AS msg_count\nFROM sessions s WHERE s.id = ?"
            )) {
         ps.setString(1, sessionId);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return Optional.of(new SessionSummary(rs.getString("id"), rs.getString("title"), rs.getLong("updated_at"), rs.getInt("msg_count")));
            }
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: findSession failed for " + sessionId, e);
      }

      return Optional.empty();
   }

   public synchronized int getNextTurnIndex(@NotNull String sessionId) {
      String sql = "SELECT COALESCE(MAX(turn_index), -1) + 1 FROM messages WHERE session_id = ?";

      try (PreparedStatement ps = this.connection.prepareStatement("SELECT COALESCE(MAX(turn_index), -1) + 1 FROM messages WHERE session_id = ?")) {
         ps.setString(1, sessionId);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: getNextTurnIndex failed", e);
         return 0;
      }
   }

   @NotNull
   public synchronized List<SearchResult> search(@NotNull String query, int limit) {
      String sql = "SELECT m.id AS msg_id, m.session_id, m.created_at,\n       s.title,\n       snippet(messages_fts, 0, '<b>', '</b>', '...', 32) AS snip\nFROM messages_fts f\nJOIN messages m ON m.rowid = f.rowid\nJOIN sessions s ON s.id = m.session_id\nWHERE messages_fts MATCH ?\n  AND s.archived = 0\nORDER BY rank\nLIMIT ?";

      List var14;
      try (PreparedStatement ps = this.connection
            .prepareStatement(
               "SELECT m.id AS msg_id, m.session_id, m.created_at,\n       s.title,\n       snippet(messages_fts, 0, '<b>', '</b>', '...', 32) AS snip\nFROM messages_fts f\nJOIN messages m ON m.rowid = f.rowid\nJOIN sessions s ON s.id = m.session_id\nWHERE messages_fts MATCH ?\n  AND s.archived = 0\nORDER BY rank\nLIMIT ?"
            )) {
         ps.setString(1, query);
         ps.setInt(2, limit);
         List<SearchResult> results = new ArrayList<>();

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               results.add(
                  new SearchResult(rs.getString("session_id"), rs.getString("title"), rs.getString("msg_id"), rs.getString("snip"), rs.getLong("created_at"))
               );
            }
         }

         var14 = results;
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: search failed for query: " + query, e);
         return Collections.emptyList();
      }

      return var14;
   }

   public synchronized void clearAll() throws SQLException {
      try (Statement stmt = this.connection.createStatement()) {
         stmt.execute("DELETE FROM sessions");
      }
   }

   public synchronized void migrateSession(@NotNull String id, @Nullable String title, long createdAt, long updatedAt, @NotNull List<HistoryMessage> messages) throws SQLException {
      this.connection.setAutoCommit(false);

      try {
         this.insertSession(id, title, createdAt, updatedAt);
         if (!messages.isEmpty()) {
            try (PreparedStatement ps = this.connection
                  .prepareStatement(
                     "INSERT OR IGNORE INTO messages(id, session_id, role, content, visible, turn_index, created_at, metadata, server_message_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                  )) {
               for (HistoryMessage msg : messages) {
                  this.bindMessage(ps, msg);
                  ps.addBatch();
               }

               ps.executeBatch();
            }
         }

         this.connection.commit();
      } catch (SQLException e) {
         this.connection.rollback();
         throw e;
      } finally {
         this.connection.setAutoCommit(true);
      }
   }

   public synchronized void close() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException e) {
         LOG.warn("HistoryDatabase: failed to close connection", e);
      }
   }

   private static HistoryMessage readMessage(ResultSet rs) throws SQLException {
      return HistoryMessage.fromDb(
         rs.getString("id"),
         rs.getString("session_id"),
         MessageRole.fromDbValue(rs.getString("role")),
         rs.getString("content"),
         rs.getInt("visible") == 1,
         rs.getInt("turn_index"),
         rs.getLong("created_at"),
         rs.getString("metadata"),
         rs.getString("server_message_id")
      );
   }
}
