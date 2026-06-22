package com.tubitech.copilot.ui;

import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.Process;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.tubitech.copilot.auth.AppScriptAuthService;
import com.tubitech.copilot.auth.AuthStateListener;
import com.tubitech.copilot.auth.AuthStatus;
import com.tubitech.copilot.auth.OAuthCallbackServer;
import com.tubitech.copilot.auth.OAuthTokenExchangeService;
import com.tubitech.copilot.auth.OAuthUrlBuilder;
import com.tubitech.copilot.auth.PkceHelper;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.auth.TokenResponse;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.BindException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AuthToolbarPanel extends JPanel implements Disposable {
   private static final Logger LOG = Logger.getInstance(AuthToolbarPanel.class);
   private final JButton authButton = new JButton();
   private final JLabel statusLabel = new JLabel();
   private volatile AuthStatus currentStatus = AuthStatus.LOGGED_OUT;
   private final AuthStateListener stateListener;
   private final AtomicReference<OAuthCallbackServer> callbackServer = new AtomicReference<>();
   @Nullable
   private Consumer<String> errorNotifier;

   public AuthToolbarPanel(@Nullable Consumer<String> errorNotifier) {
      super(new FlowLayout(0, 6, 0));
      this.errorNotifier = errorNotifier;
      this.setOpaque(false);
      this.authButton.setBorderPainted(false);
      this.authButton.setContentAreaFilled(false);
      this.authButton.setFocusPainted(false);
      this.authButton.setMargin(JBUI.emptyInsets());
      Dimension btnSize = JBUI.size(28, 28);
      this.authButton.setPreferredSize(btnSize);
      this.authButton.setMinimumSize(btnSize);
      this.authButton.setMaximumSize(btnSize);
      this.authButton.setCursor(Cursor.getPredefinedCursor(12));
      this.statusLabel.setFont(this.statusLabel.getFont().deriveFont(0, 9.0F));
      this.add(this.authButton);
      this.add(this.statusLabel);
      TokenManager tm = tokenManager();
      this.applyStatus(tm.isLoggedIn() ? AuthStatus.LOGGED_IN : AuthStatus.LOGGED_OUT);
      this.stateListener = this::applyStatus;
      tm.addAuthStateListener(this.stateListener);
      this.authButton.addActionListener(e -> this.handleButtonClick());
   }

   public void dispose() {
      tokenManager().removeAuthStateListener(this.stateListener);
      this.stopCallbackServer();
   }

   @Override
   public void removeNotify() {
      super.removeNotify();
      this.dispose();
   }

   private void handleButtonClick() {
      switch (this.currentStatus) {
         case LOGGED_IN:
            this.handleLogout();
            break;
         case LOGGED_OUT:
            this.handleLogin();
            break;
         case LOGGING_IN:
            this.handleCancel();
      }
   }

   private void handleLogout() {
      ApplicationManager.getApplication().executeOnPooledThread(() -> tokenManager().logout());
   }

   private void handleLogin() {
      this.applyStatus(AuthStatus.LOGGING_IN);
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
         AppScriptAuthService appScript = new AppScriptAuthService();
         String storedToken = appScript.tryLoginByDeviceCode();
         if (storedToken != null && !storedToken.isBlank()) {
            TokenManager tm = tokenManager();
            tm.saveRefreshToken(storedToken);

            try {
               tm.forceRefresh();
               LOG.info("Tubi Copilot: logged in via device-code shortcut");
               return;
            } catch (Exception ex) {
               LOG.warn("Tubi Copilot: device-code token invalid, falling back to browser", ex);
            }
         }

         SwingUtilities.invokeLater(() -> this.startBrowserLogin(appScript));
      });
   }

   private void startBrowserLogin(@NotNull AppScriptAuthService appScript) {
      try {
         String codeVerifier = PkceHelper.generateCodeVerifier();
         String codeChallenge = PkceHelper.generateCodeChallenge(codeVerifier);
         String state = UUID.randomUUID().toString();
         String nonce = UUID.randomUUID().toString();
         String url = OAuthUrlBuilder.buildAuthorizeUrl(codeChallenge, state, nonce);
         OAuthCallbackServer server = new OAuthCallbackServer();
         this.callbackServer.set(server);
         server.start(code -> {
            try {
               OAuthTokenExchangeService svc = new OAuthTokenExchangeService();
               TokenResponse resp = svc.exchangeCode(code, codeVerifier);
               TokenManager tm = tokenManager();
               tm.saveRefreshToken(resp.getRefreshToken());
               tm.setCachedAccessToken(resp.getAccessToken(), resp.getExpiresIn());
               tm.notifyAuthStatus(AuthStatus.LOGGED_IN);
               appScript.postRefreshToken(resp.getRefreshToken(), resp.getAccessToken());
            } catch (Exception exx) {
               LOG.warn("Tubi Copilot: token exchange failed", exx);
               tokenManager().notifyAuthStatus(AuthStatus.LOGGED_OUT);
               this.notifyError("Login failed: " + (exx.getMessage() != null ? exx.getMessage() : "unknown error"));
            }
         });
         BrowserUtil.browse(url);
      } catch (BindException ex) {
         LOG.warn("Tubi Copilot: port 7070 is busy", ex);
         this.applyStatus(AuthStatus.LOGGED_OUT);
         this.notifyError("Login failed: port 7070 is already in use.");
      } catch (Exception ex) {
         LOG.warn("Tubi Copilot: login start failed", ex);
         this.applyStatus(AuthStatus.LOGGED_OUT);
         this.notifyError("Login failed: " + (ex.getMessage() != null ? ex.getMessage() : "unknown error"));
      }
   }

   private void handleCancel() {
      this.stopCallbackServer();
      tokenManager().notifyAuthStatus(AuthStatus.LOGGED_OUT);
   }

   private void stopCallbackServer() {
      OAuthCallbackServer server = this.callbackServer.getAndSet(null);
      if (server != null) {
         server.stop();
      }
   }

   private void applyStatus(@NotNull AuthStatus status) {
      if (!SwingUtilities.isEventDispatchThread()) {
         SwingUtilities.invokeLater(() -> this.applyStatus(status));
      } else {
         this.currentStatus = status;
         switch (status) {
            case LOGGED_IN:
               this.authButton.setIcon(General.User);
               this.authButton.setToolTipText("Logout");
               this.statusLabel.setText("●");
               this.statusLabel.setToolTipText("Logged in");
               this.statusLabel.setForeground(new JBColor(new Color(52, 168, 83), new Color(129, 201, 149)));
               break;
            case LOGGED_OUT:
               this.authButton.setIcon(General.User);
               this.authButton.setToolTipText("Login");
               this.statusLabel.setText("●");
               this.statusLabel.setToolTipText("Logged out");
               this.statusLabel.setForeground(new JBColor(new Color(234, 67, 53), new Color(220, 80, 80)));
               break;
            case LOGGING_IN:
               this.authButton.setIcon(Process.Step_1);
               this.authButton.setToolTipText("Cancel login");
               this.statusLabel.setText("●");
               this.statusLabel.setToolTipText("Logging in…");
               this.statusLabel.setForeground(new JBColor(new Color(251, 188, 4), new Color(220, 190, 60)));
         }

         this.revalidate();
         this.repaint();
      }
   }

   @NotNull
   private static TokenManager tokenManager() {
      return (TokenManager)ApplicationManager.getApplication().getService(TokenManager.class);
   }

   private void notifyError(@NotNull String message) {
      if (this.errorNotifier != null) {
         SwingUtilities.invokeLater(() -> this.errorNotifier.accept(message));
      } else {
         LOG.warn("Tubi Copilot auth error: " + message);
      }
   }
}
