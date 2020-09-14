package org.jadice.filetype.domutil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMUtil {

  static final class NullResolver extends InputStream implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      return new InputSource(this);
    }

    @Override
    public int read() throws IOException {
      return -1;
    }
  }

  private static SoftReference<DocumentBuilderFactory> dbfHolder;

  private DOMUtil() {
    // hidden constr.
  }

  public static Node findChildByName(Node parent, String name) {
    Node n = parent.getFirstChild();
    while (n != null) {
      if (n instanceof Element && n.getNodeName().equals(name)) {
        return n;
      }
      n = n.getNextSibling();
    }

    return null;
  }

  public static String getNodeText(Node n) {
    if (n.getFirstChild() != null) {
      StringBuilder sb = new StringBuilder();
      String v = n.getNodeValue();
      if (null != v &&
      // See #JS-157
          (n.getNodeType() == Node.TEXT_NODE || n.getChildNodes().getLength() == 0)) {
        sb.append(v);
      }

      n = n.getFirstChild();
      do {
        v = n.getNodeValue();
        if (null != v) {
          sb.append(v);
        }
        n = n.getNextSibling();
      } while (null != n);
      return sb.toString();
    } else {
      return n.getNodeValue();
    }
  }

  public static DocumentBuilder createSimpleDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = getDBF();
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(new NullResolver());
    return builder;
  }

  static DocumentBuilderFactory getDBF() {
    DocumentBuilderFactory dbf = (dbfHolder == null) ? null : dbfHolder.get();
    if (dbf == null) {
      dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setExpandEntityReferences(false);
      dbf.setCoalescing(true);
      dbf.setIgnoringComments(true);
      dbfHolder = new SoftReference<DocumentBuilderFactory>(dbf);
    }
    return dbf;
  }


}
