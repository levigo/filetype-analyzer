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
import org.jadice.filetype.matchers.OpenDocumentMatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestOpenDocumentMatcher {

    private static final OpenDocumentMatcher MATCHER = new OpenDocumentMatcher();

    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of("OOoWriter.odt", "odt", "application/vnd.oasis.opendocument.text", "OpenDocument Writer"),
                Arguments.of("Draw001.odg", "odg", "application/vnd.oasis.opendocument.graphics",
                        "OpenDocument Drawing"));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void testODMatching(final String fileName, final String extension, final String mimeType, final String description) throws Exception {
        final Context ctx = createContext(fileName, false);
        final boolean matches = MATCHER.matches(ctx);

        assertTrue(matches, "Matcher must match the given file");
        assertEquals(extension, ctx.getProperty(ExtensionAction.KEY), "file extension");
        assertEquals(mimeType, ctx.getProperty(MimeTypeAction.KEY), "MIME Type");
        assertEquals(description, ctx.getProperty(DescriptionAction.KEY), "description");

    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void testODMatchingWithFileName(final String fileName, final String extension, final String mimeType, final String description) throws Exception {
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
