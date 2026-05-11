package org.jadice.filetype.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * An {@link MemoryInputStream} which cannot be closed.
 */
public class UncloseableMemoryInputStream extends MemoryInputStream {
  public UncloseableMemoryInputStream(final InputStream is) throws IOException {
    super(is);
  }

  /**
   * This method does not have any effect, as the {@link MemoryInputStream} cannot be closed.
   */
  @Override
  public void close() throws IOException {
    // ignore
  }
}
