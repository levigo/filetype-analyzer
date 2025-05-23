package org.jadice.filetype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEventLocator;

import opennlp.tools.util.model.UncloseableInputStream;
import org.apache.commons.io.FilenameUtils;
import org.jadice.filetype.database.Database;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.Type;
import org.jadice.filetype.io.MemoryInputStream;
import org.jadice.filetype.io.RandomAccessFileInputStream;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller class and main entry point for the filetype analyzer package.
 * 
 */
public class Analyzer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Analyzer.class);

  static final class LoggingEventHandler implements ValidationEventHandler {
    @Override
    public boolean handleEvent(final ValidationEvent event) {
      final ValidationEventLocator loc = event.getLocator();
      final String msg = "Validation event in line " + loc.getLineNumber() + " column "
          + event.getLocator().getColumnNumber() + ": " + event.getMessage();
      switch (event.getSeverity()){
        case ValidationEvent.WARNING :
          if (event.getLinkedException() == null) {
            LOGGER.warn(msg, event.getLinkedException());
          } else {
            LOGGER.warn(msg);
          }
          break;
        case ValidationEvent.ERROR :
        case ValidationEvent.FATAL_ERROR :
          if (event.getLinkedException() == null) {
            LOGGER.error(msg, event.getLinkedException());
          } else {
            LOGGER.error(msg);
          }
          break;

        default :
          if (event.getLinkedException() == null) {
            LOGGER.info(msg, event.getLinkedException());
          } else {
            LOGGER.info(msg);
          }
          break;
      }
      return true;
    }
  }

  private static final AnalysisListener DEFAULT_LISTENER = new AnalysisListener() {
    @Override
    public void warning(final Object src, final String message) {
      final Logger logger = (src == null) ? LOGGER : LoggerFactory.getLogger(src.getClass());
      logger.warn(message);
    }

    @Override
    public void info(final Object src, final String message) {
      // don't...
    }

    @Override
    public void error(final Object src, final String message, final Throwable cause) {
      final Logger logger = (src == null) ? LOGGER : LoggerFactory.getLogger(src.getClass());
      logger.error(message, cause);
    }
  };

  /**
   * The filetype database (usually read from a magic.xml definition).
   */
  private final Type database;

  private Locale locale = Locale.getDefault();

  public Analyzer(final Type database) {
    this.database = database;
  }


  /**
   * Create an {@link Analyzer} instance based on a magic.xml definition {@link Database} read from
   * a named classpath resource.
   * 
   * @param database classpath resource path, pointing to a XML {@link Database} definition
   * @return a configured {@link Analyzer} instance
   * @throws AnalyzerException if reading the XML {@link Database} failed
   */
  public static Analyzer getInstance(final String database) throws AnalyzerException {
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      return new Analyzer((Database) unmarshaller.unmarshal(Analyzer.class.getResource(database)));
    } catch (JAXBException e) {
      throw new AnalyzerException("Can't load magic database", e);
    }
  }

  /**
   * Create an {@link Analyzer} instance based on a magic.xml definition {@link Database} read from
   * a given {@link URL}.
   *
   * @param database a {@link URL} pointing to a XML {@link Database}
   * @return a configured {@link Analyzer} instance
   * @throws AnalyzerException if reading the XML {@link Database} failed
   */
  public static Analyzer getInstance(final URL database) throws AnalyzerException {
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      return new Analyzer((Database) unmarshaller.unmarshal(database));
    } catch (JAXBException e) {
      throw new AnalyzerException("Can't load magic database", e);
    }
  }

  /**
   * Create an {@link Analyzer} instance based on a magic.xml definition {@link Database} contained
   * in a file.
   *
   * @param database a {@link File} pointing to a XML {@link Database}
   * @return a configured {@link Analyzer} instance
   * @throws AnalyzerException if reading the XML {@link Database} failed
   */
  public static Analyzer getInstance(final File database) throws AnalyzerException {
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      return new Analyzer((Database) unmarshaller.unmarshal(database));
    } catch (JAXBException e) {
      throw new AnalyzerException("Can't load magic database", e);
    }
  }

  /**
   * Create an {@link Analyzer} instance based on a magic.xml definition {@link Database} read from
   * a stream.
   *
   * @param is an {@link InputStream} containing a XML {@link Database}
   * @return a configured {@link Analyzer} instance
   * @throws AnalyzerException if reading the XML {@link Database} failed
   */
  public static Analyzer getInstance(final InputStream is) throws AnalyzerException {
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      try {
        return new Analyzer((Database) unmarshaller.unmarshal(is));
      } finally {
        try {
          is.close();
        } catch (IOException e) {
          LOGGER.debug("Error when closing a stream", e);
        }
      }
    } catch (JAXBException e) {
      throw new AnalyzerException("Can't load magic database", e);
    }
  }

  private static Unmarshaller createUnmarshaller() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("org.jadice.filetype.database:org.jadice.filetype.matchers:org.jadice.filetype.matchers");

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    unmarshaller.setEventHandler(new LoggingEventHandler());

    return unmarshaller;
  }

  /**
   * Analyze the stream supplied via a {@link SeekableInputStream}.
   *
   * @deprecated Use analyze(final InputStream sis, final AnalysisListener listener) instead.
   * @param sis The data to analyze
   * @param listener an {@link AnalysisListener} to inform about the analysis progress. May be
   *          <code>null</code>.
   * @return analysis results.
   * @throws IOException if there is a problem accessing the input data.
   */
  @java.lang.SuppressWarnings("java:S1133")
  @Deprecated
  public Map<String, Object> analyze(final SeekableInputStream sis, final AnalysisListener listener) throws IOException {
    return analyze((InputStream) sis, listener);
  }

  /**
   * Analyze the stream supplied via a {@link SeekableInputStream}.
   * 
   * @param sis The data to analyze
   * @param listener an {@link AnalysisListener} to inform about the analysis progress. May be
   *          <code>null</code>.
   * @return analysis results.
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final InputStream sis, final AnalysisListener listener) throws IOException {

    return analyze(sis, listener, null);
  }

  /**
   * Analyze the stream supplied via a {@link SeekableInputStream}.
   *
   * @param is The data to analyze
   * @param listener an {@link AnalysisListener} to inform about the analysis progress. May be
   *          <code>null</code>.
   * @param fileName for the input
   * @return analysis results.
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final InputStream is, final AnalysisListener listener, final String fileName)
      throws IOException {
    Map<String, Object> result = new HashMap<>();


    // POI (3.1-Final) closes the stream during analyszs of office files - use an uncloseable stream wrapper
    final UncloseableInputStream uis = new UncloseableInputStream(is);
    final UncloseableSeekableInputStreamWrapper usis = new UncloseableSeekableInputStreamWrapper(new MemoryInputStream(uis));
    usis.lockClose(); // and don't unlock later as POI attempts to close asynchronously!

    final String sanitizedFileName = fileName != null ? fileName.replaceAll("[:\\\\/*?|<>]", "_") : null;
    String extension = FilenameUtils.getExtension(sanitizedFileName);

    Context ctx = new Context(usis, result, listener, locale, extension);

    database.analyze(ctx);

    Object obj = ctx.getProperty(DescriptionAction.KEY);
    if (null != obj && obj instanceof DescriptionAction.Description) {
      DescriptionAction.Description desc = (DescriptionAction.Description) obj;
      String s = desc.toString();
      ctx.setProperty(DescriptionAction.KEY, s);
    }

    return result;
  }

  /**
   * Analyze the stream supplied as a {@link File}. <br>
   * Caveat: the specified file will be accessed in a random-access fashion while during the
   * analysis and will therefore be locked (ro) on some systems.
   * 
   * @param file
   * @param listener an {@link AnalysisListener} to inform about the analysis progress. May be
   *          <code>null</code>.
   * @return a map of analysis results
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final File file, final AnalysisListener listener) throws IOException {
    SeekableInputStream sis = new RandomAccessFileInputStream(file);
    try {
      String fileName = file.getName();
      return analyze(sis, null, fileName);
    } finally {
      try {
        sis.close();
      } catch (IOException e) {
        listener.error(this, "Exception closing RandomAccessFileInputStream", e);
      }
    }
  }

  /**
   * Analyze the stream supplied via a {@link SeekableInputStream}.
   * 
   * @param sis
   * @return analysis results.
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final SeekableInputStream sis) throws IOException {
    return analyze(sis, DEFAULT_LISTENER, null);
  }


  public Map<String, Object> analyzeWithFilename(final SeekableInputStream sis,final String fileName) throws IOException {
    return analyze(sis, DEFAULT_LISTENER, fileName);
  }

  /**
   * Analyze the stream supplied via an {@link InputStream}. <br>
   * Caveat: the data will be buffered in memory. If you don't like this, supply a
   * {@link SeekableInputStream} implementation or a {@link File} instead.
   * 
   * @param is
   * @return a map of analysis results
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final InputStream is) throws IOException {
    return analyze(is, DEFAULT_LISTENER);
  }

  /**
   * Analyze the stream supplied as a {@link File}. <br>
   * Caveat: the specified file will be accessed in a random-access fashion while during the
   * analysis and will therefore be locked (ro) on some systems.
   * 
   * @param file
   * @return a map of analysis results
   * @throws IOException if there is a problem accessing the input data.
   */
  public Map<String, Object> analyze(final File file) throws IOException {
    return analyze(file, DEFAULT_LISTENER);
  }

  /**
   * Get the locale for which results will be generated.
   * 
   * @return the locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Set the locale for which results will be generated. This applies only to actions which are
   * locale-sensitive like the description.
   * 
   * @param locale
   */
  public void setLocale(final Locale locale) {
    this.locale = locale;
  }
}
