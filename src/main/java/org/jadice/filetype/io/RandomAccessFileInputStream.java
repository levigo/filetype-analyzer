package org.jadice.filetype.io;

import static java.lang.Math.min;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.PhantomReference;

import org.jadice.filetype.io.PhantomReferenceSweeper.CleanupCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Implementation of the {@link org.jadice.filetype.io.SeekableInputStream} which is based
 * on a locally accessible file.
 */
public class RandomAccessFileInputStream extends SeekableInputStream {

  private static Logger logger = LoggerFactory.getLogger(RandomAccessFileInputStream.class);

  private RandomAccessFile file;

  /**
   * The read-ahead buffer
   */
  private byte buffer[];
  private long bufferBase = Long.MIN_VALUE;
  private long bufferTop = Long.MIN_VALUE;

  /**
   * The current position in partial stream
   */
  private long position = 0;

  private PhantomReference<? extends Object> reference;

  /**
   * Creates a seekable file access stream
   * 
   * @param file
   * @throws IOException
   */
  public RandomAccessFileInputStream(final File file) throws IOException {
    this.file = new RandomAccessFile(file, "r");
    setReadAhead(32);
    reference = PhantomReferenceSweeper.getInstance().register(this, new CleanupCloseable(this.file));
  }

  /**
   * @see org.jadice.filetype.io.SeekableInputStream#getStreamPosition()
   */
  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return position;
  }

  /**
   * Returns the length of this stream contents.
   * <p>
   * The length will be determined by a call on the underlying {@link RandomAccessFile}. It is to be
   * noted that a call of {@link RandomAccessFile#length()} is not thread safe. If this method
   * should be used in multithreaded situations, the developer has to perform an appropriate
   * synchronisation in order to achieve the desired result.
   * 
   * @return the length, measured in bytes or -1L if unknown.
   * @exception IOException if an I/O error occurs or the streams has been already closed.
   * 
   * @see org.jadice.filetype.io.SeekableInputStream#length()
   */
  @Override
  public long length() throws IOException {
    checkClosed();
    return file.length();
  }

  /**
   * @see org.jadice.filetype.io.SeekableInputStream#seek(long)
   */
  @Override
  public void seek(final long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos)
      throw new IndexOutOfBoundsException("pos < flushedPos!");

    position = pos;
    bitOffset = 0;
  }


  /**
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    checkClosed();

    // can we satisfy the request from the read-ahead buffer?
    if (position >= bufferBase && position < bufferTop)
      return buffer[(int) (position++ - bufferBase)] & 0xff;

    bitOffset = 0;
    if (position != file.getFilePointer())
      file.seek(position);

    // fill the read-ahead buffer, unless there is nothing left or even the original
    // request could not be satisfied at this time.
    final int r = file.read(buffer, 0, buffer.length);
    if (r > 0) {
      bufferBase = position;
      bufferTop = position + r;
      position++;
      return buffer[0] & 0xff;
    }

    return -1;
  }

  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read(final byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;

    // can we satisfy the request (or at least some of it) from the read-ahead buffer?
    int read = 0;
    if (position >= bufferBase && position < bufferTop) {
      read = (int) min(bufferTop - position, len);
      System.arraycopy(buffer, (int) (position - bufferBase), b, off, read);
      position += read;

      // all of it?
      if (read == len)
        return read;

      len -= read;
      off += read;
    }

    if (position != file.getFilePointer())
      file.seek(position);

    // first satisfy the destination buffer
    int r = file.read(b, off, len);
    if (r > 0) {
      position += r;
      read += r;

      // then fill the read-ahead buffer, unless there is nothing left or even the original
      // request could not be satisfied at this time.
      if (r > 0) {
        r = file.read(buffer, 0, buffer.length);
        if (r > 0) {
          bufferBase = position;
          bufferTop = bufferBase + r;
        }
      }
    } else if (read == 0) {
      return -1;
    }
    return read;
  }

  /*
   * @see java.io.InputStream#close()
   */
  @Override
  public void close() throws IOException {
    try {
      super.close();
    } finally {
      /* Process independent of result of close() in superclass */
      if (null != file) {
        try {
          file.close();
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
        } finally {
          PhantomReferenceSweeper.getInstance().unregister(reference);
          reference = null;
          file = null;
        }
      }
    }
  }

  @Override
  public long getSizeEstimate() {
    try {
      return (int) file.length();
    } catch (final IOException e) {
      return -1;
    }
  }

  /**
   * @return the readAhead
   */
  public int getReadAhead() {
    return buffer.length;
  }

  /**
   * @param readAhead the readAhead to set
   */
  public void setReadAhead(final int readAhead) {
    buffer = new byte[readAhead];
  }

}
