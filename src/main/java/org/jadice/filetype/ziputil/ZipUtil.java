package org.jadice.filetype.ziputil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jadice.filetype.io.SeekableInputStream;

import net.lingala.zip4j.ZipFile;

public class ZipUtil {

  private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

  private ZipUtil() {
    // utility class
  }

  /** Caller must close the returned {@link ZipFile} so the temporary ZIP file can be deleted. */
  public static ZipFile createZipFile(SeekableInputStream sis) throws IOException {
    final long fp = sis.getStreamPosition();
    final Path baseDir = TEMP_DIRECTORY.toPath();
    final Path tmpZip = Files.createTempFile(baseDir, "jadice-filetype-", ".zip");
    tmpZip.toFile().deleteOnExit();
    try (OutputStream os = new FileOutputStream(tmpZip.toFile())) {
      final byte[] buffer = new byte[128 * 1024];
      int read;
      while ((read = sis.read(buffer)) != -1) {
        os.write(buffer, 0, read);
      }
    } finally {
      sis.seek(fp);
    }
    return new AutoDeletingZipFile(tmpZip.toFile());
  }

  private static final class AutoDeletingZipFile extends ZipFile {
    private final File tmpFile;
    private AutoDeletingZipFile(File tmpFile) {
      super(tmpFile);
      this.tmpFile = tmpFile;
    }
    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        try {
          Files.deleteIfExists(tmpFile.toPath());
        } catch (IOException ignore) {
          // best-effort cleanup
        }
      }
    }
  }
}
