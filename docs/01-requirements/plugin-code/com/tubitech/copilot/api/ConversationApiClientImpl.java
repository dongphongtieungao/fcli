package com.tubitech.copilot.api;

import com.google.gson.Gson;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.model.ChatRequest;
import com.tubitech.copilot.api.model.ConversationDetail;
import com.tubitech.copilot.api.model.ConversationListResponse;
import com.tubitech.copilot.api.model.ConversationSummary;
import com.tubitech.copilot.api.model.RenameConversationRequest;
import com.tubitech.copilot.api.streaming.SseEventListener;
import com.tubitech.copilot.auth.AuthStatus;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request.Builder;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

public final class ConversationApiClientImpl implements ConversationApiClient {
   private static final Logger LOG = Logger.getInstance(ConversationApiClientImpl.class);
   private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
   private static final String CONVERSATIONS_PATH = "/api/chat/v1/conversations";
   private final TubiCopilotSettings settings;
   private final OkHttpClient httpClient;
   private final Gson gson;
   private final AtomicReference<Call> currentCall = new AtomicReference<>();

   public ConversationApiClientImpl(@NotNull TubiCopilotSettings settings) {
      this(
         settings, OkHttpClientFactory.newBuilder().connectTimeout(settings.connectTimeoutSeconds, TimeUnit.SECONDS).readTimeout(0L, TimeUnit.SECONDS).build()
      );
   }

   ConversationApiClientImpl(@NotNull TubiCopilotSettings settings, @NotNull OkHttpClient httpClient) {
      this.settings = settings;
      this.httpClient = httpClient;
      this.gson = new Gson();
   }

   @Override
   public void sendMessage(@NotNull ChatRequest request, @NotNull final StreamListener listener) {
      String token;
      try {
         token = this.resolveToken();
      } catch (IOException e) {
         listener.onError(e);
         return;
      } catch (IllegalStateException e) {
         listener.onError(e);
         return;
      }

      final String url = this.buildUrl();
      String json = this.gson.toJson(request);
      LOG.debug("Tubi Copilot: POST " + url);
      RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
      final Request httpRequest = new Builder()
         .url(url)
         .post(body)
         .header("Authorization", "Bearer " + token)
         .header("Content-Type", "application/json")
         .header("Accept", "text/event-stream")
         .build();
      Call call = this.httpClient.newCall(httpRequest);
      this.currentCall.set(call);
      call.enqueue(new Callback() {
         public void onFailure(@NotNull Call call, @NotNull IOException e) {
            ConversationApiClientImpl.this.currentCall.set(null);
            ConversationApiClientImpl.LOG.warn("Tubi Copilot: request failed", e);
            listener.onError(e);
         }

         public void onResponse(@NotNull Call call, @NotNull Response response) {
            ConversationApiClientImpl.this.currentCall.set(null);
            ConversationApiClientImpl.LOG.debug("Tubi Copilot: HTTP " + response.code() + " from " + url);
            if (response.code() == 401) {
               response.close();
               Application app = ApplicationManager.getApplication();
               if (app == null) {
                  listener.onError(new IOException("HTTP 401 Unauthorized"));
               } else {
                  ConversationApiClientImpl.LOG.debug("Tubi Copilot: 401 received, attempting token refresh + retry");

                  try {
                     String newToken = ((TokenManager)app.getService(TokenManager.class)).forceRefresh();
                     Request retryRequest = httpRequest.newBuilder().header("Authorization", "Bearer " + newToken).build();
                     Response retryResponse = ConversationApiClientImpl.this.httpClient.newCall(retryRequest).execute();

                     label108: {
                        try {
                           if (retryResponse.code() == 401) {
                              ((TokenManager)app.getService(TokenManager.class)).notifyAuthStatus(AuthStatus.LOGGED_OUT);
                              listener.onError(new IOException("Session expired (HTTP 401)"));
                              break label108;
                           }

                           ConversationApiClientImpl.this.processStreamResponse(retryResponse, listener);
                        } catch (Throwable var11) {
                           if (retryResponse != null) {
                              try {
                                 retryResponse.close();
                              } catch (Throwable var9) {
                                 var11.addSuppressed(var9);
                              }
                           }

                           throw var11;
                        }

                        if (retryResponse != null) {
                           retryResponse.close();
                        }

                        return;
                     }

                     if (retryResponse != null) {
                        retryResponse.close();
                     }

                     return;
                  } catch (IOException ex) {
                     ConversationApiClientImpl.LOG.warn("Tubi Copilot: retry after 401 failed", ex);
                     listener.onError(ex);
                  } catch (RuntimeException ex) {
                     ConversationApiClientImpl.LOG.warn("Tubi Copilot: unexpected error during 401 retry", ex);
                     listener.onError(new IOException("401 retry failed", ex));
                  }
               }
            } else {
               try {
                  Response e = response;

                  try {
                     ConversationApiClientImpl.this.processStreamResponse(response, listener);
                  } catch (Throwable var14) {
                     if (e != null) {
                        try {
                           e.close();
                        } catch (Throwable var10) {
                           var14.addSuppressed(var10);
                        }
                     }

                     throw var14;
                  }

                  if (e != null) {
                     e.close();
                  }
               } catch (IOException e) {
                  ConversationApiClientImpl.LOG.warn("Tubi Copilot: stream read error", e);
                  listener.onError(e);
               }
            }
         }
      });
   }

