import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

import org.jadice.filetype.AnalysisListener;
import org.jadice.filetype.Context;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.matchers.TextMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Test;

public class TestTextMatcher {
  
  public final Mockery context = new JUnitRuleMockery();
  
  private final static Locale LOCALE = Locale.ENGLISH;
  
  private final static TextMatcher THE_MATCHER = new TextMatcher();
  
  private final static String ACCEPTED_MESSAGE = "Percentage of accepted chars:";
  
  private final static String TEST_PUNCTUATION_ONLY = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
  
  private final static String TEST_PUNCTUATION_STRING = "Hallo Welt" + TEST_PUNCTUATION_ONLY;

  @Test
  public void testBinary() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
    }});
    final boolean matches = THE_MATCHER.matches(createContext(load("/various_types/datenmuell.xyz"), listener));
    assertFalse("TextMatcher must not match on binary data", matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testShortText() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
    }});
    final boolean matches = THE_MATCHER.matches(createContext(load("/txt/short.txt"), listener));
    assertTrue("TextMatcher must match on a short text", matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testUFT8() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
      oneOf(listener).info(THE_MATCHER, "Determined charset: UTF-8");
    }});
    final boolean matches = THE_MATCHER.matches(createContext(load("/txt/UTF-8.txt"), listener));
    assertTrue("TextMatcher must match on UTF-8 text", matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testLatin1() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
    }});
    final boolean matches = THE_MATCHER.matches(createContext(load("/txt/Latin1.txt"), listener));
    assertTrue("TextMatcher must match on UTF-16 text", matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testUFT16() throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
      oneOf(listener).info(THE_MATCHER, "Determined charset: UTF-16LE");
    }});
    final boolean matches = THE_MATCHER.matches(createContext(load("/txt/UTF-16.txt"), listener));
    assertTrue("TextMatcher must match on UTF-16 text", matches);
    context.assertIsSatisfied();
  }

  @Test
  public void testMatchPunctuationString() throws Exception {
    
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
    }});
    final boolean matches = THE_MATCHER.matches(createContext(fromString(TEST_PUNCTUATION_STRING), listener));
    assertTrue("TextMatcher must match on " + TEST_PUNCTUATION_STRING, matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testMatchPunctuationOnly()  throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    context.checking(new Expectations() {{
      oneOf(listener).info(with(same(THE_MATCHER)), with(containsString(ACCEPTED_MESSAGE)));
    }});
    final boolean matches = THE_MATCHER.matches(createContext(fromString(TEST_PUNCTUATION_ONLY), listener));
    assertFalse("TextMatcher must not match on " + TEST_PUNCTUATION_ONLY, matches);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testMatchEmptyString()  throws Exception {
    final AnalysisListener listener = context.mock(AnalysisListener.class);
    final boolean matches = THE_MATCHER.matches(createContext(fromString(""), listener));
    assertFalse("TextMatcher must match on empty String", matches);
  }  
  
  private static SeekableInputStream fromString(String input) {
    return new MemoryInputStream(input.getBytes(StandardCharsets.UTF_8));
  }
  
  private static SeekableInputStream load(String resourceName) throws IOException {
    final InputStream s = TestTextMatcher.class.getResourceAsStream(resourceName);
    return new MemoryInputStream(s);
  }

  public static Context createContext(SeekableInputStream sis, AnalysisListener listener) {
    return new Context(sis, new HashMap<String, Object>(), listener, LOCALE,null);
  }
}
