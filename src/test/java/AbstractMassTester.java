import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jadice.filetype.Analyzer;
import org.jadice.filetype.AnalyzerException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public abstract class AbstractMassTester {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMassTester.class);
  
  // static method with @Parameters must be provided by non-abstract implementation!
  
  @Parameter (value = 0)
  public File testFile;
  
  @Parameter (value = 1)
  public Map<String, Object> expectations;
  
  @Parameter (value = 2)
  public String minVersion;
  
  static {
    try {
      analyzer = Analyzer.getInstance("/magic.xml");
    } catch (AnalyzerException e) {
      throw new RuntimeException(e);
    }
    
  }
  
  protected static Collection<Object[]> buildTestParam(final File folder, final Map<String, Object> expectations, final String minVersion) throws Exception {
    Collection<Object[]> data = new ArrayList<Object[]>();

    for (File f : traverseFolder(folder)) {
      data.add(new Object[] {f, expectations, minVersion});
    }
    return data;
  }

  private static Analyzer analyzer;

  private Map<String, Object> analyzerResult;

  public Map<String, Object> getAnalyzerResult() throws Exception {
    if (analyzerResult == null) {
      // Use the stream based method as jadice server also does
      analyzerResult = analyzer.analyze(new FileInputStream(testFile));
    }
    return analyzerResult;
  }
  
  private static List<File> traverseFolder(final File folder) throws Exception {
    LinkedList<File> result = new LinkedList<File>();
    traverseFolder(folder, result);
    return result;
  }

  private static void traverseFolder(final File folder, final List<File> result) throws Exception {
    if (!folder.canRead()) {
      LOGGER.warn("Cannot read " + folder.getAbsolutePath());
      return;
    }

    if (folder.isFile()) {
      result.add(folder);
    }

    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        traverseFolder(file, result);
      }
    }
  }
  
  @Before
  public void checkMinVersionMatches() {
    if (minVersion == null || minVersion.isEmpty()) {
      return;
    }
    String actualVersion = System.getProperty("maven.project.version");
    if (actualVersion == null) {
      LOGGER.warn("Could not determine artifact version. Running test anyway");
      return;
    }
    actualVersion = actualVersion.replaceAll("-(\\D*)$", ""); // remove non-Diget suffixes
    String[] splitMinVersion = minVersion.split("\\.");
    String[] splitActualVersion = actualVersion.split("\\.");
    try {
      for (int i = 0 ; i < Math.min(splitMinVersion.length, splitActualVersion.length) ; i++) {
        int minPart = Integer.parseInt(splitMinVersion[i]);
        int actPart = Integer.parseInt(splitActualVersion[i]);
        if (minPart < actPart) {
          LOGGER.debug("min version < actual version: Running test");
          return;
        } else if (minPart > actPart) {
          Assume.assumeTrue("Test case requires newer version " + minVersion, false);
          return;
        } else {
          assert minPart == actPart;
          LOGGER.debug("versions are identical in position " + i);
        }
      }
    } catch (NumberFormatException e) {
      LOGGER.error("Could not parse version number correctly. Running test anyway", e);
      return;
    }
  }
  
  @Test
  public void testResultAvailable() throws Exception {
    assertNotNull("No result available", getAnalyzerResult());
  }
  

  @Test
  public void testResults() throws Exception {
    for (Map.Entry<String, Object> expecation : expectations.entrySet()) {
      Assert.assertTrue("No " + expecation.getKey() + " detected", getAnalyzerResult().containsKey(expecation.getKey()));
      Assert.assertEquals("Wrong " + expecation.getKey() + " detected", expecation.getValue(), getAnalyzerResult().get(expecation.getKey()));
    }
  }

}
