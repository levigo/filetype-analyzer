import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class TestBasicPDFMatcher {

  private static final File INPUT_FOLDER = new File("src/test/resources/pdf");

  private static final File UNENCRYPTED_PDFS_FOLDER = new File(INPUT_FOLDER, "normal");

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
    }
  }



  private static File[] nullSafe(final File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
}
