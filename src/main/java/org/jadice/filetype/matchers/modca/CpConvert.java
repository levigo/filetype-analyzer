package org.jadice.filetype.matchers.modca;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CpConvert {
  private static final Logger LOGGER = LoggerFactory.getLogger(CpConvert.class);
  byte[] cvTable = null;
  String encoder = null;
  private byte[] revData = null;

  /**
   * Class declaration
   */
  public class CpNode implements Comparable<CpNode> {
    public final int cpValue;
    public final CpConvert cpClass;

    /**
     * Constructor declaration
     * 
     * 
     * @param cpValue
     * @param cpClass
     * 
     * @see
     */
    CpNode(final int cpValue, final CpConvert cpClass) {
      this.cpValue = cpValue;
      this.cpClass = cpClass;
    }

    @Override
    public String toString() {
      return Integer.toString(cpValue);
    }

    @Override
    public int compareTo(final CpNode o) {
      return Integer.valueOf(cpValue).compareTo(o.cpValue);
    }
  }

  static CpConvert cpConvert = new CpConvert();
  static CpNode[] codePages = {
      cpConvert.new CpNode(1004, new CpConvert(1004)), //
      cpConvert.new CpNode(382, new Cp382()), //
      cpConvert.new CpNode(280, new Cp280()), //
      cpConvert.new CpNode(285, new Cp285()), //
      cpConvert.new CpNode(273, new Cp273()), //
      cpConvert.new CpNode(850, new Cp850()), //
      cpConvert.new CpNode(500, new Cp500()), //
      cpConvert.new CpNode(37, new Cp037()), //
      cpConvert.new CpNode(437, new Cp437()), //
      cpConvert.new CpNode(1250, new Cp1250()), //
      cpConvert.new CpNode(1252, new Cp1252()), //
      cpConvert.new CpNode(1025, new Cp1025()), //
      cpConvert.new CpNode(2067, new Cp273()), //
      cpConvert.new CpNode(1200, new Cp1201()), // UTF-16BE (with IBM Private Use Area)
      cpConvert.new CpNode(1201, new Cp1201()), // UTF-16BE 
      cpConvert.new CpNode(1202, new Cp1203()), // UTF-16LE (with IBM Private Use Area)
      cpConvert.new CpNode(1203, new Cp1203()), // UTF-16LE 
      cpConvert.new CpNode(1204, new Cp1205()), // UTF-16 (with IBM Private Use Area)
      cpConvert.new CpNode(1205, new Cp1205()), // UTF-16 
      cpConvert.new CpNode(1208, new Cp1209()), // UTF-8 (with IBM Private Use Area)
      cpConvert.new CpNode(1209, new Cp1209()), // UTF-8 
  };
  static {
    codePages[0].cpClass.encoder = "8859_1";
  }

  /**
   * Documentation currently unavailable
   * 
   * @return byte[]
   * @param input byte[]
   */
  private final synchronized byte[] convert(final byte[] input, final int offset, final int length) {
    if (cvTable == null) {
      cvTable = getTable();
    }

    final byte[] retData = new byte[length];

    for (int i = length; --i >= 0;) {
      retData[i] = cvTable[(0xFF & input[i + offset])];
    }

    return retData;
  }

  /**
   * Returns the string encoded with the relevant codepage.
   * 
   * @return The encoded data
   * @param javaString The string to be encoded
   */
  public byte[] encode(final String javaString) throws java.io.UnsupportedEncodingException {
    final byte[] raw = javaString.getBytes("8859_1");

    /*
     * Support for the euro-character: This is the reverse hack to the one in the toString method
     * below. If we encounter the euro-character (which, for reasons detailed below, can only be
     * found in the char-array), we output a iso8859-15 euro-character.
     */
    final char[] rawChars = javaString.toCharArray();
    for (int i = 0; i < rawChars.length; i++) {
      if (rawChars[i] == '\u20ac')
        raw[i] = (byte) 0xa4;
    }

    final byte[] ret = new byte[raw.length];
    final byte[] revtabel = getRevTable();

    for (int i = 0; i < raw.length; i++) {
      ret[i] = revtabel[(0xFF & raw[i])];
    }

    return ret;
  }

  /**
   * Documentation currently unavailable
   * 
   * @return com.bm_soft.util.cpsupp.CpConvert
   * @param codePage int
   */
  public static CpConvert getConv(final int codePage) {
    for (final CpNode codePage2 : codePages) {
      if (codePage2.cpValue == codePage) {
        return codePage2.cpClass;
      }
    }

    return new CpConvert(codePage);
  }

  public static CpNode[] getSupportedCodePages() {
    return Arrays.copyOf(codePages, codePages.length);
  }

  /**
   * Documentation currently unavailable
   * 
   * @return byte[]
   */
  protected byte[] getRevTable() {
    if (revData == null) {
      try {
        final byte[] data = new byte[256];

        for (int i = 0; i < data.length; i++) {
          data[i] = (byte) i;
        }

        final String val = new String(data, "8859_1");

        revData = val.getBytes(encoder);
      } catch (final java.io.UnsupportedEncodingException e) {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Unsupported encoding " + encoder);
      }
    }

    return revData;
  }

  /**
   * Documentation currently unavailable
   * 
   * @return byte[]
   */
  protected byte[] getTable() {
    return null;
  }

  /**
   * Documentation currently unavailable
   * 
   * @param cpNumber int
   */
  private CpConvert(final int cpNumber) {
    if (cpNumber < 100) {
      encoder = "Cp0" + cpNumber;
    } else {
      encoder = "Cp" + cpNumber;
    }
  }

  /**
   * Documentation currently unavailable
   * 
   * @return byte[]
   * @param input byte[]
   */
  public String toString(final byte[] input, final int offset, final int length) throws java.io.UnsupportedEncodingException {
    if (encoder != null) {
      try {
        return new String(input, offset, length, encoder);
      } catch (final java.io.UnsupportedEncodingException X) {
        throw new java.io.UnsupportedEncodingException("Can't decode: \"" + encoder + "\"");
      }
    }

    final byte[] data = convert(input, offset, length);

    /*
     * Support for the "euro-character": The unicode code for the euro is \u20ac. The constructor
     * String(byte [], "8859_1") call will perform conversion in a way that the values of the byte
     * array are simply extended to 16-bit unicode characters, because the bottom 256 entries of the
     * unicode character match the iso8859-1 set. There is a new ISO recommendation 8859-15 which
     * equals 8859-1 with a few replacements. Most significantly, the 8859-1 character for the
     * "General Currency Sign" has been replaced by the euro-sign in 8815-15. Unfortunately, Java
     * doesn't support conversion based on 8815-15 yet, therefore we "hand-tune" the conversion
     * here.
     * 
     * FIXME: This is a very ugly hack, but there's no simple solution. A better solution would be
     * to let the CodePage converters convert directly to and from unicode (16-bit data).
     */
    final String result = new String(data, "8859_1").replace('\u00a4', '\u20ac');
    // "General Currency Sign" -> "euro"
    return result;
  }

  /**
   * Documentation currently unavailable
   * 
   * @return byte[]
   * @param input byte[]
   */
  public String toString(final byte[] input) throws java.io.UnsupportedEncodingException {
    return toString(input, 0, input.length);
  }

  /**
   * Documentation currently unavailable
   */
  CpConvert() {
    // we don't want implementations outside this package
  }
}