package org.jadice.filetype.matchers.modca;


/**
 * Class declaration
 */
class Cp850 extends CpConvert {
  private static char[] chars858 =
    {
      (char) 0x0000,
      (char) 0x0001,
      (char) 0x0002,
      (char) 0x0003,
      (char) 0x0004,
      (char) 0x0005,
      (char) 0x0006,
      (char) 0x0007,
      (char) 0x0008,
      (char) 0x0009,
      (char) 0x000A,
      (char) 0x000B,
      (char) 0x000C,
      (char) 0x000D,
      (char) 0x000E,
      (char) 0x000F,
      (char) 0x0010,
      (char) 0x0011,
      (char) 0x0012,
      (char) 0x0013,
      (char) 0x0014,
      (char) 0x0015,
      (char) 0x0016,
      (char) 0x0017,
      (char) 0x0018,
      (char) 0x0019,
      (char) 0x001A,
      (char) 0x001B,
      (char) 0x001C,
      (char) 0x001D,
      (char) 0x001E,
      (char) 0x001F,
      (char) 0x0020,
      (char) 0x0021,
      (char) 0x0022,
      (char) 0x0023,
      (char) 0x0024,
      (char) 0x0025,
      (char) 0x0026,
      (char) 0x0027,
      (char) 0x0028,
      (char) 0x0029,
      (char) 0x002A,
      (char) 0x002B,
      (char) 0x002C,
      (char) 0x002D,
      (char) 0x002E,
      (char) 0x002F,
      (char) 0x0030,
      (char) 0x0031,
      (char) 0x0032,
      (char) 0x0033,
      (char) 0x0034,
      (char) 0x0035,
      (char) 0x0036,
      (char) 0x0037,
      (char) 0x0038,
      (char) 0x0039,
      (char) 0x003A,
      (char) 0x003B,
      (char) 0x003C,
      (char) 0x003D,
      (char) 0x003E,
      (char) 0x003F,
      (char) 0x0040,
      (char) 0x0041,
      (char) 0x0042,
      (char) 0x0043,
      (char) 0x0044,
      (char) 0x0045,
      (char) 0x0046,
      (char) 0x0047,
      (char) 0x0048,
      (char) 0x0049,
      (char) 0x004A,
      (char) 0x004B,
      (char) 0x004C,
      (char) 0x004D,
      (char) 0x004E,
      (char) 0x004F,
      (char) 0x0050,
      (char) 0x0051,
      (char) 0x0052,
      (char) 0x0053,
      (char) 0x0054,
      (char) 0x0055,
      (char) 0x0056,
      (char) 0x0057,
      (char) 0x0058,
      (char) 0x0059,
      (char) 0x005A,
      (char) 0x005B,
      (char) 0x005C,
      (char) 0x005D,
      (char) 0x005E,
      (char) 0x005F,
      (char) 0x0060,
      (char) 0x0061,
      (char) 0x0062,
      (char) 0x0063,
      (char) 0x0064,
      (char) 0x0065,
      (char) 0x0066,
      (char) 0x0067,
      (char) 0x0068,
      (char) 0x0069,
      (char) 0x006A,
      (char) 0x006B,
      (char) 0x006C,
      (char) 0x006D,
      (char) 0x006E,
      (char) 0x006F,
      (char) 0x0070,
      (char) 0x0071,
      (char) 0x0072,
      (char) 0x0073,
      (char) 0x0074,
      (char) 0x0075,
      (char) 0x0076,
      (char) 0x0077,
      (char) 0x0078,
      (char) 0x0079,
      (char) 0x007A,
      (char) 0x007B,
      (char) 0x007C,
      (char) 0x007D,
      (char) 0x007E,
      (char) 0x007F,
      (char) 0x00C7,
      (char) 0x00FC,
      (char) 0x00E9,
      (char) 0x00E2,
      (char) 0x00E4,
      (char) 0x00E0,
      (char) 0x00E5,
      (char) 0x00E7,
      (char) 0x00EA,
      (char) 0x00EB,
      (char) 0x00E8,
      (char) 0x00EF,
      (char) 0x00EE,
      (char) 0x00EC,
      (char) 0x00C4,
      (char) 0x00C5,
      (char) 0x00C9,
      (char) 0x00E6,
      (char) 0x00C6,
      (char) 0x00F4,
      (char) 0x00F6,
      (char) 0x00F2,
      (char) 0x00FB,
      (char) 0x00F9,
      (char) 0x00FF,
      (char) 0x00D6,
      (char) 0x00DC,
      (char) 0x00F8,
      (char) 0x00A3,
      (char) 0x00D8,
      (char) 0x00D7,
      (char) 0x0192,
      (char) 0x00E1,
      (char) 0x00ED,
      (char) 0x00F3,
      (char) 0x00FA,
      (char) 0x00F1,
      (char) 0x00D1,
      (char) 0x00AA,
      (char) 0x00BA,
      (char) 0x00BF,
      (char) 0x00AE,
      (char) 0x00AC,
      (char) 0x00BD,
      (char) 0x00BC,
      (char) 0x00A1,
      (char) 0x00AB,
      (char) 0x00BB,
      (char) 0x2591,
      (char) 0x2592,
      (char) 0x2593,
      (char) 0x2502,
      (char) 0x2524,
      (char) 0x00C1,
      (char) 0x00C2,
      (char) 0x00C0,
      (char) 0x00A9,
      (char) 0x2563,
      (char) 0x2551,
      (char) 0x2557,
      (char) 0x255D,
      (char) 0x00A2,
      (char) 0x00A5,
      (char) 0x2510,
      (char) 0x2514,
      (char) 0x2534,
      (char) 0x252C,
      (char) 0x251C,
      (char) 0x2500,
      (char) 0x253C,
      (char) 0x00E3,
      (char) 0x00C3,
      (char) 0x255A,
      (char) 0x2554,
      (char) 0x2569,
      (char) 0x2566,
      (char) 0x2560,
      (char) 0x2550,
      (char) 0x256C,
      (char) 0x00A4,
      (char) 0x00F0,
      (char) 0x00D0,
      (char) 0x00CA,
      (char) 0x00CB,
      (char) 0x00C8,
      (char) 0x20AC,
      (char) 0x00CD,
      (char) 0x00CE,
      (char) 0x00CF,
      (char) 0x2518,
      (char) 0x250C,
      (char) 0x2588,
      (char) 0x2584,
      (char) 0x00A6,
      (char) 0x00CC,
      (char) 0x2580,
      (char) 0x00D3,
      (char) 0x00DF,
      (char) 0x00D4,
      (char) 0x00D2,
      (char) 0x00F5,
      (char) 0x00D5,
      (char) 0x00B5,
      (char) 0x00FE,
      (char) 0x00DE,
      (char) 0x00DA,
      (char) 0x00DB,
      (char) 0x00D9,
      (char) 0x00FD,
      (char) 0x00DD,
      (char) 0x00AF,
      (char) 0x00B4,
      (char) 0x00AD,
      (char) 0x00B1,
      (char) 0x2017,
      (char) 0x00BE,
      (char) 0x00B6,
      (char) 0x00A7,
      (char) 0x00F7,
      (char) 0x00B8,
      (char) 0x00B0,
      (char) 0x00A8,
      (char) 0x00B7,
      (char) 0x00B9,
      (char) 0x00B3,
      (char) 0x00B2,
      (char) 0x25A0,
      (char) 0x00A0 };

