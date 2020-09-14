package org.jadice.filetype;

/**
 * An {@link AnalysisListener} may be implemented by clients, in order to receive notifications
 * about the analysis process.
 */
public interface AnalysisListener {

  /**
   * Receive informational notification. Used for debugging/tracing.
   * 
   * @param src
   * @param message
   */
  public void info(Object src, String message);

  /**
   * Receive warning message. Warnings point to potential problems. However, they may also be caused
   * by garbled or mis-interpreted input data.
   * 
   * @param src
   * @param message
   */
  public void warning(Object src, String message);

  /**
   * Receive error message. Errors point to potential problems. However, they may also be caused by
   * garbled or mis-interpreted input data.
   * 
   * @param src
   * @param message
   * @param cause
   */
  public void error(Object src, String message, Throwable cause);
}
