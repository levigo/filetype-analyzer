import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestHeicImages {

  private static Analyzer analyzer;

  private static final File INPUT_FOLDER = new File("src/test/resources/heic");
  private static final File INPUT_FOLDER_FALSE_POSITIVE = new File("src/test/resources/various_types");

  @BeforeAll
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testHeicFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER.listFiles())) {
      final Map<String, Object> result = analyzer.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertEquals("image/heic", result.get(MimeTypeAction.KEY), f + " is not recognized as heic");

    }
  }

  @Test
  void testNonHeicFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER_FALSE_POSITIVE.listFiles())) {
      final Map<String, Object> result = analyzer.analyze(f);
      assertNotNull(result, f + " could not be analyzed");
      assertNotEquals("image/heic", result.get(MimeTypeAction.KEY), "false positive:  " + f + " is recognized as heic");

    }
  }

  private static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
}
