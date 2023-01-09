import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestModcaAfp {

  private static Analyzer analyzer;

  @BeforeAll
  public static void createAnalyzer() throws AnalyzerException {
    analyzer = Analyzer.getInstance("/magic.xml");
  }

  @Test
  void testModcaStream() throws IOException {
    Map<String, Object> result = analyzer.analyze(getClass().getResourceAsStream("/modca/mod_mit_annotation.mod"));
    
    assertThat(result, hasEntry("extension", "mod"));
    assertThat((String) result.get("description"), startsWith("MODCA"));
    
    System.out.println(result);
  }
}
