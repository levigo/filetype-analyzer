import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.database.RTFMatcher;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestRTFMatcher {

  private static final File INPUT_FOLDER = new File("src/test/resources/rtf");
  
  private static final File EMBEDDED_FILES_FOLDER = new File(INPUT_FOLDER, "embedded");
  
  private static final File NORMAL_FOLDER = new File(INPUT_FOLDER, "normal");
  
  private static Analyzer ANALYZER;
  
  @BeforeClass
  public static void init() throws AnalyzerException {
      ANALYZER = Analyzer.getInstance("/magic.xml");
  }
  
  @Test
  public void testRtfEmbeddedFiles() throws IOException {
    for (File f : nullSafe(EMBEDDED_FILES_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is recognized as RTF", "text/rtf", result.get(MimeTypeAction.KEY));
      assertValidDetails(result); 
      assertTrue(f + " has embedded documents, but none were found", hasEmbeddedDocuments(result));
    }
  }
  
  @Test
  public void testNormalRtfFiles() throws IOException {
    for (File f : nullSafe(NORMAL_FOLDER.listFiles())) {
      final Map<String, Object> result = ANALYZER.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is recognized as RTF", "text/rtf", result.get(MimeTypeAction.KEY));
      assertValidDetails(result); 
      assertFalse(f + " has no embedded documents, but some were found", hasEmbeddedDocuments(result));
    }
  }
  
  public static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
  
  private static void assertValidDetails(Map<String, Object> result) {
    assertTrue("No RTF details were found", result.containsKey(RTFMatcher.DETAILS_KEY));
    final Object object = result.get(RTFMatcher.DETAILS_KEY);
    assertTrue("RTF details are not a map, but " + object.getClass(), object instanceof Map);
  }

  @SuppressWarnings("unchecked")
  private boolean hasEmbeddedDocuments(Map<String, Object> result) {
    final Map<String, Object> details = (Map<String, Object>) result.get(RTFMatcher.DETAILS_KEY);
    if (!details.containsKey(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY)) {
      return false;
    };
    assertTrue("RTF Files classes is not a list", details.get(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY) instanceof List);
    
    return !((List<String>)details.get(RTFMatcher.EMBEDDED_FILE_CLASSES_KEY)).isEmpty();
  }
}
