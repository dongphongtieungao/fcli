package com.tubitech.copilot.api;

import com.tubitech.copilot.api.model.ChatRequest;
import com.tubitech.copilot.api.model.ConversationDetail;
import com.tubitech.copilot.api.model.ConversationSummary;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface ConversationApiClient {
   void sendMessage(ChatRequest var1, StreamListener var2);

   void cancel();

   List<ConversationSummary> listConversations(int var1, int var2) throws IOException;

   ConversationDetail getConversation(String var1) throws IOException;

   void deleteConversation(String var1) throws IOException;

   ConversationSummary renameConversation(@NotNull String var1, @NotNull String var2) throws IOException;
}
