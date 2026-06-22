package com.tubitech.copilot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StreamListener {
   void onStart();

   void onToken(String var1);

   void onComplete(@NotNull String var1, @Nullable String var2, @Nullable String var3);

   void onError(Throwable var1);
}
