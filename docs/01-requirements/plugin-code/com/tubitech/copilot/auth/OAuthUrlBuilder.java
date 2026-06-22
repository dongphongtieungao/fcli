package com.tubitech.copilot.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class OAuthUrlBuilder {
   public static final String TENANT_ID = "f01e930a-b52e-42b1-b70f-a8882b5d043b";
   public static final String CLIENT_ID = "75bb3326-a75b-47b0-97f5-67638167d3b7";
   public static final String SCOPES = "api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access";
   public static final String REDIRECT_URI_LOGIN = "http://localhost:7070/auth";
   public static final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/token";
   private static final String AUTHORIZE_ENDPOINT = "https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/authorize";

   private OAuthUrlBuilder() {
   }

   public static String buildAuthorizeUrl(String codeChallenge, String state, String nonce) {
      return "https://login.microsoftonline.com/f01e930a-b52e-42b1-b70f-a8882b5d043b/oauth2/v2.0/authorize?client_id="
         + enc("75bb3326-a75b-47b0-97f5-67638167d3b7")
         + "&response_type=code&response_mode=fragment&redirect_uri="
         + enc("http://localhost:7070/auth")
         + "&scope="
         + enc("api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access")
         + "&code_challenge="
         + enc(codeChallenge)
         + "&code_challenge_method=S256&state="
         + enc(state)
         + "&nonce="
         + enc(nonce)
         + "&prompt=select_account";
   }

   private static String enc(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }
}
