package org.jadice.filetype.pdfutil;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for signature util classes that declares the map keys
 * for subclasses and implements default generic functions.
 */
public abstract class SignatureUtil {

  protected static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtil.class);

  public static final String IS_SIGNED_KEY = "is-signed";

  public static final String SIGNATURE_DETAILS_KEY = "SIGNATURE_DETAILS";

  public static final String SIGNATURE_NUMBER_KEY = "number";
  public static final String SIGNATURE_NAME_KEY = "name";
  public static final String SIGNATURE_DATE_KEY = "date";
  public static final String SIGNATURE_CONTACT_INFO_KEY = "contact-info";
  public static final String SIGNATURE_LOCATION_KEY = "location";
  public static final String SIGNATURE_REASON_KEY = "reason";
  public static final String SIGNATURE_DOCUMENT_COVERAGE_KEY = "document-coverage";
  public static final String SIGNATURE_PAGE_KEY = "page";
  public static final String SIGNATURE_SUB_FILTER_KEY = "sub-filter";

  /**
   * Determines whether the given signature covers the whole
   * document ("WHOLE_DOCUMENT") or only parts of it ("PARTS").
   * Requires that the given byteRange array has length 4. If that's not the
   * case, "NOT_DETERMINED" is returned.
   * In case of any error, "NOT_DETERMINED" is returned.
   * <p>
   * This is inspired by <a href="https://github.com/apache/pdfbox/blob/535a7755a4b98bf225261ac58090e7c324e09b78/examples/src/main/java/org/apache/pdfbox/examples/signature/ShowSignature.java#L277">ShowSignature.java</a>.
   * <p>
   * <a href="https://stackoverflow.com/a/48185913/535646">Here</a> is explained why "PARTS" as result doesn't necessarily mean that the PDF is a fake.
   *
   * @param byteRange byterange integer array, e.g. retrieved by {@link PDSignature#getByteRange()}
   * @param signatureLength length of the signature's content, e.g. retrieved by the length attribute of {@link PDSignature#getContents()}
   * @param fileLen length of PDF file
   * @return signature coverage
   */
  public static String determineCoverageOfSignature(int[] byteRange, final int signatureLength, final long fileLen) {
    if (byteRange.length != 4) {
      return "NOT_DETERMINED";
    }
    long rangeMax = byteRange[2] + (long) byteRange[3];
    // multiply content length with 2 (because it is in hex in the PDF) and add 2 for < and >
    int contentLen = signatureLength * 2 + 2;
    if (fileLen != rangeMax || byteRange[0] != 0 || byteRange[1] + contentLen != byteRange[2]) {
      return "PARTS";
    } else {
      return "WHOLE_DOCUMENT";
    }
  }

}
