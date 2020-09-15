package org.jadice.filetype.matchers;

import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.Location;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for matchers acting on a certain location in the stream
 * 
 */
@XmlTransient
public abstract class StreamMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamMatcher.class);

  protected Location location;

  protected Comparison comparison = Comparison.EQUALS;

  @Override
  public boolean matches(final Context context) {
    try {
      return matches(context, location.getPositionedStream(context));
    } catch (IOException e) {
      LOGGER.debug("Mismatch due to exception", e);
      return false;
    }
  }

  protected abstract boolean matches(Context context, SeekableInputStream positionedStream) throws IOException;

  protected void setComparison(final String comparison) {
    this.comparison = Comparison.get(comparison);
  }
}
