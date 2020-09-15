package org.jadice.filetype.matchers;

import java.nio.ByteOrder;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Abstract base class for matchers based on simple numeric data matching.
 * 
 */
@XmlTransient
public abstract class NumericMatcher extends StreamMatcher {

  protected boolean unsigned;

  protected ByteOrder order = ByteOrder.nativeOrder();

  protected long mask = 0xffffffffffffffffL;

  public NumericMatcher() {
    super();
  }

  protected void setMask(String s) {
    if (s.startsWith("0x")) {
      this.mask = Long.parseLong(s.substring(2), 16);
    } else {
      this.mask = Long.parseLong(s);
    }
  }
}
