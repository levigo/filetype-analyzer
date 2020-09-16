import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestModcaAfp {

  private static Analyzer analyzer;

  @BeforeClass
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  public void testModcaStream() throws IOException {
    Map<String, Object> result = analyzer.analyze(getClass().getResourceAsStream("/modca/mod_mit_annotation.mod"));
    
    assertThat(result, hasEntry("extension", "mod"));
    assertThat((String) result.get("description"), startsWith("MODCA"));
    
    System.out.println(result);
  }
}
