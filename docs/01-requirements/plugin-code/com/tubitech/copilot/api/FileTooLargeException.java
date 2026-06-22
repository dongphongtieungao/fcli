package com.tubitech.copilot.api;

public class FileTooLargeException extends Exception {
   private final long actualBytes;
   private final long maxBytes;

   public FileTooLargeException(long actualBytes, long maxBytes) {
      super("File size " + actualBytes + " bytes exceeds the upload limit of " + maxBytes + " bytes");
      this.actualBytes = actualBytes;
      this.maxBytes = maxBytes;
   }

   public long getActualBytes() {
      return this.actualBytes;
   }

   public long getMaxBytes() {
      return this.maxBytes;
   }
}
