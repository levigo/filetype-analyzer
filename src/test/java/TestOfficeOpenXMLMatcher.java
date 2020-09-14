import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.database.OfficeOpenXMLMatcher;
import org.jadice.filetype.io.MemoryInputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestOfficeOpenXMLMatcher {

  private static final OfficeOpenXMLMatcher MATCHER = new OfficeOpenXMLMatcher();

  private static int blockSizeMemento;

  @BeforeClass
  public static void storeMemoryStreamBlocksize() {
    // [JS-1491] Enforce a MemoryInputStream with blocks that are smaller then TrueZIP will read
    blockSizeMemento = MemoryInputStream.getDefaultBlockSize();
    MemoryInputStream.setDefaultBlockSize(1024);
  }

  @AfterClass
  public static void restoreMemoryStreamBlocksize() {
    MemoryInputStream.setDefaultBlockSize(blockSizeMemento);
  }

  @Test
  public void testPPTXMatching() throws Exception {
    final Context ctx = createContext();
    final boolean matches = MATCHER.matches(ctx);

    assertTrue("Matcher must match the given file", matches);
    assertEquals("PPTX MIME Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ctx.getProperty(MimeTypeAction.KEY));
    assertEquals("PPTX file extension", "pptx", ctx.getProperty(ExtensionAction.KEY));
    assertEquals("PPTX MIME Type", "Microsoft PowerPoint 2007 Presentation", ctx.getProperty(DescriptionAction.KEY));

  }

  private static Context createContext() throws IOException {
    final InputStream is = TestOfficeOpenXMLMatcher.class.getResourceAsStream("/various_types/PowerPoint2007.pptx");
    return new Context(new MemoryInputStream(is), new HashMap<String, Object>(), null, Locale.ENGLISH);
  }
}
