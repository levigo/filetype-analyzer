package org.jadice.filetype.pdfutil;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

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
   * @param pdfDetails pdf details map that should be enriched with PDF signature information
   * @param document pdf document
   * @param fileLen length of pdf document
   */
  public static void addSignatureInfo(final Map<String, Object> pdfDetails, final PDDocument document, final long fileLen) {
    int counter = 0;
    try {
      List<Map<String,Object>> signatures = new ArrayList<>();
      for (final PDSignatureField signatureField : document.getSignatureFields()) {
        final PDSignature sig = signatureField.getSignature();

        final List<Integer> byteRange = Arrays.stream(sig.getByteRange()).boxed().collect(Collectors.toList());

        Map<String,Object> signatureDetails = new HashMap<>();
        signatureDetails.put(SIGNATURE_NUMBER_KEY, ++counter);
        signatureDetails.put(SIGNATURE_NAME_KEY, sig.getName());
        signatureDetails.put(SIGNATURE_DATE_KEY, sig.getSignDate());
        signatureDetails.put(SIGNATURE_CONTACT_INFO_KEY, sig.getContactInfo());
        signatureDetails.put(SIGNATURE_LOCATION_KEY, sig.getLocation());
        signatureDetails.put(SIGNATURE_REASON_KEY, sig.getReason());
        signatureDetails.put(SIGNATURE_DOCUMENT_COVERAGE_KEY, determineCoverageOfSignature(byteRange, sig.getContents().length, fileLen));
        signatureDetails.put(SIGNATURE_PAGE_KEY, determinePageOfSignature(signatureField, document));
        signatureDetails.put(SIGNATURE_SUB_FILTER_KEY, sig.getSubFilter());
        signatures.add(signatureDetails);
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
   * Tries to determine the page of the given signature field. If this is not successful,
   * the cause for it is logged and null returned.
   * <p>
   * Inspired by <a href="https://stackoverflow.com/a/22132921/19199839">this</a>.
   *
   * @param signatureField signature field
   * @param document document
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
