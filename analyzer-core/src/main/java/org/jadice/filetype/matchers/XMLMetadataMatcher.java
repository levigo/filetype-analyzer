package org.jadice.filetype.matchers;

import java.io.IOException;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.MimeTypeAction;

/**
 * Match XML root-element and namespace pairs.
 */
@XmlRootElement(name = "match-xml-metadata")
public class XMLMetadataMatcher extends Matcher {

  public static final String X_RECHNUNG_KEY = "x_rechnung";

  private String namespaceUri;

  private String rootElementName;

  private String value;

  @Override
  public boolean matches(Context context) throws IOException {
    if (this.namespaceUri != null && this.rootElementName != null
        && context.getProperty(XMLMatcher.DETAILS_KEY) instanceof Map<?, ?> xmlDetailsMap) {
      final Map<String, Object> xmlDetails = (Map<String, Object>) xmlDetailsMap;
      final String xmlNamespaceUri = (String) xmlDetails.get(XMLMatcher.NAMESPACE_URI_KEY);
      boolean namespaceUriMatches = xmlNamespaceUri != null && xmlNamespaceUri.startsWith(this.namespaceUri);
      boolean rootElementMatches = this.rootElementName.equalsIgnoreCase((String) xmlDetails.get(XMLMatcher.ROOT_ELEMENT_NAME_KEY));
      if (namespaceUriMatches && rootElementMatches) {
        if (value != null && value.equalsIgnoreCase(X_RECHNUNG_KEY)) {
          xmlDetails.put(X_RECHNUNG_KEY, true);
          context.setProperty(MimeTypeAction.KEY, context.getProperty(MimeTypeAction.KEY) + ";x-rechnung=true");
        }
        return true;
      }
    }
    return false;
  }

  @XmlAttribute
  public void setNamespaceUri(String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  @XmlAttribute
  public void setRootElementName(String rootElementName) {
    this.rootElementName = rootElementName;
  }

  @XmlValue
  public void setValue(String value) {
    this.value = value;
  }
}
