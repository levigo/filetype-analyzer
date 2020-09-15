package org.jadice.filetype.matchers.modca;

/**
 * Converter class for IBM code page 1201 (UTF-16BE)
 */
class Cp1201 extends CpConvert {

  private static final String CHARSET_NAME = "UTF-16BE";
  
  public byte[] encode(final String javaString) throws java.io.UnsupportedEncodingException {
    return javaString.getBytes(CHARSET_NAME);
  }

  public final String toString(final byte[] input, final int offset, final int length) throws java.io.UnsupportedEncodingException {
    return new String(input, offset, length, CHARSET_NAME);
  }
}