package org.jadice.filetype.matchers;

import static org.jadice.filetype.matchers.XMLMatcher.DEFAULT_MAX_ENTITY_EXPANSIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hamcrest.Matchers;
import org.jadice.filetype.AnalysisListener;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TestXMLMatcher {

  @RegisterExtension
  public JUnit5Mockery context = new JUnit5Mockery() {
    {
      // See http://jmock.org/threading-synchroniser.html
      setThreadingPolicy(new Synchroniser());
    }
  };

  private static final XMLMatcher MATCHER = new XMLMatcher();

  private final static Locale LOCALE = Locale.ENGLISH;

  @Test
  void testXML_withoutNS_match() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-8";
    final String expectedRootName = "project";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expectedRootName)));
    }});

    final Context ctx = createContext(load("/xml/build.xml"), listener);

    assertTrue(MATCHER.matches(ctx), "XML Matcher shall match on XML");
    verifyContext(ctx, expMimeType, null, expectedRootName, "1.0");
  }

  @Test
  void testXML_withNS_match() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-8";
    final String expNamespaceURI = "http://jakarta.apache.org/log4j/";
    final String expRootName = "configuration";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expNamespaceURI)));
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(load("/xml/log4j.xml"), listener);

    assertTrue(MATCHER.matches(ctx), "XML Matcher shall match on XML");
    verifyContext(ctx, expMimeType, expNamespaceURI, expRootName, "1.0");
  }

  @Test
  void testXML_With_UTF16() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-16BE";
    final String expNamespaceURI = "http://jakarta.apache.org/log4j/";
    final String expRootName = "configuration";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expNamespaceURI)));
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(load("/xml/log4j-utf-16.xml"), listener);

    assertTrue(MATCHER.matches(
            ctx), "XML Matcher shall match on UTF-16 encoded XML");
    verifyContext(ctx, expMimeType, expNamespaceURI, expRootName, "1.0");
  }

  @SuppressWarnings("unchecked")
  private void verifyContext(Context ctx, String expMimeType, String expNamespaceURI, String expRootName, String expXmlVersion) {
    assertEquals(expMimeType, ctx.getProperty(MimeTypeAction.KEY), "Wrong MIME type detected");
    final Object xmlPropertiesRaw = ctx.getProperty(XMLMatcher.DETAILS_KEY);
    assertNotNull(xmlPropertiesRaw, "No XML details found");
    assertTrue(xmlPropertiesRaw instanceof Map, "XML details must be a map");
    Map<String, String> xmlProperties = (Map<String, String>) xmlPropertiesRaw;

    assertEquals(expNamespaceURI, xmlProperties.get(XMLMatcher.NAMESPACE_URI_KEY), "Wrong NS URI detected");
    assertEquals(expRootName, xmlProperties.get(XMLMatcher.ROOT_ELEMENT_NAME_KEY), "Wrong root element detected");
    assertEquals(expXmlVersion, xmlProperties.get(XMLMatcher.DOCUMENT_XML_VERSION_KEY), "Wrong XML version detected");
  }

  @Test
  void testXML_withoutProlog_match() throws Exception {
     AnalysisListener listener = context.mock(AnalysisListener.class);
    final String xmlNoProlog = "<x/>";

    final String expMimeType = "application/xml;charset=UTF-8";
    final String expRootName = "x";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(new MemoryInputStream(xmlNoProlog.getBytes(StandardCharsets.UTF_8)), listener);
    final boolean matches = MATCHER.matches(ctx);
    assertTrue(matches, "Matcher shall match on XML without prolog");

    verifyContext(ctx, expMimeType, null, expRootName, "1.0");

  }

  @Test
  void testNoInput() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final boolean matches = MATCHER.matches(createContext(new MemoryInputStream(new byte[0]), listener));
    assertFalse(matches, "Matcher shall not match on empty input");
  }

  @Test
  void testPDF_noMatch() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);

    final Context ctx = createContext(load("/pdf/normal/lorem-ipsum.pdf"), listener);
    assertFalse(MATCHER.matches(ctx), "Matcher shall not match on a pdf file");
    context.assertIsSatisfied();
  }

  @Test
  void testTXT_noMatch() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(load("/txt/Latin1.txt"), listener);
    assertFalse(MATCHER.matches(ctx), "Matcher shall not match on a pdf file");
  }

  @Test
  void testTXT_with_LT_GT_noMatch() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(load("/txt/text-no-xml.txt"), listener);
    assertFalse(MATCHER.matches(ctx), "Matcher shall not match on a Text file with arbitrary < and >");
  }

  private static SeekableInputStream load(String resource) throws IOException {
    return new MemoryInputStream(TestXMLMatcher.class.getResourceAsStream(resource));
  }

  public Context createContext(SeekableInputStream sis, AnalysisListener listener) {
    return new Context(sis, new HashMap<>(), listener, LOCALE, null);
  }

  @Test
  void testMatchXMLWithLeadingWhitespaces() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(new MemoryInputStream("         <empty/>".getBytes()), listener);

    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString("empty")));
    }});


    assertTrue(MATCHER.matches(ctx));
  }

  @Test
  void testDoNotMatchWhenExceedingLookahead() throws Exception {

    byte[] buf = new byte[XMLMatcher.LOOK_AHEAD + 10];
    // fill everything with whitespaces
    Arrays.fill(buf, (byte)' ');

    // begin an XML tag some bytes before the lookahead limit
    buf[XMLMatcher.LOOK_AHEAD - 3] = '<';
    buf[XMLMatcher.LOOK_AHEAD - 2] = 's';
    buf[XMLMatcher.LOOK_AHEAD - 1] = 'o';
    buf[XMLMatcher.LOOK_AHEAD] = 'm';
    buf[XMLMatcher.LOOK_AHEAD + 1] = 'e';
    buf[XMLMatcher.LOOK_AHEAD + 2] = 'X';
    buf[XMLMatcher.LOOK_AHEAD + 3] = 'm';
    buf[XMLMatcher.LOOK_AHEAD + 4] = 'l';
    buf[XMLMatcher.LOOK_AHEAD + 5] = '>';

    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(new MemoryInputStream(buf), listener);

    assertFalse(MATCHER.matches(ctx));
  }

  @Test
  void testSaxParserIsConfiguredSecurely() throws Exception {
    SAXParser saxParser = XMLMatcher.createSAXParser();
    for (String s : XMLMatcher.JAPX_EXTERNALS_TO_DISABLE) {
      if(!s.equals("http://javax.xml.XMLConstants/property/accessExternalStylesheet")){
        assertEquals("", saxParser.getProperty(s));
      }
    }
    assertEquals(String.valueOf(DEFAULT_MAX_ENTITY_EXPANSIONS), saxParser.getProperty("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit"));
  }

  @Test
  void testSaxParserFactoryIsConfiguredSecurely() throws Exception {
    SAXParserFactory saxParserFactory = XMLMatcher.getSaxParserFactory();
    for (Map.Entry<String,Boolean> entry : XMLMatcher.SAX_FACTORY_FEATURES.entrySet()) {
        assertEquals(entry.getValue(), saxParserFactory.getFeature(entry.getKey()));
    }
  }
}
