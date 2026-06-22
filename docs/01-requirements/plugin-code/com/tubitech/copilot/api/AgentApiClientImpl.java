package com.tubitech.copilot.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.model.AgentDetail;
import com.tubitech.copilot.api.model.AgentFile;
import com.tubitech.copilot.api.model.AgentListResponse;
import com.tubitech.copilot.api.model.AgentSummary;
import com.tubitech.copilot.api.model.CreateAgentRequest;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;

public final class AgentApiClientImpl implements AgentApiClient {
   private static final Logger LOG = Logger.getInstance(AgentApiClientImpl.class);
   private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
   private static final MediaType OCTET_STREAM = MediaType.get("application/octet-stream");
   private static final String AGENTS_PATH = "/api/chat/v1/agents";
   private static final String AGENTS_DISCOVERY_PATH = "/api/chat/v1/agents/discovery/created_by";
   private static final int PAGE_SIZE = 50;
   private final TubiCopilotSettings settings;
   private final OkHttpClient httpClient;
   private final Gson gson;

   public AgentApiClientImpl(@NotNull TubiCopilotSettings settings) {
      this.settings = settings;
      this.httpClient = OkHttpClientFactory.newBuilder()
         .connectTimeout(settings.connectTimeoutSeconds, TimeUnit.SECONDS)
         .readTimeout(30L, TimeUnit.SECONDS)
         .build();
      this.gson = new Gson();
   }

