package org.jadice.filetype.matchers.modca;

/**
 * Converter class for IBM code page 1205 (UTF-16)
 */
class Cp1205 extends CpConvert {

  private static final String CHARSET_NAME = "UTF-16";
  
  public byte[] encode(final String javaString) throws java.io.UnsupportedEncodingException {
    return javaString.getBytes(CHARSET_NAME);
  }

  public final String toString(final byte[] input, final int offset, final int length) throws java.io.UnsupportedEncodingException {
    return new String(input, offset, length, CHARSET_NAME);
  }
}