package org.jadice.filetype.database;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;

/**
 * An {@link Action} to set the stream's MIME type.
 * 
 */
@XmlRootElement(name = "mime-type")
public class MimeTypeAction extends Action {
  public static final String KEY = "mimeType";

  @XmlValue
  private String type;

  @Override
  public void perform(Context ctx) {
    ctx.setProperty(KEY, type);
  }
}
