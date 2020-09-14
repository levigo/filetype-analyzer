package org.jadice.filetype.database;

import java.io.IOException;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * A {@link Location} denoting an absolute position in the stream (i.e. relative to the start of the
 * stream).
 */
public class AbsoluteLocation extends Location {
  private final int offset;

  public AbsoluteLocation(int offset) {
    this.offset = offset;
  }

  @Override
  public SeekableInputStream getPositionedStream(Context context) throws IOException {
    SeekableInputStream sis = context.getStream();
    sis.seek(offset);
    return sis;
  }
}
