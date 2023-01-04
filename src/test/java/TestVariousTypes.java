import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
      for (final Map.Entry<String, Object> e : results.entrySet())
        LOGGER.info("   " + e.getKey() + "=" + e.getValue());
      LOGGER.info("\n-------------------");
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
}
