package org.jadice.filetype.matchers.modca;

/**
 * Converter class for IBM code page 1203 (UTF-16LE)
 */
class Cp1203 extends CpConvert {

  private static final String CHARSET_NAME = "UTF-16LE";
  
  public byte[] encode(final String javaString) throws java.io.UnsupportedEncodingException {
    return javaString.getBytes(CHARSET_NAME);
  }

  public final String toString(final byte[] input, final int offset, final int length) throws java.io.UnsupportedEncodingException {
    return new String(input, offset, length, CHARSET_NAME);
  }
}