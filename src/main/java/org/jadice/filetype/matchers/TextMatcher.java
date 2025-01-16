package org.jadice.filetype.matchers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A matcher which tries to tell text files apart from binary by looking at the first 1024 bytes of
 * content.
 */
public class TextMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextMatcher.class);

  /**
   * We consider it to be text if more than 90% are defined characters
   */
  private static final float ACCEPTANCE_RATIO = 0.9f;

  /**
   * The range of bytes in which we try to determine if text or not
   */
  private static final int LOOKAHEAD = 1024;

  /**
   * Default charset to use
   */
  private static final Charset DEFAULT_CHARSET = BOM.UTF_8.charset;

  /**
   * Punctuation characters which are ignored for the text matching.
   * 
   * @see Pattern
   */
  private static final Set<Character> PUNCTUATION = new HashSet<>();

  static {
    // We use a precalculated hash set here because the lookup will
    // be performed in O(1)
    for (char c : "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray()) {
      PUNCTUATION.add(c);
    }
  }

  private enum BOM {
    UTF_8("UTF-8", (byte) 0xEF, (byte) 0xBB, (byte) 0xBF), //
    UTF_16BE("UTF-16BE", (byte) 0xFE, (byte) 0xFF), //
    UTF_16LE("UTF-16LE", (byte) 0xFF, (byte) 0xFE);

    public final Charset charset;
    public final byte[] bytes;

    BOM(final String charsetName, final byte... bytes) {
      this.charset = Charset.forName(charsetName);
      this.bytes = bytes;
    }

    public boolean matches(final byte[] buffer) {
      if (buffer.length < bytes.length) {
        return false;
      }
      for (int i = 0; i < bytes.length; i++) {
        if (buffer[i] != bytes[i]) {
          return false;
        }
      }
      return true;
    }
  }


  @Override
  public boolean matches(final Context context) {
    try {
      SeekableInputStream s = context.getStream();
      s.seek(0);

      final byte[] buffer = new byte[LOOKAHEAD];
      final int read = s.read(buffer);

      if (read <= 0) {
        return false;
      }

      final BOM bom = findBOM(buffer);
      final int start = bom == null ? 0 : bom.bytes.length;
      final Charset charset = bom == null ? DEFAULT_CHARSET : bom.charset;

      final String text = new String(buffer, start, read - start, charset);
      if (text.isEmpty()) {
        return false;
      }

      float ratio = countCharacters(text, context);

      final boolean matches = ratio > ACCEPTANCE_RATIO;

      boolean isCSV = false;
      String mimeType = "text/plain";
      final String statedExtension = context.getStatedExtension();
      LOGGER.debug("stated extension: {}", statedExtension);
      if (matches && statedExtension != null && statedExtension.equals("csv")) {
        isCSV = true;
        mimeType = "text/csv";
      }

      if (matches && bom != null) {
        // Inject charset in MIME type for later usage
        context.setProperty(MimeTypeAction.KEY, mimeType + ";charset=" + bom.charset.name());
        context.info(this, String.format("Determined charset: %s", bom.charset.name()));
        if (isCSV) {
          context.setProperty(ExtensionAction.KEY, "csv");
          context.setProperty(DescriptionAction.KEY, "Comma-separated values (CSV)");
        }
      } else {
        context.setProperty(MimeTypeAction.KEY, mimeType);
      }
      return matches;
    } catch (IOException e) {
      LOGGER.warn("Error when matching a text file", e);
      return false;
    }
  }


  private BOM findBOM(final byte[] buffer) {
    for (BOM bom : BOM.values()) {
      if (bom.matches(buffer)) {
        return bom;
      }
    }
    return null;
  }


  private float countCharacters(final String text, final Context context) {
    // determine percentage of printable stuff
    int defined = 0;
    int ignored = 0;

    final char[] chars = text.toCharArray();
    for (int i = 0; i < text.length(); i++) {
      final char c = chars[i];

      if (c == 0) {
        // NULL bytes don't count
        continue;
      }
      if (Character.isLetterOrDigit(c) //
          || Character.isIdentifierIgnorable(c) //
          || Character.isSpaceChar(c) //
          || Character.isWhitespace(c)) {
        LOGGER.debug("Recognized char '{}' @ {}", c, i);
        defined++;
      } else if (PUNCTUATION.contains(c)) {
        LOGGER.debug("Ignoring punctuation char '{}' @ {}", c, i);
        ignored++;
      }
    }

    final float ratio = (text.length() == ignored) ? 0f : (float) (defined) / (text.length() - ignored);

    context.info(this, //
        String.format("Percentage of accepted chars: %.1f %% in first %d characters. (ignored %d characters)", //
            ratio * 100, text.length(), ignored));
    return ratio;
  }
}
