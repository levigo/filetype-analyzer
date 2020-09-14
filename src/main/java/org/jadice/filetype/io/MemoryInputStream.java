package org.jadice.filetype.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * A SeekableInputStream which decorates a plain InputStream with seekability by using an in-memory
 * cache.
 *
 * <b>Configurable:</b> jadice.io.memoryInputStream.blocksize integer 2048 The block size to use
 * when buffering input data in memory.
 */
public class MemoryInputStream extends SeekableInputStream {

  private static int defaultBlockSize;

  private int blockSize = 2048;
  private InputStream sourceInputStream;
  private long sourceStreamPosition = 0;
  private int currentBlockPosition = 0; // position within the last block
  private final List<byte[]> cachedBlocks;
  private long targetStreamPosition = 0;
  private long streamLength = -1L;

  /**
   * Constructs a new MemoryInputStream which wraps the given InputStream and uses the given block
   * size. The first block of data will be pre-fetched within this constructor call.
   *
   * @param is the input stream
   * @param blockSize the used data block size in bytes. Caused by performance and memory reasons
   *          this value should be a multiple of 1024 greater than 1MB. If this value is smaller
   *          than 1024 bytes, a minimal block size of 1024 will be assumed.
   * @throws IOException in case of problems during pre fetching
   */
  public MemoryInputStream(final InputStream is, final int blockSize) throws IOException {
    this(is,blockSize,true);
  }  
  
  /**
   * Constructs a new MemoryInputStream which wraps the given InputStream and uses the given block
   * size.
   *
   * @param is the input stream
   * @param blockSize the used data block size in bytes. Caused by performance and memory reasons
   *          this value should be a multiple of 1024 greater than 1MB. If this value is smaller
   *          than 1024 bytes, a minimal block size of 1024 will be assumed.
   * @param forcePrefetch whether the first block of data should be pre-fetched or not.
   * @throws IOException in case of problems during pre-fetching
   */
  public MemoryInputStream(final InputStream is, final int blockSize, final boolean forcePrefetch) throws IOException {
    if (is == null) {
      throw new IllegalArgumentException("source input stream must not be null");
    }
    sourceInputStream = is;

    this.blockSize = Math.max(1024, blockSize);

    cachedBlocks = new ArrayList<>();
    
 // Read the first block;
    if (forcePrefetch)
      fill(); 
  }

  /*
   * In contrast to the constructor above we don't limit the block size to >= 1024 bytes.
   * This constructor is for unit test use only
   * DO NOT MAKE THIS METHOD PUBLIC! 
   */
  MemoryInputStream(final InputStream is, final int blockSize, final int bogusParameter) throws IOException {
    if (is == null)
      throw new IllegalArgumentException("source input stream must not be null");

    sourceInputStream = is;

    // in contrast to the constructor above we don't limit the block size to >= 1024 bytes.
    this.blockSize = blockSize;

    cachedBlocks = new ArrayList<>();
    fill(); // Read the first block;
  }

  /**
   * Constructs a new MemoryInputStream which wraps the given InputStream and uses the default block
   * size of 2048 bytes.
   *
   * @param is the stream to wrap
   * @throws IOException in case of problems during pre-fetching
   */
  public MemoryInputStream(final InputStream is) throws IOException {
    this(is, getDefaultBlockSize());
  }

  /**
   * Construct a new MemoryInputStream which reads from exactly one data block.
   *
   * @param data the data block to read from
   */
  public MemoryInputStream(final byte[] data) {
    cachedBlocks = new LinkedList<>();
    cachedBlocks.add(data);
    sourceStreamPosition = data.length;
    blockSize = data.length;
    currentBlockPosition = data.length;
    streamLength = data.length;
  }

  @Override
  public int read(final byte b[]) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(final byte buffer[], final int offset, final int length) throws IOException {
    checkClosed();
    bitOffset = 0;

    if (offset < 0 || length < 0 || offset + length > buffer.length || offset + length < 0)
      throw new IndexOutOfBoundsException();

    // try to fill up the cache in order to satisfy the request
    while (targetStreamPosition >= sourceStreamPosition)
      if (!fill())
        return -1; // not enough data in the input stream

    // if there wasn't enough data, just read less
    int lengthToRead = length;
    if (targetStreamPosition + length > sourceStreamPosition)
      lengthToRead = (int) (sourceStreamPosition - targetStreamPosition);

    // if the position is outside the range, return -1
    if (lengthToRead <= 0)
      return -1;

    // find the first block to read from
    final int readBlockIndex = (int) (targetStreamPosition / blockSize);

    // assert that the block is there. should not happen.
    if (readBlockIndex >= cachedBlocks.size())
      throw new IOException("Internal error in MemoryInputStream");

    final int positionWithinBlock = (int) (targetStreamPosition - readBlockIndex * blockSize);

    // decide whether we are reading from the last (current) block
    if (readBlockIndex == cachedBlocks.size() - 1) {
      // constrain lengthToRead
      if (lengthToRead + positionWithinBlock > currentBlockPosition)
        lengthToRead = currentBlockPosition - positionWithinBlock;
    } else {
      // constrain lengthToRead
      if (lengthToRead + positionWithinBlock > blockSize)
        lengthToRead = blockSize - positionWithinBlock;
    }

    // should not happen
    if (lengthToRead < 0)
      throw new IOException("Internal error in MemoryInputStream");

    System.arraycopy(cachedBlocks.get(readBlockIndex), positionWithinBlock, buffer, offset, lengthToRead);

    targetStreamPosition += lengthToRead;

    return lengthToRead;
  }

