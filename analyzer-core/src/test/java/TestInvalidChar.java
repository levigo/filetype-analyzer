import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.RandomAccessFileInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestInvalidChar {
  private static Analyzer analyzer;


  @BeforeAll
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testInvalidCharInFilename() throws IOException {

    SeekableInputStream inStream = new RandomAccessFileInputStream(
        new File("src/test/resources/pdf/normal/lorem-ipsum.pdf"));

    Map<String, Object> result;
    result = analyzer.analyzeWithFilename(inStream, "cid:test.pdf");
    assertNotNull(result, inStream + " could not be analyzed");
    assertEquals("application/pdf", result.get(MimeTypeAction.KEY), inStream + " is not recognized as application/pdf");
  }


}
