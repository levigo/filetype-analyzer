package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A matcher that matches well-formated XML documents. It also detects some content information of
 * the document. These are
 * <ul>
 * <li>the XML version of the document</li>
 * <li>the encoding of the document</li>
 * <li>the name of the root element</li>
 * <li>the namespace of the root element, if applicable</li>
 * </ul>
 * This information will be available in the {@link Context#getProperty(String) context properties}
 * under the key {@value #DETAILS_KEY}. Please note that not all information may be available
 * depending on the SAX implementation of the JVM.
 *
 *
 */
public class XMLMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(XMLMatcher.class);

  private static final Pattern PROLOG_PATTERN = Pattern.compile("<\\?(\\s*)xml");

  public static final String MIME_TYPE_XML = "application/xml";

  public static final String DETAILS_KEY = "XML_DETAILS";

  public static final String NAMESPACE_URI_KEY = "namespace_uri";

  public static final String ROOT_ELEMENT_NAME_KEY = "root_element_name";

  public static final String DOCUMENT_XML_VERSION_KEY = "document_xml_version";

  /**
   * Feature which shall not be performed while analysis, especially loading of external resources!
   * 
   * @see <a href="https://xerces.apache.org/xerces2-j/features.html">xerces documentation</a> and
   *      <a href=
   *      "http://sax.sourceforge.net/apidoc/org/xml/sax/package-summary.html#package_description">SAX
   *      javadoc</a>
   */
  private static final String[] SAX_FACTORY_FEATURES_TO_DISABLE = new String[]{
      "http://xml.org/sax/features/external-general-entities", //
      "http://xml.org/sax/features/external-parameter-entities", //
      "http://xml.org/sax/features/resolve-dtd-uris", //
      "http://xml.org/sax/features/validation", //
      "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", //
      "http://apache.org/xml/features/nonvalidating/load-external-dtd",
      "http://apache.org/xml/features/validation/schema", //
  };

  /**
   * Disable any external resource resolution.
   * 
   * These values are available as constants since jdk 1.7_u40, but we require u7 only. So we need
   * to inject the constant values
   */
  private static final String[] JAPX_EXTERNALS_TO_DISABLE = new String[]{
      "http://javax.xml.XMLConstants/property/accessExternalDTD", // =
                                                                  // XMLConstants.ACCESS_EXTERNAL_DTD
      "http://javax.xml.XMLConstants/property/accessExternalSchema", // =
                                                                     // XMLConstants.ACCESS_EXTERNAL_SCHEMA
      "http://javax.xml.XMLConstants/property/accessExternalStylesheet" // =
                                                                        // XMLConstants.ACCESS_EXTERNAL_STYLESHEET
  };
  public static final int LOOK_AHEAD = 500;

  private static SoftReference<SAXParserFactory> saxFactoryReference = new SoftReference<>(null);

  private final class SAXAnalysisHandler extends DefaultHandler {

    private final Context context;

    boolean hitRoot = false;

    private String rootElementName;

    private String namespaceURI;

    private Locator2 locator;

    private String xmlVersion;

    private String encoding;

    private SAXAnalysisHandler(final Context context) {
      this.context = context;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
      if (locator instanceof Locator2) {
        this.locator = (Locator2) locator;
      }
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
      // Don't resolve any entities
      return null;
    }

    @Override
    public void startDocument() throws SAXException {
      if (locator != null) {
        this.xmlVersion = locator.getXMLVersion();
        this.encoding = locator.getEncoding();
      }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
      if (!hitRoot) {
        hitRoot = true;

        if (uri != null && !uri.isEmpty()) {
          namespaceURI = uri;
          context.info(XMLMatcher.this, "Found namespace URI: '" + uri + "'");
        }

        rootElementName = localName;
        context.info(XMLMatcher.this, "Found root element: '" + localName + "'");
      }
    }

    public String getRootElementName() {
      return rootElementName;
    }

    public String getNamespaceURI() {
      return namespaceURI;
    }

    public String getXmlVersion() {
      return xmlVersion;
    }

    public String getEncoding() {
      return encoding;
    }
  }

  @Override
  public boolean matches(final Context context) throws IOException {
    try {

      final SeekableInputStream sis = context.getStream();
      if (!hasXmlProlog(sis) && !hasXmlTags(sis, LOOK_AHEAD)) {
        // neither XML-prolog nor XML-tags -> no XML
        return false;
      }

      final SAXParser saxParser = createSAXParser();
      final SAXAnalysisHandler handler = new SAXAnalysisHandler(context);
      saxParser.parse(sis, handler);

      String mimeType = MIME_TYPE_XML;
      if (handler.getEncoding() != null && !handler.getEncoding().isEmpty()) {
        mimeType += ";charset=" + handler.getEncoding();
      }

      context.setProperty(MimeTypeAction.KEY, mimeType);
      context.setProperty(ExtensionAction.KEY, "xml");
      context.setProperty(DescriptionAction.KEY, "Extensible Markup Language (XML)");

      final Map<String, Object> xmlDetails = new HashMap<>();
      context.setProperty(DETAILS_KEY, xmlDetails);
      putIfPresent(NAMESPACE_URI_KEY, handler.getNamespaceURI(), xmlDetails);
      putIfPresent(ROOT_ELEMENT_NAME_KEY, handler.getRootElementName(), xmlDetails);
      // xml version: see
      // http://sax.sourceforge.net/apidoc/org/xml/sax/package-summary.html#package_description
      putIfPresent(DOCUMENT_XML_VERSION_KEY, handler.getXmlVersion(), xmlDetails);

      // Parser would have thrown a SAXException is this is no proper XML
      return true;
    } catch (ParserConfigurationException | SAXException e) {
      context.error(this, "Attempt to load invalid XML", e);
      return false;
    }
  }

  /**
   * Checks if the stream starts with an XML prolog ("<?xml ...").
   * 
   * @param sis the stream to check
   * 
   * @return true iff an XML prolog is present
   * @throws IOException
   */
  private boolean hasXmlProlog(final SeekableInputStream sis) throws IOException {
    sis.seek(0);
    try {

      final InputStreamReader reader = createReader(sis);

      // read a few char more in order to allow BOM or any some space before actual prolog
      final char[] buff = new char[6];
      final int read = reader.read(buff, 0, buff.length);

      if (read <= 0) {
        return false;
      }

      final String prolog = new String(buff);
      return PROLOG_PATTERN.matcher(prolog).find();
    } finally {
      sis.seek(0);
    }
  }

  /**
   * Create a {@link java.io.Reader} instance that will obey potential {@link ByteOrderMark BOMs}.
   * As XML is suspected to be human readable text, we're leveraging the {@link java.io.Reader}
   * framework to analyze the contents. This method will take care of creating the appropriate
   * {@link java.io.Reader} with a matching {@link Charset} configuration.
   *
   * @param sis the {@link SeekableInputStream} a {@link java.io.Reader} shall be created for
   * @return a {@link java.io.Reader} instance respecting the encoding if defined by a
   *         {@link ByteOrderMark BOM}
   */
  private InputStreamReader createReader(final SeekableInputStream sis) throws IOException {
    final BOMInputStream bomIS = new BOMInputStream(sis, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
        ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE);

    final Charset charset;
    if (bomIS.hasBOM()) {
      charset = Charset.forName(bomIS.getBOMCharsetName());
    } else {
      // if there is no BOM, we're guessing UTF-8 (which is safe for the prolog, as it will only
      // contain ASCII characters)
      charset = Charset.forName(ByteOrderMark.UTF_8.getCharsetName());
    }

    return new InputStreamReader(bomIS, charset);
  }

  /**
   * Checks if this stream contains XML tags in the first <tt>lookAhead</tt> bytes.
   * 
   * @param sis The stream to check
   * @param lookAhead how many bytes we will check
   * @return true iff there is an XML tag present
   * 
   * @throws IOException
   */
  private boolean hasXmlTags(final SeekableInputStream sis, final int lookAhead) throws IOException {
    sis.seek(0);
    boolean foundLT = false;

    try {
      final InputStreamReader reader = createReader(sis);
      int c;
      int charactersRead = 0;

      // Check every single byte we read if this "<" or ">"
      while ((c = reader.read()) != -1 && charactersRead++ < lookAhead) {
        if (!foundLT) {
          // no tag beginning has been found yet. In that case, only whitespace characters are
          // allowed
          if (c == '<') {
            // found the opening character for a XML tag
            foundLT = true;
          } else if (!Character.isWhitespace(c)) {
            // not a whitespace character. Cancel the interpretation
            return false;
          }
        } else if (c == '>') {
          // closing character found. Files seems to be a XML
          return true;
        }
      }
      return false;
    } finally {
      sis.seek(0);
    }
  }

  private static void putIfPresent(final String key, final Object value, final Map<String, Object> target) {
    if (value != null) {
      target.put(key, value);
    }
  }

  /**
   * Creates a pre-configured SAX Parser
   * 
   * @return {@link SAXParser}
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  private SAXParser createSAXParser() throws ParserConfigurationException, SAXException {
    SAXParserFactory spf = saxFactoryReference.get();
    if (spf == null) {
      spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      spf.setValidating(false);
      for (String feature : SAX_FACTORY_FEATURES_TO_DISABLE) {
        disableFeatureSafely(spf, feature);
      }
      saxFactoryReference = new SoftReference<SAXParserFactory>(spf);
    }

    final SAXParser parser = spf.newSAXParser();
    for (String feature : JAPX_EXTERNALS_TO_DISABLE) {
      disableExternalSafely(parser, feature);
    }
    return parser;
  }

  /**
   * Disables a feature safely; that means it logs a possible exception instead of throwing if.
   * 
   * This is necessary because the array #{@link #SAX_FACTORY_FEATURES_TO_DISABLE} contains some
   * feature names for Xerces which is part of the Oracle JVM, but not meant do be for JVMs of other
   * manufactures.
   * 
   * @param spf the {@link SAXParserFactory} to configure
   * @param feature the feature to disable
   */
  private static void disableFeatureSafely(final SAXParserFactory spf, final String feature) {
    try {
      spf.setFeature(feature, false);
    } catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
      LOGGER.warn("Disabling feature '" + feature + "' is not supported. Consider to upgrade to a new JVM version", e);
    }
  }

  private static void disableExternalSafely(final SAXParser parser, final String external) {
    try {
      // Empty value means no external location, see
      // https://docs.oracle.com/javase/tutorial/jaxp/properties/properties.html
      parser.setProperty(external, "");
    } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
      LOGGER.warn("Disabling feature '" + external + "' is not supported. Consider to upgrade to a new JVM version", e);
    }
  }
}
