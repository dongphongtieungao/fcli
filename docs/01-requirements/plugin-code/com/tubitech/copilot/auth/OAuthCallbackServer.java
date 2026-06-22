package com.tubitech.copilot.auth;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;

public final class OAuthCallbackServer {
   private static final Logger LOG = Logger.getInstance(OAuthCallbackServer.class);
   private static final int TIMEOUT_MINUTES = 5;
   @Nullable
   private HttpServer httpServer;
   private final AtomicBoolean stopped = new AtomicBoolean(false);
   private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "tubi-oauth-timeout");
      t.setDaemon(true);
      return t;
   });

   public void start(OAuthCallbackHandler handler) throws IOException {
      this.httpServer = HttpServer.create(new InetSocketAddress("localhost", 7070), 0);
      this.httpServer.setExecutor(Executors.newCachedThreadPool(r -> {
         Thread t = new Thread(r, "tubi-oauth-server");
         t.setDaemon(true);
         return t;
      }));
      this.httpServer.createContext("/auth/code", exchange -> this.handleCodeRequest(exchange, handler));
      this.httpServer.createContext("/auth", exchange -> handleFragmentCapturePage(exchange));
      this.httpServer.start();
      LOG.info("Tubi Copilot: OAuth2 callback server started on localhost:7070");
      this.timeoutExecutor
         .schedule(
            () -> {
               if (!this.stopped.get()) {
                  LOG.info("Tubi Copilot: OAuth2 callback server timed out after 5 min");
                  this.stop();
                  ApplicationManager.getApplication()
                     .executeOnPooledThread(
                        () -> ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).notifyAuthStatus(AuthStatus.LOGGED_OUT)
                     );
               }
            },
            5L,
            TimeUnit.MINUTES
         );
   }

   public void stop() {
      if (this.stopped.compareAndSet(false, true)) {
         this.timeoutExecutor.shutdownNow();
         if (this.httpServer != null) {
            this.httpServer.stop(0);
            LOG.info("Tubi Copilot: OAuth2 callback server stopped");
         }
      }
   }

   private static void handleFragmentCapturePage(HttpExchange exchange) throws IOException {
      String html = "<!DOCTYPE html>\n<html>\n<head><title>Tubi Copilot — Logging in…</title></head>\n<body>\n<p>Completing login, please wait…</p>\n<script>\n  var hash = window.location.hash.substring(1);\n  var params = new URLSearchParams(hash);\n  var code = params.get('code');\n  if (code) {\n    window.location.href = '/auth/code?code=' + encodeURIComponent(code);\n  } else {\n    document.body.innerText = 'Login failed: no code returned.';\n  }\n</script>\n</body>\n</html>";
      sendHtml(
         exchange,
         200,
         "<!DOCTYPE html>\n<html>\n<head><title>Tubi Copilot — Logging in…</title></head>\n<body>\n<p>Completing login, please wait…</p>\n<script>\n  var hash = window.location.hash.substring(1);\n  var params = new URLSearchParams(hash);\n  var code = params.get('code');\n  if (code) {\n    window.location.href = '/auth/code?code=' + encodeURIComponent(code);\n  } else {\n    document.body.innerText = 'Login failed: no code returned.';\n  }\n</script>\n</body>\n</html>"
      );
   }

   private void handleCodeRequest(HttpExchange exchange, OAuthCallbackHandler handler) throws IOException {
      String query = exchange.getRequestURI().getQuery();
      String code = extractQueryParam(query, "code");
      if (code != null && !code.isBlank()) {
         sendHtml(exchange, 200, "<html><body><h2>Login successful!</h2><p>You may close this tab and return to your IDE.</p></body></html>");
         ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
               handler.onCodeReceived(code);
            } catch (Exception e) {
               LOG.warn("Tubi Copilot: OAuth callback handler threw", e);
            } finally {
               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var11) {
               }

               this.stop();
            }
         });
      } else {
         sendHtml(exchange, 400, "<html><body>Login failed: no code in request.</body></html>");
      }
   }

   private static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
      byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
      exchange.sendResponseHeaders(statusCode, bytes.length);

      try (OutputStream os = exchange.getResponseBody()) {
         os.write(bytes);
      }
   }

   @Nullable
   private static String extractQueryParam(@Nullable String query, String name) {
      if (query != null && !query.isBlank()) {
         for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && name.equals(kv[0])) {
               return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
