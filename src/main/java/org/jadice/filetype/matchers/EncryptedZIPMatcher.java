package org.jadice.filetype.matchers;

import java.io.IOException;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;

public class EncryptedZIPMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedZIPMatcher.class);

  private static final String NEEDLE = "Wrong password".toLowerCase();

  @Override
  public boolean matches(final Context context) throws IOException {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);
      try (ZipInputStream zis = new ZipInputStream(sis)) {
        return detect(zis);
      }
    } catch (Exception e) {
      context.error(this, "Exception analyzing ZIP Container", e);
    }
    return false;
  }

  private boolean detect(final ZipInputStream zis) throws IOException {
    try {
      while (zis.getNextEntry() != null) {
        // No exception thrown -> just continue with the next entries
      }
      LOGGER.debug("Went through all ZIP entries without an exception. So it's not encrypted");
      return false;
    } catch (ZipException e) {
      LOGGER.debug("ZIP analysis hit an exception", e);
      if (isEncryptedException(e)) {
        return true;
      } else {
        throw e;
      }
    }
  }

  private boolean isEncryptedException(final ZipException ze) {
    String[] haystack = {
        ze.getMessage(), ze.getLocalizedMessage(), ze.toString()
    };
    for (String s : haystack) {
      if (s.toLowerCase().contains(NEEDLE)) {
        return true;
      }
    }
    return false;
  }
}
