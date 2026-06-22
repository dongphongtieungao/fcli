package com.tubitech.copilot.editor;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public record MentionSuggestion(String displayText, String insertText, boolean isDirectory, @Nullable VirtualFile virtualFile) {
}
