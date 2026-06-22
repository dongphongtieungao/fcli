package com.tubitech.copilot.auth;

import java.io.IOException;

public interface TokenManager {
   String getValidAccessToken() throws IOException;

   String forceRefresh() throws IOException;

   boolean isLoggedIn();

   void logout();

   void setCachedAccessToken(String var1, int var2);

   void saveRefreshToken(String var1);

   void addAuthStateListener(AuthStateListener var1);

   void removeAuthStateListener(AuthStateListener var1);

   void notifyAuthStatus(AuthStatus var1);
}