   @NotNull
   @Override
   public List<AgentSummary> listMyAgents() throws IOException {
      String token = this.getToken();
      List<AgentSummary> all = new ArrayList<>();
      int offset = 0;

      while (true) {
         String url = this.baseUrl() + "/api/chat/v1/agents/discovery/created_by?offset=" + offset + "&limit=50";
         Request req = new Builder().url(url).get().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
         Response resp = this.httpClient.newCall(req).execute();

         label57: {
            try {
               if (!resp.isSuccessful()) {
                  throw new IOException("listMyAgents HTTP " + resp.code());
               }

               ResponseBody body = resp.body();
               if (body == null) {
                  throw new IOException("Empty response body");
               }

               AgentListResponse page = (AgentListResponse)this.gson.fromJson(body.charStream(), AgentListResponse.class);
               if (page.getItems() != null) {
                  all.addAll(page.getItems());
               }

               if (page.getItems() == null || page.getItems().size() < 50) {
                  break label57;
               }

               offset += 50;
            } catch (Throwable var10) {
               if (resp != null) {
                  try {
                     resp.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (resp != null) {
               resp.close();
            }
            continue;
         }

         if (resp != null) {
            resp.close();
         }

         LOG.debug("Tubi Copilot: fetched " + all.size() + " agents");
         return all;
      }
   }

   @NotNull
   @Override
   public AgentDetail createAgent(@NotNull CreateAgentRequest request) throws IOException {
      String token = this.getToken();
      String json = this.gson.toJson(request);
      Request req = new Builder()
         .url(this.baseUrl() + "/api/chat/v1/agents")
         .post(RequestBody.create(json, JSON))
         .header("Authorization", "Bearer " + token)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .build();
      Response resp = this.httpClient.newCall(req).execute();

      AgentDetail var8;
      try {
         if (!resp.isSuccessful()) {
            throw new IOException("createAgent HTTP " + resp.code());
         }

         ResponseBody body = resp.body();
         if (body == null) {
            throw new IOException("Empty response body");
         }

         AgentDetail detail = (AgentDetail)this.gson.fromJson(body.charStream(), AgentDetail.class);
         LOG.debug("Tubi Copilot: agent created id=" + detail.getId());
         var8 = detail;
      } catch (Throwable var10) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (resp != null) {
         resp.close();
      }

      return var8;
   }

   @NotNull
   @Override
   public AgentDetail updateAgent(@NotNull String agentId, @NotNull CreateAgentRequest request) throws IOException {
      String token = this.getToken();
      String json = this.gson.toJson(request);
      Request req = new Builder()
         .url(this.baseUrl() + "/api/chat/v1/agents/" + agentId)
         .put(RequestBody.create(json, JSON))
         .header("Authorization", "Bearer " + token)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .build();
      Response resp = this.httpClient.newCall(req).execute();

      AgentDetail var9;
      try {
         if (!resp.isSuccessful()) {
            throw new IOException("updateAgent HTTP " + resp.code());
         }

         ResponseBody body = resp.body();
         if (body == null) {
            throw new IOException("Empty response body");
         }

         AgentDetail detail = (AgentDetail)this.gson.fromJson(body.charStream(), AgentDetail.class);
         LOG.debug("Tubi Copilot: agent updated id=" + detail.getId());
         var9 = detail;
      } catch (Throwable var11) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (resp != null) {
         resp.close();
      }

      return var9;
   }

   @NotNull
   @Override
   public AgentDetail getAgent(@NotNull String agentId) throws IOException {
      String token = this.getToken();
      Request req = new Builder()
         .url(this.baseUrl() + "/api/chat/v1/agents/" + agentId)
         .get()
         .header("Authorization", "Bearer " + token)
         .header("Accept", "application/json")
         .build();
      Response resp = this.httpClient.newCall(req).execute();

      AgentDetail var7;
      try {
         if (!resp.isSuccessful()) {
            throw new IOException("getAgent HTTP " + resp.code());
         }

         ResponseBody body = resp.body();
         if (body == null) {
            throw new IOException("getAgent: empty response body");
         }

         AgentDetail detail = (AgentDetail)this.gson.fromJson(body.charStream(), AgentDetail.class);
         LOG.debug("Tubi Copilot: fetched agent id=" + detail.getId());
         var7 = detail;
      } catch (Throwable var9) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (resp != null) {
         resp.close();
      }

      return var7;
   }

   @NotNull
   @Override
   public List<AgentFile> listAgentFiles(@NotNull String agentId) throws IOException {
      String token = this.getToken();
      String url = this.baseUrl() + "/api/chat/v1/agents/" + agentId + "/files";
      Request req = new Builder().url(url).get().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response resp = this.httpClient.newCall(req).execute();

      List var9;
      try {
         if (!resp.isSuccessful()) {
            throw new IOException("listAgentFiles HTTP " + resp.code());
         }

         ResponseBody body = resp.body();
         if (body == null) {
            throw new IOException("Empty response body");
         }

         Type listType = (new TypeToken<List<AgentFile>>() {}).getType();
         List<AgentFile> files = (List<AgentFile>)this.gson.fromJson(body.charStream(), listType);
         LOG.debug("Tubi Copilot: fetched " + files.size() + " files for agent " + agentId);
         var9 = files;
      } catch (Throwable var11) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (resp != null) {
         resp.close();
      }

      return var9;
   }

   @NotNull
   @Override
   public AgentFile uploadAgentFile(@NotNull String agentId, @NotNull File file, @NotNull String fileName) throws IOException {
      String token = this.getToken();
      String url = this.baseUrl() + "/api/chat/v1/agents/" + agentId + "/files/upload";
      RequestBody fileBody = RequestBody.create(file, OCTET_STREAM);
      RequestBody multipart = new okhttp3.MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", fileName, fileBody).build();
      Request req = new Builder().url(url).post(multipart).header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response resp = this.httpClient.newCall(req).execute();

      AgentFile var12;
      try {
         if (!resp.isSuccessful()) {
            throw new IOException("uploadAgentFile HTTP " + resp.code());
         }

         ResponseBody body = resp.body();
         if (body == null) {
            throw new IOException("Empty response body");
         }

         AgentFile agentFile = (AgentFile)this.gson.fromJson(body.charStream(), AgentFile.class);
         LOG.debug("Tubi Copilot: uploaded file '" + fileName + "' id=" + agentFile.id);
         var12 = agentFile;
      } catch (Throwable var14) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }
         }

         throw var14;
      }

      if (resp != null) {
         resp.close();
      }

      return var12;
   }

   @Override
   public void deleteAgentFile(@NotNull String agentId, @NotNull String fileId) throws IOException {
      String token = this.getToken();
      String url = this.baseUrl() + "/api/chat/v1/agents/" + agentId + "/files/" + fileId;
      Request req = new Builder().url(url).delete().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response resp = this.httpClient.newCall(req).execute();

      try {
         if (!resp.isSuccessful()) {
            throw new IOException("deleteAgentFile HTTP " + resp.code());
         }

         LOG.debug("Tubi Copilot: deleted file id=" + fileId + " from agent " + agentId);
      } catch (Throwable var10) {
         if (resp != null) {
            try {
               resp.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (resp != null) {
         resp.close();
      }
   }

   @NotNull
   private String getToken() throws IOException {
      return ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).getValidAccessToken();
   }

   @NotNull
   private String baseUrl() {
      return this.settings.baseUrl.replaceAll("/+$", "");
   }
}
