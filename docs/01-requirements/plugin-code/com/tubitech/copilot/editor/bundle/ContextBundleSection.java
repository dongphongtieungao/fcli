package com.tubitech.copilot.editor.bundle;

import java.util.List;

public record ContextBundleSection(String sectionId, String sectionDescription, List<ContextBundleEntry> entries) {
}
