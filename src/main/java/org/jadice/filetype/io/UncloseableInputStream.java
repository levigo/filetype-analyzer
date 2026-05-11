package org.jadice.filetype.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link InputStream} which cannot be closed.
 */
public class UncloseableInputStream extends FilterInputStream {

  private static final Logger logger = LoggerFactory.getLogger(UncloseableInputStream.class);

  public UncloseableInputStream(final InputStream is) throws IOException {
    super(is);
  }

  /**
   * This method does not have any effect, as the {@link InputStream} cannot be closed.
   */
  @Override
  public void close() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Attempt to close instance of {}. Ignoring close().", getClass().getSimpleName());
    }
  }
}
