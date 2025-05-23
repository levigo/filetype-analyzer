package org.jadice.filetype.matchers;

import java.io.IOException;
import java.nio.ByteOrder;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.AbsoluteLocation;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * Match a long (four-byte!) value at a given position.
 * 
 */
@XmlRootElement(name = "match-long")
public class LongMatcher extends NumericMatcher {
  private long value;

  @Override
  protected boolean matches(Context context, SeekableInputStream positionedStream) throws IOException {
    positionedStream.setByteOrder(order);
    int s = positionedStream.readShort();
    s &= mask;

    return unsigned ? comparison.matches(value & 0xffffffff, s & 0xffffffff) : comparison.matches(value, s);
  }

  @XmlValue
  protected void setReference(String s) {
    if (s.isEmpty()) {
      // special case: don't match, just extract
      // ignored for now.
      this.value = 0;
    } else if (s.startsWith("0x")) {
      this.value = Long.parseLong(s.substring(2), 16);
    } else {
      this.value = Long.parseLong(s);
    }
  }

  @XmlAttribute
  protected void setOrder(String order) {
    order = order.toLowerCase();

    if ("be".equals(order) || order.startsWith("big")) {
      this.order = ByteOrder.BIG_ENDIAN;
    } else if ("le".equals(order) || order.startsWith("litt")) {
      this.order = ByteOrder.LITTLE_ENDIAN;
    } else if (order.startsWith("native")) {
      this.order = ByteOrder.BIG_ENDIAN;
    } else {
      throw new IllegalArgumentException("Unsupported byte order " + order);
    }
  }

  @XmlAttribute
  protected void setUnsigned(boolean unsigned) {
    this.unsigned = unsigned;
  }

  @Override
  @XmlAttribute
  protected void setMask(String s) {
    super.setMask(s);
  }

  @Override
  @XmlAttribute
  protected void setComparison(String comparison) {
    super.setComparison(comparison);
  }

  @XmlAttribute(name = "offset")
  protected void setOffset(int offset) {
    this.location = new AbsoluteLocation(offset);
  }
}
