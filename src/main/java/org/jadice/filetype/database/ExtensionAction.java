package org.jadice.filetype.database;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;

/**
 * An {@link Action} to set a stream's default file extension.
 * 
 */
@XmlRootElement(name = "extension")
public class ExtensionAction extends Action {
  public static final String KEY = "extension";

  @XmlValue
  private String type;

  @Override
  public void perform(Context ctx) {
    ctx.setProperty(KEY, type);
  }
}
