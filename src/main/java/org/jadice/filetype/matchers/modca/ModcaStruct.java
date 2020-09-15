/**
 * Copyright (c) 1995-2015 levigo holding gmbh. All Rights Reserved.
 *
 * This software is the proprietary information of levigo holding gmbh. Use is subject to license
 * terms.
 */
package org.jadice.filetype.matchers.modca;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.pdfbox.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modca structure class.
 */
final public class ModcaStruct implements MODCAConstants, Serializable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModcaStruct.class);

  private static final long serialVersionUID = 230540971691271128L;

  private static final int MAX_0xD3_FUZZ = 100;

  private ModcaStruct parent = null;

  private int code = 0;
  private byte flags;
  private byte[] header = null;
  private byte[] data = null;
  public boolean isAFP = false;

  // the offsets of the various elements (triples, repeating groups, names)
  private int byteOffsets[];

  // Child elements
  private List<ModcaStruct> children = null;

  // Triplets & repeating groups
  private List<Triplet> triplets = null;
  private List<List<Triplet>> repeatingGroups = null;

  // Code page for this Object
  private int codePage = 500;

  // Other information
  private String name = null;

  // reserved header data -> last 2 bytes of header (see modca documentation)
  private final byte[] reservedHeaderData = new byte[2];

  // encoding scheme
  private int encoding = 0;

  @Override
  protected void finalize() throws Throwable {
    if (children != null)
      children.clear();
    children = null;
    if (triplets != null)
      triplets.clear();
    triplets = null;
    if (repeatingGroups != null)
      repeatingGroups.clear();
    repeatingGroups = null;
    super.finalize();
  }

  /**
   * The table used for code page identification: 1. ccsid 2. cpgid (code page) 3. gcsgid 4.
   * encoding scheme
   */
  private static final int[] ccsids[] = {
      {
          37, 37, 697, 1100
      }, {
          256, 256, 337, 1100
      }, {
          273, 273, 697, 1100
      }, {
          277, 277, 697, 1100
      }, {
          278, 278, 697, 1100
      }, {
          280, 280, 697, 1100
      }, {
          284, 284, 697, 1100
      }, {
          285, 285, 697, 1100
      }, {
          290, 290, 1172, 1100
      }, {
          297, 297, 697, 1100
      }, {
          300, 300, 1001, 1200
      }, {
          301, 301, 370, 2200
      }, {
          367, 367, 103, 5100
      }, {
          420, 420, 235, 1100
      }, {
          423, 423, 218, 1100
      }, {
          424, 424, 941, 1100
      }, {
          437, 437, 1212, 2100
      }, {
          500, 500, 697, 1100
      }, {
          720, 720, 814, 2100
      }, {
          737, 737, 812, 2100
      }, {
          775, 775, 813, 2100
      }, {
          808, 808, 1384, 2100
      }, {
          813, 813, 925, 4100
      }, {
          819, 819, 697, 4100
      }, {
          833, 833, 1173, 1100
      }, {
          834, 834, 934, 1200
      }, {
          835, 835, 935, 1200
      }, {
          836, 836, 1174, 1100
      }, {
          837, 837, 937, 1200
      }, {
          838, 838, 1176, 1100
      }, {
          848, 848, 1389, 2100
      }, {
          849, 849, 1386, 2100
      }, {
          850, 850, 1106, 2100
      }, {
          851, 851, 1231, 2100
      }, {
          852, 852, 1232, 2100
      }, {
          855, 855, 1235, 2100
      }, {
          857, 857, 1237, 2100
      }, {
          860, 860, 1213, 2100
      }, {
          861, 861, 1214, 2100
      }, {
          862, 862, 1217, 2100
      }, {
          863, 863, 1215, 2100
      }, {
          864, 864, 1244, 2100
      }, {
          865, 865, 1216, 2100
      }, {
          866, 866, 1190, 2100
      }, {
          868, 868, 1248, 2100
      }, {
          869, 869, 1249, 2100
      }, {
          870, 870, 959, 1100
      }, {
          871, 871, 697, 1100
      }, {
          872, 872, 1382, 2100
      }, {
          874, 874, 1176, 2100
      }, {
          875, 875, 925, 1100
      }, {
          878, 878, 948, 4105
      }, {
          880, 880, 960, 1100
      }, {
          891, 891, 1224, 2100
      }, {
          897, 897, 1122, 2100
      }, {
          901, 901, 1393, 4100
      }, {
          902, 902, 1391, 4100
      }, {
          903, 903, 1185, 2100
      }, {
          904, 904, 103, 2100
      }, {
          905, 905, 965, 1100
      }, {
          912, 912, 959, 4100
      }, {
          914, 914, 1256, 4100
      }, {
          915, 915, 1150, 4100
      }, {
          916, 916, 941, 4100
      }, {
          918, 918, 1160, 1100
      }, {
          920, 920, 1152, 4100
      }, {
          921, 921, 1305, 4100
      }, {
          922, 922, 1307, 4100
      }, {
          923, 923, 1353, 4100
      }, {
          924, 924, 1353, 1100
      }, {
          926, 926, 934, 2200
      }, {
          927, 927, 935, 2200
      }, {
          928, 928, 937, 2200
      }, {
          930, 290, 1172, 1301
      }, {
          932, 897, 1122, 2300
      }, {
          933, 833, 1173, 1301
      }, {
          934, 891, 1224, 2300
      }, {
          935, 836, 1174, 1301
      }, {
          936, 903, 1185, 2300
      }, {
          937, 37, 1175, 1301
      }, {
          938, 904, 103, 2300
      }, {
          939, 1027, 1172, 1301
      }, {
          941, 941, 65535, 2200
      }, {
          942, 1041, 1172, 2300
      }, {
          943, 941, 65535, 2300
      }, {
          944, 1040, 1173, 2300
      }, {
          946, 1042, 1239, 2300
      }, {
          947, 947, 935, 2200
      }, {
          948, 1043, 1175, 2300
      }, {
          949, 1088, 1278, 2300
      }, {
          950, 1114, 103, 2300
      }, {
          951, 1050, 951, 2200
      }, {
          954, 895, 1120, 4403
      }, {
          956, 895, 1120, 5404
      }, {
          957, 895, 1120, 5404
      }, {
          958, 367, 103, 5404
      }, {
          959, 367, 103, 5404
      }, {
          964, 367, 103, 4403
      }, {
          965, 367, 103, 5404
      }, {
          970, 367, 103, 4403
      }, {
          1008, 1008, 1162, 4100
      }, {
          1009, 1009, 1169, 5100
      }, {
          1010, 1010, 288, 5100
      }, {
          1011, 1011, 265, 5100
      }, {
          1012, 1012, 293, 5100
      }, {
          1013, 1013, 1118, 5100
      }, {
          1014, 1014, 1119, 5100
      }, {
          1015, 1015, 1116, 5100
      }, {
          1016, 1016, 1117, 5100
      }, {
          1017, 1017, 1135, 5100
      }, {
          1018, 1018, 1136, 5100
      }, {
          1019, 1019, 1137, 5100
      }, {
          1025, 1025, 1150, 1100
      }, {
          1026, 1026, 1152, 1100
      }, {
          1027, 1027, 1172, 1100
      }, {
          1040, 1040, 1173, 2100
      }, {
          1041, 1041, 1172, 2100
      }, {
          1042, 1042, 1239, 2100
      }, {
          1043, 1043, 1175, 2100
      }, {
          1046, 1046, 1177, 4105
      }, {
          1051, 1051, 1201, 4100
      }, {
          1088, 1088, 1278, 2100
      }, {
          1089, 1089, 1271, 4100
      }, {
          1097, 1097, 1219, 1100
      }, {
          1098, 1098, 1288, 2100
      }, {
          1112, 1112, 1305, 1100
      }, {
          1114, 1114, 103, 2100
      }, {
          1115, 1115, 103, 2100
      }, {
          1122, 1122, 1307, 1100
      }, {
          1123, 1123, 1326, 1100
      }, {
          1124, 1124, 1326, 4100
      }, {
          1125, 1125, 1332, 2100
      }, {
          1126, 1126, 65535, 2100
      }, {
          1129, 1129, 1336, 4100
      }, {
          1130, 1130, 1336, 1100
      }, {
          1131, 1131, 1339, 2100
      }, {
          1132, 1132, 1341, 1100
      }, {
          1133, 1133, 1341, 4100
      }, {
          1137, 1137, 1467, 1100
      }, {
          1140, 1140, 695, 1100
      }, {
          1141, 1141, 695, 1100
      }, {
          1142, 1142, 695, 1100
      }, {
          1143, 1143, 695, 1100
      }, {
          1144, 1144, 695, 1100
      }, {
          1145, 1145, 695, 1100
      }, {
          1146, 1146, 695, 1100
      }, {
          1147, 1147, 695, 1100
      }, {
          1148, 1148, 695, 1100
      }, {
          1149, 1149, 695, 1100
      }, {
          1153, 1153, 1375, 1100
      }, {
          1154, 1154, 1381, 1100
      }, {
          1155, 1155, 1378, 1100
      }, {
          1156, 1156, 1393, 1100
      }, {
          1157, 1157, 1391, 1100
      }, {
          1158, 1158, 1388, 1100
      }, {
          1160, 1160, 1395, 1100
      }, {
          1164, 1164, 1397, 1100
      }, {
          1200, 1400, 65535, 7200
      }, {
          1208, 1400, 65535, 7807
      }, {
          1250, 1250, 1400, 4105
      }, {
          1251, 1251, 1401, 4105
      }, {
          1252, 1252, 1402, 4105
      }, {
          1253, 1253, 1403, 4105
      }, {
          1254, 1254, 1404, 4105
      }, {
          1255, 1255, 1405, 4105
      }, {
          1256, 1256, 1406, 4105
      }, {
          1257, 1257, 1407, 4105
      }, {
          1258, 1258, 1408, 4105
      }, {
          1275, 1275, 1425, 4105
      }, {
          1280, 1280, 1430, 4105
      }, {
          1281, 1281, 1431, 4105
      }, {
          1282, 1282, 1432, 4105
      }, {
          1283, 1283, 1433, 4105
      }, {
          1362, 1362, 65535, 2200
      }, {
          1363, 1363, 65535, 2300
      }, {
          1364, 1364, 65535, 1301
      }, {
          1380, 1380, 937, 2200
      }, {
          1381, 1115, 103, 2300
      }, {
          1382, 1382, 1081, 8200
      }, {
          1383, 367, 103, 2300
      }, {
          1385, 1385, 65535, 2200
      }, {
          1386, 1114, 65535, 2300
      }, {
          4396, 300, 370, 1200
      }, {
          4948, 852, 959, 2100
      }, {
          4951, 855, 1150, 2100
      }, {
          4952, 856, 941, 2100
      }, {
          4953, 857, 1152, 2100
      }, {
          4960, 864, 235, 2100
      }, {
          4965, 869, 925, 2100
      }, {
          5026, 290, 1172, 1301
      }, {
          5035, 1027, 1172, 1301
      }, {
          5050, 895, 1120, 4403
      }, {
          5052, 895, 1120, 5404
      }, {
          5053, 895, 1120, 5404
      }, {
          5054, 367, 103, 5404
      }, {
          5055, 367, 103, 5404
      }, {
          8612, 420, 1142, 1100
      }, {
          9030, 838, 1279, 1100
      }, {
          9056, 864, 1101, 2100
      }, {
          9066, 874, 1279, 2100
      }
  };

  /**
   * Class representing a MO:DCA triplet
   */
  final public class Triplet implements Serializable {

    private static final long serialVersionUID = 3186184789397500150L;

    public byte bId;
    int len;
    public byte[] data;

    @Override
    public String toString() {
      return "Triplet: 0x" + Integer.toHexString(bId) + ", len:" + len + ", data: 0x" + Hex.getString(data);
    }

    Triplet(final int offset, final byte[] data) throws IOException {
      len = 0xFF & data[offset];

      if (len > data.length - offset) {
        len = data.length - offset;
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Unsufficient triplet data");
      }
      if (len <= 0)
        throw new IOException("Zero triplet length");

      bId = data[offset + 1];
      this.data = new byte[(len - 2)];

      System.arraycopy(data, offset + 2, this.data, 0, this.data.length);

      if (bId == 1) {
        setCodePage();
      }
    }

    public int getEncoding() {
      return encoding;
    }

    /**
     * Decode an integer at the given data offset with the given length in bytes.
     */
    public final int getInt(final int offset, final int length) {
      return decodeInt(data, offset, length);
    }

    /**
     * Method declaration
     * 
     * 
     * @see
     */
    void setCodePage() {
      int origEncoder = getCodePage();
      int gcsgid = ((0xFF & data[0]) << 8) + (0xFF & data[1]);

      ModcaStruct.this.setCodePage(((0xFF & data[2]) << 8) + (0xFF & data[3]));

      // System.out.println( "1) setCodePage: gcsgid:"+gcsgid+",
      // encoder:"+encoder );
      if (gcsgid == 0) {
        int ccsid = getCodePage();

        ModcaStruct.this.setCodePage(0);

        for (int i = 0; i < ccsids.length; i++) {
          if (ccsids[i][0] == ccsid) {
            ModcaStruct.this.setCodePage(ccsids[i][1]);
            gcsgid = ccsids[i][2];
            encoding = ccsids[i][3];
            break;
          }
        }

        // System.out.println( "*) ccsid:"+ccsid+", gcsgid:"+gcsgid+",
        // encoder:"+encoder );
        if (getCodePage() == 0) {
          ModcaStruct.this.setCodePage(origEncoder);
          gcsgid = 697;
        }
      }

      // System.out.println( "2) setCodePage: gcsgid:"+gcsgid+",
      // encoder:"+encoder );
    }
  }

  /**
   * Create a new ModcaStruct from the given InputStream. The parent may be null.
   */
  public ModcaStruct(final ModcaStruct parent, final int id, final byte data[]) {
    this.parent = parent;
    this.code = id;
    this.data = data;

    if (null != data)
      try {
        processData();
      } catch (IOException e) {
        LOGGER.warn("Can't process struct data", e);
      }
  }

  /**
   * Create a new ModcaStruct from the given InputStream. The parene may be null.
   */
  public ModcaStruct(final ModcaStruct parent, final InputStream is) throws IOException {

    this.parent = parent;

    if (parent != null)
      codePage = parent.codePage;

    byte[] modcaHeader = new byte[8];

    readFully(is, modcaHeader, 0, modcaHeader.length);

    // Some documents have a block lenght of 0x0D0A, these "valid block headrs"
    // were being "reduced" by 2 bytes, leading to JiveInvalidFormatException,
    // This code block checks for valid MO:DCA or AFP structure.
    // This would/could possibly remove the "QuirkManager" requirement here.

    int skip = 0;

    // CR/LF detected
    if ((modcaHeader[0] & 0xff) == 0x0D && (modcaHeader[1] & 0xff) == 0x0A) {
      // check for AFP
      if ((modcaHeader[2] & 0xff) == 0x5A && (modcaHeader[5] & 0xff) == 0xD3) {
        isAFP = true;
        skip = 2;
      } else {
        // check for MODCA
        // if 0xD3 is not found we have no CR/LF
        // in this case 0x0D0A is MODCA header lenght
        if ((modcaHeader[4] & 0xff) == 0xD3) {
          skip = 2;
        }
      }
    }

    if (skip > 0)
      skip(is, modcaHeader, skip);

    // if there's still no 0xd3 @ 2, we're doomed...
    int fuzz = 0;
    while ((modcaHeader[2] & 0xff) != 0xD3 && !((modcaHeader[0] & 0xff) == 0x5A && (modcaHeader[3] & 0xff) == 0xD3)) {
      if (fuzz++ < MAX_0xD3_FUZZ) {
        skip(is, modcaHeader, 1);
        continue;
      }

      throw new IOException(" - 0xD3 mismatch rawData - 0x" + Hex.getString(modcaHeader));
    }

    // Check for AFP data
    if ((modcaHeader[0] & 0xff) == 0x5A && (modcaHeader[3] & 0xff) == 0xD3) {
      isAFP = true;
      skip(is, modcaHeader, 1);
    }

    if (fuzz != 0)
      LOGGER.warn("Struct data length error", fuzz);

    header = modcaHeader;

    // store reserved header data
    reservedHeaderData[0] = header[6];
    reservedHeaderData[1] = header[7];

    int length = decodeInt(modcaHeader, 0, 2);
    code = decodeInt(modcaHeader, 3, 2);
    flags = modcaHeader[5];

    length -= 8;

    if (length > 0) {
      data = new byte[length];

      readFully(is, data, 0, length);
      processData();
    }
  }

  private void skip(final InputStream is, final byte[] modcaHeader, final int skip) throws IOException {
    System.arraycopy(modcaHeader, skip, modcaHeader, 0, modcaHeader.length - skip);

    // re-fill the header
    readFully(is, modcaHeader, modcaHeader.length - skip, skip);
  }

  /**
   * Load a single struct from the given input stream. The parent may be null.
   */
  static final public ModcaStruct loadStruct(final ModcaStruct parent, final InputStream is) throws IOException {
    return new ModcaStruct(parent, is);
  }

  private static final int decodeInt(final byte data[], int offset, int length) {
    int result = 0;
    while (length-- > 0) {
      result <<= 8;
      result |= 0xff & data[offset++];
    }
    return result;
  }

  public static final int storeInt(final byte data[], final int offset, int length, int value) {
    int result = 0;
    while (length-- > 0) {
      data[offset + length] = (byte) (value & 0xff);
      value >>= 8;
    }
    return result;
  }

  /**
   * Decode an integer at the given data offset with the given length in bytes.
   */
  public int getInt(final int offset, final int length) {
    return decodeInt(data, offset, length);
  }

  /**
   * Decode an integer at the given data offset with a length of one octet.
   */
  public int getIntOctet(final int offset) {
    return data[offset] & 0xff;
  }

  /**
   * Store an integer at the given data offset with the given length in bytes.
   */
  public void storeInt(final int offset, final int length, final int value) {
    storeInt(data, offset, length, value);
  }

  /**
   * Store an integer at the given data offset with a length of one octet.
   */
  public void storeIntOctet(final int offset, final int value) {
    data[offset] = (byte) (value & 0xff);
  }

  /**
   * Load a whole tree of structs from the given input stream.
   */
  public void loadChildren(final InputStream is) throws IOException {
    if (isBeginCode()) {
      // System.out.println( "<<< - - "+ toHex( parent.code ) + " - " +
      // parent.name );
      children = new LinkedList<ModcaStruct>();

      ModcaStruct child;
      do {
        child = loadStruct(this, is);

        children.add(child);
        child.loadChildren(is);
      } while (child.code != getCorrespondingEndCode());
    }
  }

  public void saveStruct(final OutputStream os, final boolean includeChildren) throws IOException {

    if (isAFP)
      // write afp head
      os.write(new byte[]{
          0x5a
      });

    byte header[] = new byte[8];
    int length = 8;
    if (null != data)
      length += data.length;

    // store length
    storeInt(header, 0, 2, length);

    // store code
    storeInt(header, 2, 1, 0xd3);
    storeInt(header, 3, 2, code);

    // store flags
    storeInt(header, 5, 1, flags);

    // store reserved header data
    header[6] = reservedHeaderData[0];
    header[7] = reservedHeaderData[1];

    // write header
    os.write(header);

    // write data (if applicable)
    if (null != data)
      os.write(data);

    // white children
    if (includeChildren && null != children)
      for (Iterator<ModcaStruct> i = children.iterator(); i.hasNext();) {
        ModcaStruct child = i.next();
        child.saveStruct(os, true);
      }
  }

  // Field attributes: id, triplet offset, repeating group offset, name offset
  private static final int FIELD_ATTRIBUTES[][] = new int[][]{ //
      {
          BDT, 10, -1, 0
      }, {
          BAG, 8, -1, 0
      }, {
          BBC, 8, -1, 0
      }, {
          BCA, 8, -1, 0
      }, {
          BDG, 8, -1, 0
      }, {
          BDI, 8, -1, 0
      }, {
          BFM, 8, -1, 0
      }, {
          BGR, 8, -1, 0
      }, {
          BIM, 8, -1, 0
      }, {
          BMM, 8, -1, 0
      }, {
          BMO, 8, -1, 0
      }, {
          BNG, 8, -1, 0
      }, {
          BOC, 8, -1, 0
      }, {
          BOG, 8, -1, 0
      }, {
          BPG, 8, -1, 0
      }, {
          BPS, 8, -1, 0
      }, {
          BPT, 8, -1, 0
      }, {
          BRG, 8, -1, 0
      }, {
          EBC, 8, -1, 0
      }, {
          ECA, 8, -1, 0
      }, {
          EDI, 8, -1, 0
      }, {
          EDT, 8, -1, 0
      }, {
          EGR, 8, -1, 0
      }, {
          EIM, 8, -1, 0
      }, {
          EMO, 8, -1, 0
      }, {
          ENG, 8, -1, 0
      }, {
          EOC, 8, -1, 0
      }, {
          EPG, 8, -1, 0
      }, {
          EPT, 8, -1, 0
      }, {
          ERG, 8, -1, 0
      }, {
          IMM, 8, -1, 0
      }, {
          CDD, 12, -1, -1
      }, {
          EAG, -1, -1, 0
      }, {
          EDG, -1, -1, 0
      }, {
          EFM, -1, -1, 0
      }, {
          EMM, -1, -1, 0
      }, {
          EOG, -1, -1, 0
      }, {
          EPS, -1, -1, 0
      }, {
          IEL, 0, -1, -1
      }, {
          IOB, 27, -1, 0
      }, {
          IPG, 17, -1, 0
      }, {
          IPO, 16, -1, 0
      }, {
          IPS, 14, -1, 0
      }, {
          LLE, 3, -1, -1
      }, {
          MBC, -1, 0, -1
      }, {
          MCA, -1, 0, -1
      }, {
          MCD, -1, 0, -1
      }, {
          MCF, -1, 0, -1
      }, {
          MDR, -1, 0, -1
      }, {
          MGO, -1, 0, -1
      }, {
          MIO, -1, 0, -1
      }, {
          MPG, -1, 0, -1
      }, {
          MPO, -1, 0, -1
      }, {
          MDD, 13, -1, -1
      }, {
          MFC, 4, -1, -1
      }, {
          PFC, 4, -1, -1
      }, {
          OBD, 0, -1, -1
      }, {
          TLE, 0, -1, -1
      }, {
          PGD, 15, -1, -1
      }, {
          PMC, 2, -1, -1
      }, {
          BFG, -1, -1, 0
      }, {
          EFG, -1, -1, 0
      }, {
          ER, -1, -1, 0
      }, {
          BII, -1, -1, 0
      }, {
          EII, -1, -1, 0
      }, {
          BFN, -1, -1, 0
      }, {
          BCP, -1, -1, 0
      }, {
          BCF, -1, -1, 0
      }, {
          BR, 10, -1, 0
      },
  };

  /**
   * Process the raw tag data into something usable
   * 
   * @throws IOException
   */
  private void processData() throws IOException {
    // int origEncoder = codePage;
    int idxTriplets = -1; // offset of triplets
    int idxRepeating = -1; // offset of repeating groups

    byteOffsets = getFieldAttributes();
    idxTriplets = byteOffsets[1];
    idxRepeating = byteOffsets[2];

    // special case handling
    switch (code){
      case MCF1 :
        processMCF1();
        break;
    }

    if (byteOffsets[3] != -1)
      try {
        int availableLength = Math.min(data.length - byteOffsets[3], 8);

        byte[] nameData = new byte[availableLength];
        System.arraycopy(data, byteOffsets[3], nameData, 0, availableLength);
        for (int i = 0; i < nameData.length; i++) {
          if (nameData[i] == 0x00)
            nameData[i] = (byte) 0x40;
        }

        name = CpConvert.getConv(codePage).toString(nameData);
        // name = CpConvert.getConv(codePage).toString(data, byteOffsets[3],
        // availableLength);
        if (availableLength < 8 && LOGGER.isWarnEnabled()) {
          LOGGER.warn("Struct may be invalid", toString(), availableLength, name);
        }
      } catch (UnsupportedEncodingException e) {
        LOGGER.warn("Unsupported encoding {}", codePage);
      }

    if (idxTriplets != -1)
      processTriplets(idxTriplets, data.length);

    if (idxRepeating != -1)
      processRepeating(idxRepeating, data.length);
  }

  private static final int NO_ATTRIBUTES[] = {
      -1, -1, -1, -1
  };

  private int[] getFieldAttributes() {
    // find the appropriate attributes
    for (int i = 0; i < FIELD_ATTRIBUTES.length; i++)
      if (FIELD_ATTRIBUTES[i][0] == code)
        return FIELD_ATTRIBUTES[i];
    return NO_ATTRIBUTES;
  }

  /**
   * Special case handling MCF1 field
   * 
   * @throws IOException
   */
  private void processMCF1() throws IOException {
    repeatingGroups = new LinkedList<List<Triplet>>();
    triplets = new LinkedList<Triplet>();

    byte[] xNN = new byte[12];

    // System.out.println( "MCF1 data: 0x"+toHex(data) );
    int rgl = 0xFF & data[0];
    int offset = 4;

    while (offset < data.length) {

      // int cfId = 0xFF&data[offset];
      xNN[0] = (byte) 0x04;
      xNN[1] = (byte) 0x24;
      xNN[2] = (byte) 0x05;
      xNN[3] = data[offset];

      Triplet tNew = new Triplet(0, xNN);

      triplets.add(tNew);

      // int sectId = 0xFF&data[offset+2];
      xNN[0] = (byte) 0x03;
      xNN[1] = (byte) 0x25;
      xNN[2] = data[offset + 2];
      tNew = new Triplet(0, xNN);

      triplets.add(tNew);

      if ((0xFF & data[offset + 12]) == 0xFF || (0xFF & data[offset + 20]) == 0xFF) {
        xNN[0] = (byte) 0x0C;
        xNN[1] = (byte) 0x02;
        xNN[2] = (byte) 0x8E;
        xNN[3] = (byte) 0x00;

        for (int i = 0; i < 8; i++) {
          xNN[4 + i] = data[offset + 4 + i];
        }

        tNew = new Triplet(0, xNN);

        triplets.add(tNew);
      } else {

        // CPName = new String( data, offset+12, 8, encoder );
        xNN[0] = (byte) 0x0C;
        xNN[1] = (byte) 0x02;
        xNN[2] = (byte) 0x85;
        xNN[3] = (byte) 0x00;

        for (int i = 0; i < 8; i++) {
          xNN[4 + i] = data[offset + 12 + i];
        }

        tNew = new Triplet(0, xNN);

        triplets.add(tNew);

        // String FCSName = new String( data, offset+20, 8, encoder );
        xNN[0] = (byte) 0x0C;
        xNN[1] = (byte) 0x02;
        xNN[2] = (byte) 0x86;
        xNN[3] = (byte) 0x00;

        for (int i = 0; i < 8; i++) {
          xNN[4 + i] = data[offset + 20 + i];
        }

        tNew = new Triplet(0, xNN);

        triplets.add(tNew);
      }

      if (rgl == 0x1E) {
        xNN[0] = (byte) 0x04;
        xNN[1] = (byte) 0x26;
        xNN[2] = data[offset + 28];
        xNN[3] = data[offset + 29];
        tNew = new Triplet(0, xNN);

        triplets.add(tNew);
      }

      repeatingGroups.add(triplets);

      // System.out.println( "MCF1 Triplets:" + triplets );
      triplets = new LinkedList<Triplet>();
      offset += rgl;
    }
  }

  /**
   * Process repeating group data
   */
  private void processRepeating(int offset, final int length) {
    int groupSize = 0;

    while (offset < length) {
      if (repeatingGroups == null) {
        repeatingGroups = new LinkedList<List<Triplet>>();
      }

      groupSize = getInt(offset, 2) - 2;
      offset += 2;

      processTriplets(offset, offset + groupSize);
      repeatingGroups.add(triplets);

      triplets = new LinkedList<Triplet>();
      offset += groupSize;
    }
  }

  /**
   * Process triplet data
   */
  private void processTriplets(int offset, final int length) {
    if (data == null || offset >= data.length || data[offset] == (byte) 0)
      return;

    int offset2 = offset;
    int length2 = length;

    try {
      while (offset <= (length - 3)) {
        if (triplets == null)
          triplets = new LinkedList<Triplet>();

        // skip zeros in triplet data. try to recover by skipping
        // zeros until we find something non-zero.
        if (data[offset] == 0) {
          offset++;
          continue;
        }

        Triplet tNew = new Triplet(offset, data);

        triplets.add(tNew);

        offset += tNew.len;
      }
    } catch (InvalidFormatException xFmt) {
      LOGGER.error("Invalid triplet", xFmt);
    } catch (Exception e) {
      // System.err.println(
      // "processTriplets: "
      // + e
      // + " -> offset = "
      // + offset
      // + " length = "
      // + length);
      // System.err.println(" - offset2 = " + offset2 + " length2 = " +
      // length2);
      // System.err.println();

      LOGGER.error("Problem processing triplet {} @{}, len={}, offset={}, len2={}", e, offset, length, offset2, length2);
    }
  }

  /**
   * Read from stream until the specified number of bytes have been read or EOF.
   */
  private static void readFully(final InputStream is, final byte[] data, final int offset, final int length)
      throws IOException {
    int dataOffset = 0;
    int dataRead;

    while (dataOffset < length) {
      dataRead = is.read(data, offset + dataOffset, (length - dataOffset));

      if (dataRead < 0) {
        throw new EOFException();
      }

      dataOffset += dataRead;
    }
  }

  /**
   * Return whether this is a B?? (begin) code.
   */
  public boolean isBeginCode() {
    return code >>> 8 == 0xA8;
  }

  /**
   * Return whether this is an E?? (end) code.
   */
  public int getCorrespondingEndCode() {
    return 0xA900 | (0xFF & code);
  }

  public List<ModcaStruct> getChildren() {
    return children != null ? children : new ArrayList<ModcaStruct>(0);
  }

  /**
   * Return a list of all children with the given ID
   * 
   * @param id
   * @return List
   */
  public List<ModcaStruct> getChildren(final int id) {
    if (children == null)
      return new ArrayList<ModcaStruct>(0);

    List<ModcaStruct> result = new ArrayList<ModcaStruct>(2); // assume small
    // number of
    // results
    for (Iterator<ModcaStruct> i = children.iterator(); i.hasNext();) {
      ModcaStruct struct = i.next();
      if (struct.getCode() == id)
        result.add(struct);
    }
    return result;
  }

  /**
   * The first child with the given ID
   * 
   * @param id
   * @return List
   */
  public ModcaStruct getFirstChild(final int id) {
    if (children == null)
      return null;

    for (Iterator<ModcaStruct> i = children.iterator(); i.hasNext();) {
      ModcaStruct struct = i.next();
      if (struct.getCode() == id)
        return struct;
    }
    return null;
  }

  public int getChildCount() {
    return children != null ? children.size() : 0;
  }

  public boolean hasChildren() {
    return children != null && children.size() > 0;
  }

  public ModcaStruct getChild(final int index) {
    return children != null ? (ModcaStruct) children.get(index) : null;
  }

  public void addChild(final ModcaStruct child) {
    child.setParent(this);
    if (null == children)
      children = new LinkedList<ModcaStruct>();
    children.add(child);
  }

  public List<Triplet> getTriplets() {
    return triplets != null ? triplets : new ArrayList<Triplet>(0);
  }

  public int getTripletCount() {
    return triplets != null ? triplets.size() : 0;
  }

  public boolean hasTriplets() {
    return null != triplets && triplets.size() > 0;
  }

  public Triplet getTriplet(final int index) {
    return triplets != null ? (Triplet) triplets.get(index) : null;
  }

  public List<List<Triplet>> getRepeatingGroups() {
    return repeatingGroups != null ? repeatingGroups : new ArrayList<List<Triplet>>(0);
  }

  public int getRepeatingGroupCount() {
    return repeatingGroups != null ? repeatingGroups.size() : 0;
  }

  public boolean hasRepeatingGroups() {
    return null != repeatingGroups && repeatingGroups.size() > 0;
  }

  public List<Triplet> getRepeatingGroup(final int index) {
    return repeatingGroups != null ? repeatingGroups.get(index) : null;
  }

  public Triplet getTripletID(final int id) {
    List<Triplet> triplets = getTriplets();

    if (triplets != null) {
      for (Iterator<Triplet> iter = triplets.iterator(); iter.hasNext();) {
        Triplet triplet = iter.next();
        if (triplet.bId == id)
          return triplet;
      }
    }

    return null;
  }

  /**
   * Documentation currently unavailable
   * 
   * @return java.lang.String
   */
  @Override
  public String toString() {
    ModcaStructInfo msi = ModcaStructInfo.getNameByID(code);
    return msi.getShortName() + ": " + msi.getDescription();
  }

  /**
   * Return a copy of this structure. Children aren't copied (yet).
   * 
   * @return ModcaStruct
   */
  public ModcaStruct copy() {
    if (null != data) {
      byte dataCopy[] = new byte[data.length];
      System.arraycopy(data, 0, dataCopy, 0, data.length);
      return new ModcaStruct(parent, code, dataCopy);
    }
    return new ModcaStruct(parent, code, null);
  }

  public int getLength() {
    // return null != data ? data.length + 8 : 8;
    int size = null != data ? data.length + 8 : 8;

    for (Iterator<ModcaStruct> i = getChildren().iterator(); i.hasNext();) {
      ModcaStruct child = i.next();
      size += child.getLength();
    }

    return size;
  }

  public void setCode(final int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public ModcaStruct getParent() {
    return parent;
  }

  public void setParent(final ModcaStruct parent) {
    this.parent = parent;
  }

  public void setCodePage(final int codePage) {
    this.codePage = codePage;
  }

  public int getCodePage() {
    return codePage;
  }

  public void setData(final byte[] data) {
    this.data = data;
  }

  /**
   * Append a chunk of data to the data array.
   * 
   * @param data
   */
  public void appendData(final byte[] data) {
    if (data != null && this.data != null) {
      byte newData[] = new byte[this.data.length + data.length];
      System.arraycopy(this.data, 0, newData, 0, this.data.length);
      System.arraycopy(data, 0, newData, this.data.length, data.length);
      this.data = newData;
    } else
      setData(data);
  }

  public byte[] getData() {
    return data;
  }

  public void setAFP(final boolean isAFP) {
    this.isAFP = isAFP;
  }

  public boolean isAFP() {
    return isAFP;
  }

  public void setName(final String newName) {
    int fieldAttributes[] = getFieldAttributes();
    if (null == fieldAttributes || fieldAttributes[3] < 0)
      throw new IllegalArgumentException("Field " + toString() + " does not carry a name.");

    try {
      byte bName[] = CpConvert.getConv(codePage).encode(newName);

      if (data == null)
        data = new byte[8];

      System.arraycopy(bName, 0, data, fieldAttributes[3], Math.min(8, bName.length));

      name = CpConvert.getConv(codePage).toString(data, fieldAttributes[3], 8);
    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("Unsupported encoding {}", codePage);
    }
  }

  public String getName() {
    for (Iterator<Triplet> iter = getTriplets().iterator(); iter.hasNext();) {
      Triplet t = iter.next();
      if (t.bId == 0x02 && t.getInt(0, 1) == 0x01) {
        char[] refName = null;

        if ((0x0F00 & t.getEncoding()) >> 16 == 1) {
          refName = new char[t.data.length - 2];
          for (int i = 0; i < refName.length; i++) {
            refName[i] = (char) t.data[2 + i];
          }
        } else {
          refName = new char[(t.data.length - 2) / 2];
          for (int i = 0; i < refName.length; i++) {
            refName[i] = (char) t.getInt(2 + (i * 2), 2);
          }
          // refName = ModcaEncoding.decodeWithCpSupport(codePage, refName);
          refName = decodeWithCpSupport(codePage, refName);
        }
        return new String(refName);
      }
    }

    return name;
  }

  private static char[] decodeWithCpSupport(final int codepage, final char[] codepoints) {
    byte[] data = new byte[codepoints.length];

    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (0xFF & codepoints[i]);
    }

    String text = null;

    try {
      text = CpConvert.getConv(codepage).toString(data);
    } catch (UnsupportedEncodingException e) {
      // direct encoding
      LOGGER.warn("Unsupported encoding {}", codepage);
      return codepoints;
    } catch (NullPointerException e) {
      // direct encoding
      LOGGER.warn("Unsupported encoding {}", codepage);
      return codepoints;
    }

    return text.toCharArray();
  }

  byte[] getHeader() {
    return header;
  }
}