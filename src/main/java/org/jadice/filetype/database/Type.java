package org.jadice.filetype.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jadice.filetype.Context;

/**
 * A {@link Type} represents a stream type consisting of a set of matchers, actions to perform when
 * the type is matched and sub-types to refine the match.
 * 
 */
@XmlRootElement(name = "type")
public class Type {
  @XmlAnyElement(lax = true)
  private List<Object> any = new ArrayList<Object>();

  @XmlTransient
  private final List<Matcher> matchers = new ArrayList<Matcher>();

  @XmlTransient
  private final List<Action> actions = new ArrayList<Action>();

  @XmlTransient
  private final List<Type> children = new ArrayList<Type>();

  public boolean analyze(Context ctx) throws IOException {
    initialize();

    // try matchers, return if one of them fails
    for (Matcher matcher : matchers) {
      if (!matcher.matches(ctx)) {
        return false;
      }
    }

    // apply actions
    for (Action action : actions) {
      action.perform(ctx);
    }

    // try child types until one of them matches
    for (Type child : children) {
      if (child.analyze(ctx)) {
        break;
      }
    }

    return true;
  }

  private void initialize() {
    // sort objects by type - needed due to JAXB constraint.
    if (null != any) {
      for (Object o : any) {
        if (o instanceof Matcher) {
          matchers.add((Matcher) o);
        } else if (o instanceof Action) {
          actions.add((Action) o);
        } else if (o instanceof Type) {
          children.add((Type) o);
        } else {
          throw new IllegalArgumentException("Don't know how to deal with a " + o.getClass() + ": " + o);
        }
      }

      any = null;
    }
  }
}
