package org.jadice.filetype;

import java.util.Locale;
import java.util.Map;

import org.jadice.filetype.io.SeekableInputStream;

/**
 * A context used to hold data relevant to a single analysis run.
 *
 */
public class Context {
  /**
   * Source data.
   */
  private final SeekableInputStream sis;

  /**
   * Results.
   */
  private final Map<String, Object> result;

  private final AnalysisListener listener;

  private final Locale locale;

  private final String statedExtension;

  public Context(final SeekableInputStream sis, final Map<String, Object> result, final AnalysisListener listener, final Locale locale, final String statedExtension) {
    this.sis = sis;
    this.result = result;
    this.listener = listener;
    this.locale = locale;
    this.statedExtension = statedExtension;
  }

  /**
   * Set a result property.
   *
   * @param name
   * @param value
   */
  public void setProperty(final String name, final Object value) {
    result.put(name, value);
  }

  /**
   * Get the source data.
   *
   * @return the source stream
   */
  public SeekableInputStream getStream() {
    return sis;
  }

  /**
   * Get a result property.
   *
   * @param key
   * @return the property value
   */
  public Object getProperty(final String key) {
    return result.get(key);
  }

  /**
   * Get the statedExtension.
   *
   * @return the extension String
   */
  public String getStatedExtension() {
    return statedExtension;
  }

  public void error(final Object src, final String message, final Throwable cause) {
    if (null != listener) {
      listener.error(src, message, cause);
    }
  }

  public void info(final Object src, final String message) {
    if (null != listener) {
      listener.info(src, message);
    }
  }

  public void warning(final Object src, final String message) {
    if (null != listener) {
      listener.warning(src, message);
    }
  }

  public Locale getLocale() {
    return locale;
  }
}
