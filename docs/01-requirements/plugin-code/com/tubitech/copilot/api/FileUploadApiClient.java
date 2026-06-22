package com.tubitech.copilot.api;

import java.io.File;

public interface FileUploadApiClient {
   void uploadFile(File var1, FileUploadListener var2);

   void deleteFile(String var1);
}
