import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.RandomAccessFileInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInvalidChar {
  private static Analyzer analyzer;


  @BeforeClass
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  public void testInvalidCharInFilename() throws IOException {

    SeekableInputStream inStream = new RandomAccessFileInputStream(
        new File("src/test/resources/pdf/normal/lorem-ipsum.pdf"));

    Map<String, Object> result;
    result = analyzer.analyzeWithFilename(inStream, "cid:test.pdf");
    assertNotNull(inStream + " could not be analyzed", result);
    assertEquals(inStream + " is not recognized as application/pdf", "application/pdf", result.get(MimeTypeAction.KEY));
  }


}
