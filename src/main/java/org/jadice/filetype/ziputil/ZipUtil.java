package org.jadice.filetype.ziputil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

public class ZipUtil {

  private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

  public static ZipFile createZipFile(SeekableInputStream sis, Context ctx) throws IOException {

    final UUID uuid = UUID.randomUUID();
    final File tmpDir = new File(TEMP_DIRECTORY + File.separator + uuid);

    long fp = sis.getStreamPosition();
    LocalFileHeader localFileHeader;
    int readLen;
    byte[] readBuffer = new byte[4096];

    try (ZipInputStream zipInputStream = new ZipInputStream(sis)) {
      List<File> files = new ArrayList<>();
      while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
        if (localFileHeader != null && !localFileHeader.isDirectory()) {
          final File extractedFile = new File(tmpDir.getAbsolutePath() + File.separator + localFileHeader.getFileName());
          File parentFolder = new File(extractedFile.getParent());
          parentFolder.mkdirs();
          try (OutputStream outputStream = new FileOutputStream(extractedFile)) {
            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
              outputStream.write(readBuffer, 0, readLen);
            }
          }
          files.add(extractedFile);
        }
      }
      sis.seek(fp);
      final ZipFile zipFile = new ZipFile(uuid.toString());
      zipFile.addFolder(tmpDir);
      return zipFile;
    }
  }
}