   private void processStreamResponse(@NotNull Response response, @NotNull StreamListener listener) throws IOException {
      if (!response.isSuccessful()) {
         throw new IOException("HTTP " + response.code());
      }

      ResponseBody responseBody = response.body();
      if (responseBody == null) {
         throw new IOException("Empty response body");
      }

      SseEventListener sseListener = new SseEventListener(listener);
      BufferedSource source = responseBody.source();

      String line;
      while ((line = source.readUtf8Line()) != null) {
         sseListener.processLine(line);
      }

      sseListener.processEof();
   }

   @Override
   public void cancel() {
      Call call = this.currentCall.getAndSet(null);
      if (call != null) {
         call.cancel();
      }
   }

   @Override
   public List<ConversationSummary> listConversations(int offset, int limit) throws IOException {
      String token = ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).getValidAccessToken();
      String url = this.buildBaseUrl() + "/api/chat/v1/conversations?offset=" + offset + "&limit=" + limit;
      LOG.debug("Tubi Copilot: GET " + url);
      Request request = new Builder().url(url).get().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response response = this.httpClient.newCall(request).execute();

      List var13;
      label51: {
         List var10;
         try {
            if (!response.isSuccessful()) {
               throw new IOException("listConversations failed: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
               var13 = Collections.emptyList();
               break label51;
            }

            ConversationListResponse parsed = (ConversationListResponse)this.gson.fromJson(body.charStream(), ConversationListResponse.class);
            List<ConversationSummary> items = parsed.getItems();
            var10 = items != null ? items : Collections.emptyList();
         } catch (Throwable var12) {
            if (response != null) {
               try {
                  response.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (response != null) {
            response.close();
         }

         return var10;
      }

      if (response != null) {
         response.close();
      }

      return var13;
   }

   @Override
   public ConversationDetail getConversation(@NotNull String conversationId) throws IOException {
      String token = ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).getValidAccessToken();
      String url = this.buildBaseUrl() + "/api/chat/v1/conversations/" + conversationId;
      LOG.debug("Tubi Copilot: GET " + url);
      Request request = new Builder().url(url).get().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response response = this.httpClient.newCall(request).execute();

      ConversationDetail var7;
      try {
         if (!response.isSuccessful()) {
            throw new IOException("getConversation failed: HTTP " + response.code());
         }

         ResponseBody body = response.body();
         if (body == null) {
            throw new IOException("getConversation: empty response body");
         }

         var7 = (ConversationDetail)this.gson.fromJson(body.charStream(), ConversationDetail.class);
      } catch (Throwable var9) {
         if (response != null) {
            try {
               response.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (response != null) {
         response.close();
      }

      return var7;
   }

   @Override
   public void deleteConversation(@NotNull String conversationId) throws IOException {
      String token = ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).getValidAccessToken();
      String url = this.buildBaseUrl() + "/api/chat/v1/conversations/" + conversationId;
      LOG.debug("Tubi Copilot: DELETE " + url);
      Request request = new Builder().url(url).delete().header("Authorization", "Bearer " + token).build();
      Response response = this.httpClient.newCall(request).execute();

      try {
         int code = response.code();
         if (code != 200 && code != 204) {
            throw new IOException("Delete failed: HTTP " + code);
         }
      } catch (Throwable var9) {
         if (response != null) {
            try {
               response.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (response != null) {
         response.close();
      }
   }

   @Override
   public ConversationSummary renameConversation(@NotNull String conversationId, @NotNull String newName) throws IOException {
      String token = ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).getValidAccessToken();
      String url = this.buildBaseUrl() + "/api/chat/v1/conversations/" + conversationId;
      LOG.debug("Tubi Copilot: PUT " + url + " (rename → " + newName + ")");
      String json = this.gson.toJson(new RenameConversationRequest(newName));
      RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
      Request request = new Builder()
         .url(url)
         .put(body)
         .header("Authorization", "Bearer " + token)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .build();
      Response response = this.httpClient.newCall(request).execute();

      ConversationSummary var10;
      try {
         if (!response.isSuccessful()) {
            throw new IOException("renameConversation failed: HTTP " + response.code());
         }

         ResponseBody responseBody = response.body();
         if (responseBody == null) {
            throw new IOException("renameConversation: empty response body");
         }

         var10 = (ConversationSummary)this.gson.fromJson(responseBody.charStream(), ConversationSummary.class);
      } catch (Throwable var12) {
         if (response != null) {
            try {
               response.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (response != null) {
         response.close();
      }

      return var10;
   }

   @NotNull
   private String resolveToken() throws IOException {
      Application app = ApplicationManager.getApplication();
      if (app != null) {
         return ((TokenManager)app.getService(TokenManager.class)).getValidAccessToken();
      } else {
         String fallback = this.settings.getBearerToken();
         if (fallback != null && !fallback.isBlank()) {
            return fallback;
         } else {
            throw new IllegalStateException("No access token available — configure the plugin or provide a bearer token");
         }
      }
   }

   @NotNull
   private String buildUrl() {
      return this.buildBaseUrl() + "/api/chat/v1/conversations";
   }

   @NotNull
   private String buildBaseUrl() {
      return this.settings.baseUrl.replaceAll("/+$", "");
   }
}
