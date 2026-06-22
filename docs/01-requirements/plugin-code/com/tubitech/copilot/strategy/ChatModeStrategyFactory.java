package com.tubitech.copilot.strategy;

import com.tubitech.copilot.settings.ChatMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ChatModeStrategyFactory {
   private static final Map<ChatMode, ChatModeStrategy> STRATEGIES;

   private ChatModeStrategyFactory() {
   }

   @NotNull
   public static ChatModeStrategy get(@NotNull ChatMode mode) {
      return STRATEGIES.get(mode);
   }

   static {
      EnumMap<ChatMode, ChatModeStrategy> map = new EnumMap<>(ChatMode.class);
      map.put(ChatMode.ASK, new AskModeStrategy());
      map.put(ChatMode.AGENT, new AgentModeStrategy());
      map.put(ChatMode.PLAN, new PlanModeStrategy());
      STRATEGIES = Collections.unmodifiableMap(map);
   }
}
