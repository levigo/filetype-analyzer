package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.AbsoluteLocation;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * A {@link Matcher} based on regular expressions.
 *
 */
@XmlRootElement(name = "match-regexp")
public class RegexpMatcher extends StreamMatcher {
  private Pattern pattern;
  private String patternString;

  /**
   * The range (number of bytes) to be considered in the match. Default: 100 bytes.
   */
  @XmlAttribute
  private int range = 100;

  @XmlTransient
  private String encoding = "ASCII";

  /**
   * By default, case-insensitive matching assumes that only characters in the US-ASCII charset are
   * being matched. Unicode-aware case-insensitive matching can be enabled by specifying the
   * {@link #unicodeCase} flag in conjunction with this flag.
   *
   * @see Pattern#CASE_INSENSITIVE
   */
  @XmlAttribute
  private boolean caseInsensitive;

  /**
   * In multiline mode the expressions ^ and $ match just after or just before, respectively, a line
   * terminator or the end of the input sequence. By default these expressions only match at the
   * beginning and the end of the entire input sequence.
   *
   * @see Pattern#MULTILINE
   */
  @XmlAttribute
  private boolean multiline;

  /**
   * In dotall mode, the expression '.' matches any character, including a line terminator. Default:
   * true
   *
   * @see Pattern#DOTALL
   */
  @XmlAttribute
  private boolean dotall = true;

  /**
   * When this flag is specified then case-insensitive matching is done in a manner consistent with
   * the Unicode Standard. By default, case-insensitive matching assumes that only characters in the
   * US-ASCII charset are being matched.
   *
   * @see Pattern#UNICODE_CASE
   */
  @XmlAttribute
  private boolean unicodeCase;

  /**
   * When this flag is specified then two characters will be considered to match if, and only if,
   * their full canonical decompositions match. The expression "a\u030A", for example, will match
   * the string "\u00E5" when this flag is specified. By default, matching does not take canonical
   * equivalence into account.
   *
   *
   * @see Pattern#CANON_EQ
   */
  @XmlAttribute
  private boolean canonEq;

  @Override
  protected boolean matches(Context context, SeekableInputStream s) throws IOException {
    if (null == pattern) {
      int flags = 0;
      if (caseInsensitive) {
        flags |= Pattern.CASE_INSENSITIVE;
      }
      if (multiline) {
        flags |= Pattern.MULTILINE;
      }
      if (dotall) {
        flags |= Pattern.DOTALL;
      }
      if (unicodeCase) {
        flags |= Pattern.UNICODE_CASE;
      }
      if (canonEq) {
        flags |= Pattern.CANON_EQ;
      }

      pattern = Pattern.compile(".*" + patternString + ".*", flags);
    }

    byte[] buffer = new byte[range];
    int offset = 0;
    int read;
    while ((read = s.read(buffer, offset, range - offset)) > 0) {
      offset += read;
    }

    String data = new String(buffer, encoding);

    boolean matches = pattern.matcher(data).matches();

    return comparison == Comparison.EQUALS ? matches : !matches;
  }

  @XmlAttribute
  protected void setEncoding(String encoding) throws UnsupportedEncodingException {
    this.encoding = encoding;

    // test whether the encoding is ok
    "".getBytes(encoding);
  }

  @XmlValue
  protected void setPattern(String pattern) {
    this.patternString = pattern;
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

  protected void setRange(int range) {
    this.range = range;
  }

  protected void setDotall(boolean dotall) {
    this.dotall = dotall;
  }
}
