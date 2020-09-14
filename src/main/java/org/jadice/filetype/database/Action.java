package org.jadice.filetype.database;

import javax.xml.bind.annotation.XmlTransient;

import org.jadice.filetype.Context;

/**
 * Abstract base class for actions to be performed when a type detects a match.
 * 
 */
@XmlTransient
public abstract class Action {
  public abstract void perform(Context ctx);
}
