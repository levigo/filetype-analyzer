import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.matchers.CSVMatcher;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCSVMatcher {


  private static Analyzer analyzer;

  private static final File INPUT_FOLDER = new File("src/test/resources/csv");
  private static final File INPUT_FOLDER_FALSE_POSITIVE = new File("src/test/resources/various_types");

  @BeforeClass
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  public void testCSVFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER.listFiles())) {
      final Map<String, Object> result = analyzer.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertEquals(f + " is not recognized as cvs", "text/csv", result.get(MimeTypeAction.KEY));
      assertEquals(f + " is not recognized as cvs", "UTF-8", result.get(CSVMatcher.CHARSET_KEY));
    }
  }

  @Test
  public void testNonCSVFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER_FALSE_POSITIVE.listFiles())) {
      final Map<String, Object> result = analyzer.analyze(f);
      assertNotNull(f + " could not be analyzed", result);
      assertNotEquals("false positive:  " + f + " is recognized as csv", "text/csv", result.get(MimeTypeAction.KEY));

    }
  }

  /**
   * This test is used to assure that by not recognizing the file's extension the algorithm still
   * works.
   */
  @Test
  public void testIncorrectFilename() throws IOException {
    File file = new File("src/test/resources/csv_incorrect/incorrectFilenamecsv");
    Map<String, Object> analyze = analyzer.analyze(file);
    assertNotNull(file + " could not be analyzed", analyze);
    // Tika at the moment cannot recognize this file as csv.
    assertEquals(file + " is not recognized as text/plain", "text/plain", analyze.get(MimeTypeAction.KEY));
  }

  public static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
}
