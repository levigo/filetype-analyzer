import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.matchers.OfficeOpenXMLMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestOfficeOpenXMLMatcher {

  private static final OfficeOpenXMLMatcher MATCHER = new OfficeOpenXMLMatcher();

  private static int blockSizeMemento;

  @BeforeAll
  public static void storeMemoryStreamBlocksize() {
    // [JS-1491] Enforce a MemoryInputStream with blocks that are smaller than TrueZIP will read
    blockSizeMemento = MemoryInputStream.getDefaultBlockSize();
    MemoryInputStream.setDefaultBlockSize(1024);
  }

  @AfterAll
  public static void restoreMemoryStreamBlocksize() {
    MemoryInputStream.setDefaultBlockSize(blockSizeMemento);
  }

  @Test
  public void testPPTXMatching() throws Exception {
    final Context ctx = createContext("PowerPoint2007.pptx", false);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ctx.getProperty(MimeTypeAction.KEY), "PPTX MIME Type");
    assertEquals("pptx", ctx.getProperty(ExtensionAction.KEY), "PPTX file extension");
    assertEquals("Microsoft PowerPoint 2007 Presentation", ctx.getProperty(DescriptionAction.KEY), "PPTX MIME Type");

  }

  @Test
  public void testPPTXMatchingWithFileName() throws Exception {
    final Context ctx = createContext("PowerPoint2007.pptx", true);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ctx.getProperty(MimeTypeAction.KEY), "PPTX MIME Type");
    assertEquals("pptx", ctx.getProperty(ExtensionAction.KEY), "PPTX file extension");
    assertEquals("Microsoft PowerPoint 2007 Presentation", ctx.getProperty(DescriptionAction.KEY), "PPTX MIME Type");

  }

  private static Context createContext(String fileName, boolean withFileName) throws IOException {
    final InputStream is = TestOfficeOpenXMLMatcher.class.getResourceAsStream("/various_types/" + fileName);
    if (withFileName) {
      return new Context(new MemoryInputStream(is), new HashMap<String, Object>(), null, Locale.ENGLISH, fileName);
    } else {
      return new Context(new MemoryInputStream(is), new HashMap<String, Object>(), null, Locale.ENGLISH, null);
    }
  }
}
