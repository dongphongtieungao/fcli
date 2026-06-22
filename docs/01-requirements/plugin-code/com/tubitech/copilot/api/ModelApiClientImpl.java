package com.tubitech.copilot.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.model.SupportedModel;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;

public final class ModelApiClientImpl implements ModelApiClient {
   private static final Logger LOG = Logger.getInstance(ModelApiClientImpl.class);
   private static final String MODELS_PATH = "/api/chat/v1/conversations/support-models";
   private static final Type SUPPORTED_MODEL_LIST_TYPE = (new TypeToken<List<SupportedModel>>() {}).getType();
   private final TokenManager tokenManager;
   private final TubiCopilotSettings settings;
   private final OkHttpClient httpClient;
   private final Gson gson;

   public ModelApiClientImpl(@NotNull TokenManager tokenManager, @NotNull TubiCopilotSettings settings) {
      this(tokenManager, settings, OkHttpClientFactory.newBuilder().connectTimeout(30L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).build());
   }

   ModelApiClientImpl(@NotNull TokenManager tokenManager, @NotNull TubiCopilotSettings settings, @NotNull OkHttpClient httpClient) {
      this.tokenManager = tokenManager;
      this.settings = settings;
      this.httpClient = httpClient;
      this.gson = new Gson();
   }

   @NotNull
   @Override
   public List<SupportedModel> fetchSupportedModels() throws IOException {
      String token = this.tokenManager.getValidAccessToken();
      String url = this.settings.baseUrl.replaceAll("/+$", "") + "/api/chat/v1/conversations/support-models";
      LOG.debug("Tubi Copilot: GET " + url);
      Request request = new Builder().url(url).get().header("Authorization", "Bearer " + token).header("Accept", "application/json").build();
      Response response = this.httpClient.newCall(request).execute();

      List var13;
      label80: {
         List<SupportedModel> models;
         label81: {
            List var9;
            try {
               int code = response.code();
               LOG.debug("Tubi Copilot: models API returned HTTP " + code);
               if (!response.isSuccessful()) {
                  ResponseBody errorBody = response.body();
                  String bodyText = errorBody != null ? errorBody.string() : "";
                  throw new IOException("HTTP " + code + ": " + bodyText);
               }

               ResponseBody body = response.body();
               if (body == null) {
                  LOG.warn("Tubi Copilot: models API returned empty body");
                  var13 = Collections.emptyList();
                  break label80;
               }

               String json = body.string();
               if (json.isBlank()) {
                  models = Collections.emptyList();
                  break label81;
               }

               models = (List<SupportedModel>)this.gson.fromJson(json, SUPPORTED_MODEL_LIST_TYPE);
               var9 = models != null ? models : Collections.emptyList();
            } catch (Throwable var11) {
               if (response != null) {
                  try {
                     response.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (response != null) {
               response.close();
            }

            return var9;
         }

         if (response != null) {
            response.close();
         }

         return models;
      }

      if (response != null) {
         response.close();
      }

      return var13;
   }
}
