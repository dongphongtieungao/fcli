package com.tubitech.copilot.history;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "TubiCopilotHistory", storages = @Storage("TubiCopilotHistory.xml"))
public class ConversationHistory implements PersistentStateComponent<ConversationHistory> {
   public List<ConversationSession> sessions = new ArrayList<>();

   @NotNull
   public static ConversationHistory getInstance() {
      return (ConversationHistory)ApplicationManager.getApplication().getService(ConversationHistory.class);
   }

   @Nullable
   public ConversationHistory getState() {
      return this;
   }

   public void loadState(@NotNull ConversationHistory state) {
      XmlSerializerUtil.copyBean(state, this);
   }

   public synchronized void addSession(@NotNull ConversationSession session) {
      this.addSession(session, TubiCopilotSettings.getInstance().maxSavedSessions);
   }

   synchronized void addSession(@NotNull ConversationSession session, int maxSessions) {
      this.sessions.removeIf(s -> Objects.equals(s.id, session.id));
      this.sessions.add(0, session);

      while (this.sessions.size() > maxSessions) {
         this.sessions.remove(this.sessions.size() - 1);
      }
   }

   public synchronized void removeSession(@NotNull String sessionId) {
      this.sessions.removeIf(s -> Objects.equals(s.id, sessionId));
   }

   public synchronized void clearAll() {
      this.sessions.clear();
   }

   @NotNull
   public synchronized List<ConversationSession> getSessions() {
      return this.sessions.stream().sorted(Comparator.<ConversationSession>comparingLong(s -> s.updatedAt).reversed()).collect(Collectors.toUnmodifiableList());
   }

   @NotNull
   public synchronized Optional<ConversationSession> findById(@NotNull String id) {
      return this.sessions.stream().filter(s -> Objects.equals(s.id, id)).findFirst();
   }
}