  public byte[] encode(final String javaString)
    throws java.io.UnsupportedEncodingException {
    char[] raw = javaString.toCharArray();
    byte[] data = new byte[raw.length];

    for (int i = 0; i < raw.length; i++) {
      if (raw[i] <= 0x007F) {
        data[i] = (byte) raw[i];
      } else {
        switch (raw[i]) {
          case (char) 0x00c7 :
            data[i] = (byte) 0x80;
            break;
          case (char) 0x00fc :
            data[i] = (byte) 0x81;
            break;
          case (char) 0x00e9 :
            data[i] = (byte) 0x82;
            break;
          case (char) 0x00e2 :
            data[i] = (byte) 0x83;
            break;
          case (char) 0x00e4 :
            data[i] = (byte) 0x84;
            break;
          case (char) 0x00e0 :
            data[i] = (byte) 0x85;
            break;
          case (char) 0x00e5 :
            data[i] = (byte) 0x86;
            break;
          case (char) 0x00e7 :
            data[i] = (byte) 0x87;
            break;
          case (char) 0x00ea :
            data[i] = (byte) 0x88;
            break;
          case (char) 0x00eb :
            data[i] = (byte) 0x89;
            break;
          case (char) 0x00e8 :
            data[i] = (byte) 0x8A;
            break;
          case (char) 0x00ef :
            data[i] = (byte) 0x8B;
            break;
          case (char) 0x00ee :
            data[i] = (byte) 0x8C;
            break;
          case (char) 0x00ec :
            data[i] = (byte) 0x8D;
            break;
          case (char) 0x00c4 :
            data[i] = (byte) 0x8E;
            break;
          case (char) 0x00c5 :
            data[i] = (byte) 0x8F;
            break;
          case (char) 0x00c9 :
            data[i] = (byte) 0x90;
            break;
          case (char) 0x00e6 :
            data[i] = (byte) 0x91;
            break;
          case (char) 0x00c6 :
            data[i] = (byte) 0x92;
            break;
          case (char) 0x00f4 :
            data[i] = (byte) 0x93;
            break;
          case (char) 0x00f6 :
            data[i] = (byte) 0x94;
            break;
          case (char) 0x00f2 :
            data[i] = (byte) 0x95;
            break;
          case (char) 0x00fb :
            data[i] = (byte) 0x96;
            break;
          case (char) 0x00f9 :
            data[i] = (byte) 0x97;
            break;
          case (char) 0x00ff :
            data[i] = (byte) 0x98;
            break;
          case (char) 0x00d6 :
            data[i] = (byte) 0x99;
            break;
          case (char) 0x00dc :
            data[i] = (byte) 0x9A;
            break;
          case (char) 0x00f8 :
            data[i] = (byte) 0x9B;
            break;
          case (char) 0x00a3 :
            data[i] = (byte) 0x9C;
            break;
          case (char) 0x00d8 :
            data[i] = (byte) 0x9D;
            break;
          case (char) 0x00d7 :
            data[i] = (byte) 0x9E;
            break;
          case (char) 0x0192 :
            data[i] = (byte) 0x9F;
            break;
          case (char) 0x00e1 :
            data[i] = (byte) 0xA0;
            break;
          case (char) 0x00ed :
            data[i] = (byte) 0xA1;
            break;
          case (char) 0x00f3 :
            data[i] = (byte) 0xA2;
            break;
          case (char) 0x00fa :
            data[i] = (byte) 0xA3;
            break;
          case (char) 0x00f1 :
            data[i] = (byte) 0xA4;
            break;
          case (char) 0x00d1 :
            data[i] = (byte) 0xA5;
            break;
          case (char) 0x00aa :
            data[i] = (byte) 0xA6;
            break;
          case (char) 0x00ba :
            data[i] = (byte) 0xA7;
            break;
          case (char) 0x00bf :
            data[i] = (byte) 0xA8;
            break;
          case (char) 0x00ae :
            data[i] = (byte) 0xA9;
            break;
          case (char) 0x00ac :
            data[i] = (byte) 0xAA;
            break;
          case (char) 0x00bd :
            data[i] = (byte) 0xAB;
            break;
          case (char) 0x00bc :
            data[i] = (byte) 0xAC;
            break;
          case (char) 0x00a1 :
            data[i] = (byte) 0xAD;
            break;
          case (char) 0x00ab :
            data[i] = (byte) 0xAE;
            break;
          case (char) 0x00bb :
            data[i] = (byte) 0xAF;
            break;
          case (char) 0x2591 :
            data[i] = (byte) 0xB0;
            break;
          case (char) 0x2592 :
            data[i] = (byte) 0xB1;
            break;
          case (char) 0x2593 :
            data[i] = (byte) 0xB2;
            break;
          case (char) 0x2502 :
            data[i] = (byte) 0xB3;
            break;
          case (char) 0x2524 :
            data[i] = (byte) 0xB4;
            break;
          case (char) 0x00c1 :
            data[i] = (byte) 0xB5;
            break;
          case (char) 0x00c2 :
            data[i] = (byte) 0xB6;
            break;
          case (char) 0x00c0 :
            data[i] = (byte) 0xB7;
            break;
          case (char) 0x00a9 :
            data[i] = (byte) 0xB8;
            break;
          case (char) 0x2563 :
            data[i] = (byte) 0xB9;
            break;
          case (char) 0x2551 :
            data[i] = (byte) 0xBA;
            break;
          case (char) 0x2557 :
            data[i] = (byte) 0xBB;
            break;
          case (char) 0x255d :
            data[i] = (byte) 0xBC;
            break;
          case (char) 0x00a2 :
            data[i] = (byte) 0xBD;
            break;
          case (char) 0x00a5 :
            data[i] = (byte) 0xBE;
            break;
          case (char) 0x2510 :
            data[i] = (byte) 0xBF;
            break;
          case (char) 0x2514 :
            data[i] = (byte) 0xC0;
            break;
          case (char) 0x2534 :
            data[i] = (byte) 0xC1;
            break;
          case (char) 0x252c :
            data[i] = (byte) 0xC2;
            break;
          case (char) 0x251c :
            data[i] = (byte) 0xC3;
            break;
          case (char) 0x2500 :
            data[i] = (byte) 0xC4;
            break;
          case (char) 0x253c :
            data[i] = (byte) 0xC5;
            break;
          case (char) 0x00e3 :
            data[i] = (byte) 0xC6;
            break;
          case (char) 0x00c3 :
            data[i] = (byte) 0xC7;
            break;
          case (char) 0x255a :
            data[i] = (byte) 0xC8;
            break;
          case (char) 0x2554 :
            data[i] = (byte) 0xC9;
            break;
          case (char) 0x2569 :
            data[i] = (byte) 0xCA;
            break;
          case (char) 0x2566 :
            data[i] = (byte) 0xCB;
            break;
          case (char) 0x2560 :
            data[i] = (byte) 0xCC;
            break;
          case (char) 0x2550 :
            data[i] = (byte) 0xCD;
            break;
          case (char) 0x256c :
            data[i] = (byte) 0xCE;
            break;
          case (char) 0x00a4 :
            data[i] = (byte) 0xCF;
            break;
          case (char) 0x00f0 :
            data[i] = (byte) 0xD0;
            break;
          case (char) 0x00d0 :
            data[i] = (byte) 0xD1;
            break;
          case (char) 0x00ca :
            data[i] = (byte) 0xD2;
            break;
          case (char) 0x00cb :
            data[i] = (byte) 0xD3;
            break;
          case (char) 0x00c8 :
            data[i] = (byte) 0xD4;
            break;
          case (char) 0x20ac :
            data[i] = (byte) 0xD5;
            break;
          case (char) 0x00cd :
            data[i] = (byte) 0xD6;
            break;
          case (char) 0x00ce :
            data[i] = (byte) 0xD7;
            break;
          case (char) 0x00cf :
            data[i] = (byte) 0xD8;
            break;
          case (char) 0x2518 :
            data[i] = (byte) 0xD9;
            break;
          case (char) 0x250c :
            data[i] = (byte) 0xDA;
            break;
          case (char) 0x2588 :
            data[i] = (byte) 0xDB;
            break;
          case (char) 0x2584 :
            data[i] = (byte) 0xDC;
            break;
          case (char) 0x00a6 :
            data[i] = (byte) 0xDD;
            break;
          case (char) 0x00cc :
            data[i] = (byte) 0xDE;
            break;
          case (char) 0x2580 :
            data[i] = (byte) 0xDF;
            break;
          case (char) 0x00d3 :
            data[i] = (byte) 0xE0;
            break;
          case (char) 0x00df :
            data[i] = (byte) 0xE1;
            break;
          case (char) 0x00d4 :
            data[i] = (byte) 0xE2;
            break;
          case (char) 0x00d2 :
            data[i] = (byte) 0xE3;
            break;
          case (char) 0x00f5 :
            data[i] = (byte) 0xE4;
            break;
          case (char) 0x00d5 :
            data[i] = (byte) 0xE5;
            break;
          case (char) 0x00b5 :
            data[i] = (byte) 0xE6;
            break;
          case (char) 0x00fe :
            data[i] = (byte) 0xE7;
            break;
          case (char) 0x00de :
            data[i] = (byte) 0xE8;
            break;
          case (char) 0x00da :
            data[i] = (byte) 0xE9;
            break;
          case (char) 0x00db :
            data[i] = (byte) 0xEA;
            break;
          case (char) 0x00d9 :
            data[i] = (byte) 0xEB;
            break;
          case (char) 0x00fd :
            data[i] = (byte) 0xEC;
            break;
          case (char) 0x00dd :
            data[i] = (byte) 0xED;
            break;
          case (char) 0x00af :
            data[i] = (byte) 0xEE;
            break;
          case (char) 0x00b4 :
            data[i] = (byte) 0xEF;
            break;
          case (char) 0x00ad :
            data[i] = (byte) 0xF0;
            break;
          case (char) 0x00b1 :
            data[i] = (byte) 0xF1;
            break;
          case (char) 0x2017 :
            data[i] = (byte) 0xF2;
            break;
          case (char) 0x00be :
            data[i] = (byte) 0xF3;
            break;
          case (char) 0x00b6 :
            data[i] = (byte) 0xF4;
            break;
          case (char) 0x00a7 :
            data[i] = (byte) 0xF5;
            break;
          case (char) 0x00f7 :
            data[i] = (byte) 0xF6;
            break;
          case (char) 0x00b8 :
            data[i] = (byte) 0xF7;
            break;
          case (char) 0x00b0 :
            data[i] = (byte) 0xF8;
            break;
          case (char) 0x00a8 :
            data[i] = (byte) 0xF9;
            break;
          case (char) 0x00b7 :
            data[i] = (byte) 0xFA;
            break;
          case (char) 0x00b9 :
            data[i] = (byte) 0xFB;
            break;
          case (char) 0x00b3 :
            data[i] = (byte) 0xFC;
            break;
          case (char) 0x00b2 :
            data[i] = (byte) 0xFD;
            break;
          case (char) 0x25a0 :
            data[i] = (byte) 0xFE;
            break;
          case (char) 0x00a0 :
            data[i] = (byte) 0xFF;
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
      charData[i] = chars858[(0xFF & input[i + offset])];
    }

    return new String(charData);
  }
}