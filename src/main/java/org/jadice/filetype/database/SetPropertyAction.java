package org.jadice.filetype.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;

/**
 * {@link Action} to set an arbitrary named property of the result.
 * 
 */
@XmlRootElement(name = "property")
public class SetPropertyAction extends Action {
  @XmlAttribute
  private String name;

  @XmlValue
  private String value;

  @Override
  public void perform(Context ctx) {
    ctx.setProperty(name, value);
  }
}
