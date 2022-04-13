import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestArchives {

  private static Analyzer analyzer;

  private static final File INPUT_FOLDER = new File("src/test/resources/archives");
  private static final File INPUT_FOLDER_FALSE_POSITIVE = new File("src/test/resources/various_types");
  private static final Map<String,String> expectedMimeTypes = new HashMap<>();

  static {
    expectedMimeTypes.put("encrypted.zip","application/zip;protection=encrypted");
    expectedMimeTypes.put("unencrypted.zip","application/zip");
  }

  @BeforeClass
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  public void testArchiveFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER.listFiles())) {
        final Map<String, Object> result = analyzer.analyze(f);
        assertNotNull(f + " could not be analyzed", result);
        assertEquals(f + " is not recognized as archive", expectedMimeTypes.get(f.getName()), result.get(MimeTypeAction.KEY));
    }
  }

  @Test
  public void testNonArchiveFiles() throws IOException {
    for (File f : nullSafe(INPUT_FOLDER_FALSE_POSITIVE.listFiles())) {
        final Map<String, Object> result = analyzer.analyze(f);
        assertNotNull(f + " could not be analyzed", result);
        assertNotEquals("false positive:  " + f + " is recognized as archive", "application/zip",
            result.get(MimeTypeAction.KEY));
    }
  }

  public static File[] nullSafe(File[] filesOrNull) {
    if (filesOrNull == null) {
      return new File[0];
    }
    return filesOrNull;
  }
}
