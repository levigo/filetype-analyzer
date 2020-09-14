package org.jadice.filetype;

import java.io.IOException;

import org.jadice.filetype.io.SeekableInputStream;

public class UncloseableSeekableInputStreamWrapper extends SeekableInputStream {

  private final SeekableInputStream delegate;

  private boolean locked = false;

  public UncloseableSeekableInputStreamWrapper(SeekableInputStream delegate) {
    this.delegate = delegate;
  }

  @Override
  public long getSizeEstimate() {
    return delegate.getSizeEstimate();
  }

  @Override
  public long length() throws IOException {
    return delegate.length();
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }

  @Override
  public void seek(long pos) throws IOException {
    delegate.seek(pos);
  }

  @Override
  public void close() throws IOException {
    if (!locked) {
      super.close();
      delegate.close();
    }
  }

  public boolean isCloseLocked() {
    return locked;
  }

  public void lockClose() {
    locked = true;
  }

  public void unlockClose() {
    locked = false;
  }

  @Override
  public long getStreamPosition() throws IOException {
    return delegate.getStreamPosition();
  }

}
