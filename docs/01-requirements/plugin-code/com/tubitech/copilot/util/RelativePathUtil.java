package com.tubitech.copilot.util;

import org.jetbrains.annotations.NotNull;

public final class RelativePathUtil {
   private RelativePathUtil() {
   }

   @NotNull
   public static String relativize(@NotNull String basePath, @NotNull String filePath) {
      if (filePath.startsWith(basePath + "/")) {
         return filePath.substring(basePath.length() + 1);
      } else if (filePath.startsWith(basePath + "\\")) {
         return filePath.substring(basePath.length() + 1).replace('\\', '/');
      } else {
         return filePath.contains("/") ? filePath.substring(filePath.lastIndexOf(47) + 1) : filePath;
      }
   }
}
