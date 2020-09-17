package org.jadice.filetype.database;

import java.io.IOException;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * Abstract base class for classes representing a location within the source data.
 * 
 */
public abstract class Location {
  /**
   * Return the source data stream, positioned to whatever location where the next match should
   * occur.
   * 
   * @param context
   * @return a SeekableInputStream positioned where the next match should occur
   * @throws IOException
   */
  public abstract SeekableInputStream getPositionedStream(Context context) throws IOException;
}
