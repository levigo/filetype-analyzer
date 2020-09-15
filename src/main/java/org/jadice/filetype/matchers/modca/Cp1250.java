package org.jadice.filetype.matchers.modca;



/**
 * Class declaration
 */
class Cp1250 extends CpConvert
{
	private final char[] Cp1250 = {
		(char)0x0000,(char)0x0001,(char)0x0002,(char)0x0003,(char)0x0004,(char)0x0005,(char)0x0006,(char)0x0007,
		(char)0x0008,(char)0x0009,(char)0x000A,(char)0x000B,(char)0x000C,(char)0x000D,(char)0x000E,(char)0x000F,
		(char)0x0010,(char)0x0011,(char)0x0012,(char)0x0013,(char)0x0014,(char)0x0015,(char)0x0016,(char)0x0017,
		(char)0x0018,(char)0x0019,(char)0x001A,(char)0x001B,(char)0x001C,(char)0x001D,(char)0x001E,(char)0x001F,
		(char)0x0020,(char)0x0021,(char)0x0022,(char)0x0023,(char)0x0024,(char)0x0025,(char)0x0026,(char)0x0027,
		(char)0x0028,(char)0x0029,(char)0x002A,(char)0x002B,(char)0x002C,(char)0x002D,(char)0x002E,(char)0x002F,
		(char)0x0030,(char)0x0031,(char)0x0032,(char)0x0033,(char)0x0034,(char)0x0035,(char)0x0036,(char)0x0037,
		(char)0x0038,(char)0x0039,(char)0x003A,(char)0x003B,(char)0x003C,(char)0x003D,(char)0x003E,(char)0x003F,
		(char)0x0040,(char)0x0041,(char)0x0042,(char)0x0043,(char)0x0044,(char)0x0045,(char)0x0046,(char)0x0047,
		(char)0x0048,(char)0x0049,(char)0x004A,(char)0x004B,(char)0x004C,(char)0x004D,(char)0x004E,(char)0x004F,
		(char)0x0050,(char)0x0051,(char)0x0052,(char)0x0053,(char)0x0054,(char)0x0055,(char)0x0056,(char)0x0057,
		(char)0x0058,(char)0x0059,(char)0x005A,(char)0x005B,(char)0x005C,(char)0x005D,(char)0x005E,(char)0x005F,
		(char)0x0060,(char)0x0061,(char)0x0062,(char)0x0063,(char)0x0064,(char)0x0065,(char)0x0066,(char)0x0067,
		(char)0x0068,(char)0x0069,(char)0x006A,(char)0x006B,(char)0x006C,(char)0x006D,(char)0x006E,(char)0x006F,
		(char)0x0070,(char)0x0071,(char)0x0072,(char)0x0073,(char)0x0074,(char)0x0075,(char)0x0076,(char)0x0077,
		(char)0x0078,(char)0x0079,(char)0x007A,(char)0x007B,(char)0x007C,(char)0x007D,(char)0x007E,(char)0x007F,
		(char)0x20AC,(char)0xFFFD,(char)0x201A,(char)0xFFFD,(char)0x201E,(char)0x2026,(char)0x2020,(char)0x2021,
		(char)0xFFFD,(char)0x2030,(char)0x0160,(char)0x2039,(char)0x015A,(char)0x0164,(char)0x017D,(char)0x0179,
		(char)0xFFFD,(char)0x2018,(char)0x2019,(char)0x201C,(char)0x201D,(char)0x2022,(char)0x2013,(char)0x2014,
		(char)0xFFFD,(char)0x2122,(char)0x0161,(char)0x203A,(char)0x015B,(char)0x0165,(char)0x017E,(char)0x017A,
		(char)0x00A0,(char)0x02C7,(char)0x02D8,(char)0x0141,(char)0x00A4,(char)0x0104,(char)0x00A6,(char)0x00A7,
		(char)0x00A8,(char)0x00A9,(char)0x015E,(char)0x00AB,(char)0x00AC,(char)0x00AD,(char)0x00AE,(char)0x017B,
		(char)0x00B0,(char)0x00B1,(char)0x02DB,(char)0x0142,(char)0x00B4,(char)0x00B5,(char)0x00B6,(char)0x00B7,
		(char)0x00B8,(char)0x0105,(char)0x015F,(char)0x00BB,(char)0x013D,(char)0x02DD,(char)0x013E,(char)0x017C,
		(char)0x0154,(char)0x00C1,(char)0x00C2,(char)0x0102,(char)0x00C4,(char)0x0139,(char)0x0106,(char)0x00C7,
		(char)0x010C,(char)0x00C9,(char)0x0118,(char)0x00CB,(char)0x011A,(char)0x00CD,(char)0x00CE,(char)0x010E,
		(char)0x0110,(char)0x0143,(char)0x0147,(char)0x00D3,(char)0x00D4,(char)0x0150,(char)0x00D6,(char)0x00D7,
		(char)0x0158,(char)0x016E,(char)0x00DA,(char)0x0170,(char)0x00DC,(char)0x00DD,(char)0x0162,(char)0x00DF,
		(char)0x0155,(char)0x00E1,(char)0x00E2,(char)0x0103,(char)0x00E4,(char)0x013A,(char)0x0107,(char)0x00E7,
		(char)0x010D,(char)0x00E9,(char)0x0119,(char)0x00EB,(char)0x011B,(char)0x00ED,(char)0x00EE,(char)0x010F,
		(char)0x0111,(char)0x0144,(char)0x0148,(char)0x00F3,(char)0x00F4,(char)0x0151,(char)0x00F6,(char)0x00F7,
		(char)0x0159,(char)0x016F,(char)0x00FA,(char)0x0171,(char)0x00FC,(char)0x00FD,(char)0x0163,(char)0x02D9,
	};

