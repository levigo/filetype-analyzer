import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.matchers.PDFMatcher;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestPDFMatcher {

  private static final File INPUT_FOLDER = new File("src/test/resources/pdf");

  private static final File ENCRYPTED_PDFS_FOLDER = new File(INPUT_FOLDER, "encrypted");

  private static final File UNENCRYPTED_PDFS_FOLDER = new File(INPUT_FOLDER, "normal");

  private static final File PORTABLE_COLLECTION_FOLDER = new File(INPUT_FOLDER, "portfolio");

  private static Analyzer ANALYZER;

  @BeforeClass
  public static void init() throws AnalyzerException {
    ANALYZER = Analyzer.getInstance("/magic.xml");
  }

  @Test
  public void testUnencryptedPDFs() throws IOException {
    for (File f : nullSafe(UNENCRYPTED_PDFS_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is recognized as PDF", "application/pdf", result.get(MimeTypeAction.KEY));
      assertValidDetails(result);
      assertFalse(f + " is not recognized as unencrypted PDF", isEncrypted(result));
      assertFalse(f + " has no embedded documents", hasEmbeddedDocuments(result));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_PDF_testdoc_1_3_Karte() throws IOException {
    final Map<String, Object> result = ANALYZER.analyze(
        getClass().getResourceAsStream("pdf/normal/PDF-testdoc-1.3-Karte.pdf"));
    Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);

    assertThat(details, hasEntry("title", "karte2all.ai"));
    assertThat((Iterable<String>) details.get("metadata-keys"),
        hasItems("CreationDate", "Creator", "ModDate", "Producer", "Title"));
    assertThat(details, hasEntry("producer", "Adobe PDF library 5.00"));
    assertThat(details, hasEntry("creator", "Adobe Illustrator 10.0"));
    assertThat((String) details.get("metadata"),
        containsString("<pdf:CreationDate>2005-09-20T16:31:42+02:00</pdf:CreationDate>"));
    assertThat(((GregorianCalendar) details.get("creation-date")).getTime().getTime(), equalTo(1127226702000L));
  }

  @Test
  public void testPDFPortfolios() throws IOException {
    for (File f : nullSafe(PORTABLE_COLLECTION_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is recognized as PDF", "application/pdf", result.get(MimeTypeAction.KEY));
      assertValidDetails(result);
      assertFalse(f + " is not recognized as unencrypted PDF", isEncrypted(result));
      assertTrue(f + " has embedded documents, but none were found", hasEmbeddedDocuments(result));
    }
  }

  @Test
  public void testEncryptedPDFs() throws IOException {
    for (File f : nullSafe(ENCRYPTED_PDFS_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      System.out.println(f + ": -> \n" + result);

      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is recognized as PDF", "application/pdf", result.get(MimeTypeAction.KEY));
      assertValidDetails(result);
      assertTrue(f + " is not recognized as encrypted PDF", isEncrypted(result));
      assertFalse(f + " has no embedded documents", hasEmbeddedDocuments(result));
    }
  }


  public static File[] nullSafe(final File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }

  private static void assertValidDetails(final Map<String, Object> result) {
    assertTrue("No PDF details were found", result.containsKey(PDFMatcher.DETAILS_KEY));
    final Object object = result.get(PDFMatcher.DETAILS_KEY);
    assertTrue("PDF details are not a map, but " + object.getClass(), object instanceof Map);
  }

  @SuppressWarnings("unchecked")
  public static boolean isEncrypted(final Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    return details.containsKey(PDFMatcher.IS_ENCRYPTED_KEY) && (Boolean) details.get(PDFMatcher.IS_ENCRYPTED_KEY);
  }

  @SuppressWarnings("unchecked")
  private boolean hasEmbeddedDocuments(final Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    if (!details.containsKey(PDFMatcher.EMBEDDED_FILE_NAMES_KEY)) {
      return false;
    }
    ;
    assertTrue("PDF embedded file names is not a list",
        details.get(PDFMatcher.EMBEDDED_FILE_NAMES_KEY) instanceof List);

    return !((List<String>) details.get(PDFMatcher.EMBEDDED_FILE_NAMES_KEY)).isEmpty();
  }


}
