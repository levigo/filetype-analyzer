package org.jadice.filetype.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Stack;

import javax.imageio.IIOException;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

/**
 * This class defines an extended InputStream which is seekable.
 */
public abstract class SeekableInputStream extends InputStream implements ImageInputStream {
  private final Stack<Long> markByteStack = new Stack<>();
  private final Stack<Integer> markBitStack = new Stack<>();

  private boolean isClosed = false;

  // Length of the buffer used for readFully(type[], int, int)
  private static final int BYTE_BUF_LENGTH = 8192;

  // Byte buffer used for readFully(type[], int, int)
  private byte[] byteBuf;

  /**
   * The byte order of the stream as an instance of the enumeration class
   * <code>java.nio.ByteOrder</code>, where <code>ByteOrder.BIG_ENDIAN</code> indicates network byte
   * order and <code>ByteOrder.LITTLE_ENDIAN</code> indicates the reverse order. By default, the
   * value is <code>ByteOrder.BIG_ENDIAN</code>.
   */
  protected ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

  /**
   * The current bit offset within the stream. Subclasses are responsible for keeping this value
   * current from any method they override that alters the bit offset.
   */
  protected int bitOffset;

  /**
   * The position prior to which data may be discarded. Seeking to a smaller position is not
   * allowed. <code>flushedPos</code> will always be &gt;= 0.
   */
  protected long flushedPos = 0;

  private void checkByteBufInitialized() {
    if (null == byteBuf)
      byteBuf = new byte[BYTE_BUF_LENGTH];
  }

  /**
   * Throws an <code>IOException</code> if the stream has been closed. Subclasses may call this
   * method from any of their methods that require the stream not to be closed.
   * 
   * @exception IOException if the stream is closed.
   */
  protected final void checkClosed() throws IOException {
    if (isClosed) {
      throw new IOException("Attempt to read from closed stream");
    }
  }

