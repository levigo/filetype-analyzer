package org.jadice.filetype.matchers.modca;

public class Hex {

  private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


  public static String getString(byte[] bytes) {
    StringBuilder string = new StringBuilder(bytes.length * 2);

    for(byte b : bytes) {
      string.append(HEX_CHARS[getHighNibble(b)]).append(HEX_CHARS[getLowNibble(b)]);
    }

    return string.toString();
  }

  private static int getHighNibble(byte b) {
    return (b & 240) >> 4;
  }

  private static int getLowNibble(byte b) {
    return b & 15;
  }
}
