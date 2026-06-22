package com.tubitech.copilot.api;

import com.google.gson.Gson;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.tubitech.copilot.api.model.FileReference;
import com.tubitech.copilot.auth.TokenManager;
import com.tubitech.copilot.settings.TubiCopilotSettings;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody.Builder;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FileUploadApiClientImpl implements FileUploadApiClient {
   private static final Logger LOG = Logger.getInstance(FileUploadApiClientImpl.class);
   private static final String UPLOAD_PATH = "/api/chat/v1/conversations/files/upload";
   private static final String DELETE_PATH = "/api/chat/v1/conversations/files/";
   private static final String FALLBACK_MIME = "application/octet-stream";
   private final TubiCopilotSettings settings;
   private final OkHttpClient httpClient;
   private final Gson gson;

   public FileUploadApiClientImpl(@NotNull TubiCopilotSettings settings) {
      this(
         settings,
         OkHttpClientFactory.newBuilder()
            .connectTimeout(settings.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build()
      );
   }

   FileUploadApiClientImpl(@NotNull TubiCopilotSettings settings, @NotNull OkHttpClient httpClient) {
      this.settings = settings;
      this.httpClient = httpClient;
      this.gson = new Gson();
   }

   @Override
   public void uploadFile(@NotNull File file, @NotNull final FileUploadListener listener) {
      String token;
      try {
         token = this.resolveToken();
      } catch (IOException | IllegalStateException e) {
         listener.onError(e);
         return;
      }

      long maxBytes = this.settings.maxUploadSizeMb * 1024L * 1024L;
      if (file.length() > maxBytes) {
         listener.onError(new FileTooLargeException(file.length(), maxBytes));
      } else {
         String mimeType = URLConnection.guessContentTypeFromName(file.getName());
         if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
         }

         RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
         RequestBody rawMultipart = new Builder().setType(MultipartBody.FORM).addFormDataPart("file", file.getName(), fileBody).build();
         long totalSize = -1L;

         try {
            totalSize = rawMultipart.contentLength();
         } catch (IOException e) {
            LOG.warn("Tubi Copilot: could not determine upload content length", e);
         }

         RequestBody countingBody = new FileUploadApiClientImpl.CountingRequestBody(rawMultipart, totalSize, listener);
         String url = this.buildUrl();
         LOG.debug("Tubi Copilot: uploading '" + file.getName() + "' (" + file.length() + " bytes) to " + url);
         final Request request = new okhttp3.Request.Builder()
            .url(url)
            .post(countingBody)
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/json, text/plain, */*")
            .build();
         this.httpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
               FileUploadApiClientImpl.LOG.warn("Tubi Copilot: upload request failed", e);
               listener.onError(e);
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
               FileUploadApiClientImpl.LOG.debug("Tubi Copilot: upload HTTP " + response.code());

               try {
                  Response e = response;

                  label104: {
                     try {
                        int code = response.code();
                        if (code == 401) {
                           FileUploadApiClientImpl.LOG.debug("Tubi Copilot: upload 401, retrying with refreshed token");

                           try {
                              String newToken = ((TokenManager)ApplicationManager.getApplication().getService(TokenManager.class)).forceRefresh();
                              Request retryRequest = request.newBuilder().header("Authorization", "Bearer " + newToken).build();
                              Response retryResponse = FileUploadApiClientImpl.this.httpClient.newCall(retryRequest).execute();

                              try {
                                 if (retryResponse.isSuccessful()) {
                                    FileUploadApiClientImpl.this.handleSuccess(retryResponse, listener);
                                 } else {
                                    listener.onError(new IOException("Upload failed after token refresh: HTTP " + retryResponse.code()));
                                 }
                              } catch (Throwable var12) {
                                 if (retryResponse != null) {
                                    try {
                                       retryResponse.close();
                                    } catch (Throwable var11) {
                                       var12.addSuppressed(var11);
                                    }
                                 }

                                 throw var12;
                              }

                              if (retryResponse != null) {
                                 retryResponse.close();
                              }
                           } catch (IOException ex) {
                              FileUploadApiClientImpl.LOG.warn("Tubi Copilot: upload retry after 401 failed", ex);
                              listener.onError(ex);
                           }
                           break label104;
                        }

                        if (response.isSuccessful()) {
                           FileUploadApiClientImpl.this.handleSuccess(response, listener);
                        } else if (code == 413) {
                           listener.onError(new IOException("File too large (server rejected)"));
                        } else if (code == 415) {
                           listener.onError(new IOException("File type not supported by the API"));
                        } else {
                           listener.onError(new IOException("Upload failed: HTTP " + code));
                        }
                     } catch (Throwable var14) {
                        if (e != null) {
                           try {
                              e.close();
                           } catch (Throwable var10) {
                              var14.addSuppressed(var10);
                           }
                        }

                        throw var14;
                     }

                     if (e != null) {
                        e.close();
                     }

                     return;
                  }

                  if (e != null) {
                     e.close();
                  }
               } catch (IOException e) {
                  FileUploadApiClientImpl.LOG.warn("Tubi Copilot: error reading upload response", e);
                  listener.onError(e);
               }
            }
         });
      }
   }

   @Override
   public void deleteFile(@NotNull String fileId) {
      if (!fileId.isBlank()) {
         String token;
         try {
            token = this.resolveToken();
         } catch (IOException | IllegalStateException e) {
            LOG.warn("Tubi Copilot: delete file — cannot resolve token", e);
            return;
         }

         String url = this.settings.baseUrl.replaceAll("/+$", "") + "/api/chat/v1/conversations/files/" + fileId;
         Request request = new okhttp3.Request.Builder()
            .url(url)
            .delete()
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/json, text/plain, */*")
            .build();

         try {
            Response response = this.httpClient.newCall(request).execute();

            try {
               if (response.isSuccessful()) {
                  LOG.info("Tubi Copilot: deleted server file " + fileId);
               } else {
                  LOG.warn("Tubi Copilot: delete file " + fileId + " failed: HTTP " + response.code());
               }
            } catch (Throwable var10) {
               if (response != null) {
                  try {
                     response.close();
                  } catch (Throwable var8) {
                     var10.addSuppressed(var8);
                  }
               }

               throw var10;
            }

            if (response != null) {
               response.close();
            }
         } catch (IOException e) {
            LOG.warn("Tubi Copilot: delete file " + fileId + " failed", e);
         }
      }
   }

   private void handleSuccess(@NotNull Response response, @NotNull FileUploadListener listener) throws IOException {
      ResponseBody body = response.body();
      if (body == null) {
         listener.onError(new IOException("Empty upload response body"));
      } else {
         FileReference ref = (FileReference)this.gson.fromJson(body.string(), FileReference.class);
         listener.onSuccess(ref);
      }
   }

   @NotNull
   private String buildUrl() {
      return this.settings.baseUrl.replaceAll("/+$", "") + "/api/chat/v1/conversations/files/upload";
   }

   @NotNull
   private String resolveToken() throws IOException {
      Application app = ApplicationManager.getApplication();
      if (app != null) {
         return ((TokenManager)app.getService(TokenManager.class)).getValidAccessToken();
      } else {
         String fallback = this.settings.getBearerToken();
         if (fallback != null && !fallback.isBlank()) {
            return fallback;
         } else {
            throw new IllegalStateException("No access token available — configure the plugin or provide a bearer token");
         }
      }
   }

   private static final class CountingRequestBody extends RequestBody {
      static final long REPORT_INTERVAL_BYTES = 8192L;
      private final RequestBody delegate;
      private final long totalBytes;
      private final FileUploadListener listener;

      CountingRequestBody(@NotNull RequestBody delegate, long totalBytes, @NotNull FileUploadListener listener) {
         this.delegate = delegate;
         this.totalBytes = totalBytes;
         this.listener = listener;
      }

      @Nullable
      public MediaType contentType() {
         return this.delegate.contentType();
      }

      public long contentLength() throws IOException {
         return this.delegate.contentLength();
      }

      public void writeTo(@NotNull BufferedSink sink) throws IOException {
         FileUploadApiClientImpl.CountingRequestBody.ProgressSink progressSink = new FileUploadApiClientImpl.CountingRequestBody.ProgressSink(sink);
         BufferedSink bufferedDelegate = Okio.buffer(progressSink);
         this.delegate.writeTo(bufferedDelegate);
         bufferedDelegate.flush();
         this.listener.onProgress(100);
      }

      private final class ProgressSink extends ForwardingSink {
         private long bytesWritten = 0L;
         private long lastReportedAt = 0L;

         ProgressSink(@NotNull Sink delegate) {
            super(delegate);
         }

         public void write(@NotNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            this.bytesWritten += byteCount;
            if (CountingRequestBody.this.totalBytes > 0L) {
               if (this.bytesWritten - this.lastReportedAt >= 8192L) {
                  this.lastReportedAt = this.bytesWritten;
                  int percent = (int)Math.min(99L, this.bytesWritten * 100L / CountingRequestBody.this.totalBytes);
                  if (percent > 0) {
                     CountingRequestBody.this.listener.onProgress(percent);
                  }
               }
            }
         }
      }
   }
}
