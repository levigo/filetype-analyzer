package org.jadice.filetype.database;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class doesn't add anything to {@link Type}, except for the annotation for the XML
 * unmarshalling.
 * 
 */
@XmlRootElement(name = "magic")
public class Database extends Type {
}
