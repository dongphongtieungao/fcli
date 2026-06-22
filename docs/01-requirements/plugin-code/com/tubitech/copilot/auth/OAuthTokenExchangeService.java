package com.tubitech.copilot.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tubitech.copilot.api.OkHttpClientFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.FormBody.Builder;

public final class OAuthTokenExchangeService {
   private final OkHttpClient httpClient = OkHttpClientFactory.newBuilder()
      .connectTimeout(30L, TimeUnit.SECONDS)
      .readTimeout(30L, TimeUnit.SECONDS)
      .writeTimeout(30L, TimeUnit.SECONDS)
      .build();

   public TokenResponse exchangeCode(String code, String codeVerifier) throws IOException {
      FormBody body = new Builder()
         .add("grant_type", "authorization_code")
         .add("client_id", "75bb3326-a75b-47b0-97f5-67638167d3b7")
         .add("redirect_uri", "http://localhost:7070/auth")
         .add("scope", "api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access")
         .add("code", code)
         .add("code_verifier", codeVerifier)
         .add("client_info", "1")
         .build();
      Request request = new okhttp3.Request.Builder()
         .url("https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/token")
         .post(body)
         .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
         .addHeader("Accept", "*/*")
         .addHeader("Origin", "https://privategpt.fptconsulting.co.jp")
         .addHeader("Referer", "https://privategpt.fptconsulting.co.jp/")
         .build();
      Response response = this.httpClient.newCall(request).execute();

      TokenResponse var12;
      try {
         String responseBody = response.body() != null ? response.body().string() : "";
         if (!response.isSuccessful()) {
            String errDesc = extractField(responseBody, "error_description");
            String err = extractField(responseBody, "error");
            String msg = errDesc != null && !errDesc.isBlank() ? errDesc : (err != null ? err : "HTTP " + response.code());
            throw new IOException("Token exchange failed: " + msg);
         }

         JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
         String accessToken = json.get("access_token").getAsString();
         String refreshToken = json.has("refresh_token") && !json.get("refresh_token").isJsonNull() ? json.get("refresh_token").getAsString() : null;
         int expiresIn = json.has("expires_in") ? json.get("expires_in").getAsInt() : 3600;
         String tokenType = json.has("token_type") ? json.get("token_type").getAsString() : "Bearer";
         var12 = new TokenResponse(accessToken, refreshToken, expiresIn, tokenType);
      } catch (Throwable var14) {
         if (response != null) {
            try {
               response.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }
         }

         throw var14;
      }

      if (response != null) {
         response.close();
      }

      return var12;
   }

   private static String extractField(String json, String field) {
      if (json != null && !json.isBlank()) {
         try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
         } catch (Exception e) {
            return null;
         }
      } else {
         return null;
      }
   }
}
