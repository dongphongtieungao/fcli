package com.tubitech.copilot.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.OkHttpClientFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.FormBody.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TokenManagerImpl implements TokenManager {
   private static final Logger LOG = Logger.getInstance(TokenManagerImpl.class);
   private static final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/token";
   private static final String CLIENT_ID = "75bb3326-a75b-47b0-97f5-67638167d3b7";
   private static final String SCOPE = "api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access";
   private static final long REFRESH_BUFFER_MS = 60000L;
   private static final String REFRESH_TOKEN_SERVICE = "TubiCopilot.RefreshToken";
   @Nullable
   private volatile String cachedAccessToken = null;
   private volatile long accessTokenExpiresAt = 0L;
   private final OkHttpClient httpClient = OkHttpClientFactory.newBuilder()
      .connectTimeout(30L, TimeUnit.SECONDS)
      .readTimeout(30L, TimeUnit.SECONDS)
      .writeTimeout(30L, TimeUnit.SECONDS)
      .build();
   private final List<AuthStateListener> authStateListeners = new CopyOnWriteArrayList<>();

   @Override
   public synchronized String getValidAccessToken() throws IOException {
      return this.cachedAccessToken != null && System.currentTimeMillis() < this.accessTokenExpiresAt - 60000L ? this.cachedAccessToken : this.forceRefresh();
   }

   @Override
   public synchronized String forceRefresh() throws IOException {
      String refreshToken = readRefreshToken();
      if (refreshToken != null && !refreshToken.isBlank()) {
         RequestBody body = new Builder()
            .add("client_id", "75bb3326-a75b-47b0-97f5-67638167d3b7")
            .add("scope", "api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access")
            .add("grant_type", "refresh_token")
            .add("client_info", "1")
            .add("refresh_token", refreshToken)
            .build();
         Request request = new okhttp3.Request.Builder()
            .url("https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/token")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "*/*")
            .addHeader("Origin", "https://privategpt.fptconsulting.co.jp")
            .addHeader("Referer", "https://privategpt.fptconsulting.co.jp/")
            .build();
         LOG.debug("TokenManager: refreshing access token via OAuth2 endpoint");
         Response response = this.httpClient.newCall(request).execute();

         String var11;
         try {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
               String errorDesc = extractJsonField(responseBody, "error_description");
               String error = extractJsonField(responseBody, "error");
               String msg = errorDesc != null && !errorDesc.isBlank() ? errorDesc : (error != null ? error : "HTTP " + response.code());
               this.notifyAuthStatus(AuthStatus.LOGGED_OUT);
               throw new IOException(msg);
            }

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String newAccessToken = json.get("access_token").getAsString();
            long expiresIn = json.get("expires_in").getAsLong();
            String newRefreshToken = json.has("refresh_token") && !json.get("refresh_token").isJsonNull()
               ? json.get("refresh_token").getAsString()
               : refreshToken;
            this.cachedAccessToken = newAccessToken;
            this.accessTokenExpiresAt = System.currentTimeMillis() + expiresIn * 1000L;
            this.saveRefreshToken(newRefreshToken);
            LOG.debug("TokenManager: access token refreshed, expires in " + expiresIn + "s");
            this.notifyAuthStatus(AuthStatus.LOGGED_IN);
            var11 = this.cachedAccessToken;
         } catch (Throwable var13) {
            if (response != null) {
               try {
                  response.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (response != null) {
            response.close();
         }

         return var11;
      } else {
         this.notifyAuthStatus(AuthStatus.LOGGED_OUT);
         throw new IOException("Refresh token not configured. Open Settings → Tools → Tubi Copilot.");
      }
   }

   @Override
   public synchronized boolean isLoggedIn() {
      String rt = readRefreshToken();
      return rt != null && !rt.isBlank() ? this.cachedAccessToken != null && System.currentTimeMillis() < this.accessTokenExpiresAt - 60000L : false;
   }

   @Override
   public synchronized void logout() {
      this.cachedAccessToken = null;
      this.accessTokenExpiresAt = 0L;
      this.saveRefreshToken(null);
      this.notifyAuthStatus(AuthStatus.LOGGED_OUT);
   }

   @Override
   public synchronized void setCachedAccessToken(@NotNull String token, int expiresInSeconds) {
      this.cachedAccessToken = token;
      this.accessTokenExpiresAt = System.currentTimeMillis() + expiresInSeconds * 1000L;
   }

   @Override
   public void addAuthStateListener(@NotNull AuthStateListener listener) {
      this.authStateListeners.add(listener);
   }

   @Override
   public void removeAuthStateListener(@NotNull AuthStateListener listener) {
      this.authStateListeners.remove(listener);
   }

   @Override
   public void notifyAuthStatus(@NotNull AuthStatus status) {
      for (AuthStateListener l : this.authStateListeners) {
         SwingUtilities.invokeLater(() -> l.onAuthStatusChanged(status));
      }
   }

   @Nullable
   static String readRefreshToken() {
      CredentialAttributes attrs = buildRefreshTokenAttributes();
      Credentials credentials = PasswordSafe.getInstance().get(attrs);
      return credentials == null ? null : credentials.getPasswordAsString();
   }

   @Override
   public void saveRefreshToken(@Nullable String token) {
      CredentialAttributes attrs = buildRefreshTokenAttributes();
      if (token != null && !token.isBlank()) {
         PasswordSafe.getInstance().set(attrs, new Credentials("refresh_token", token));
      } else {
         PasswordSafe.getInstance().set(attrs, null);
      }
   }

   @NotNull
   public static CredentialAttributes buildRefreshTokenAttributes() {
      return new CredentialAttributes(CredentialAttributesKt.generateServiceName("TubiCopilot", "TubiCopilot.RefreshToken"));
   }

   @Nullable
   private static String extractJsonField(@Nullable String json, @NotNull String fieldName) {
      if (json != null && !json.isBlank()) {
         try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.has(fieldName) && !obj.get(fieldName).isJsonNull() ? obj.get(fieldName).getAsString() : null;
         } catch (Exception e) {
            return null;
         }
      } else {
         return null;
      }
   }
}
