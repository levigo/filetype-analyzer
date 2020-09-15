package org.jadice.filetype.matchers.modca;


class Cp382 extends CpConvert {
  private final char[] Cp382 =
    {
      // 0x40 - 0x4F
      (char) 0x0020,  // space
      (char) 0x00,    // 
      (char) 0x00E2,  // Ã¢
      (char) 0x007B,  // {
      (char) 0x00E0,  // Ã 
      (char) 0x00E1,  // Ã¡
      (char) 0x00E3,  // ~a
      (char) 0x00E5,  // Â°a
      (char) 0x00E7,  // 
      (char) 0x00F1,  // ~n
      (char) 0x00C4,  // Ã
      (char) 0x002E,  // .
      (char) 0x003C,  // <
      (char) 0x0028,  // (
      (char) 0x002B,  // +
      (char) 0x0021,  // !

      // 0x50 - 0x5F
      (char) 0x0026,  // &
      (char) 0x00E9,  // Ã©
      (char) 0x00EA,  // Ãª
      (char) 0x00EB,  // ..e
      (char) 0x00E8,  // Ãš 
      (char) 0x00ED,  // Ã­
      (char) 0x00EE,  // Ã®
      (char) 0x00EF,  // ..i
      (char) 0x00EC,  // Ã¬
      (char) 0x0020,  // 
      (char) 0x00DC,  // Ã
      (char) 0x0024,  // $
      (char) 0x002A,  // *
      (char) 0x0029,  // )
      (char) 0x003B,  // ;
      (char) 0x00AC,  // not sign 
      
      // 0x60 - 0x6F
      (char) 0x002D,  // -
      (char) 0x002F,  // /
      (char) 0x00C2,  // Ã
      (char) 0x005B,  // [
      (char) 0x00C0,  // Ã
      (char) 0x00C1,  // Ã?
      (char) 0x00C3,  // ~A
      (char) 0x00C5,  // Â°A
      (char) 0x00C7,  // 
      (char) 0x00D1,  // ~N
      (char) 0x00F6,  // Ã¶
      (char) 0x002C,  // ,
      (char) 0x0025,  // %
      (char) 0x005F,  // _
      (char) 0x003E,  // >
      (char) 0x003F,  // ?
      
      // 0x70 - 0x7F
      (char) 0x00F8,  // /o
      (char) 0x00C9,  // Ã
      (char) 0x00CA,  // Ã
      (char) 0x00CB,  // ..E 
      (char) 0x00C8,  // Ã 
      (char) 0x00CD,  // Ã?
      (char) 0x00CE,  // Ã
      (char) 0x00CF,  // ..I
      (char) 0x00CC,  // Ã
      (char) 0x0020,  //
      (char) 0x003A,  // :
      (char) 0x0023,  // #
      (char) 0x00A7,  // Â§
      (char) 0x0027,  // '
      (char) 0x003D,  // =
      (char) 0x0022,  // "
      
      // 0x80 - 0x8F
      (char) 0x00D8,  // /O
      (char) 0x0061,  // a
      (char) 0x0062,  // b
      (char) 0x0063,  // c
      (char) 0x0064,  // d
      (char) 0x0065,  // e
      (char) 0x0066,  // f
      (char) 0x0067,  // g
      (char) 0x0068,  // h
      (char) 0x0069,  // i
      (char) 0x00AB,  // <<
      (char) 0x00BB,  // >>
      (char) 0x2030,  // promille
      (char) 0x002D,  // -
      (char) 0x0133,  // ij
      (char) 0xFB03,  // ffi

      // 0x90 - 0x9F
      (char) 0x00B0,  // Â° 
      (char) 0x006A,  // j 
      (char) 0x006B,  // k
      (char) 0x006C,  // l
      (char) 0x006D,  // m
      (char) 0x006E,  // n
      (char) 0x006F,  // o
      (char) 0x0070,  // p
      (char) 0x0071,  // q
      (char) 0x0072,  // r
      (char) 0x2012,  // - (long)
      (char) 0x0152,  // OE
      (char) 0x00E6,  // ae
      (char) 0x0178,  // ..Y
      (char) 0x00C6,  // AE
      (char) 0x2219,  // dot (bold)
      
      // 0xA0 - 0xAF
      (char) 0xFB00,  // ff 
      (char) 0x00DF,  // Ã
      (char) 0x0073,  // s
      (char) 0x0074,  // t
      (char) 0x0075,  // u
      (char) 0x0076,  // v
      (char) 0x0077,  // w
      (char) 0x0078,  // x
      (char) 0x0079,  // y
      (char) 0x007A,  // z
      (char) 0x00A1,  // ! (inverted)
      (char) 0x00BF,  // ? (inverted)
      (char) 0x0153,  // oe
      (char) 0xFB04,  // ffl
      (char) 0x2021,  // double cross
      (char) 0x201E,  // " (bottom english style)
      
      // 0xB0 - 0xBF
      (char) 0x023C,  // /c
      (char) 0x00A3,  // pound
      (char) 0x00A5,  // yen
      (char) 0xFB01,  // fi
      (char) 0xFB02,  // fl
      (char) 0x0040,  // @ 
      (char) 0x00B6,  // paragraph sign
      (char) 0x00BC,  // 1/4
      (char) 0x00BD,  // 1/2
      (char) 0x00BE,  // 3/4
      (char) 0x215B,  // 1/8
      (char) 0x2018,  // ' english paired
      (char) 0x2019,  // ' english paired
      (char) 0x201C,  // " english paired
      (char) 0x201D,  // " english paired
      (char) 0x2020,  // cross // TODO  
      
      // 0xC0 - 0xCF
      (char) 0x00E4,  // Ã€
      (char) 0x0041,  // A
      (char) 0x0042,  // B
      (char) 0x0043,  // C
      (char) 0x0044,  // D
      (char) 0x0045,  // E
      (char) 0x0046,  // F
      (char) 0x0047,  // G
      (char) 0x0048,  // H
      (char) 0x0049,  // I
      (char) 0x00,  // 
      (char) 0x00F4,  // ÃŽ
      (char) 0x00A6,  // |  (oder 0x01C0)
      (char) 0x00F2,  // Ã²
      (char) 0x00F3,  // Ã³
      (char) 0x00F5,  // ~o
      
      // 0xD0 - 0xDF
      (char) 0x00FC,  // ÃŒ 
      (char) 0x004A,  // J
      (char) 0x004B,  // K
      (char) 0x004C,  // L
      (char) 0x004D,  // M
      (char) 0x004E,  // N
      (char) 0x004F,  // O
      (char) 0x0050,  // P 
      (char) 0x0051,  // Q
      (char) 0x0052,  // R
      (char) 0x215C,  // 3/8
      (char) 0x00FB,  // Ã»
      (char) 0x007D,  // }
      (char) 0x00F9,  // Ã¹
      (char) 0x00FA,  // Ãº
      (char) 0x00FF,  // ..y
      
      // 0xE0 - 0xEF
      (char) 0x00D6,  // Ã 
      (char) 0x00,  // 
      (char) 0x0053,  // S
      (char) 0x0054,  // T
      (char) 0x0055,  // U
      (char) 0x0056,  // V
      (char) 0x0057,  // W
      (char) 0x0058,  // X
      (char) 0x0059,  // Y
      (char) 0x005A,  // Z
      (char) 0x215D,  // 5/8
      (char) 0x00D4,  // Ã
      (char) 0x005C,  // \
      (char) 0x00D2,  // Ã
      (char) 0x00D3,  // Ã
      (char) 0x00D5,  // ~O
      
      // 0xF0 - 0xFF
      (char) 0x0030,  // 0
      (char) 0x0031,  // 1
      (char) 0x0032,  // 2
      (char) 0x0033,  // 3
      (char) 0x0034,  // 4
      (char) 0x0035,  // 5
      (char) 0x0036,  // 6
      (char) 0x0037,  // 7
      (char) 0x0038,  // 8 
      (char) 0x0039,  // 9
      (char) 0x215E,  // 7/8
      (char) 0x00DB,  // Ã
      (char) 0x005D,  // ]
      (char) 0x00D9,  // Ã
      (char) 0x00DA,  // Ã
      (char) 0x00   //
    };

  public byte[] encode(final String javaString)
    throws java.io.UnsupportedEncodingException {
    char[] raw = javaString.toCharArray();
    byte[] data = new byte[raw.length];

    // TODO hack...
    for (int i = 0; i < raw.length; i++) {
      for(int j = 0; j < Cp382.length; j++) {
        if (Cp382[j] == raw[i]) {
          data[i] = (byte) ((byte)j + (byte)0x40);
          break;
        }
      }
    }
    return data;
  }

  public final String toString(final byte[] input, final int offset, final int length)
      throws java.io.UnsupportedEncodingException {
    char[] charData = new char[length];

    for (int i = length; --i >= 0;) {
      int index = (0xFF & input[i + offset]) - 0x40;
      if (index >= 0)
        charData[i] = Cp382[index];
      else
        // zero width space -> sonst stimmt Laufweite nicht...
        charData[i] = '\u200B';
    }

    return new String(charData);
  }
}