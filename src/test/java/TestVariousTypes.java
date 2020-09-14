import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.io.MemoryInputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestVariousTypes {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TestVariousTypes.class);
  
	private static Analyzer analyzer;

	@BeforeClass
	public static void createAnalyzer() throws AnalyzerException {
		analyzer = Analyzer.getInstance("/magic.xml");
	}

	@Test
	// FIXME: Unit-Test ohne Aussagekraft !!!
	public void testVariousTypes() throws IOException {
		final File files[] = new File("src/test/resources/various_types").listFiles(new FileFilter() {
			@Override
      public boolean accept(final File pathname) {
				return pathname.isFile() && pathname.canRead();
			}
		});

		for (final File file : files) {
			LOGGER.info("File: " + file);
			final Map<String, Object> results = analyzer.analyze(file);
			for (final Map.Entry<String, Object> e : results.entrySet()) 
				LOGGER.info("   " + e.getKey() + "=" + e.getValue());
			LOGGER.info("\n-------------------");
		}
	}

  @Test
  public void testEmptyStream() throws Exception {
    analyzer.analyze(new MemoryInputStream(new byte[0]));
  }
}
