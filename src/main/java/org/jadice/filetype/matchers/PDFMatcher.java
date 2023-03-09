package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.pdfutil.PDFBoxSignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Matcher} for PDF documents .
 * <p>
 * Caveat: for performance reasons, this should only be called from a context where the stream has
 * already be identified as a PDF file/stream.
 */
public class PDFMatcher extends Matcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFMatcher.class);

  public static final String PDF_MIME_TYPE = "application/pdf";

  public static final String DETAILS_KEY = "PDF_DETAILS";

  public static final String EMBEDDED_FILE_NAMES_KEY = "embedded-file-names";

  public static final String REQUIRES_USER_PASSWORD_KEY = "requires-user-password";

  public static final String SECURITY_HANDLER_KEY = "security-handler";

  public static final String IS_ENCRYPTED_KEY = "is-encrypted";

  public static final String METADATA_KEY = "metadata";

  public static final String VERSION_KEY = "version";

  public static final String NUMBER_OF_PAGES_KEY = "number-of-pages";

  public static final String CONTAINS_TEXT_KEY = "contains-text";
  public static final String TEXT_LENGTH_KEY = "text-length";
  public static final String TEXT_LENGTH_PER_PAGE_KEY = "text-length-per-page";

  /**
   * Most likely language of the text of the PDF, analyzed with <a href="https://github.com/pemistahl/lingua">lingua</a>
   */
  public static final String MOST_LIKELY_TEXT_LANGUAGE = "most-likely-text-language";
  /**
   * All possible languages of the PDF's text, sorted by their confidence value, analyzed with <a href="https://github.com/pemistahl/lingua">lingua</a>
   */
  public static final String TEXT_LANGUAGE_CONFIDENCE_VALUES = "text-language-confidence-values";

  private static boolean lookForText() {
    return "true".equalsIgnoreCase(System.getProperty(PDFMatcher.class.getName() + ".lookForText", "false"));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.levigo.jadice.filetype.database.Matcher#matches(com.levigo.jadice.filetype.Context)
   */
  @Override
  public boolean matches(final Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);
      try (PDDocument document = PDDocument.load(sis)) {
        context.setProperty(MimeTypeAction.KEY, PDF_MIME_TYPE);

        Map<String, Object> pdfDetails = new HashMap<String, Object>();
        context.setProperty(DETAILS_KEY, pdfDetails);

        pdfDetails.put(NUMBER_OF_PAGES_KEY, Integer.valueOf(document.getNumberOfPages()));

        PDDocumentInformation info = document.getDocumentInformation();
        if (null != info) {
          provideDocumentInformation(pdfDetails, info);
        }

        PDDocumentCatalog catalog = document.getDocumentCatalog();
        pdfDetails.put(VERSION_KEY, catalog.getVersion() != null ? catalog.getVersion() : "PDF-1.4");

        PDMetadata meta = catalog.getMetadata();
        if (null != meta) {
          provideXMPMetadata(pdfDetails, meta);
        }

        PDEncryption encryption = document.getEncryption();
        if (null != encryption) {
          pdfDetails.put(IS_ENCRYPTED_KEY, true);
          pdfDetails.put(SECURITY_HANDLER_KEY, encryption.getSecurityHandler().getClass().getSimpleName());
          pdfDetails.put(REQUIRES_USER_PASSWORD_KEY, encryption.getSecurityHandler().hasProtectionPolicy());
        } else
          pdfDetails.put(IS_ENCRYPTED_KEY, false);


        final List<String> filenames = new ArrayList<String>();

        PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());
        PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
        if (efTree != null) {
          extractFilesFromEFTree(efTree, filenames);
        }

        // extract files from page annotations
        for (PDPage page : document.getPages()) {
          extractFilesFromPage(page, filenames);
        }

        if (!filenames.isEmpty())
          pdfDetails.put(EMBEDDED_FILE_NAMES_KEY, filenames);

        long fileLength = -1; // won't be used if there are no signatures in the PDF
        if (document.getSignatureFields().size() > 0) {
          fileLength = getFileLength(sis);
        }
        PDFBoxSignatureUtil.addSignatureInfo(pdfDetails, document, fileLength);
        if (lookForText())
          addTextInfo(pdfDetails, document);
      }

      return true;
    } catch (IOException e) {
      LOGGER.warn("Failed to extract PDF details", e);
      return false;
    }
  }

  private void provideDocumentInformation(final Map<String, Object> pdfDetails, final PDDocumentInformation info) {
    pdfDetails.put("creator", info.getCreator());
    pdfDetails.put("author", info.getAuthor());
    pdfDetails.put("producer", info.getProducer());

    pdfDetails.put("creation-date", info.getCreationDate());
    pdfDetails.put("modification-date", info.getModificationDate());

    pdfDetails.put("keywords", info.getKeywords());
    pdfDetails.put("metadata-keys", info.getMetadataKeys());
    pdfDetails.put("subject", info.getSubject());
    pdfDetails.put("title", info.getTitle());
    pdfDetails.put("trapped", info.getTrapped());
  }

  private void provideXMPMetadata(final Map<String, Object> pdfDetails, final PDMetadata meta) throws IOException {
    InputStream xmpMetadata = meta.exportXMPMetadata();

    try {
      // Instantiate transformer input
      Source xmlInput = new StreamSource(xmpMetadata);
      StreamResult xmlOutput = new StreamResult(new StringWriter());

      // Configure transformer
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);

      pdfDetails.put(METADATA_KEY, xmlOutput.getWriter().toString());
    } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
      LOGGER.warn("Cannot provide XMP metadata", e);
    }
  }

  private static void extractFilesFromPage(final PDPage page, final List<String> filenames) throws IOException {
    for (PDAnnotation annotation : page.getAnnotations()) {
      if (annotation instanceof PDAnnotationFileAttachment) {
        PDAnnotationFileAttachment annotationFileAttachment = (PDAnnotationFileAttachment) annotation;
        PDFileSpecification fileSpec = annotationFileAttachment.getFile();
        if (fileSpec instanceof PDComplexFileSpecification) {
          PDComplexFileSpecification complexFileSpec = (PDComplexFileSpecification) fileSpec;
          PDEmbeddedFile embeddedFile = getEmbeddedFile(complexFileSpec);
          if (embeddedFile != null) {
            extractFile(filenames, complexFileSpec.getFilename(), embeddedFile);
          }
        }
      }
    }
  }

  private static void extractFilesFromEFTree(final PDEmbeddedFilesNameTreeNode efTree, final List<String> filenames)
      throws IOException {
    Map<String, PDComplexFileSpecification> names = efTree.getNames();
    if (names != null) {
      extractFiles(names, filenames);
    } else {
      List<PDNameTreeNode<PDComplexFileSpecification>> kids = efTree.getKids();
      for (PDNameTreeNode<PDComplexFileSpecification> node : kids) {
        names = node.getNames();
        extractFiles(names, filenames);
      }
    }
  }

  private static void extractFiles(final Map<String, PDComplexFileSpecification> names, final List<String> filenames)
      throws IOException {
    for (Entry<String, PDComplexFileSpecification> entry : names.entrySet()) {
      PDComplexFileSpecification fileSpec = entry.getValue();
      PDEmbeddedFile embeddedFile = getEmbeddedFile(fileSpec);
      if (embeddedFile != null) {
        extractFile(filenames, fileSpec.getFilename(), embeddedFile);
      }
    }
  }

  private static void extractFile(final List<String> filenames, final String filename,
                                  final PDEmbeddedFile embeddedFile) throws IOException {
    filenames.add(filename);
  }

  private static PDEmbeddedFile getEmbeddedFile(final PDComplexFileSpecification fileSpec) {
    // search for the first available alternative of the embedded file
    PDEmbeddedFile embeddedFile = null;
    if (fileSpec != null) {
      embeddedFile = fileSpec.getEmbeddedFileUnicode();
      if (embeddedFile == null) {
        embeddedFile = fileSpec.getEmbeddedFileDos();
      }
      if (embeddedFile == null) {
        embeddedFile = fileSpec.getEmbeddedFileMac();
      }
      if (embeddedFile == null) {
        embeddedFile = fileSpec.getEmbeddedFileUnix();
      }
      if (embeddedFile == null) {
        embeddedFile = fileSpec.getEmbeddedFile();
      }
    }
    return embeddedFile;
  }

  /**
   * Adds the following information to the result map:
   * <ul>
   *   <li>{@link #CONTAINS_TEXT_KEY} whether the whole document contains any text (without line breaks)</li>
   *   <li>{@link #TEXT_LENGTH_PER_PAGE_KEY} list of integers that indicate how long the text in each page is (only set if there is text at all)</li>
   *   <li>{@link #TEXT_LENGTH_KEY} length of the text of the whole document (only set if there is text at all)</li>
   *   <li>{@link #MOST_LIKELY_TEXT_LANGUAGE} detected language (only set if there is text at all)</li>
   *   <li>{@link #TEXT_LANGUAGE_CONFIDENCE_VALUES} map of all possible languages, sorted by their confidence value (only set if there is text at all)</li>
   * </ul>
   *
   * @param pdfDetails map to which the results get added
   * @param doc document
   */
  private static void addTextInfo(final Map<String, Object> pdfDetails, final PDDocument doc) throws IOException {
    boolean containsText = false;
    List<Integer> textLengthPerPages = new ArrayList<>();
    PDFTextStripper reader = new PDFTextStripper();
    for (int i = 1; i <= doc.getNumberOfPages(); i++) {
      reader.setStartPage(i);
      reader.setEndPage(i);
      final String pdfText = reader.getText(doc).replaceAll("([\\r\\n])", "");
      textLengthPerPages.add(pdfText.length());
      if (pdfText.length() > 0) {
        containsText = true;
      }
    }
    pdfDetails.put(CONTAINS_TEXT_KEY, containsText);
    if (containsText) {
      final String pdfText = new PDFTextStripper().getText(doc);
      pdfDetails.put(TEXT_LENGTH_PER_PAGE_KEY, textLengthPerPages);
      pdfDetails.put(TEXT_LENGTH_KEY, pdfText.replaceAll("([\\r\\n])", "").length());
      addLanguageInformation(pdfDetails, pdfText);
    }
  }

  /**
   * Adds information about the given text to the given map.
   * The most likely text language will be {@link com.github.pemistahl.lingua.api.Language#UNKNOWN} in case
   * language detection is not reliably possible.
   *
   * @param pdfDetails map to which the results get added
   * @param text text to analyze
   */
  public static void addLanguageInformation(final Map<String, Object> pdfDetails, final String text) {
    LanguageDetectorBuilder languageDetectorBuilder =
        LanguageDetectorBuilder
            .fromAllLanguages()
            .withMinimumRelativeDistance(0.1);
    if (text.length() > 120)
      languageDetectorBuilder.withLowAccuracyMode();
    final LanguageDetector languageDetector = languageDetectorBuilder.build();
    final long startTime = System.currentTimeMillis();
    pdfDetails.put(TEXT_LANGUAGE_CONFIDENCE_VALUES, languageDetector.computeLanguageConfidenceValues(text));
    pdfDetails.put(MOST_LIKELY_TEXT_LANGUAGE, languageDetector.detectLanguageOf(text).toString());
    LOGGER.debug("Language recognition took {} ms.", System.currentTimeMillis() - startTime);
  }

  /**
   * Reads the whole stream to determine the length of it.
   *
   * @param sis stream
   * @return length of given stream or -1 if any error occurred
   */
  private static long getFileLength(final SeekableInputStream sis) {
    try {
      sis.seek(0);
      int read = 0;
      final byte[] buffer = new byte[4096];
      do {
        synchronized (sis) { // perform synchronization inside while loop! See DOCPV-932
          read = sis.read(buffer);
        }
      } while (read != -1);

      // whole sis is read now
      return sis.length();
    } catch (Exception e) {
      LOGGER.warn("Failed to determine file length.", e);
      return -1;
    }
  }
}
