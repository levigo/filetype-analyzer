//package org.jadice.filetype.ziputil;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//
//import org.jadice.filetype.Context;
//import org.jadice.filetype.io.SeekableInputStream;
//
//import de.schlichtherle.truezip.rof.BufferedReadOnlyFile;
//import de.schlichtherle.truezip.rof.ReadOnlyFile;
//import de.schlichtherle.truezip.zip.ZipFile;
//
///**
// * Class that bridges the levigo's {@link SeekableInputStream} to a TrueZIP {@link ZipFile}.
// */
//public class ZipArchiveInput implements ReadOnlyFile {
//
//  private SeekableInputStream sis = null;
//
//  private long length = 0;
//
//  /**
//   * Use static factory method {@link #createZipFile(SeekableInputStream, Context)} instead.
//   *
//   * @param sis
//   * @throws IOException
//   */
//  private ZipArchiveInput(SeekableInputStream sis) throws IOException {
//    this.sis = sis;
//
//    synchronized (sis) {
//      long fp = sis.getStreamPosition();
//      byte[] data = new byte[4096];
//      int bytesRead;
//      while ((bytesRead = sis.read(data)) != -1) {
//        length += bytesRead;
//      }
//      sis.seek(fp);
//    }
//  }
//
//  public static ZipFile createZipFile(SeekableInputStream sis, Context ctx) throws IOException {
//    // [JS-1491] Buffer the ZipArchiveInput; else TrueZIP will mingle the resulting stream!
//    final ReadOnlyFile file = new BufferedReadOnlyFile(new ZipArchiveInput(sis));
//    try {
//      // Codepage 437 ist angeblich der Standard für ZIP
//      // siehe https://truezip.dev.java.net/#Introduction
//      return new ZipFile(file, Charset.forName("Cp437"), false, false);
//    } catch (UnsupportedEncodingException e) {
//      // fallback UTF-8 encoding
//      ctx.warning(ZipArchiveInput.class, "Charset Cp437 unsupported, falling back to UTF-8");
//      return new ZipFile(file, StandardCharsets.UTF_8, false, false);
//    }
//  }
//
//  @Override
//  public void close() throws IOException {
//    // ignore
//  }
//
//  @Override
//  public long getFilePointer() throws IOException {
//    return sis.getStreamPosition();
//  }
//
//  @Override
//  public long length() throws IOException {
//    return length;
//  }
//
//  @Override
//  public int read() throws IOException {
//    return sis.read();
//  }
//
//  @Override
//  public int read(byte[] arg0) throws IOException {
//    return sis.read(arg0);
//  }
//
//  @Override
//  public int read(byte[] arg0, int arg1, int arg2) throws IOException {
//    return sis.read(arg0, arg1, arg2);
//  }
//
//  @Override
//  public void readFully(byte[] arg0) throws IOException {
//    sis.readFully(arg0);
//  }
//
//  @Override
//  public void readFully(byte[] arg0, int arg1, int arg2) throws IOException {
//    sis.readFully(arg0, arg1, arg2);
//  }
//
//  @Override
//  public void seek(long arg0) throws IOException {
//    sis.seek(arg0);
//  }
//}
