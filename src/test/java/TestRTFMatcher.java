import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.matchers.RTFMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class TestRTFMatcher {

  private static final File INPUT_FOLDER = new File("src/test/resources/rtf");
  
  private static final File EMBEDDED_FILES_FOLDER = new File(INPUT_FOLDER, "embedded");
  
  private static final File NORMAL_FOLDER = new File(INPUT_FOLDER, "normal");
  
  private static Analyzer ANALYZER;
  
  @BeforeAll
  public static void init() throws AnalyzerException {
      ANALYZER = Analyzer.getInstance("/magic.xml");
  }
  
  @Test
  void testRtfEmbeddedFiles() throws IOException {
    for (File f : nullSafe(EMBEDDED_FILES_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertEquals("text/rtf", result.get(MimeTypeAction.KEY), f + " is recognized as RTF");
      assertValidDetails(result); 
      assertTrue(hasEmbeddedDocuments(result), f + " has embedded documents, but none were found");
    }
  }
  
  @Test
  void testNormalRtfFiles() throws IOException {
    for (File f : nullSafe(NORMAL_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertEquals("text/rtf", result.get(MimeTypeAction.KEY), f + " is recognized as RTF");
      assertValidDetails(result); 
      assertFalse(hasEmbeddedDocuments(result), f + " has no embedded documents, but some were found");
    }
  }
  
  private static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
  
  private static void assertValidDetails(Map<String, Object> result) {
    assertTrue(result.containsKey(RTFMatcher.DETAILS_KEY), "No RTF details were found");
    final Object object = result.get(RTFMatcher.DETAILS_KEY);
    assertTrue(object instanceof Map, "RTF details are not a map, but " + object.getClass());
  }

  @SuppressWarnings("unchecked")
  private boolean hasEmbeddedDocuments(Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(RTFMatcher.DETAILS_KEY);
    if (!details.containsKey(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY)) {
      return false;
    }
    assertTrue(details.get(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY) instanceof List, "RTF Files classes is not a list");
    
    return !((List<String>)details.get(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY)).isEmpty();
  }
}
