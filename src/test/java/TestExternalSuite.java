import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestExternalSuite extends AbstractMassTester {
  
  @Parameters (name = "{0}")
  public static Collection<Object[]> getTestCases() {
    final String testSuiteLocation = System.getProperty("com.levigo.analyzer.testsuite");
    if (testSuiteLocation == null) {
      return Collections.emptySet();
    }

    File configFile = new File(testSuiteLocation);
    if (!configFile.isFile() || !configFile.canRead()) {
      return Collections.emptySet();
    }

    try {
      final Collection<Object[]> result = new LinkedList<Object[]>();
      final Document config = parserXML(configFile);
      final Node root = config.getDocumentElement();
      final NodeList childNodes = root.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        final Node node = childNodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE || !"folder".equals(node.getNodeName())) {
          continue;
        }
        result.addAll(parseFolderNode(node, configFile));
      }
      return result;
    } catch (Exception e) {
      throw new AssertionError("Could not parse test suite", e);
    }
  }

  public static Collection<Object[]> parseFolderNode(Node node, File configFile) throws Exception {
    final Node locNode = node.getAttributes().getNamedItem("location");
    if (locNode == null) {
      return Collections.emptySet();
    }

    final Node minVersionNode = node.getAttributes().getNamedItem("min-version");
    final String minVersion = minVersionNode == null ? null : minVersionNode.getNodeValue();

    final String locValue = locNode.getNodeValue();
    File location = new File(locValue);
    if (!location.isAbsolute()) {
      location = new File(configFile.getParentFile(), locValue);
    }
    return buildTestParam(location, findExpectations(node), minVersion);

  }

  private static Map<String, Object> findExpectations(Node node) {
    final Map<String, Object> result = new HashMap<String, Object>();
    final NodeList children = node.getChildNodes();
    for (int i = 0 ; i < children.getLength(); i++) {
      final Node expectations = children.item(i);
      if (expectations.getNodeType() != Node.ELEMENT_NODE || !"expectations".equals(expectations.getNodeName())) {
        continue;
      }
      final NodeList children2 = expectations.getChildNodes();
      for (int j = 0 ; j < children2.getLength(); j++) {
        Node expectation = children2.item(j);
        if (expectation.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        result.put(expectation.getNodeName(), expectation.getTextContent());
      }
        
    }
    return result;
  }


  public static Document parserXML(File file) throws SAXException, IOException, ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
  }
}
