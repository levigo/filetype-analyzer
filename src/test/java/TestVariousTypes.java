import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Stream;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.matchers.PDFMatcher;
import org.jadice.filetype.matchers.XMLMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestVariousTypes {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestVariousTypes.class);

  private static Analyzer analyzer;

  @BeforeAll
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testVariousTypes() throws IOException {
    final File[] files = new File("src/test/resources/various_types").listFiles(
        pathname -> pathname.isFile() && pathname.canRead());
    assert files != null;

    for (final File file : files) {
      LOGGER.info("File: " + file);
      final Map<String, Object> results = analyzer.analyze(file);
      assertNotNull(results, file + " could not be analyzed");
      assertNotNull(results.get(MimeTypeAction.KEY), "mimeType missing for " + file);
      assertNotNull(results.get(DescriptionAction.KEY), "description missing for" + file);
      // extension can be null
      // assertNotNull(results.get(ExtensionAction.KEY), file + " could not be analyzed");
      printResult(results);
    }
  }

  @Test
  void testEmptyStream() throws Exception {
    Map<String, Object> results = analyzer.analyze(new MemoryInputStream(new byte[0]));
    assertNotNull(results, "empty stream could not be analyzed");
    assertEquals("text/plain", results.get(MimeTypeAction.KEY));
    assertEquals("txt", results.get(ExtensionAction.KEY));
    assertEquals("Binary data, ASCII Text Document", results.get(DescriptionAction.KEY));
  }

  public static Stream<Arguments> dataProviderVarious() {
    return Stream.of(
        arguments("/various_types/iworks_pages_file.pages", "application/vnd.apple.pages", "Apple Pages Document", "pages"),
        arguments("/various_types/iworks_numbers_file.numbers", "application/vnd.apple.numbers", "Apple Numbers Document", "numbers"),
        arguments("/various_types/iworks_keynote_file.key", "application/vnd.apple.keynote", "Apple Keynote Document", "key")
    );
  }

  @ParameterizedTest
  @MethodSource("dataProviderVarious")
  void testVariousTypesExplicitly(String resource, String expectedMimeType, String expectedDescription, String expectedExtension) throws Exception {
    final URL url = getClass().getResource(resource);
    assertNotNull(url);
    final File file = new File(url.toURI());
    final Map<String, Object> results = analyzer.analyze(file);
    printResult(results);
    assertNotNull(results, file + " could not be analyzed");
    assertNotNull(results.get(MimeTypeAction.KEY), "mimeType missing");
    assertEquals(expectedMimeType, results.get(MimeTypeAction.KEY), "wrong mimeType");
    assertNotNull(results.get(DescriptionAction.KEY), "description missing");
    assertEquals(expectedDescription, results.get(DescriptionAction.KEY), "wrong description");
    assertNotNull(results.get(ExtensionAction.KEY), "could not be analyzed");
    assertEquals(expectedExtension, results.get(ExtensionAction.KEY), "wrong extension");
  }

  public static Stream<Arguments> dataProviderXRechnung() {
    return Stream.of(
        arguments("/various_types/BASIC_Einfach.pdf", "application/pdf"),
        arguments("/various_types/EN16931_Einfach.pdf", "application/pdf"),
        arguments("/various_types/EN16931_Einfach.cii.xml", "application/xml;charset=UTF-8;x-rechnung=true"),
        arguments("/various_types/EN16931_Einfach.ubl.xml", "application/xml;charset=UTF-8;x-rechnung=true"),
        arguments("/various_types/ZUGFeRD-invoice_rabatte_3_abschlag_duepayableamount.xml", "application/xml;charset=UTF-8;x-rechnung=true"),
        arguments("/various_types/ubl-creditNote-2.0-Example.xml", "application/xml;charset=UTF-8;x-rechnung=true")
    );
  }

  @ParameterizedTest
  @MethodSource("dataProviderXRechnung")
  void testXRechnung(String resource, String expectedMimeType) throws Exception {
    final URL url = getClass().getResource(resource);
    assertNotNull(url);
    final File file = new File(url.toURI());
    final Map<String, Object> results = analyzer.analyze(file);
    assertNotNull(results, file + " could not be analyzed");
    assertNotNull(results.get(MimeTypeAction.KEY), "mimeType missing");
    assertEquals(expectedMimeType, results.get(MimeTypeAction.KEY), "wrong mimeType");
    assertNotNull(results.get(DescriptionAction.KEY), "description missing");
    assertNotNull(results.get(ExtensionAction.KEY), "could not be analyzed");
    checkForDetails(results);
    printResult(results);
  }

  private void checkForDetails(final Map<String, Object> results) {
    final String mimeType = (String)results.get(MimeTypeAction.KEY);
    switch (mimeType) {
      case "application/pdf": ensureXRechnungIsTrue(results, PDFMatcher.DETAILS_KEY); break;
      case "application/xml;charset=UTF-8;x-rechnung=true": ensureXRechnungIsTrue(results, XMLMatcher.DETAILS_KEY); break;
      default: fail("unexpected mime type");
    }
  }

  @SuppressWarnings("unchecked")
  private void ensureXRechnungIsTrue(final Map<String, Object> results, final String detailsKey) {
    final Object details = results.get(detailsKey);
    assertNotNull(details, "details are missing");
    final Map<String, Object> detailsMap = (Map<String, Object>) details;
    final boolean isXRechnung = (Boolean)detailsMap.get(XMLMatcher.X_RECHNUNG_KEY);
    assertTrue(isXRechnung, "x_rechnung should be true");
  }


  public static void printResult(final Map<String, Object> results) {
    for (final Map.Entry<String, Object> e : results.entrySet()) {
      LOGGER.info("   {}={}", e.getKey(), e.getValue());
    }
    LOGGER.info("\n-------------------");
  }
}
