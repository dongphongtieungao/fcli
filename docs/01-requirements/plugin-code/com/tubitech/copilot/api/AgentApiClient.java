package com.tubitech.copilot.api;

import com.tubitech.copilot.api.model.AgentDetail;
import com.tubitech.copilot.api.model.AgentFile;
import com.tubitech.copilot.api.model.AgentSummary;
import com.tubitech.copilot.api.model.CreateAgentRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface AgentApiClient {
   @NotNull
   List<AgentSummary> listMyAgents() throws IOException;

   @NotNull
   AgentDetail createAgent(@NotNull CreateAgentRequest var1) throws IOException;

   @NotNull
   AgentDetail updateAgent(@NotNull String var1, @NotNull CreateAgentRequest var2) throws IOException;

   @NotNull
   List<AgentFile> listAgentFiles(@NotNull String var1) throws IOException;

   @NotNull
   AgentFile uploadAgentFile(@NotNull String var1, @NotNull File var2, @NotNull String var3) throws IOException;

   @NotNull
   AgentDetail getAgent(@NotNull String var1) throws IOException;

   void deleteAgentFile(@NotNull String var1, @NotNull String var2) throws IOException;
}
