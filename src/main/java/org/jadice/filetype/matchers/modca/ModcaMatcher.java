package org.jadice.filetype.matchers.modca;

import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.matchers.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matcher class to identify AFP/MODCA data.
 */
public class ModcaMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModcaMatcher.class);

  private static final String MIME_TYPE_AFP = "application/vnd.ibm.afplinedata";

  private static final String MIME_TYPE_MODCA = "application/vnd.ibm.modcap";

  private static final String EXTENSION_AFP = "afp";

  private static final String EXTENSION_MODCA = "mod";

  private static final String MIME_PARAMETER_TYPE = "type";

  private static final String[] DESCRIPTION_EN = new String[]{
      // application/vnd.ibm.afplinedata
      "AFP document",
      // application/vnd.ibm.modcap
      "MODCA document",
      // <mimetype>; type=resource
      " resource",
      // <mimetype>; type=formmap
      " form map definitions",
      // <mimetype>; type=overlay
      " overlay",
      // <mimetype>; type=pagesegment
      " page segment",
      // <mimetype>; type=charset
      " FOCA character set",
      // <mimetype>; type=codepage
      " FOCA code page",
      // <mimetype>; type=codedfont
      " FOCA coded font",
      // <mimetype>; type=objectcontainer
      " object container"
  };

  private static final String[] DESCRIPTION_DE = new String[]{
      // application/vnd.ibm.afplinedata
      "AFP Dokument",
      // application/vnd.ibm.modcap
      "MODCA Dokument",
      // <mimetype>; type=resource
      " Ressource",
      // <mimetype>; type=formmap
      " Form Map Definitionen",
      // <mimetype>; type=overlay
      " Overlay",
      // <mimetype>; type=pagesegment
      " Seitensegment",
      // <mimetype>; type=charset
      " FOCA Schriftart",
      // <mimetype>; type=codepage
      " FOCA Codepage",
      // <mimetype>; type=codedfont
      " FOCA kodierte Schrift",
      // <mimetype>; type=objectcontainer
      " Objekt Container"
  };

  private static class Info {
    private static final String AFP = "AFP";

    private static final String MODCA = "MODCA";

    private String format = null;

    // --> document related

    // begin document
    // Mimetype:
    // - application/vnd.ibm.afplinedata
    // - application/vnd.ibm.modcap
    int countBDT = 0;
    // alternativ: falls kein BDT definiert ist
    int countBPG = 0;

    // begin resource group
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=resource
    // - application/vnd.ibm.modcap; type=resource
    int countBRG = 0;

    // formmap
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=formmap
    // - application/vnd.ibm.modcap; type=formmap
    int countBFM = 0;

    // overlay
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=overlay
    // - application/vnd.ibm.modcap; type=overlay
    int countBMO = 0;

    // pagesegment
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=pagesegment
    // - application/vnd.ibm.modcap; type=pagesegment
    int countBPS = 0;

    // --> font related

    // charset
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=charset
    // - application/vnd.ibm.modcap; type=charset
    int countBFN = 0;

    // codepage
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=codepage
    // - application/vnd.ibm.modcap; type=codepage
    int countBCP = 0;

    // coded font
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=codedfont
    // - application/vnd.ibm.modcap; type=codedfont
    int countBCF = 0;

    // --> other content

    // begin object container
    // Mimetype:
    // - application/vnd.ibm.afplinedata; type=objectcontainer
    // - application/vnd.ibm.modcap; type=objectcontainer
    int countBOC = 0;

    String getMimeType() {
      if (format == null) {
        throw new IllegalArgumentException("Format == null");
      } else if (isAFP()) {
        return MIME_TYPE_AFP;
      } else if (isMODCA()) {
        return MIME_TYPE_MODCA;
      } else {
        throw new IllegalArgumentException("Unknown format: " + format);
      }
    }

    void setAFP() {
      format = AFP;
    }

    boolean isAFP() {
      return format != null && format.equals(AFP);
    }

    void setMODCA() {
      format = MODCA;
    }

    boolean isMODCA() {
      return format != null && format.equals(MODCA);
    }
  }

  @Override
  public boolean matches(final Context context) {
    try {
      Info info = analyzeStream(context);
      if (info == null) {
        return false;
      }
      analyzeInfo(context, info);
      return (info.isAFP() || info.isMODCA()) && context.getProperty(MimeTypeAction.KEY) != null;
    } catch (InvalidFormatException e) {
      LOGGER.debug("Handling stream which is no (valid) MO:DCA", e);
      return false;
    } catch (Exception e) {
      context.error(this, "Invalid AFP/MODCA format", e);
    }

    return false;
  }

  private String[] getDescription(final Locale locale) {
    try {
      final boolean german = "DE".equalsIgnoreCase(locale.getLanguage());
      return german ? DESCRIPTION_DE : DESCRIPTION_EN;
    } catch (Exception e) {
      LOGGER.error("Error when resolving localized description", e);
      return DESCRIPTION_EN;
    }
  }

  private void analyzeInfo(final Context context, final Info info) {
    if (info.format == null) {
      return;
    }

    String[] description = getDescription(context.getLocale());

    String mimeType = info.getMimeType();

    if ((info.countBDT > 0 || info.countBPG > 0) && info.isAFP()) {
      context.setProperty(MimeTypeAction.KEY, mimeType);
      context.setProperty(DescriptionAction.KEY, description[0]);
      context.setProperty(ExtensionAction.KEY, EXTENSION_AFP);
    } else if ((info.countBDT > 0 || info.countBPG > 0) && info.isMODCA()) {
      context.setProperty(MimeTypeAction.KEY, mimeType);
      context.setProperty(DescriptionAction.KEY, description[1]);
      context.setProperty(ExtensionAction.KEY, EXTENSION_MODCA);
    } else {
      final String mimePart = info.format;

      if (info.countBRG == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "resource"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[2]);
        context.setProperty(ExtensionAction.KEY, "res");
      } else if (info.countBFM == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "formmap"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[3]);
        context.setProperty(ExtensionAction.KEY, "fde");
      } else if (info.countBMO == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "overlay"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[4]);
        context.setProperty(ExtensionAction.KEY, "");
      } else if (info.countBPS == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "pagesegment"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[5]);
        context.setProperty(ExtensionAction.KEY, "");
      } else if (info.countBFN == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "charset"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[6]);
        context.setProperty(ExtensionAction.KEY, "");
      } else if (info.countBCP == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "codepage"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[7]);
        context.setProperty(ExtensionAction.KEY, "");
      } else if (info.countBCF == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "codedfont"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[8]);
        context.setProperty(ExtensionAction.KEY, "");
      } else if (info.countBOC == 1) {
        context.setProperty(MimeTypeAction.KEY, addParameter(mimeType, "objectcontainer"));
        context.setProperty(DescriptionAction.KEY, mimePart + description[9]);
        context.setProperty(ExtensionAction.KEY, "");
      }
    }
  }

  private static String addParameter(final String mimeType, final String parameterValue) {
    return mimeType + "; " + MIME_PARAMETER_TYPE + "=" + parameterValue;
  }

  private Info analyzeStream(final Context context) throws IOException {
    final SeekableInputStream sis = context.getStream();
    final long fp = sis.getStreamPosition();
    final Info info = new Info();

    try {
      if (!isModca(sis) && !isAfp(sis)) {
        return null;
      }

      sis.seek(0);
      while (true) {
        ModcaStruct struct = ModcaStruct.loadStruct(null, sis);

        if (info.format == null) {
          if (struct.isAFP()) {
            info.setAFP();
          } else {
            info.setMODCA();
          }
        }

        // document
        if (struct.getCode() == MODCAConstants.BDT) {
          info.countBDT++;
          // bei BDT Ladevorgang abbrechen
          break;
        } else if (struct.getCode() == MODCAConstants.BPG) {
          info.countBPG++;
          // bei BPG Ladevorgang abbrechen
          break;
        } else if (struct.getCode() == MODCAConstants.BRG) {
          info.countBRG++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BFM) {
          info.countBFM++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BMO) {
          info.countBMO++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BPS) {
          info.countBPS++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BFN) { // font
          info.countBFN++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BCP) {
          info.countBCP++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BCF) {
          info.countBCF++;
          struct.loadChildren(sis);
        } else if (struct.getCode() == MODCAConstants.BOC) { // other content
          info.countBOC++;
          struct.loadChildren(sis);
        }
      }
    } catch (EOFException e) {
      LOGGER.debug("Attempt to seek behind EOF", e);
      final long readBytes = sis.getStreamPosition();
      if (readBytes < 8) {
        throw new IOException("Invalid format, data length = " + readBytes + " byte(s), 8 bytes or more expected.");
      }
    } finally {
      sis.seek(fp);
    }

    return info;
  }

  private boolean isModca(final SeekableInputStream stream) throws IOException {
    if (stream == null)
      return false;

    stream.seek(2);
    final int sign = stream.read();
    stream.seek(0);
    return sign > -1 && 0xD3 == (0xFF & sign);
  }

  private boolean isAfp(final SeekableInputStream stream) throws IOException {
    if (stream == null)
      return false;

    final byte[] sip = new byte[4];
    stream.seek(0);
    stream.readFully(sip);
    stream.seek(0);
    return 0x5A == (0xFF & sip[0]) && 0xD3 == (0xFF & sip[3]);
  }
}
