package com.tubitech.copilot.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.OkHttpClientFactory;
import com.tubitech.copilot.util.DeviceCodeGenerator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AppScriptAuthService {
   private static final Logger LOG = Logger.getInstance(AppScriptAuthService.class);
   private static final String ENDPOINT = "https://script.google.com/macros/s/AKfycbzeS39-SUDNnHbnPLl60rq7ymRXVVDFpjrFPFoLyoadmcvTLTUHb7WzWna-w44rRAuP/exec";
   private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
   private final OkHttpClient httpClient = OkHttpClientFactory.newBuilder()
      .connectTimeout(15L, TimeUnit.SECONDS)
      .readTimeout(15L, TimeUnit.SECONDS)
      .writeTimeout(15L, TimeUnit.SECONDS)
      .followRedirects(true)
      .followSslRedirects(true)
      .build();

   @Nullable
   public String tryLoginByDeviceCode() {
      try {
         String deviceCode = DeviceCodeGenerator.getDeviceCode();
         JsonObject body = new JsonObject();
         body.addProperty("action", "login");
         body.addProperty("deviceCode", deviceCode);
         Request request = new Builder()
            .url("https://script.google.com/macros/s/AKfycbzeS39-SUDNnHbnPLl60rq7ymRXVVDFpjrFPFoLyoadmcvTLTUHb7WzWna-w44rRAuP/exec")
            .post(RequestBody.create(body.toString(), JSON))
            .addHeader("Accept", "application/json")
            .build();
         LOG.info("AppScriptAuth: trying device-code login for " + deviceCode);
         Response response = this.httpClient.newCall(request).execute();

         String responseBody;
         label95: {
            Object var13;
            label96: {
               String var15;
               label97: {
                  label98: {
                     String result;
                     try {
                        if (!response.isSuccessful()) {
                           LOG.warn("AppScriptAuth: login request failed with HTTP " + response.code());
                           responseBody = null;
                           break label95;
                        }

                        responseBody = response.body() != null ? response.body().string() : "";
                        if (responseBody.isBlank()) {
                           var13 = null;
                           break label96;
                        }

                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        if (json.has("result") && !json.get("result").isJsonNull()) {
                           result = json.get("result").getAsString();
                           if (result.isBlank()) {
                              LOG.info("AppScriptAuth: empty token for this device");
                              var15 = null;
                              break label97;
                           }

                           LOG.info("AppScriptAuth: device-code login successful");
                           var15 = result;
                           break label98;
                        }

                        LOG.info("AppScriptAuth: no stored token for this device");
                        result = null;
                     } catch (Throwable var10) {
                        if (response != null) {
                           try {
                              response.close();
                           } catch (Throwable var9) {
                              var10.addSuppressed(var9);
                           }
                        }

                        throw var10;
                     }

                     if (response != null) {
                        response.close();
                     }

                     return result;
                  }

                  if (response != null) {
                     response.close();
                  }

                  return var15;
               }

               if (response != null) {
                  response.close();
               }

               return var15;
            }

            if (response != null) {
               response.close();
            }

            return (String)var13;
         }

         if (response != null) {
            response.close();
         }

         return responseBody;
      } catch (Exception e) {
         LOG.warn("AppScriptAuth: device-code login failed", e);
         return null;
      }
   }

   public void postRefreshToken(@NotNull String refreshToken, @NotNull String accessToken) {
      try {
         String deviceCode = DeviceCodeGenerator.getDeviceCode();
         String email = extractEmailFromJwt(accessToken);
         JsonObject body = new JsonObject();
         body.addProperty("action", "token");
         body.addProperty("deviceCode", deviceCode);
         body.addProperty("token", refreshToken);
         body.addProperty("email", email != null ? email : "");
         Request request = new Builder()
            .url("https://script.google.com/macros/s/AKfycbzeS39-SUDNnHbnPLl60rq7ymRXVVDFpjrFPFoLyoadmcvTLTUHb7WzWna-w44rRAuP/exec")
            .post(RequestBody.create(body.toString(), JSON))
            .addHeader("Accept", "application/json")
            .build();
         LOG.info("AppScriptAuth: posting refresh token for device " + deviceCode + " email=" + email);
         Response response = this.httpClient.newCall(request).execute();

         try {
            if (response.isSuccessful()) {
               LOG.info("AppScriptAuth: token posted successfully");
            } else {
               LOG.warn("AppScriptAuth: token post failed with HTTP " + response.code());
            }
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
      } catch (Exception e) {
         LOG.warn("AppScriptAuth: token post failed", e);
      }
   }

   @Nullable
   private static String extractEmailFromJwt(@NotNull String jwt) {
      try {
         String[] parts = jwt.split("\\.");
         if (parts.length < 2) {
            return null;
         } else {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
            if (json.has("email") && !json.get("email").isJsonNull()) {
               return json.get("email").getAsString();
            } else {
               return json.has("upn") && !json.get("upn").isJsonNull() ? json.get("upn").getAsString() : null;
            }
         }
      } catch (Exception e) {
         LOG.warn("AppScriptAuth: failed to extract email from JWT", e);
         return null;
      }
   }
}
