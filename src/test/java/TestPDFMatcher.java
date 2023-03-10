import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.matchers.PDFMatcher;
import org.jadice.filetype.pdfutil.SignatureUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;


class TestPDFMatcher {

  private static final File INPUT_FOLDER = new File("src/test/resources/pdf");

  private static final File ENCRYPTED_PDFS_FOLDER = new File(INPUT_FOLDER, "encrypted");

  private static final File UNENCRYPTED_PDFS_FOLDER = new File(INPUT_FOLDER, "normal");

  private static final File PORTABLE_COLLECTION_FOLDER = new File(INPUT_FOLDER, "portfolio");

  private static Analyzer ANALYZER;

  @BeforeAll
  public static void init() throws AnalyzerException {
    ANALYZER = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testUnencryptedPDFs() throws IOException {
    for (File f : nullSafe(UNENCRYPTED_PDFS_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertEquals("application/pdf", result.get(MimeTypeAction.KEY),  f + " is recognized as PDF");
      assertValidDetails(result);
      assertFalse(isEncrypted(result), f + " is not recognized as unencrypted PDF");
      assertFalse(hasEmbeddedDocuments(result), f + " has no embedded documents");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void test_PDF_testdoc_1_3_Karte() throws IOException {
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
  void testPDFPortfolios() throws IOException {
    for (File f : nullSafe(PORTABLE_COLLECTION_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertEquals("application/pdf", result.get(MimeTypeAction.KEY), f + " is recognized as PDF");
      assertValidDetails(result);
      assertFalse(isEncrypted(result), f + " is not recognized as unencrypted PDF");
      assertTrue(hasEmbeddedDocuments(result), f + " has embedded documents, but none were found");
    }
  }

  @Test
  void testEncryptedPDFs() throws IOException {
    for (File f : nullSafe(ENCRYPTED_PDFS_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      System.out.println(f + ": -> \n" + result);

      assertNotNull(result, f + " could not be analyzed");
      assertEquals("application/pdf", result.get(MimeTypeAction.KEY), f + " is recognized as PDF");
      assertValidDetails(result);
      assertTrue(isEncrypted(result),f + " is not recognized as encrypted PDF");
      assertFalse(hasEmbeddedDocuments(result), f + " has no embedded documents");
    }
  }

  @Test
  void numberOfPagesEncryptedPdf() throws IOException {
    File f = new File(ENCRYPTED_PDFS_FOLDER, "11_enc128bit-aes_pw-owner.pdf");

    final Map<String, Object> result = ANALYZER.analyze(f);
    assertNotNull(result, f + " could not be analyzed");
    assertValidDetails(result);
    assertNumberOfPages(result, 1);
  }

  @Test
  void numberOfPagesUnencryptedPdf() throws IOException {
    File f = new File(UNENCRYPTED_PDFS_FOLDER, "lorem-ipsum.pdf");

    final Map<String, Object> result = ANALYZER.analyze(f);
    assertNotNull(result, f + " could not be analyzed");
    assertValidDetails(result);
    assertNumberOfPages(result, 4);
  }

  @Test
  void numberOfPagesPortableCollectionPdf() throws IOException {
    File f = new File(PORTABLE_COLLECTION_FOLDER, "portable-collection-1.pdf");

    final Map<String, Object> result = ANALYZER.analyze(f);
    assertNotNull(result, f + " could not be analyzed");
    assertValidDetails(result);
    assertNumberOfPages(result, 1);
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/pdf/signed.csv", numLinesToSkip = 1)
  void testSignedPDFs(final String urlString, final int expectedSignatureCount) throws IOException {
    final Map<String, Object> result = ANALYZER.analyze(new URL(urlString).openStream());
    assertNotNull(result);
    assertThat(result, hasKey(PDFMatcher.DETAILS_KEY));
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    assertThat(details, hasKey(SignatureUtil.IS_SIGNED_KEY));
    assertEquals(expectedSignatureCount != 0, details.get(SignatureUtil.IS_SIGNED_KEY));
    if (expectedSignatureCount != 0) {
      assertThat(details, hasKey(SignatureUtil.SIGNATURE_DETAILS_KEY));
      final Object signatureDetails = details.get(SignatureUtil.SIGNATURE_DETAILS_KEY);
      assertThat(signatureDetails, instanceOf(List.class));
      final List<Map<String,Object>> signatureList = (List<Map<String,Object>>) signatureDetails;
      assertThat(signatureList.size(), equalTo(expectedSignatureCount));
      for (Map<String, Object> signature : signatureList) {
        assertThat(signature, hasKey(SignatureUtil.SIGNATURE_DOCUMENT_COVERAGE_KEY));
        assertThat(signature, hasKey(SignatureUtil.SIGNATURE_PAGE_KEY));
        assertThat(signature, hasKey(SignatureUtil.SIGNATURE_NUMBER_KEY));
        assertThat(signature.get(SignatureUtil.SIGNATURE_PAGE_KEY), notNullValue());
        if (expectedSignatureCount == 1) {
          assertThat(signature.get(SignatureUtil.SIGNATURE_DOCUMENT_COVERAGE_KEY), equalTo("WHOLE_DOCUMENT"));
          assertThat(signature.get(SignatureUtil.SIGNATURE_NUMBER_KEY), equalTo(1));
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @CsvFileSource(resources = "/pdf/contains-text.csv", numLinesToSkip = 1)
  void testContainsText(final String filePath, final boolean expected, final String language) throws IOException {
    System.setProperty(PDFMatcher.class.getName() + ".languageCheck", "true");
    System.setProperty(PDFMatcher.class.getName() + ".lookForText", "true");
    Map<String, Object> result = ANALYZER.analyze(new File(filePath));
    assertNotNull(result);
    assertTrue(result.containsKey(PDFMatcher.DETAILS_KEY));
    Map<String, Object> pdfDetails = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    assertTrue(pdfDetails.containsKey(PDFMatcher.CONTAINS_TEXT_KEY));
    assertEquals(expected, pdfDetails.get(PDFMatcher.CONTAINS_TEXT_KEY));
    if (expected) {
      assertTrue(pdfDetails.containsKey(PDFMatcher.TEXT_LENGTH_KEY));
      final int totalTextLength = (int) pdfDetails.get(PDFMatcher.TEXT_LENGTH_KEY);
      assertTrue(totalTextLength > 0);
      assertTrue(pdfDetails.containsKey(PDFMatcher.TEXT_LENGTH_PER_PAGE_KEY));
      final List<Integer> textLengthPerPages = (List<Integer>) pdfDetails.get(PDFMatcher.TEXT_LENGTH_PER_PAGE_KEY);
      final int sum = textLengthPerPages.stream().mapToInt(Integer::intValue).sum();
      assertEquals(totalTextLength, sum);
      if (!language.equals("null")) {
        assertEquals(language, pdfDetails.get(PDFMatcher.MOST_LIKELY_TEXT_LANGUAGE));
      }
      assertTrue(pdfDetails.containsKey(PDFMatcher.TEXT_LANGUAGE_CONFIDENCE_VALUES));
    }
    System.clearProperty(PDFMatcher.class.getName() + ".languageCheck");
    System.clearProperty(PDFMatcher.class.getName() + ".lookForText");
  }

  private static File[] nullSafe(final File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }

  @SuppressWarnings("unchecked")
  private static void assertNumberOfPages(final Map<String, Object> result, final Integer expectedNumberOfPages) {
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    assertTrue(
        details.containsKey(PDFMatcher.NUMBER_OF_PAGES_KEY)
            && details.get(PDFMatcher.NUMBER_OF_PAGES_KEY) instanceof Integer,
        PDFMatcher.NUMBER_OF_PAGES_KEY + " not found");
    assertEquals(expectedNumberOfPages, details.get(PDFMatcher.NUMBER_OF_PAGES_KEY),
        PDFMatcher.NUMBER_OF_PAGES_KEY + " not correct");
  }

  private static void assertValidDetails(final Map<String, Object> result) {
    assertTrue(result.containsKey(PDFMatcher.DETAILS_KEY), "No PDF details were found");
    final Object object = result.get(PDFMatcher.DETAILS_KEY);
    assertTrue(object instanceof Map, "PDF details are not a map, but " + object.getClass());
  }

  @SuppressWarnings("unchecked")
  private static boolean isEncrypted(final Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    return details.containsKey(PDFMatcher.IS_ENCRYPTED_KEY) && (Boolean) details.get(PDFMatcher.IS_ENCRYPTED_KEY);
  }

  @SuppressWarnings("unchecked")
  private boolean hasEmbeddedDocuments(final Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(PDFMatcher.DETAILS_KEY);
    if (!details.containsKey(PDFMatcher.EMBEDDED_FILE_NAMES_KEY)) {
      return false;
    }
    assertTrue(details.get(PDFMatcher.EMBEDDED_FILE_NAMES_KEY) instanceof List,
            "PDF embedded file names is not a list");

    return !((List<String>) details.get(PDFMatcher.EMBEDDED_FILE_NAMES_KEY)).isEmpty();
  }


}
