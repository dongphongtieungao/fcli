package com.tubitech.copilot.agent;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class FileFilterConstants {
   public static final Set<String> SKIP_DIRS = Set.of(
      ".git",
      ".idea",
      ".tubi",
      "node_modules",
      "target",
      "build",
      "out",
      ".gradle",
      "dist",
      "__pycache__",
      ".venv",
      "venv",
      ".mvn",
      ".settings",
      ".classpath",
      ".project",
      "bin",
      ".svn",
      ".hg",
      ".next",
      ".DS_Store"
   );
   public static final Set<String> TEXT_EXTENSIONS = Set.of(
      "java",
      "kt",
      "groovy",
      "scala",
      "py",
      "js",
      "ts",
      "tsx",
      "jsx",
      "rb",
      "php",
      "lua",
      "html",
      "htm",
      "css",
      "scss",
      "less",
      "vue",
      "svelte",
      "xml",
      "json",
      "json5",
      "yaml",
      "yml",
      "toml",
      "properties",
      "ini",
      "cfg",
      "conf",
      "env",
      "dotenv",
      "editorconfig",
      "sql",
      "graphql",
      "gql",
      "sh",
      "bash",
      "zsh",
      "fish",
      "bat",
      "cmd",
      "ps1",
      "md",
      "mdx",
      "txt",
      "rst",
      "adoc",
      "asciidoc",
      "csv",
      "tsv",
      "gradle",
      "kts",
      "makefile",
      "mk",
      "cmake",
      "dockerfile",
      "dockerignore",
      "gitignore",
      "gitattributes",
      "go",
      "rs",
      "c",
      "cc",
      "cpp",
      "cxx",
      "h",
      "hh",
      "hpp",
      "hxx",
      "swift",
      "dart",
      "m",
      "mm",
      "tf",
      "tfvars",
      "hcl",
      "rego",
      "pdf",
      "docx",
      "pptx",
      "xlsx",
      "xls",
      "r",
      "jl",
      "clj",
      "cljs",
      "ex",
      "exs",
      "erl",
      "elm",
      "proto",
      "thrift",
      "avro",
      "lock"
   );
   public static final Set<String> SKIP_EXTENSIONS = Set.of(
      "class",
      "jar",
      "war",
      "ear",
      "zip",
      "tar",
      "gz",
      "png",
      "jpg",
      "jpeg",
      "gif",
      "ico",
      "svg",
      "woff",
      "woff2",
      "ttf",
      "eot",
      "lock",
      "exe",
      "dll",
      "so",
      "dylib",
      "pdf",
      "docx",
      "xlsx"
   );

   private FileFilterConstants() {
   }

   public static boolean shouldSkipDir(String name) {
      return SKIP_DIRS.contains(name) || name.startsWith(".");
   }

   public static boolean shouldSkipFile(String fileName) {
      if (fileName.startsWith(".")) {
         return true;
      }

      int dot = fileName.lastIndexOf(46);
      return dot >= 0 ? SKIP_EXTENSIONS.contains(fileName.substring(dot + 1).toLowerCase()) : false;
   }

   @NotNull
   public static String relativePath(@NotNull String basePath, @NotNull VirtualFile file) {
      String p = file.getPath();
      if (p.startsWith(basePath)) {
         String rel = p.substring(basePath.length());
         return rel.startsWith("/") ? rel.substring(1) : rel;
      } else {
         return p;
      }
   }
}
