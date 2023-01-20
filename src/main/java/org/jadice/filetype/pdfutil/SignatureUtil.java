package org.jadice.filetype.pdfutil;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignatureUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtil.class);

  public static final String IS_SIGNED_KEY = "is-signed";

  public static final String SIGNATURE_DETAILS_KEY = "SIGNATURE_DETAILS";

  private SignatureUtil() {
    // static utility class
  }

  public static void addSignatureInfo(final Map<String, Object> pdfDetails, final PDDocument document, final long fileLen) {
    int counter = 0;
    try {
      List<Map<String,Object>> signatures = new ArrayList<>();
      for (PDSignatureField signatureField : document.getSignatureFields()) {
        final PDSignature sig = signatureField.getSignature();
        Integer page = null;
        if (signatureField.getWidgets().size() == 1) {
          page = determinePageOfSignature(document, signatureField.getWidgets().get(0));
        } else {
          LOGGER.warn("Failed to determine signature's page because PDSignatureField has more than one widget ({})!",
              signatureField.getWidgets().size());
        }
        final String coverage = determineSignatureCoverage(sig, fileLen);

        Map<String,Object> signatureDetails = new HashMap<>();
        signatureDetails.put("number", ++counter);
        signatureDetails.put("name", sig.getName());
        signatureDetails.put("date", sig.getSignDate());
        signatureDetails.put("contact-info", sig.getContactInfo());
        signatureDetails.put("location", sig.getLocation());
        signatureDetails.put("reason", sig.getReason());
        signatureDetails.put("document-coverage", coverage);
        signatureDetails.put("page", page);
        signatureDetails.put("sub-filter", sig.getSubFilter());
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
   * Determines the page of the given widget.
   *
   * @param document document
   * @param widget   widget
   * @return page of the given widget, starting with 1 or null of page could not be determined
   */
  private static Integer determinePageOfSignature(final PDDocument document, final PDAnnotationWidget widget) {
    final COSDictionary widgetObject = widget.getCOSObject();
    final PDPageTree pages = document.getPages();
    try {
      for (int i = 0; i < pages.getCount(); i++) {
        for (PDAnnotation annotation : pages.get(i).getAnnotations()) {
          COSDictionary annotationObject = annotation.getCOSObject();
          if (annotationObject.equals(widgetObject))
            return i + 1;
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to determine page of signature at first try.", e);
    }
    // second try to find out page
    final PDPage page = widget.getPage();
    return page != null ? pages.indexOf(page) + 1 : null;
  }

  /**
   * Determines whether the given signature covers the whole document or only parts of it.
   * In case of any error,  is returned.
   *
   * @param sig     signature
   * @param fileLen length of PDF
   * @return signature coverage
   */
  private static String determineSignatureCoverage(final PDSignature sig, final long fileLen) {
    int[] byteRange = sig.getByteRange();
    if (byteRange.length == 4) {
      long rangeMax = byteRange[2] + (long) byteRange[3];
      // multiply content length with 2 (because it is in hex in the PDF) and add 2 for < and >
      int contentLen = sig.getContents().length * 2 + 2;
      if (fileLen != rangeMax || byteRange[0] != 0 || byteRange[1] + contentLen != byteRange[2]) {
        // a false result doesn't necessarily mean that the PDF is a fake
        // see this answer why:
        // https://stackoverflow.com/a/48185913/535646
        return "PARTS";
      } else {
        return "WHOLE_DOCUMENT";
      }
    } else {
      LOGGER.warn("Signature byteRange did not have length 4, but {}!", byteRange.length);
      return "NOT_DETERMINED";
    }
  }

}
