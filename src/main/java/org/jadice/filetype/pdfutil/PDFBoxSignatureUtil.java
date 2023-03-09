package org.jadice.filetype.pdfutil;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.COSFilterInputStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.jadice.filetype.io.SeekableInputStream;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Signature utility class that uses PDFBox to analyse PDFs.
 */
public class PDFBoxSignatureUtil extends SignatureUtil {

  private PDFBoxSignatureUtil() {
    // static utility class
  }

  /**
   * Adds information about the given PDF document to the given map at the keys defined in {@link SignatureUtil}.
   *
   * @param pdfDetails pdf details map that should be enriched with PDF signature information
   * @param document   pdf document
   * @param pdfStream  pdf content stream
   */
  public static void addSignatureInfo(final Map<String, Object> pdfDetails, final PDDocument document, final SeekableInputStream pdfStream) {
    int counter = 0;
    try {
      List<Map<String, Object>> signatures = new ArrayList<>();
      for (final PDSignatureField signatureField : document.getSignatureFields()) {
        Map<String, Object> signatureDetails = new HashMap<>();
        signatures.add(signatureDetails);
        final PDSignature sig = signatureField.getSignature();
        long fileLength = getFileLength(pdfStream);

        // use a stream for the signed content to be able to process large files, too
        pdfStream.seek(0);
        try (final InputStream signedContent = new COSFilterInputStream(pdfStream, sig.getByteRange())) {

          final List<Integer> byteRange = Arrays.stream(sig.getByteRange()).boxed().collect(Collectors.toList());
          final byte[] contents = sig.getContents();

          signatureDetails.put(SIGNATURE_NUMBER_KEY, ++counter);
          signatureDetails.put(SIGNATURE_NAME_KEY, sig.getName());
          signatureDetails.put(SIGNATURE_DATE_KEY, sig.getSignDate());
          signatureDetails.put(SIGNATURE_CONTACT_INFO_KEY, sig.getContactInfo());
          signatureDetails.put(SIGNATURE_LOCATION_KEY, sig.getLocation());
          signatureDetails.put(SIGNATURE_REASON_KEY, sig.getReason());
          signatureDetails.put(SIGNATURE_DOCUMENT_COVERAGE_KEY, determineCoverageOfSignature(byteRange, contents.length, fileLength));
          signatureDetails.put(SIGNATURE_PAGE_KEY, determinePageOfSignature(signatureField, document));
          signatureDetails.put(SIGNATURE_FILTER_KEY, sig.getFilter());
          signatureDetails.put(SIGNATURE_SUB_FILTER_KEY, sig.getSubFilter());
          signatureDetails.put(SIGNATURE_VALIDITY, verifySignature(contents, signedContent, sig.getSubFilter(), sig.getSignDate(), getCertData(sig)));
        } catch (Exception e) {
          LOGGER.warn("Error occurred while analyzing signature.", e);
        }
      }
      if (counter > 0) {
        pdfDetails.put(IS_SIGNED_KEY, true);
        pdfDetails.put(SIGNATURE_DETAILS_KEY, signatures);
      } else
        pdfDetails.put(IS_SIGNED_KEY, false);
    } catch (Exception e) {
      LOGGER.warn("Failed to add signature information.", e);
    }
  }

  /**
   * Returns the bytes of the "Cert" entry of the signature's dictionary,
   * but only if subfilter is "adbe.x509.rsa_sha1". For other subfilters null is returned.
   * @param signature signature
   * @return bytes of the "Cert" entry or null
   */
  private static byte[] getCertData(final PDSignature signature) {
    try {
      if (PDSignature.SUBFILTER_ADBE_X509_RSA_SHA1.getName().equals(signature.getSubFilter())) {
        final COSString certString = (COSString) signature.getCOSObject().getDictionaryObject(COSName.CERT);
        return certString.getBytes();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Tries to determine the page of the given signature field. If this is not successful,
   * the cause for it is logged and null returned.
   * <p>
   * Inspired by <a href="https://stackoverflow.com/a/22132921/19199839">this</a>.
   *
   * @param signatureField signature field
   * @param document       document
   * @return 1-based page index of the given signature, or null of page could not be determined
   */
  public static Integer determinePageOfSignature(final PDSignatureField signatureField, final PDDocument document) {
    if (signatureField.getWidgets().size() != 1) {
      LOGGER.debug("Could not determine page of signature because " +
          "the given signature field does not contain exactly 1 widget.");
      return null;
    }

    final PDAnnotationWidget widget = signatureField.getWidgets().get(0);
    final COSDictionary widgetObject = widget.getCOSObject();
    final PDPageTree pages = document.getPages();

    // fast method
    final PDPage page = widget.getPage();
    if (page != null) {
      int pageIndex = pages.indexOf(page);
      if (pageIndex != -1)
        return pageIndex + 1;
    }

    // safe method
    try {
      for (int i = 0; i < pages.getCount(); i++) {
        for (PDAnnotation annotation : pages.get(i).getAnnotations()) {
          COSDictionary annotationObject = annotation.getCOSObject();
          if (annotationObject.equals(widgetObject))
            return i + 1;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error occurred while trying to determine page of signature.", e);
    }

    LOGGER.debug("Failed to determine page of signature with fast and safe method.");
    return null;
  }

}
