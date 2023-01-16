import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestArchives {

  private static Analyzer analyzer;

  private static final File INPUT_FOLDER = new File("src/test/resources/archives");
  private static final File INPUT_FOLDER_FALSE_POSITIVE = new File("src/test/resources/various_types");
  private static final Map<String,String> expectedMimeTypes = new HashMap<>();

  static {
    expectedMimeTypes.put("encrypted.zip","application/zip;protection=encrypted");
    expectedMimeTypes.put("unencrypted.zip","application/zip");
    expectedMimeTypes.put("Re.Möller.zip","application/zip");
  }

  @BeforeAll
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testArchiveFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER.listFiles())) {
        final Map<String, Object> result = analyzer.analyze(f);
        assertNotNull(result, f + " could not be analyzed");
        assertEquals(expectedMimeTypes.get(f.getName()), result.get(MimeTypeAction.KEY), f + " is not recognized as archive");
    }
  }

  @Test
  void testNonArchiveFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER_FALSE_POSITIVE.listFiles())) {
        final Map<String, Object> result = analyzer.analyze(f);
        assertNotNull(result, f + " could not be analyzed");
        assertNotEquals("application/zip", result.get(MimeTypeAction.KEY),
            "false positive:  " + f + " is recognized as archive");
    }
  }

  public static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
}