  /**
   * Fills up the cache from the source stream.
   *
   * @return flag indicating whether filling up process was successful
   * @throws IOException
   */
  private boolean fill() throws IOException {
    checkClosed();

    if (sourceInputStream == null)
      return false;

    // check if there is room left in the last block
    if (cachedBlocks.size() == 0 || currentBlockPosition >= blockSize) {
      // make room by adding a block
      cachedBlocks.add(new byte[blockSize]);
      currentBlockPosition = 0;
    }

    // fill up into last block
    final byte currentBlock[] = cachedBlocks.get(cachedBlocks.size() - 1);
    final int read = sourceInputStream.read(currentBlock, currentBlockPosition, blockSize - currentBlockPosition);
    if (read < 0) {
      sourceInputStream.close();
      sourceInputStream = null;
      streamLength = sourceStreamPosition;
      return false;
    }

    sourceStreamPosition += read;
    currentBlockPosition += read;
    return true;
  }

  @Override
  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;

    // try to fill up the cache in order to satisfy the request
    while (targetStreamPosition >= sourceStreamPosition)
      if (!fill())
        return -1; // not enough data in the input stream

    // find the block to read from
    final int readBlockIndex = (int) (targetStreamPosition / blockSize);

    // assert that the block is there. should not happen.
    if (readBlockIndex >= cachedBlocks.size())
      throw new IOException("Internal error in MemoryInputStream");

    final int positionWithinBlock = (int) (targetStreamPosition - readBlockIndex * blockSize);

    // decide whether we have enough data
    if (readBlockIndex == cachedBlocks.size() - 1 && positionWithinBlock >= currentBlockPosition
        || positionWithinBlock >= blockSize)
      return -1;

    targetStreamPosition++;

    return cachedBlocks.get(readBlockIndex)[positionWithinBlock] & 0xff;
  }

  @Override
  public void seek(final long position) throws IOException {
    checkClosed();

    if (position < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;
    while (position >= sourceStreamPosition)
      if (!fill())
        break;
    targetStreamPosition = position;
  }

  @Override
  public void close() throws IOException {
    super.close();
    if (null != sourceInputStream) {
      sourceInputStream.close();
      sourceInputStream = null;
    }
    cachedBlocks.clear();
  }

  @Override
  public int available() throws IOException {
    checkClosed();

    if (null == sourceInputStream)
      return 0;

    final int available = sourceInputStream.available();
    return (int) Math.max(available, sourceStreamPosition);
  }

  @Override
  public long getStreamPosition() throws IOException {
    return targetStreamPosition;
  }

  @Override
  public long length() throws IOException {
    return streamLength;
  }

  /**
   * Returns <code>true</code> since this <code>ImageInputStream</code> caches data in order to
   * allow seeking backwards.
   *
   * @return <code>true</code>.
   *
   * @see #isCachedMemory
   * @see #isCachedFile
   */
  @Override
  public boolean isCached() {
    return true;
  }

  /**
   * Returns <code>false</code> since this <code>ImageInputStream</code> does not maintain a file
   * cache.
   *
   * @return <code>false</code>.
   *
   * @see #isCached
   * @see #isCachedMemory
   */
  @Override
  public boolean isCachedFile() {
    return false;
  }

  /**
   * Returns <code>true</code> since this <code>ImageInputStream</code> maintains a main memory
   * cache.
   *
   * @return <code>true</code>.
   *
   * @see #isCached
   * @see #isCachedFile
   */
  @Override
  public boolean isCachedMemory() {
    return true;
  }

  @Override
  public long getSizeEstimate() {
    return streamLength > 0 ? (int) streamLength : (int) targetStreamPosition;
  }

  public static void setDefaultBlockSize(final int defaultBlockSize) {
    MemoryInputStream.defaultBlockSize = defaultBlockSize;
  }

  public static int getDefaultBlockSize() {
    return defaultBlockSize;
  }
}