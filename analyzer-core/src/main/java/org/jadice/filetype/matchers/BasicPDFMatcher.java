package org.jadice.filetype.matchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Matcher} for PDF documents .
 * <p>
 * Caveat: for performance reasons, this should only be called from a context where the stream has
 * already been identified as a PDF file/stream.
 */
public class BasicPDFMatcher extends Matcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicPDFMatcher.class);

  public static final String PDF_MIME_TYPE = "application/pdf";


  /*
   * (non-Javadoc)
   *
   * @see com.levigo.jadice.filetype.database.Matcher#matches(com.levigo.jadice.filetype.Context)
   */
  @Override
  public boolean matches(final Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);
// Read entire stream into byte array (PDFs are usually not too big)
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] temp = new byte[4096];
      int read;
      while ((read = sis.read(temp)) != -1) {
        buffer.write(temp, 0, read);
      }
      byte[] data = buffer.toByteArray();

      // 1. Check for %PDF- after optional whitespace at the start
      int start = 0;
      while (start < data.length && Character.isWhitespace(data[start])) {
        start++;
      }

      if (data.length - start < 5 || !startsWith(data, start, "%PDF-")) {
        return false;
      }

      // 2. Check for "xref" table near the end (within last ~1KB)
      int searchWindowSize = Math.min(1024, data.length);
      String tail = new String(data, data.length - searchWindowSize, searchWindowSize, StandardCharsets.ISO_8859_1);
      if (!tail.contains("xref")) {
        return false;
      }

      return true;
    } catch (IOException e) {
      LOGGER.warn("Failed to extract PDF details", e);
      return false;
    }
  }

  private static boolean startsWith(byte[] data, int offset, String prefix) {
    byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
    if (data.length - offset < prefixBytes.length) {
      return false;
    }
    for (int i = 0; i < prefixBytes.length; i++) {
      if (data[offset + i] != prefixBytes[i]) {
        return false;
      }
    }
    return true;
  }
}
