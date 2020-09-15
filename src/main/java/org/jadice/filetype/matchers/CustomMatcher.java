package org.jadice.filetype.matchers;

import java.io.EOFException;
import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Matcher} using a custom class (specified by its class name) as a delegate to do the
 * actual matching.
 * 
 */
@XmlRootElement(name = "match-custom")
public class CustomMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomMatcher.class);

  private Matcher delegate;

  @Override
  public boolean matches(final Context context) throws IOException {
    if (null != delegate) {
      try {
        return delegate.matches(context);
      } catch (final EOFException e) {
        // Some matchers throw EOFExceptions during attempts to read the header
        // when there is too little data. Always treat that as no match.
        LOGGER.debug("Attempt to seek behind EOF", e);
        return false;
      }
    }
    return false;
  }

  @XmlValue
  protected void setClassName(final String name) throws ReflectiveOperationException {
    final Class<?> c = Class.forName(name);
    delegate = (Matcher) c.newInstance();
  }
}
