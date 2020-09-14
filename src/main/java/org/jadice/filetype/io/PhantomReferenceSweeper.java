package org.jadice.filetype.io;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomReferenceSweeper {

  private static final PhantomReferenceSweeper INSTANCE = new PhantomReferenceSweeper();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

  private final Map<PhantomReference<? extends Object>, Runnable> references = new ConcurrentHashMap<>();

  private PhantomReferenceSweeper() {
    SweeperThread sweeperThread = new SweeperThread();
    sweeperThread.setDaemon(true);
    sweeperThread.setPriority(Thread.NORM_PRIORITY + 1);
    sweeperThread.setName(SweeperThread.class.getSimpleName());
    sweeperThread.start();
  }

  public static PhantomReferenceSweeper getInstance() {
    return INSTANCE;
  }

  public <T extends Object> PhantomReference<T> register(final T referent, final Runnable sweepCode) {
    final PhantomReference<T> reference = new PhantomReference<>(referent, referenceQueue);
    references.put(reference, sweepCode);
    return reference;
  }

  public void unregister(final PhantomReference<? extends Object> reference) {
    if (reference != null) {
      /* Just skip if no reference is passed*/
      references.remove(reference);
      reference.clear();
    }
  }

  private class SweeperThread extends Thread {
    @Override
    public void run() {
      while (true) {
        try {
          /* Process reference queue permanently */
          while (true) {
            /* Thread ist ony active when a reference can be retrieved form ReferenceQueue */
            Reference<?> reference = referenceQueue.remove();
            if (reference != null) {
              final Runnable runnable = references.remove(reference);
              if (runnable != null) {
                try {
                  runnable.run();
                  if (logger.isWarnEnabled()) {
                    logger.debug("Cleanup in finalization required for: " + runnable);
                  }
                } catch (RuntimeException e) {
                  logger.error("Error invoking cleanup method for: " + runnable, e);
                }
              }
            }
          }
        } catch (InterruptedException e) {
          /* Reestablish state */
          /*
           * According to "Java Concurrency in Practice Chapter 7.1.3: Responding to Interruption"
           */
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public static class CleanupCloseable implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CleanupCloseable.class);
    
    private final Closeable closeable;

    public CleanupCloseable(final Closeable closeable) {
      this.closeable = closeable;
    }

    @Override
    public void run() {
      try {
        closeable.close();
      } catch (IOException e) {
        logger.warn("Exception in cleanup processing: " + closeable, e);
      }
    }

    @Override
    /* Delegates to method on closeable object */
    public String toString() {
      return closeable.toString();
    }
  }

}