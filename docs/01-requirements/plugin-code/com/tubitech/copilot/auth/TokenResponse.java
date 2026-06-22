package com.tubitech.copilot.auth;

public final class TokenResponse {
   private final String accessToken;
   private final String refreshToken;
   private final int expiresIn;
   private final String tokenType;

   public TokenResponse(String accessToken, String refreshToken, int expiresIn, String tokenType) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.expiresIn = expiresIn;
      this.tokenType = tokenType;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public String getRefreshToken() {
      return this.refreshToken;
   }

   public int getExpiresIn() {
      return this.expiresIn;
   }

   public String getTokenType() {
      return this.tokenType;
   }
}
