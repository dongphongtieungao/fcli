package com.tubitech.copilot.api;

import com.tubitech.copilot.api.model.FileReference;

public interface FileUploadListener {
   void onProgress(int var1);

   void onSuccess(FileReference var1);

   void onError(Throwable var1);
}
