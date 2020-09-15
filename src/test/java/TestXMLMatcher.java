import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jadice.filetype.AnalysisListener;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.matchers.XMLMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Test;

public class TestXMLMatcher {

  public final Mockery context = new JUnitRuleMockery();

  private static final XMLMatcher MATCHER = new XMLMatcher();

  private final static Locale LOCALE = Locale.ENGLISH;

  @Test
  public void testXML_withoutNS_match() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-8";
    final String expectedRootName = "project";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expectedRootName)));
    }});

    final Context ctx = createContext(load("/xml/build.xml"), listener);

    assertTrue("XML Matcher shall match on XML", MATCHER.matches(ctx));
    verifyContext(ctx, expMimeType, null, expectedRootName, "1.0");
  }

  @Test
  public void testXML_withNS_match() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-8";
    final String expNamespaceURI = "http://jakarta.apache.org/log4j/";
    final String expRootName = "configuration";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expNamespaceURI)));
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(load("/xml/log4j.xml"), listener);

    assertTrue("XML Matcher shall match on XML", MATCHER.matches(ctx));
    verifyContext(ctx, expMimeType, expNamespaceURI, expRootName, "1.0");
  }

  @Test
  public void testXML_With_UTF16() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String expMimeType = "application/xml;charset=UTF-16BE";
    final String expNamespaceURI = "http://jakarta.apache.org/log4j/";
    final String expRootName = "configuration";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expNamespaceURI)));
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(load("/xml/log4j-utf-16.xml"), listener);

    assertTrue("XML Matcher shall match on UTF-16 encoded XML", MATCHER.matches(
        ctx));
    verifyContext(ctx, expMimeType, expNamespaceURI, expRootName, "1.0");
  }

  @SuppressWarnings("unchecked")
  private void verifyContext(Context ctx, String expMimeType, String expNamespaceURI, String expRootName, String expXmlVersion) {
    assertEquals("Wrong MIME type detected", expMimeType, ctx.getProperty(MimeTypeAction.KEY));
    final Object xmlPropertiesRaw = ctx.getProperty(XMLMatcher.DETAILS_KEY);
    assertNotNull("No XML details found", xmlPropertiesRaw);
    assertTrue("XML details must be a map", xmlPropertiesRaw instanceof Map);
    Map<String, String> xmlProperties = (Map<String, String>) xmlPropertiesRaw;

    assertEquals("Wrong NS URI detected", expNamespaceURI, xmlProperties.get(XMLMatcher.NAMESPACE_URI_KEY));
    assertEquals("Wrong root element detected", expRootName, xmlProperties.get(XMLMatcher.ROOT_ELEMENT_NAME_KEY));
    assertEquals("Wrong XML version detected", expXmlVersion, xmlProperties.get(XMLMatcher.DOCUMENT_XML_VERSION_KEY));
  }

  @Test
  public void testXML_withoutProlog_match() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final String xmlNoProlog = "<x/>";

    final String expMimeType = "application/xml;charset=UTF-8";
    final String expRootName = "x";
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString(expRootName)));
    }});
    final Context ctx = createContext(new MemoryInputStream(xmlNoProlog.getBytes("UTF-8")), listener);
    final boolean matches = MATCHER.matches(ctx);
    assertTrue("Matcher shall match on XML without prolog", matches);

    verifyContext(ctx, expMimeType, null, expRootName, "1.0");

  }

  @Test
  public void testNoInput() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final boolean matches = MATCHER.matches(createContext(new MemoryInputStream(new byte[0]), listener));
    assertFalse("Matcher shall not match on empty input", matches);

  }

  @Test
  public void testPDF_noMatch() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);

    final Context ctx = createContext(load("/pdf/normal/lorem-ipsum.pdf"), listener);
    assertFalse("Matcher shall not match on a pdf file", MATCHER.matches(ctx));
    context.assertIsSatisfied();
  }

  @Test
  public void testTXT_noMatch() throws IOException {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(load("/txt/Latin1.txt"), listener);
    assertFalse("Matcher shall not match on a pdf file", MATCHER.matches(ctx));
  }

  @Test
  public void testTXT_with_LT_GT_noMatch() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(load("/txt/text-no-xml.txt"), listener);
    assertFalse("Matcher shall not match on a Text file with arbitrary < and >", MATCHER.matches(ctx));
  }

  private static SeekableInputStream load(String resource) throws IOException {
    return new MemoryInputStream(TestXMLMatcher.class.getResourceAsStream(resource));
  }

  public Context createContext(SeekableInputStream sis, AnalysisListener listener) {
    return new Context(sis, new HashMap<String, Object>(), listener, LOCALE);
  }

  @Test
  public void testMatchXMLWithLeadingWhitespaces() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final Context ctx = createContext(new MemoryInputStream("         <empty/>".getBytes()), listener);

    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(MATCHER)), with(Matchers.containsString("empty")));
    }});


    assertTrue(MATCHER.matches(ctx));
  }

  @Test
  public void testDoNotMatchWhenExceedingLookahead() throws Exception {

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
}
