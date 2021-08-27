package org.jadice.filetype.matchers;

import java.io.IOException;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVMatcher extends Matcher {

  public static final String CHARSET_KEY = "charset";

  private static final Logger LOGGER = LoggerFactory.getLogger(CSVMatcher.class);

  @Override
  public boolean matches(Context context) throws IOException {
    SeekableInputStream seekableInputStream = context.getStream();
    seekableInputStream.seek(0);
    // Step 1 get encoding
    final CharsetDetector charsetDetector = new CharsetDetector();
    charsetDetector.setText(seekableInputStream);
    final CharsetMatch match = charsetDetector.detect();
    final int confidence = match.getConfidence();
    final String charsetName = confidence >= 80 ? match.getName() : null;

    // Step 2 Mime-Type
    TikaConfig config = TikaConfig.getDefaultConfig();
    Detector detector = config.getDetector();
    seekableInputStream.seek(0);
    TikaInputStream stream = TikaInputStream.get(seekableInputStream);

    Metadata metadata = new Metadata();
    String extension = context.getStatedExtension();
    if (extension != null) {
      metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, "file." + extension);
    }
    MediaType mediaType = detector.detect(stream, metadata);
    LOGGER.info("MediaType detected: {}", mediaType);

    if (MediaType.text("csv").equals(mediaType)) {
      context.setProperty(MimeTypeAction.KEY, "text/csv");
      if (charsetName != null) {
        context.setProperty(CHARSET_KEY, charsetName);

      }
      context.setProperty(ExtensionAction.KEY, "csv");
      context.setProperty(DescriptionAction.KEY, "Comma-separated values (CSV)");
      return true;
    } else {
      LOGGER.warn("Error when matching a text file");
      return false;
    }
  }

}