  /**
   * Closes this seekable input stream. Further attempts to read from it will throw
   * {@link IOException}s. Additional attempts to {@link #close()}, however, have no effect.
   * 
   * @see java.io.InputStream#close()
   */
  @Override
  public void close() throws IOException {
    isClosed = true;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#flush()
   */
  @Override
  public void flush() throws IOException {
    flushBefore(getStreamPosition());
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#flushBefore(long)
   */
  @Override
  public void flushBefore(final long pos) throws IOException {
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    if (pos > getStreamPosition()) {
      throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
    }
    // Invariant: flushedPos >= 0
    flushedPos = pos;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#getBitOffset()
   */
  @Override
  public int getBitOffset() throws IOException {
    checkClosed();
    return bitOffset;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#getByteOrder()
   */
  @Override
  public ByteOrder getByteOrder() {
    return byteOrder;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#getFlushedPosition()
   */
  @Override
  public long getFlushedPosition() {
    return flushedPos;
  }

  /**
   * A rough estimated size for this stream instance, which is used for internal cache management.
   * 
   * @return an estimated size
   */
  public abstract long getSizeEstimate();

  /**
   * Default implementation returns false. Subclasses should override this if they cache data.
   * 
   * @see javax.imageio.stream.ImageInputStream#isCached()
   */
  @Override
  public boolean isCached() {
    return false;
  }

  /**
   * Default implementation returns false. Subclasses should override this if they cache data in a
   * temporary file.
   * 
   * @see javax.imageio.stream.ImageInputStream#isCachedFile()
   */
  @Override
  public boolean isCachedFile() {
    return false;
  }

  /**
   * Default implementation returns false. Subclasses should override this if they cache data in
   * main memory.
   * 
   * @see javax.imageio.stream.ImageInputStream#isCachedMemory()
   */
  @Override
  public boolean isCachedMemory() {
    return false;
  }

  /**
   * Returns the length of this stream contents, if available. Otherwise -1L will be returned.
   * 
   * @return the length, measured in bytes or -1L if unknown.
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public abstract long length() throws IOException;

  /**
   * Pushes the current stream position onto a stack of marked positions.
   */
  @Override
  public void mark() {
    try {
      markByteStack.push(getStreamPosition());
      markBitStack.push(getBitOffset());
    } catch (final IOException e) {
      // The API for ImageInputStream.mark() should have declared an IOException.
      // Since if is only thrown if the stream is closed, we ignore it here.
    }
  }

  @Override
  public synchronized void mark(final int readlimit) {
    mark();
  }

  /**
   * Reads a single byte from the stream and returns it as an <code>int</code> between 0 and 255. If
   * EOF is reached, <code>-1</code> is returned.
   * 
   * <p>
   * Subclasses must provide an implementation for this method. The subclass implementation should
   * update the stream position before exiting.
   * 
   * <p>
   * The bit offset within the stream must be reset to zero before the read occurs.
   * 
   * @return the value of the next byte in the stream, or <code>-1</code> if EOF is reached.
   * 
   * @exception IOException if the stream has been closed.
   */
  @Override
  public abstract int read() throws IOException;

  /**
   * A convenience method that calls <code>read(b, 0, b.length)</code>.
   * 
   * <p>
   * The bit offset within the stream is reset to zero before the read occurs.
   * 
   * @param b the byte buffer to fill up
   * @return the number of bytes actually read, or <code>-1</code> to indicate EOF.
   * 
   * @exception NullPointerException if <code>b</code> is <code>null</code>.
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public int read(final byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  /**
   * Reads up to <code>len</code> bytes from the stream, and stores them into <code>b</code>
   * starting at index <code>off</code>. If no bytes can be read because the end of the stream has
   * been reached, <code>-1</code> is returned.
   * 
   * <p>
   * The bit offset within the stream must be reset to zero before the read occurs.
   * 
   * <p>
   * Subclasses must provide an implementation for this method. The subclass implementation should
   * update the stream position before exiting.
   * 
   * @param b an array of bytes to be written to.
   * @param off the starting position within <code>b</code> to write to.
   * @param len the maximum number of bytes to read.
   * 
   * @return the number of bytes actually read, or <code>-1</code> to indicate EOF.
   * 
   * @exception IndexOutOfBoundsException if <code>off</code> is negative, <code>len</code> is
   *              negative, or <code>off +
   * len</code> is greater than <code>b.length</code>.
   * @exception NullPointerException if <code>b</code> is <code>null</code>.
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public abstract int read(byte[] b, int off, int len) throws IOException;

  /**
   * @see javax.imageio.stream.ImageInputStream#readBit()
   */
  @Override
  public int readBit() throws IOException {
    checkClosed();

    // Compute final bit offset before we call read() and seek()
    final int newBitOffset = getBitOffset() + 1 & 0x7;

    int val = read();
    if (val == -1) {
      throw new EOFException();
    }

    if (newBitOffset != 0) {
      // Move byte position back if in the middle of a byte
      seek(getStreamPosition() - 1);
      // Shift the bit to be read to the rightmost position
      val >>= 8 - newBitOffset;
    }
    setBitOffset(newBitOffset);

    return val & 0x1;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readBits(int)
   */
  @Override
  public long readBits(final int numBits) throws IOException {
    checkClosed();

    if (numBits < 0 || numBits > 64) {
      throw new IllegalArgumentException();
    }
    if (numBits == 0) {
      return 0L;
    }
    int biOff = getBitOffset();
    // Have to read additional bits on the left equal to the bit offset
    int bitsToRead = numBits + biOff;

    // Compute final bit offset before we call read() and seek()
    final int newBitOffset = biOff + numBits & 0x7;

    // Read a byte at a time, accumulate
    long accum = 0L;
    while (bitsToRead > 0) {
      final int val = read();
      if (val == -1) {
        throw new EOFException();
      }

      accum <<= 8;
      accum |= val;
      bitsToRead -= 8;
    }

    // Move byte position back if in the middle of a byte
    if (newBitOffset != 0) {
      seek(getStreamPosition() - 1);
    }
    setBitOffset(newBitOffset);

    // Shift away unwanted bits on the right.
    accum >>>= -bitsToRead; // Negative of bitsToRead == extra bits read

    // Mask out unwanted bits on the left
    accum &= -1L >>> 64 - numBits;

    return accum;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readBoolean()
   */
  @Override
  public boolean readBoolean() throws IOException {
    final int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch != 0;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readByte()
   */
  @Override
  public byte readByte() throws IOException {
    final int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (byte) ch;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readBytes(javax.imageio.stream.IIOByteBuffer, int)
   */
  @Override
  public void readBytes(final IIOByteBuffer buf, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException("len < 0!");
    }
    if (buf == null) {
      throw new NullPointerException("buf == null!");
    }

    final byte[] data = new byte[len];
    len = read(data, 0, len);

    buf.setData(data);
    buf.setOffset(0);
    buf.setLength(len);
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readChar()
   */
  @Override
  public char readChar() throws IOException {
    return (char) readShort();
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readDouble()
   */
  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFloat()
   */
  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(byte[])
   */
  @Override
  public void readFully(final byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(byte[], int, int)
   */
  @Override
  public void readFully(final byte[] b, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > b.length!");
    }

    while (len > 0) {
      final int nbytes = read(b, off, len);
      if (nbytes == -1)
        throw new EOFException();
      off += nbytes;
      len -= nbytes;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(char[], int, int)
   */
  @Override
  public void readFully(final char[] c, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > c.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > c.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 2);
      readFully(byteBuf, 0, nelts * 2);
      toChars(byteBuf, c, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(double[], int, int)
   */
  @Override
  public void readFully(final double[] d, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > d.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > d.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 8);
      readFully(byteBuf, 0, nelts * 8);
      toDoubles(byteBuf, d, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(float[], int, int)
   */
  @Override
  public void readFully(final float[] f, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > f.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > f.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 4);
      readFully(byteBuf, 0, nelts * 4);
      toFloats(byteBuf, f, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(int[], int, int)
   */
  @Override
  public void readFully(final int[] i, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 4);
      readFully(byteBuf, 0, nelts * 4);
      toInts(byteBuf, i, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(long[], int, int)
   */
  @Override
  public void readFully(final long[] l, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > l.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > l.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 8);
      readFully(byteBuf, 0, nelts * 8);
      toLongs(byteBuf, l, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readFully(short[], int, int)
   */
  @Override
  public void readFully(final short[] s, int off, int len) throws IOException {
    // Fix 4430357 - if off + len < 0, overflow occurred
    if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
    }

    checkByteBufInitialized();

    while (len > 0) {
      final int nelts = Math.min(len, byteBuf.length / 2);
      readFully(byteBuf, 0, nelts * 2);
      toShorts(byteBuf, s, off, nelts);
      off += nelts;
      len -= nelts;
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readInt()
   */
  @Override
  public int readInt() throws IOException {
    final int ch1 = this.read();
    final int ch2 = this.read();
    final int ch3 = this.read();
    final int ch4 = this.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }

    if (getByteOrder() == ByteOrder.BIG_ENDIAN) {
      return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    } else {
      return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }
  }

  /**
   * Reads <code>appliedBytes</code> bytes from the stream, and (conceptually) concatenates them
   * according to the current byte order and returns the result as an <code>int</code>. As
   * consequence, <code>appliedBytes</code> have to be greater than 0 and smaller or equals than 4.
   * All other values of <code>appliedBytes</code> will cause an IllegalArgumentException.
   * 
   * <p>
   * The bit offset within the stream is ignored and treated as though it were zero.
   * 
   * @param appliedBytes
   * @return a signed int value from the stream.
   * 
   * @exception EOFException if the stream reaches the end before reading all the bytes.
   * @exception IOException if an I/O error occurs.
   * 
   * @see #getByteOrder
   */
  public int readInt(final int appliedBytes) throws IOException {
    if (appliedBytes < 1 || appliedBytes > 4)
      throw new IllegalArgumentException("Applied bytes should be greater than 0 and smaller or eqals than 4.");

    int[] readBuffer = new int[appliedBytes];
    for (int i = 0; i < appliedBytes; i++) {
      readBuffer[i] = this.read();
      if (readBuffer[i] < 0) {
        throw new EOFException();
      }
    }

    if (getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
      int[] swapped = new int[appliedBytes];
      for (int i = 0; i < appliedBytes; i++) {
        swapped[appliedBytes - 1 - i] = readBuffer[i];
      }
      readBuffer = swapped;
    }

    int result = 0;
    for (int i = 0; i < appliedBytes; i++) {
      result += (readBuffer[i] & 0xFF) << (8 * (appliedBytes - i - 1));
    }

    if ((readBuffer[0] & 0x80) == 0x80) {
      int mask = 0;
      for (int i = 0; i < appliedBytes; i++) {
        mask += 0xFF << (8 * i);
      }
      // negative
      result = -((~result & mask) + 1);
    }

    return result;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readLine()
   */
  @Override
  public String readLine() throws IOException {
    final StringBuffer input = new StringBuffer();
    int c = -1;
    boolean eol = false;

    while (!eol) {
      switch (c = read()){
        case -1 :
        case '\n' :
          eol = true;
          break;
        case '\r' :
          eol = true;
          final long cur = getStreamPosition();
          if (read() != '\n') {
            seek(cur);
          }
          break;
        default :
          input.append((char) c);
          break;
      }
    }

    if (c == -1 && input.length() == 0) {
      return null;
    }
    return input.toString();
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readLong()
   */
  @Override
  public long readLong() throws IOException {
    final int i1 = readInt();
    final int i2 = readInt();

    if (getByteOrder() == ByteOrder.BIG_ENDIAN) {
      return ((long) i1 << 32) + (i2 & 0xFFFFFFFFL);
    } else {
      return ((long) i2 << 32) + (i1 & 0xFFFFFFFFL);
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readShort()
   */
  @Override
  public short readShort() throws IOException {
    final int ch1 = this.read();
    final int ch2 = this.read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }

    if (getByteOrder() == ByteOrder.BIG_ENDIAN) {
      return (short) ((ch1 << 8) + (ch2 << 0));
    } else {
      return (short) ((ch2 << 8) + (ch1 << 0));
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readUnsignedByte()
   */
  @Override
  public int readUnsignedByte() throws IOException {
    final int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readUnsignedInt()
   */
  @Override
  public long readUnsignedInt() throws IOException {
    return readInt() & 0xffffffffL;
  }

  /**
   * Reads <code>appliedBytes</code> bytes from the stream, and (conceptually) concatenates them
   * according to the current byte order, converts the result to a long, masks it with
   * <code>0xffffffffL</code> in order to strip off any sign-extension bits, and returns the result
   * as an unsigned <code>long</code> value. As consequence, <code>appliedBytes</code> have to be
   * greater than 0 and smaller or equals than 4. All other values of <code>appliedBytes</code> will
   * cause an IllegalArgumentException.
   * 
   * <p>
   * The bit offset within the stream is reset to zero before the read occurs.
   * 
   * @param appliedBytes
   * @return an unsigned int value from the stream, as a long.
   * 
   * @exception EOFException if the stream reaches the end before reading all the bytes.
   * @exception IOException if an I/O error occurs.
   * 
   * @see #getByteOrder
   */
  public long readUnsignedInt(final int appliedBytes) throws IOException {
    if (appliedBytes < 1 || appliedBytes > 4)
      throw new IllegalArgumentException("Applied bytes should be greater than 0 and smaller or eqals than 4.");

    final int[] readBuffer = new int[appliedBytes];
    for (int i = 0; i < readBuffer.length; i++) {
      readBuffer[i] = this.read();
      if (readBuffer[i] < 0) {
        throw new EOFException();
      }
    }
    int result = 0;
    if (getByteOrder() == ByteOrder.BIG_ENDIAN) {
      for (int i = 0; i < readBuffer.length; i++) {
        result += readBuffer[i] << (readBuffer.length - 1 - i) * 8;
      }
      return 0xFFFFFFFFL & result;
      // ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    } else {
      for (int i = readBuffer.length - 1; i >= 0; i--) {
        result += readBuffer[i] << i * 8;
      }
      return 0xFFFFFFFFL & result;
      // ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readUnsignedShort()
   */
  @Override
  public int readUnsignedShort() throws IOException {
    return readShort() & 0xffff;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#readUTF()
   */
  @Override
  public String readUTF() throws IOException {
    setBitOffset(0);

    // Fix 4494369: method ImageInputStreamImpl.readUTF()
    // does not work as specified (it should always assume
    // network byte order).
    final ByteOrder oldByteOrder = getByteOrder();
    setByteOrder(ByteOrder.BIG_ENDIAN);

    String ret;
    try {
      ret = DataInputStream.readUTF(this);
    } catch (final IOException e) {
      // Restore the old byte order even if an exception occurs
      setByteOrder(oldByteOrder);
      throw e;
    }

    setByteOrder(oldByteOrder);
    return ret;
  }

  /**
   * Resets the current stream byte and bit positions from the stack of marked positions.
   * 
   * <p>
   * An <code>IOException</code> will be thrown if the previous marked position lies in the
   * discarded portion of the stream.
   * 
   * @exception IOException if an I/O error occurs.
   */
  @Override
  public void reset() throws IOException {
    if (markByteStack.empty()) {
      return;
    }

    final long pos = markByteStack.pop().longValue();
    if (pos < flushedPos) {
      throw new IIOException("Previous marked position has been discarded!");
    }
    seek(pos);

    final int offset = markBitStack.pop().intValue();
    setBitOffset(offset);
  }

  /**
   * Resets the current stream byte, bit positions from the stack of marked positions, the flushed
   * position and seeks to <code>0</code>.
   * 
   * @exception IOException if an I/O error occurs.
   */
  public void resetToInitialState() throws IOException {
    markByteStack.clear();
    markBitStack.clear();
    flushedPos = 0;
    seek(0);
    setBitOffset(0);
  }

  /**
   * Sets the current stream position, measured from the beginning of this data stream, at which the
   * next read occurs. The offset may be set beyond the end of this data stream. Setting the offset
   * beyond the end does not change the data length, an <code>EOFException</code> will be thrown
   * only if a read is performed. The bit offset is set to 0.
   * 
   * <p>
   * An <code>IndexOutOfBoundsException</code> will be thrown if <code>pos</code> is smaller than
   * the flushed position (as returned by <code>getflushedPosition</code>).
   * 
   * <p>
   * It is legal to seek past the end of the file; an <code>EOFException</code> will be thrown only
   * if a read is performed.
   * 
   * @param pos a <code>long</code> containing the desired file pointer position.
   * 
   * @exception IndexOutOfBoundsException if <code>pos</code> is smaller than the flushed position.
   * @exception IOException if any other I/O error occurs.
   */
  @Override
  public abstract void seek(long pos) throws IOException;

  /**
   * @see javax.imageio.stream.ImageInputStream#setBitOffset(int)
   */
  @Override
  public void setBitOffset(final int bitOffset) throws IOException {
    checkClosed();
    if (bitOffset < 0 || bitOffset > 7) {
      throw new IllegalArgumentException("bitOffset must be betwwen 0 and 7!");
    }
    this.bitOffset = bitOffset;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#setByteOrder(java.nio.ByteOrder)
   */
  @Override
  public void setByteOrder(final ByteOrder byteOrder) {
    this.byteOrder = byteOrder;
  }

  @Override
  public long skip(final long n) throws IOException {
    return skipBytes(n);
  }

  /**
   * Advances the current stream position by calling <code>seek(getStreamPosition() + n)</code>.
   * 
   * <p>
   * The bit offset is reset to zero.
   * 
   * @param n the number of bytes to seek forward.
   * 
   * @return an <code>int</code> representing the number of bytes skipped.
   * 
   * @exception IOException if <code>getStreamPosition</code> throws an <code>IOException</code>
   *              when computing either the starting or ending position.
   */
  @Override
  public int skipBytes(final int n) throws IOException {
    final long pos = getStreamPosition();
    seek(pos + n);
    return (int) (getStreamPosition() - pos);
  }

  /**
   * Advances the current stream position by calling <code>seek(getStreamPosition() + n)</code>.
   * 
   * <p>
   * The bit offset is reset to zero.
   * 
   * @param n the number of bytes to seek forward.
   * 
   * @return a <code>long</code> representing the number of bytes skipped.
   * 
   * @exception IOException if <code>getStreamPosition</code> throws an <code>IOException</code>
   *              when computing either the starting or ending position.
   */
  @Override
  public long skipBytes(final long n) throws IOException {
    final long pos = getStreamPosition();
    seek(pos + n);
    return getStreamPosition() - pos;
  }

  private void toChars(final byte[] b, final char[] c, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        c[off + j] = (char) (b0 << 8 | b1);
        boff += 2;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 1];
        final int b1 = b[boff] & 0xff;
        c[off + j] = (char) (b0 << 8 | b1);
        boff += 2;
      }
    }
  }

  private void toDoubles(final byte[] b, final double[] d, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        final int b2 = b[boff + 2] & 0xff;
        final int b3 = b[boff + 3] & 0xff;
        final int b4 = b[boff + 4];
        final int b5 = b[boff + 5] & 0xff;
        final int b6 = b[boff + 6] & 0xff;
        final int b7 = b[boff + 7] & 0xff;

        final int i0 = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        final int i1 = b4 << 24 | b5 << 16 | b6 << 8 | b7;
        final long l = (long) i0 << 32 | i1 & 0xffffffffL;

        d[off + j] = Double.longBitsToDouble(l);
        boff += 8;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 7];
        final int b1 = b[boff + 6] & 0xff;
        final int b2 = b[boff + 5] & 0xff;
        final int b3 = b[boff + 4] & 0xff;
        final int b4 = b[boff + 3];
        final int b5 = b[boff + 2] & 0xff;
        final int b6 = b[boff + 1] & 0xff;
        final int b7 = b[boff] & 0xff;

        final int i0 = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        final int i1 = b4 << 24 | b5 << 16 | b6 << 8 | b7;
        final long l = (long) i0 << 32 | i1 & 0xffffffffL;

        d[off + j] = Double.longBitsToDouble(l);
        boff += 8;
      }
    }
  }

  private void toFloats(final byte[] b, final float[] f, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        final int b2 = b[boff + 2] & 0xff;
        final int b3 = b[boff + 3] & 0xff;
        final int i = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        f[off + j] = Float.intBitsToFloat(i);
        boff += 4;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 3];
        final int b1 = b[boff + 2] & 0xff;
        final int b2 = b[boff + 1] & 0xff;
        final int b3 = b[boff + 0] & 0xff;
        final int i = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        f[off + j] = Float.intBitsToFloat(i);
        boff += 4;
      }
    }
  }


  private void toInts(final byte[] b, final int[] i, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        final int b2 = b[boff + 2] & 0xff;
        final int b3 = b[boff + 3] & 0xff;
        i[off + j] = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        boff += 4;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 3];
        final int b1 = b[boff + 2] & 0xff;
        final int b2 = b[boff + 1] & 0xff;
        final int b3 = b[boff] & 0xff;
        i[off + j] = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        boff += 4;
      }
    }
  }

  private void toLongs(final byte[] b, final long[] l, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        final int b2 = b[boff + 2] & 0xff;
        final int b3 = b[boff + 3] & 0xff;
        final int b4 = b[boff + 4];
        final int b5 = b[boff + 5] & 0xff;
        final int b6 = b[boff + 6] & 0xff;
        final int b7 = b[boff + 7] & 0xff;

        final int i0 = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        final int i1 = b4 << 24 | b5 << 16 | b6 << 8 | b7;

        l[off + j] = (long) i0 << 32 | i1 & 0xffffffffL;
        boff += 8;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 7];
        final int b1 = b[boff + 6] & 0xff;
        final int b2 = b[boff + 5] & 0xff;
        final int b3 = b[boff + 4] & 0xff;
        final int b4 = b[boff + 3];
        final int b5 = b[boff + 2] & 0xff;
        final int b6 = b[boff + 1] & 0xff;
        final int b7 = b[boff] & 0xff;

        final int i0 = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        final int i1 = b4 << 24 | b5 << 16 | b6 << 8 | b7;

        l[off + j] = (long) i0 << 32 | i1 & 0xffffffffL;
        boff += 8;
      }
    }
  }

  private void toShorts(final byte[] b, final short[] s, final int off, final int len) {
    int boff = 0;
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff];
        final int b1 = b[boff + 1] & 0xff;
        s[off + j] = (short) (b0 << 8 | b1);
        boff += 2;
      }
    } else {
      for (int j = 0; j < len; j++) {
        final int b0 = b[boff + 1];
        final int b1 = b[boff] & 0xff;
        s[off + j] = (short) (b0 << 8 | b1);
        boff += 2;
      }
    }
  }
}
