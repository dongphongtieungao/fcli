package com.tubitech.copilot.auth;

@FunctionalInterface
public interface AuthStateListener {
   void onAuthStatusChanged(AuthStatus var1);
}
