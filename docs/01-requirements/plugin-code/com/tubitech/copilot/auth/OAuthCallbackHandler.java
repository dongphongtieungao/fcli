package com.tubitech.copilot.auth;

@FunctionalInterface
public interface OAuthCallbackHandler {
   void onCodeReceived(String var1);
}
