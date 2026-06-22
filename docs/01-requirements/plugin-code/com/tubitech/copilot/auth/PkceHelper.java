package com.tubitech.copilot.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PkceHelper {
   private PkceHelper() {
   }

   public static String generateCodeVerifier() {
      byte[] bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
   }

   public static String generateCodeChallenge(String codeVerifier) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
         return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException("SHA-256 not available", e);
      }
   }
}
