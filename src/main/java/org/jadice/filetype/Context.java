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

  public Context(SeekableInputStream sis, Map<String, Object> result, AnalysisListener listener, Locale locale) {
    this.sis = sis;
    this.result = result;
    this.listener = listener;
    this.locale = locale;
  }

  /**
   * Set a result property.
   *
   * @param name
   * @param value
   */
  public void setProperty(String name, Object value) {
    result.put(name, value);
  }

  /**
   * Get the source data.
   *
   * @return
   */
  public SeekableInputStream getStream() {
    return sis;
  }

  /**
   * Get a result property.
   *
   * @param key
   * @return
   */
  public Object getProperty(String key) {
    return result.get(key);
  }

  public void error(Object src, String message, Throwable cause) {
    if (null != listener) {
      listener.error(src, message, cause);
    }
  }

  public void info(Object src, String message) {
    if (null != listener) {
      listener.info(src, message);
    }
  }

  public void warning(Object src, String message) {
    if (null != listener) {
      listener.warning(src, message);
    }
  }

  public Locale getLocale() {
    return locale;
  }
}
