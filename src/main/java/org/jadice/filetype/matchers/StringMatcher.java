package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.AbsoluteLocation;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * Matcher implementation matching a string at a certain location in the data. The string to be
 * matched may contain certain escape sequences. They include the usual Java/C sequences, in
 * particular:
 * <dl>
 * <dt>\\n
 * <dd>The newline character
 * <dt>\\r
 * <dd>The carriage-return character
 * <dt>\\t
 * <dd>The tab character
 * <dt>\\b
 * <dd>The backspace character
 * <dt>\\f
 * <dd>The form-feed character
 * <dt>\\\\
 * <dd>The backslash character
 * <dt>\\xXX
 * <dd>The the hexadecimal byte specified by XX
 * <dt>\\OOO
 * <dd>The the octal byte specified by OOO
 * <dt>\\uXXXX
 * <dd>The unicode character specified by the two-byte hexadecimal sequence XXXX
 * </dl>
 * 
 */
@XmlRootElement(name = "match-string")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class StringMatcher extends StreamMatcher {
  @XmlTransient
  private byte[] pattern;

  @XmlValue
  private String patternString;

  @XmlTransient
  private String encoding;

  @Override
  protected boolean matches(Context context, SeekableInputStream s) throws IOException {
    byte[] pattern = getPattern();
    byte[] buffer = new byte[pattern.length];

    s.readFully(buffer);

    return Arrays.equals(buffer, pattern);
  }

  private byte[] getPattern() {
    if (null == pattern) {
      pattern = toByteArray(patternString, encoding != null ? encoding : "ASCII");
    }

    return pattern;
  }

  private static byte[] toByteArray(String s, String encoding) {
    StringBuilder src = new StringBuilder(s);
    ByteBuffer dst = ByteBuffer.allocate(s.length() * 4);

    Charset cs = Charset.forName(encoding);

    CharBuffer tmp = CharBuffer.allocate(1);
    int mark = 0;
    for (int p = 0; p < src.length(); p++) {
      switch (src.charAt(p)){
        case '\\' :
          if (mark != p) {
            dst.put(cs.encode(src.substring(mark, p)));
            mark = p;
          }
          p++;
          switch (src.charAt(p)){
            case 'b' :
              dst.put((byte) ('\b' & 0xff));
              break;
            case 'f' :
              dst.put((byte) ('\f' & 0xff));
              break;
            case 't' :
              dst.put((byte) ('\t' & 0xff));
              break;
            case 'r' :
              dst.put((byte) ('\r' & 0xff));
              break;
            case 'n' :
              dst.put((byte) ('\n' & 0xff));
              break;
            case '\\' :
              dst.put((byte) ('\\' & 0xff));
              break;
            case 'u' :
              // run through encoder, then append
              tmp.rewind();
              tmp.append((char) parseExcactlyNDigits(src, p + 1, 4, 16));
              dst.put(cs.encode(tmp));
              p += 4;
              break;
            case 'x' :
              // append directly
              dst.put((byte) (parseExcactlyNDigits(src, p + 1, 2, 16) & 0xff));
              p += 2;
              break;
            case '0' :
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
              int len = 0;
              while (p + len < src.length() && Character.digit(src.charAt(p + len), 8) >= 0 && len < 3) {
                len++;
              }

              if (len == 0) {
                throw new IllegalArgumentException(
                    "Not a digit sequence of at up to " + 2 + " characters at " + p + " for radix " + 8);
              }

              int oct = Integer.parseInt(src.substring(p, p + len), 8);
              if (oct > 256) {
                throw new IllegalArgumentException("Not octal sequence too large at " + p);
              }

              dst.put((byte) (oct & 0xff));

              p += len - 1;
              break;
            default :
              throw new IllegalArgumentException("Unsupported escape sequence at " + p + ": \\" + src.charAt(p));
          }
          mark = p + 1;
      }
    }

    if (mark != src.length()) {
      dst.put(cs.encode(src.substring(mark, src.length())));
    }

    byte[] result = new byte[dst.position()];
    System.arraycopy(dst.array(), 0, result, 0, result.length);
    return result;
  }

  private static int parseExcactlyNDigits(StringBuilder b, int p, int n, int radix) {
    for (int i = 0; i < n; i++) {
      if (Character.digit(b.charAt(p + i), radix) < 0) {
        throw new IllegalArgumentException(
            "Not a digit sequence of " + n + " characters at " + p + " for radix " + radix);
      }
    }

    return Integer.parseInt(b.substring(p, p + n), radix);
  }

  @XmlAttribute
  protected void setEncoding(String encoding) throws UnsupportedEncodingException {
    this.encoding = encoding;

    // test whether the encoding is ok
    "".getBytes(encoding);
  }

  protected void setPatternString(String patternString) {
    this.patternString = patternString;

    // will throw exception on syntax error
    toByteArray(patternString, "ASCII");
  }

  @Override
  @XmlAttribute
  protected void setComparison(String comparison) {
    super.setComparison(comparison);
  }

  @XmlAttribute(name = "offset")
  protected void setOffset(int offset) {
    this.location = new AbsoluteLocation(offset);
  }
}
