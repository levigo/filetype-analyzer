package org.jadice.filetype;

public class AnalyzerException extends Exception {
  private static final long serialVersionUID = 1L;

  public AnalyzerException(String s) {
    super(s);
  }

  public AnalyzerException(String s, Throwable cause) {
    super(s, cause);
  }
}
