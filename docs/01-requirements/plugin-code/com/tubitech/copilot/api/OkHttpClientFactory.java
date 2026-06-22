package com.tubitech.copilot.api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient.Builder;

public final class OkHttpClientFactory {
   private static final Logger LOG = Logger.getLogger(OkHttpClientFactory.class.getName());

   private OkHttpClientFactory() {
   }

   public static Builder newBuilder() {
      try {
         TrustManagerFactory jvmTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         jvmTmf.init((KeyStore)null);
         KeyStore osKeyStore = null;

         try {
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("win")) {
               osKeyStore = KeyStore.getInstance("Windows-ROOT");
               osKeyStore.load(null, null);
            } else if (osName.contains("mac")) {
               osKeyStore = KeyStore.getInstance("KeychainStore");
               osKeyStore.load(null, null);
            }
         } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            LOG.fine("OkHttpClientFactory: OS keystore unavailable (" + e.getClass().getSimpleName() + "), using JVM cacerts only");
         }

         TrustManagerFactory osTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         osTmf.init(osKeyStore);
         X509TrustManager jvmTm = pickX509(jvmTmf);
         X509TrustManager osTm = pickX509(osTmf);
         X509TrustManager composite = new OkHttpClientFactory.CompositeX509TrustManager(jvmTm, osTm);
         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, new TrustManager[]{composite}, null);
         return new Builder().sslSocketFactory(sslContext.getSocketFactory(), composite);
      } catch (GeneralSecurityException | IllegalStateException e) {
         LOG.warning("OkHttpClientFactory: failed to build merged SSL context, falling back to default. Error: " + e.getMessage());
         return new Builder();
      }
   }

   private static X509TrustManager pickX509(TrustManagerFactory tmf) {
      for (TrustManager tm : tmf.getTrustManagers()) {
         if (tm instanceof X509TrustManager) {
            return (X509TrustManager)tm;
         }
      }

      throw new IllegalStateException("No X509TrustManager found in TrustManagerFactory");
   }

   private static final class CompositeX509TrustManager implements X509TrustManager {
      private final X509TrustManager[] delegates;

      CompositeX509TrustManager(X509TrustManager... delegates) {
         this.delegates = delegates;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         this.checkAll(chain, authType, true);
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         this.checkAll(chain, authType, false);
      }

      private void checkAll(X509Certificate[] chain, String authType, boolean client) throws CertificateException {
         CertificateException last = null;

         for (X509TrustManager tm : this.delegates) {
            try {
               if (client) {
                  tm.checkClientTrusted(chain, authType);
               } else {
                  tm.checkServerTrusted(chain, authType);
               }

               return;
            } catch (CertificateException e) {
               last = e;
            }
         }

         if (last != null) {
            throw last;
         } else {
            throw new CertificateException("No trust manager delegates available");
         }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         return Arrays.stream(this.delegates).flatMap(tm -> Arrays.stream(tm.getAcceptedIssuers())).toArray(X509Certificate[]::new);
      }
   }
}
