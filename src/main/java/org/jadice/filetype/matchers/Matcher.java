package org.jadice.filetype.matchers;

import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;

import org.jadice.filetype.Context;

/**
 * Matchers are responsible for detecting whether the input data exhibits certain properties.
 * Matchers may perform very simple steps like detecting whether the stream contains certain data
 * values, but may also perform complex analysis like un-compressing a OpenDocument stream etc.
 * 
 */
@XmlTransient
public abstract class Matcher {
  public enum Comparison {
    EQUALS("=") {
      public boolean matches(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
      }
    },
    NOT_EQUALS("!") {
      public boolean matches(Object o1, Object o2) {
        return o1 != o2 && !(o1 != null && o1.equals(o2));
      }
    },
    GREATER(">") {
      @SuppressWarnings("unchecked")
      public boolean matches(Object o1, Object o2) {
        return o1 != null && o2 != null && o1 instanceof Comparable && ((Comparable) o1).compareTo(o2) > 0;
      }
    },
    GREATER_OR_EQUAL(">=") {
      @SuppressWarnings("unchecked")
      public boolean matches(Object o1, Object o2) {
        return o1 != null && o2 != null && o1 instanceof Comparable && ((Comparable) o1).compareTo(o2) >= 0;
      }
    },
    LESS("<") {
      @SuppressWarnings("unchecked")
      public boolean matches(Object o1, Object o2) {
        return o1 != null && o2 != null && o1 instanceof Comparable && ((Comparable) o1).compareTo(o2) < 0;
      }
    },
    LESS_OR_EQUAL("<=") {
      @SuppressWarnings("unchecked")
      public boolean matches(Object o1, Object o2) {
        return o1 != null && o2 != null && o1 instanceof Comparable && ((Comparable) o1).compareTo(o2) <= 0;
      }
    };

    private final String shortcut;

    Comparison(String shortcut) {
      this.shortcut = shortcut;
    }

    public abstract boolean matches(Object o1, Object o2);

    public static Comparison get(String s) {
      for (Comparison c : values()) {
        if (s.equals(c.shortcut)) {
          return c;
        }
      }
      return valueOf(s);
    }
  }

  public abstract boolean matches(Context context) throws IOException;
}
