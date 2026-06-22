package com.tubitech.copilot.editor.bundle;

import com.tubitech.copilot.agent.FileFilterConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class FileContentReader {
   private static final Logger LOG = Logger.getLogger(FileContentReader.class.getName());
   static final int MAX_CHARS = 512000;
   private static final Set<String> TEXT_EXTENSIONS = FileFilterConstants.TEXT_EXTENSIONS;

   private FileContentReader() {
   }

   public static String read(File file, String extension) throws IOException {
      String ext = extension == null ? "" : extension.toLowerCase().trim();
      long MAX_FILE_SIZE = 52428800L;
      if (file.length() > 52428800L) {
         return "(file too large: " + file.length() / 1048576L + " MB)";
      }

      String content;
      if (TEXT_EXTENSIONS.contains(ext)) {
         content = readText(file);
      } else {
         content = switch (ext) {
            case "pdf" -> readPdf(file);
            case "docx" -> readDocx(file);
            case "pptx" -> readPptx(file);
            case "xlsx", "xls" -> readExcel(file);
            default -> {
               LOG.warning("FileContentReader: unsupported extension '" + ext + "' for file: " + file.getName() + " — returning empty string");
               yield "";
            }
         };
      }

      return truncate(content);
   }

   private static String readText(File file) throws IOException {
      byte[] bytes = Files.readAllBytes(file.toPath());
      if (bytes.length == 0) {
         return "";
      }

      CharsetDecoder utf8 = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);

      try {
         String decoded = utf8.decode(ByteBuffer.wrap(bytes)).toString();
         if (decoded.startsWith("\ufeff")) {
            decoded = decoded.substring(1);
         }

         return decoded;
      } catch (CharacterCodingException var6) {
         try {
            Charset cp1252 = Charset.forName("windows-1252");
            CharsetDecoder dec = cp1252.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            return dec.decode(ByteBuffer.wrap(bytes)).toString();
         } catch (CharacterCodingException var5) {
            return StandardCharsets.ISO_8859_1.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
         }
      }
   }

   private static String readPdf(File file) throws IOException {
      PDDocument doc = Loader.loadPDF(file);

      String var3;
      try {
         PDFTextStripper stripper = new PDFTextStripper();
         stripper.setParagraphEnd("\n\n");
         var3 = normalizeWhitespace(stripper.getText(doc));
      } catch (Throwable var5) {
         if (doc != null) {
            try {
               doc.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (doc != null) {
         doc.close();
      }

      return var3;
   }

   private static String readDocx(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         XWPFDocument doc = new XWPFDocument(fis);

         String var13;
         try {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph para : paragraphs) {
               String text = para.getText();
               if (text != null && !text.isEmpty()) {
                  sb.append(text).append("\n\n");
               }
            }

            var13 = normalizeWhitespace(sb.toString());
         } catch (Throwable var10) {
            try {
               doc.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         doc.close();
         return var13;
      } catch (EncryptedDocumentException e) {
         LOG.warning("FileContentReader: DOCX is encrypted: " + file.getName());
         return "(encrypted document)";
      }
   }

   private static String readPptx(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         XMLSlideShow ppt = new XMLSlideShow(fis);

         String var15;
         try {
            StringBuilder sb = new StringBuilder();

            for (XSLFSlide slide : ppt.getSlides()) {
               for (XSLFShape shape : slide.getShapes()) {
                  if (shape instanceof XSLFTextShape textShape) {
                     String text = textShape.getText();
                     if (text != null && !text.isBlank()) {
                        sb.append(text).append("\n\n");
                     }
                  }
               }
            }

            var15 = normalizeWhitespace(sb.toString());
         } catch (Throwable var12) {
            try {
               ppt.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }

            throw var12;
         }

         ppt.close();
         return var15;
      } catch (EncryptedDocumentException e) {
         LOG.warning("FileContentReader: PPTX is encrypted: " + file.getName());
         return "(encrypted document)";
      }
   }

   private static String readExcel(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) {
         Workbook wb = WorkbookFactory.create(fis);

         String var18;
         try {
            StringBuilder sb = new StringBuilder();

            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
               Sheet sheet = wb.getSheetAt(s);
               StringBuilder sheetSb = new StringBuilder();

               for (Row row : sheet) {
                  StringBuilder rowSb = new StringBuilder();

                  for (Cell cell : row) {
                     String val = getCellValue(cell);
                     if (!rowSb.isEmpty()) {
                        rowSb.append(" | ");
                     }

                     rowSb.append(val);
                  }

                  String rowStr = rowSb.toString().trim();
                  if (!rowStr.isBlank()) {
                     sheetSb.append(rowStr).append("\n");
                  }
               }

               if (!sheetSb.isEmpty()) {
                  sb.append("## Sheet: ").append(sheet.getSheetName()).append("\n");
                  sb.append(sheetSb).append("\n");
               }
            }

            var18 = normalizeWhitespace(sb.toString());
         } catch (Throwable var15) {
            if (wb != null) {
               try {
                  wb.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }
            }

            throw var15;
         }

         if (wb != null) {
            wb.close();
         }

         return var18;
      } catch (EncryptedDocumentException e) {
         LOG.warning("FileContentReader: Excel is encrypted: " + file.getName());
         return "(encrypted document)";
      }
   }

   private static String getCellValue(Cell cell) {
      if (cell == null) {
         return "";
      }

      return switch (cell.getCellType()) {
         case STRING -> {
            String var10 = cell.getStringCellValue();
            yield var10;
         }
         case NUMERIC -> {
            if (DateUtil.isCellDateFormatted(cell)) {
               String var8 = cell.getLocalDateTimeCellValue().toString();
               yield var8;
            } else {
               double d = cell.getNumericCellValue();
               String var9 = d == Math.floor(d) && !Double.isInfinite(d) ? String.valueOf((long)d) : String.valueOf(d);
               yield var9;
            }
         }
         case BOOLEAN -> {
            String var7 = String.valueOf(cell.getBooleanCellValue());
            yield var7;
         }
         case FORMULA -> {
            String var6;
            try {
               var6 = String.valueOf(cell.getNumericCellValue());
            } catch (Exception ex) {
               var6 = cell.getStringCellValue();
               yield var6;
            }

            yield var6;
         }
         default -> {
            String var1 = "";
            yield var1;
         }
      };
   }

   private static String normalizeWhitespace(String text) {
      if (text != null && !text.isBlank()) {
         String[] lines = text.split("\n", -1);
         StringBuilder sb = new StringBuilder(text.length());
         int blankRun = 0;

         for (String line : lines) {
            String trimmed = line.stripTrailing();
            if (trimmed.isBlank()) {
               if (++blankRun <= 2) {
                  sb.append("\n");
               }
            } else {
               blankRun = 0;
               sb.append(trimmed).append("\n");
            }
         }

         return sb.toString().trim();
      } else {
         return "";
      }
   }

   private static String truncate(String content) {
      if (content == null) {
         return "";
      } else {
         return content.length() > 512000 ? content.substring(0, 512000) : content;
      }
   }
}
