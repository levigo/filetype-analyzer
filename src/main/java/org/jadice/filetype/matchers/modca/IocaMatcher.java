package org.jadice.filetype.matchers.modca;

import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.matchers.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matcher class to identify IOCA image data.
 */
public class IocaMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(IocaMatcher.class);

  private static final String MIME_TYPE = "image/x-vnd.ibm.ioca";

  private static final String EXTENSION = "ioc";

  private static final String DESCRIPTION_EN = "IOCA image document";

  private static final String DESCRIPTION_DE = "IOCA Bilddokument";

  @Override
  public boolean matches(final Context context) {
    try {
      return analyzeStream(context);
    } catch (final EOFException e) {
      LOGGER.debug("Attempt to seek behind EOF", e);
      return false;
    } catch (final IOException e) {
      context.error(this, "Invalid IOCA image data", e);
      return false;
    }
  }

  private String getDescription(final Locale locale) {
    try {
      final boolean german = "DE".equalsIgnoreCase(locale.getLanguage());
      return german ? DESCRIPTION_DE : DESCRIPTION_EN;
    } catch (final Exception e) {
      LOGGER.error("Error when resolving localized description", e);
      return DESCRIPTION_EN;
    }
  }

  private boolean analyzeStream(final Context context) throws IOException {
    final SeekableInputStream sis = context.getStream();
    final long fp = sis.getStreamPosition();
    try {
      sis.seek(0);

      final boolean isIoca = isIoca(sis);

      if (isIoca) {
        context.setProperty(MimeTypeAction.KEY, MIME_TYPE);
        context.setProperty(DescriptionAction.KEY, getDescription(context.getLocale()));
        context.setProperty(ExtensionAction.KEY, EXTENSION);
      }

      return isIoca;
    } finally {
      sis.seek(fp);
    }
  }
  
  private boolean isIoca(final SeekableInputStream stream) throws IOException {
    if (stream == null)
      return false;

    final byte[] sip = new byte[2];
    stream.seek(0);
    stream.readFully(sip);
    stream.seek(0);
    return 0x70 == (0xFF & sip[0]) && (0x00 == (0xFF & sip[1]) || 0x04 == (0xFF & sip[1]));
  }
}
