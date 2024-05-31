package org.jadice.filetype.database;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

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