	public byte[] encode(final String javaString) throws java.io.UnsupportedEncodingException
	{
	    char[] raw  = javaString.toCharArray();
		byte[] data = new byte[raw.length];

		for (int i = 0; i < raw.length; i++) {
			if( raw[i] <= 0x007F ) {
				data[i] = (byte)raw[i];
			} else {
				switch( raw[i] ) {
					case (char)0x20AC : data[i] = (byte)128; break;
					case (char)0xFFFD : data[i] = (byte)129; break;
					case (char)0x201A : data[i] = (byte)130; break;
//					case (char)0xFFFD : data[i] = (byte)131; break;
					case (char)0x201E : data[i] = (byte)132; break;
					case (char)0x2026 : data[i] = (byte)133; break;
					case (char)0x2020 : data[i] = (byte)134; break;
					case (char)0x2021 : data[i] = (byte)135; break;
//					case (char)0xFFFD : data[i] = (byte)136; break;
					case (char)0x2030 : data[i] = (byte)137; break;
					case (char)0x0160 : data[i] = (byte)138; break;
					case (char)0x2039 : data[i] = (byte)139; break;
					case (char)0x015A : data[i] = (byte)140; break;
					case (char)0x0164 : data[i] = (byte)141; break;
					case (char)0x017D : data[i] = (byte)142; break;
					case (char)0x0179 : data[i] = (byte)143; break;
//					case (char)0xFFFD : data[i] = (byte)144; break;
					case (char)0x2018 : data[i] = (byte)145; break;
					case (char)0x2019 : data[i] = (byte)146; break;
					case (char)0x201C : data[i] = (byte)147; break;
					case (char)0x201D : data[i] = (byte)148; break;
					case (char)0x2022 : data[i] = (byte)149; break;
					case (char)0x2013 : data[i] = (byte)150; break;
					case (char)0x2014 : data[i] = (byte)151; break;
//					case (char)0xFFFD : data[i] = (byte)152; break;
					case (char)0x2122 : data[i] = (byte)153; break;
					case (char)0x0161 : data[i] = (byte)154; break;
					case (char)0x203A : data[i] = (byte)155; break;
					case (char)0x015B : data[i] = (byte)156; break;
					case (char)0x0165 : data[i] = (byte)157; break;
					case (char)0x017E : data[i] = (byte)158; break;
					case (char)0x017A : data[i] = (byte)159; break;
					case (char)0x00A0 : data[i] = (byte)160; break;
					case (char)0x02C7 : data[i] = (byte)161; break;
					case (char)0x02D8 : data[i] = (byte)162; break;
					case (char)0x0141 : data[i] = (byte)163; break;
					case (char)0x00A4 : data[i] = (byte)164; break;
					case (char)0x0104 : data[i] = (byte)165; break;
					case (char)0x00A6 : data[i] = (byte)166; break;
					case (char)0x00A7 : data[i] = (byte)167; break;
					case (char)0x00A8 : data[i] = (byte)168; break;
					case (char)0x00A9 : data[i] = (byte)169; break;
					case (char)0x015E : data[i] = (byte)170; break;
					case (char)0x00AB : data[i] = (byte)171; break;
					case (char)0x00AC : data[i] = (byte)172; break;
					case (char)0x00AD : data[i] = (byte)173; break;
					case (char)0x00AE : data[i] = (byte)174; break;
					case (char)0x017B : data[i] = (byte)175; break;
					case (char)0x00B0 : data[i] = (byte)176; break;
					case (char)0x00B1 : data[i] = (byte)177; break;
					case (char)0x02DB : data[i] = (byte)178; break;
					case (char)0x0142 : data[i] = (byte)179; break;
					case (char)0x00B4 : data[i] = (byte)180; break;
					case (char)0x00B5 : data[i] = (byte)181; break;
					case (char)0x00B6 : data[i] = (byte)182; break;
					case (char)0x00B7 : data[i] = (byte)183; break;
					case (char)0x00B8 : data[i] = (byte)184; break;
					case (char)0x0105 : data[i] = (byte)185; break;
					case (char)0x015F : data[i] = (byte)186; break;
					case (char)0x00BB : data[i] = (byte)187; break;
					case (char)0x013D : data[i] = (byte)188; break;
					case (char)0x02DD : data[i] = (byte)189; break;
					case (char)0x013E : data[i] = (byte)190; break;
					case (char)0x017C : data[i] = (byte)191; break;
					case (char)0x0154 : data[i] = (byte)192; break;
					case (char)0x00C1 : data[i] = (byte)193; break;
					case (char)0x00C2 : data[i] = (byte)194; break;
					case (char)0x0102 : data[i] = (byte)195; break;
					case (char)0x00C4 : data[i] = (byte)196; break;
					case (char)0x0139 : data[i] = (byte)197; break;
					case (char)0x0106 : data[i] = (byte)198; break;
					case (char)0x00C7 : data[i] = (byte)199; break;
					case (char)0x010C : data[i] = (byte)200; break;
					case (char)0x00C9 : data[i] = (byte)201; break;
					case (char)0x0118 : data[i] = (byte)202; break;
					case (char)0x00CB : data[i] = (byte)203; break;
					case (char)0x011A : data[i] = (byte)204; break;
					case (char)0x00CD : data[i] = (byte)205; break;
					case (char)0x00CE : data[i] = (byte)206; break;
					case (char)0x010E : data[i] = (byte)207; break;
					case (char)0x0110 : data[i] = (byte)208; break;
					case (char)0x0143 : data[i] = (byte)209; break;
					case (char)0x0147 : data[i] = (byte)210; break;
					case (char)0x00D3 : data[i] = (byte)211; break;
					case (char)0x00D4 : data[i] = (byte)212; break;
					case (char)0x0150 : data[i] = (byte)213; break;
					case (char)0x00D6 : data[i] = (byte)214; break;
					case (char)0x00D7 : data[i] = (byte)215; break;
					case (char)0x0158 : data[i] = (byte)216; break;
					case (char)0x016E : data[i] = (byte)217; break;
					case (char)0x00DA : data[i] = (byte)218; break;
					case (char)0x0170 : data[i] = (byte)219; break;
					case (char)0x00DC : data[i] = (byte)220; break;
					case (char)0x00DD : data[i] = (byte)221; break;
					case (char)0x0162 : data[i] = (byte)222; break;
					case (char)0x00DF : data[i] = (byte)223; break;
					case (char)0x0155 : data[i] = (byte)224; break;
					case (char)0x00E1 : data[i] = (byte)225; break;
					case (char)0x00E2 : data[i] = (byte)226; break;
					case (char)0x0103 : data[i] = (byte)227; break;
					case (char)0x00E4 : data[i] = (byte)228; break;
					case (char)0x013A : data[i] = (byte)229; break;
					case (char)0x0107 : data[i] = (byte)230; break;
					case (char)0x00E7 : data[i] = (byte)231; break;
					case (char)0x010D : data[i] = (byte)232; break;
					case (char)0x00E9 : data[i] = (byte)233; break;
					case (char)0x0119 : data[i] = (byte)234; break;
					case (char)0x00EB : data[i] = (byte)235; break;
					case (char)0x011B : data[i] = (byte)236; break;
					case (char)0x00ED : data[i] = (byte)237; break;
					case (char)0x00EE : data[i] = (byte)238; break;
					case (char)0x010F : data[i] = (byte)239; break;
					case (char)0x0111 : data[i] = (byte)240; break;
					case (char)0x0144 : data[i] = (byte)241; break;
					case (char)0x0148 : data[i] = (byte)242; break;
					case (char)0x00F3 : data[i] = (byte)243; break;
					case (char)0x00F4 : data[i] = (byte)244; break;
					case (char)0x0151 : data[i] = (byte)245; break;
					case (char)0x00F6 : data[i] = (byte)246; break;
					case (char)0x00F7 : data[i] = (byte)247; break;
					case (char)0x0159 : data[i] = (byte)248; break;
					case (char)0x016F : data[i] = (byte)249; break;
					case (char)0x00FA : data[i] = (byte)250; break;
					case (char)0x0171 : data[i] = (byte)251; break;
					case (char)0x00FC : data[i] = (byte)252; break;
					case (char)0x00FD : data[i] = (byte)253; break;
					case (char)0x0163 : data[i] = (byte)254; break;
					case (char)0x02D9 : data[i] = (byte)255; break;

					default: data[i] = (byte)0x20; // BLANK or SPACE
				}
			}
		} 
		return data;
	}

	public final String toString(final byte[] input, final int offset, final int length) throws java.io.UnsupportedEncodingException
	{
		char[] charData = new char[length];

		for (int i = length; --i >= 0; ) {
			charData[i] = Cp1250[(0xFF & input[i + offset])];
		} 

		return new String(charData);
	}
}