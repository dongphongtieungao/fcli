package com.tubitech.copilot.editor.bundle;

import java.util.List;
import java.util.stream.Collectors;

public class ContextBundleBuilder {
   static final int MAX_BUNDLE_CHARS = 5000000;

   private ContextBundleBuilder() {
   }

   public static String build(List<ContextBundleEntry> entries, String dateStr) {
      if (entries != null && !entries.isEmpty()) {
         StringBuilder sb = new StringBuilder();
         sb.append("# AI_CONTEXT_BUNDLE\n\n");
         sb.append("## META\n");
         sb.append("- total_files: ").append(entries.size()).append("\n");
         sb.append("- generated_at: ").append(dateStr).append("\n\n");
         sb.append("---\n");

         for (ContextBundleEntry entry : entries) {
            if (sb.length() > 5000000) {
               sb.append("\n## TRUNCATED\nBundle size limit reached (").append(5).append("MB). ").append("Remaining files omitted.\n");
               break;
            }

            sb.append("\n");
            sb.append("## FILE: ").append(entry.relativePath()).append("\n\n");
            sb.append("### CONTENT:\n");
            sb.append("```").append(entry.language()).append("\n");
            String content = entry.content();
            if (content != null && !content.isEmpty()) {
               sb.append(content);
            } else {
               sb.append("(empty)");
            }

            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
               sb.append("\n");
            }

            sb.append("```\n\n");
            sb.append("---\n");
         }

         return sb.toString();
      } else {
         throw new IllegalArgumentException("entries must not be null or empty");
      }
   }

   public static String buildSectioned(List<ContextBundleSection> sections, String dateStr) {
      if (sections != null && !sections.isEmpty()) {
         List<ContextBundleSection> nonEmpty = sections.stream().filter(s -> !s.entries().isEmpty()).collect(Collectors.toList());
         if (nonEmpty.isEmpty()) {
            throw new IllegalArgumentException("all sections are empty — nothing to build");
         }

         int totalFiles = nonEmpty.stream().mapToInt(s -> s.entries().size()).sum();
         StringBuilder sb = new StringBuilder();
         sb.append("# AI_CONTEXT_BUNDLE\n\n");
         sb.append("## META\n");
         sb.append("- total_files: ").append(totalFiles).append("\n");
         sb.append("- generated_at: ").append(dateStr).append("\n");

         for (ContextBundleSection section : nonEmpty) {
            sb.append("- ").append(section.sectionId().toLowerCase().replace('_', '-')).append(": ").append(section.entries().size()).append("\n");
         }

         sb.append("\n---\n");

         for (ContextBundleSection section : nonEmpty) {
            sb.append("\n## SECTION: ").append(section.sectionId()).append("\n");
            sb.append("> ").append(section.sectionDescription()).append("\n");

            for (ContextBundleEntry entry : section.entries()) {
               if (sb.length() > 5000000) {
                  sb.append("\n## TRUNCATED\nBundle size limit reached (").append(5).append("MB). ").append("Remaining files omitted.\n");
                  return sb.toString();
               }

               sb.append("\n## FILE: ").append(entry.relativePath()).append("\n\n");
               sb.append("### CONTENT:\n");
               sb.append("```").append(entry.language()).append("\n");
               String content = entry.content();
               if (content != null && !content.isEmpty()) {
                  sb.append(content);
               } else {
                  sb.append("(empty)");
               }

               if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                  sb.append("\n");
               }

               sb.append("```\n\n");
               sb.append("---\n");
            }
         }

         return sb.toString();
      } else {
         throw new IllegalArgumentException("sections must not be null or empty");
      }
   }
}
