package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.matchers.xml.SAXAnalysisHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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

  public static final int DEFAULT_MAX_ENTITY_EXPANSIONS = 20;
  private static final String JAXP_ENTITY_EXPANSION_LIMIT_KEY = "jdk.xml.entityExpansionLimit";
  private static volatile int MAX_ENTITY_EXPANSIONS = determineMaxEntityExpansions();
  private static final String XERCES_SECURITY_MANAGER = "org.apache.xerces.util.SecurityManager";
  private static final String XERCES_SECURITY_MANAGER_PROPERTY = "http://apache.org/xml/properties/security-manager";
  private static long LAST_LOG = -1;

  /**
   * Feature which shall not be performed while analysis, especially loading of external resources!
   * 
   * See also the <a href="https://xerces.apache.org/xerces2-j/features.html">xerces
   * documentation</a> and the <a href=
   * "http://sax.sourceforge.net/apidoc/org/xml/sax/package-summary.html#package_description">SAX
   * javadoc</a>
   */
  static final Map<String, Boolean> SAX_FACTORY_FEATURES;
  static {
    SAX_FACTORY_FEATURES = new HashMap<>();
    SAX_FACTORY_FEATURES.put(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    SAX_FACTORY_FEATURES.put("http://xml.org/sax/features/external-general-entities", false);
    SAX_FACTORY_FEATURES.put("http://xml.org/sax/features/external-parameter-entities", false);
    SAX_FACTORY_FEATURES.put("http://xml.org/sax/features/resolve-dtd-uris", false);
    SAX_FACTORY_FEATURES.put("http://xml.org/sax/features/validation", false);
    SAX_FACTORY_FEATURES.put("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    SAX_FACTORY_FEATURES.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    SAX_FACTORY_FEATURES.put("http://apache.org/xml/features/validation/schema", false);
  }

  /**
   * Disable any external resource resolution.
   *
   * https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#xmlinputfactory-a-stax-parser
   */
  static final String[] JAPX_EXTERNALS_TO_DISABLE = new String[]{
          XMLConstants.ACCESS_EXTERNAL_DTD, // "http://javax.xml.XMLConstants/property/accessExternalDTD"
          XMLConstants.ACCESS_EXTERNAL_SCHEMA, // "http://javax.xml.XMLConstants/property/accessExternalSchema"
          XMLConstants.ACCESS_EXTERNAL_STYLESHEET // "http://javax.xml.XMLConstants/property/accessExternalStylesheet"
  };

  public static final int LOOK_AHEAD = 500;

  private static SoftReference<SAXParserFactory> saxFactoryReference = new SoftReference<>(null);

  @Override
  public boolean matches(final Context context) throws IOException {
    try {

      final SeekableInputStream sis = context.getStream();
      if (!hasXmlProlog(sis) && !hasXmlTags(sis, LOOK_AHEAD)) {
        // neither XML-prolog nor XML-tags -> no XML
        return false;
      }

      final SAXParser saxParser = createSAXParser();
      final SAXAnalysisHandler handler = new SAXAnalysisHandler(this, context);
      saxParser.parse(sis, handler);

      String mimeType = MIME_TYPE_XML;
      if (handler.getEncoding() != null && !handler.getEncoding().isEmpty()) {
        mimeType += ";charset=" + handler.getEncoding();
      }

      context.setProperty(MimeTypeAction.KEY, mimeType);
      context.setProperty(ExtensionAction.KEY, "xml");
      // do not set description to allow concatenating description from file-type database
      //context.setProperty(DescriptionAction.KEY, "Extensible Markup Language (XML)");

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
   * As XML is suspected to be human-readable text, we're leveraging the {@link java.io.Reader}
   * framework to analyze the contents. This method will take care of creating the appropriate
   * {@link java.io.Reader} with a matching {@link Charset} configuration.
   *
   * @param sis the {@link SeekableInputStream} a {@link java.io.Reader} shall be created for
   * @return a {@link java.io.Reader} instance respecting the encoding if defined by a
   *         {@link ByteOrderMark BOM}
   */
  private InputStreamReader createReader(final SeekableInputStream sis) throws IOException {
    final BOMInputStream bomIS = BOMInputStream.builder().setInputStream(sis)
        .setByteOrderMarks(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
            ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)
        .get();

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
  static SAXParser createSAXParser() throws ParserConfigurationException, SAXException {
    SAXParserFactory spf = getSaxParserFactory();

    final SAXParser parser = spf.newSAXParser();
    for (String feature : JAPX_EXTERNALS_TO_DISABLE) {
      disableExternalSafely(parser, feature);
    }
    trySetXercesSecurityManager(parser);
    return parser;
  }

  /**
   * Creates a pre-configures SAXParserFactory
   *
   * @return {@link SAXParserFactory}
   */
  @SuppressWarnings("java:S2755") // compliant settings are applied, but in another method
  static SAXParserFactory getSaxParserFactory() {
    SAXParserFactory spf = saxFactoryReference.get();
    if (spf == null) {
      spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      spf.setValidating(false);
      for (Map.Entry<String,Boolean> entry : SAX_FACTORY_FEATURES.entrySet()) {
        setFeatureSafely(spf, entry.getKey(), entry.getValue());
      }
      saxFactoryReference = new SoftReference<>(spf);
    }
    return spf;
  }

  /**
   * Set a feature safely; that means it logs a possible exception instead of throwing it.
   * <p>
   * This is necessary because the array #{@link #SAX_FACTORY_FEATURES} contains some
   * feature names for Xerces which is part of the Oracle JVM, but not meant for JVMs of other
   * manufactures.
   *
   * @param spf     the {@link SAXParserFactory} to configure
   * @param feature the feature to disable
   * @param enabled weather to enable the feature
   */
  private static void setFeatureSafely(final SAXParserFactory spf, final String feature, Boolean enabled) {
    try {
      spf.setFeature(feature, enabled);
    } catch (SAXNotRecognizedException e){
      LOGGER.debug("The feature '" + feature + "' is not recognized by the SAXParserFactory. Check if the feature name is correct.", e);
    }catch (SAXNotSupportedException | ParserConfigurationException e) {
      LOGGER.debug("Setting feature '" + feature + "' is not supported.", e);
    }
  }

  private static void disableExternalSafely(final SAXParser parser, final String external) {
    try {
      // Empty value means no external location, see
      // https://docs.oracle.com/javase/tutorial/jaxp/properties/properties.html
      parser.setProperty(external, "");
    }catch (SAXNotRecognizedException e){
      LOGGER.debug("The property '" + external + "' is not recognized by the SAXParser. Check if the property name is correct.", e);
    } catch (SAXNotSupportedException e) {
      LOGGER.debug("Disabling feature '" + external + "' is not supported. Consider to upgrade to a new JVM version", e);
    }
  }

  private static void trySetXercesSecurityManager(SAXParser parser) {
    //from POI
    // Try built-in JVM one first, standalone if not
    for (String securityManagerClassName : new String[]{
            //"com.sun.org.apache.xerces.internal.util.SecurityManager",
            XERCES_SECURITY_MANAGER}) {
      try {
        Object mgr = Class.forName(securityManagerClassName).getDeclaredConstructor().newInstance();
        Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
        setLimit.invoke(mgr, MAX_ENTITY_EXPANSIONS);

        parser.setProperty(XERCES_SECURITY_MANAGER_PROPERTY, mgr);
        // Stop once one can be setup without error
        return;
      } catch (ClassNotFoundException e) {
        // continue without log, this is expected in some setups
      } catch (Throwable e) {
        // NOSONAR - also catch things like NoClassDefError here
        // throttle the log somewhat as it can spam the log otherwise
        if (System.currentTimeMillis() > LAST_LOG + TimeUnit.MINUTES.toMillis(5)) {
          LOGGER.warn(
                  "SAX Security Manager could not be setup [log suppressed for 5 " +
                          "minutes]",
                  e);
          LAST_LOG = System.currentTimeMillis();
        }
      }
    }

    // separate old version of Xerces not found => use the builtin way of setting the property
    try {
      parser.setProperty("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit",
              MAX_ENTITY_EXPANSIONS);
    } catch (SAXException e) {     // NOSONAR - also catch things like NoClassDefError here
      // throttle the log somewhat as it can spam the log otherwise
      if (System.currentTimeMillis() > LAST_LOG + TimeUnit.MINUTES.toMillis(5)) {
        LOGGER.warn("SAX Security Manager could not be setup [log suppressed for 5 minutes]",
                e);
        LAST_LOG = System.currentTimeMillis();
      }
    }
  }

  private static int determineMaxEntityExpansions() {
    String expansionLimit = System.getProperty(JAXP_ENTITY_EXPANSION_LIMIT_KEY);
    if (expansionLimit != null) {
      try {
        return Integer.parseInt(expansionLimit);
      } catch (NumberFormatException e) {
        LOGGER.warn(
                "Couldn't parse an integer for the entity expansion limit: {}; " +
                        "backing off to default: {}",
                expansionLimit, DEFAULT_MAX_ENTITY_EXPANSIONS);
      }
    }
    return DEFAULT_MAX_ENTITY_EXPANSIONS;
  }
}
