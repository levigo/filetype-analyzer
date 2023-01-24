import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.matchers.OfficeOpenXMLMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestOfficeOpenXMLMatcher {

  private static final OfficeOpenXMLMatcher MATCHER = new OfficeOpenXMLMatcher();

  @Test
  void testPPTXMatching() throws Exception {
    final Context ctx = createContext("PowerPoint2007.pptx", false);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ctx.getProperty(MimeTypeAction.KEY), "PPTX MIME Type");
    assertEquals("pptx", ctx.getProperty(ExtensionAction.KEY), "PPTX file extension");
    assertEquals("Microsoft PowerPoint 2007 Presentation", ctx.getProperty(DescriptionAction.KEY), "PPTX MIME Type");

  }

  @Test
  void testPPTXMatchingWithFileName() throws Exception {
    final Context ctx = createContext("PowerPoint2007.pptx", true);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ctx.getProperty(MimeTypeAction.KEY), "PPTX MIME Type");
    assertEquals("pptx", ctx.getProperty(ExtensionAction.KEY), "PPTX file extension");
    assertEquals("Microsoft PowerPoint 2007 Presentation", ctx.getProperty(DescriptionAction.KEY), "PPTX MIME Type");

  }

  private static Stream<Arguments> provideTestData() {
    return Stream.of(
        Arguments.of("Visio_drawing.vsdx", "vsdx", "application/vnd.ms-visio.drawing", "Microsoft Visio 2013 Drawing"),
        Arguments.of("Visio_drawing_macro-enabled.vsdm", "vsdm", "application/vnd.ms-visio.drawing.macroEnabled.12",
            "Microsoft Visio 2013 macro-enabled Drawing"),
        Arguments.of("Visio_stencil.vssx", "vssx", "application/vnd.ms-visio.stencil", "Microsoft Visio 2013 Stencil"),
        Arguments.of("Visio_stencil_macro-enabled.vssm", "vssm", "application/vnd.ms-visio.stencil.macroEnabled.12",
            "Microsoft Visio 2013 macro-enabled Stencil"),
        Arguments.of("Visio_template.vstx", "vstx", "application/vnd.ms-visio.template",
            "Microsoft Visio 2013 Template"),
        Arguments.of("Visio_template_macro-enabled.vstm", "vstm", "application/vnd.ms-visio.template.macroEnabled.12",
            "Microsoft Visio 2013 macro-enabled Template"));
  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  void testVSDXMatching(final String fileName, final String extension, final String mimeType, final String description) throws Exception {
    final Context ctx = createContext(fileName, false);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals(extension, ctx.getProperty(ExtensionAction.KEY), "file extension");
    assertEquals(mimeType, ctx.getProperty(MimeTypeAction.KEY), "MIME Type");
    assertEquals(description, ctx.getProperty(DescriptionAction.KEY), "description");

  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  void testVSDXMatchingWithFileName(final String fileName, final String extension, final String mimeType, final String description) throws Exception {
    final Context ctx = createContext(fileName, true);
    final boolean matches = MATCHER.matches(ctx);

    assertTrue(matches, "Matcher must match the given file");
    assertEquals(extension, ctx.getProperty(ExtensionAction.KEY), "file extension");
    assertEquals(mimeType, ctx.getProperty(MimeTypeAction.KEY), "MIME Type");
    assertEquals(description, ctx.getProperty(DescriptionAction.KEY), "description");

  }

  private static Context createContext(String fileName, boolean withFileName) throws IOException {
    final InputStream is = TestOfficeOpenXMLMatcher.class.getResourceAsStream("/various_types/" + fileName);
    if (withFileName) {
      return new Context(new MemoryInputStream(is), new HashMap<>(), null, Locale.ENGLISH, fileName);
    } else {
      return new Context(new MemoryInputStream(is), new HashMap<>(), null, Locale.ENGLISH, null);
    }
  }
}
