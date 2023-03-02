package org.jadice.filetype.pdfutil;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
  public static final String SIGNATURE_FILTER_KEY = "filter";
  public static final String SIGNATURE_SUB_FILTER_KEY = "sub-filter";
  public static final String SIGNATURE_VALIDITY = "validity";

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
   * @param byteRange       byterange integer List, e.g. retrieved by {@link PDSignature#getByteRange()} and casted to List
   * @param signatureLength length of the signature's content, e.g. retrieved by the length attribute of {@link PDSignature#getContents()}
   * @param fileLen         length of PDF file
   * @return signature coverage
   */
  public static String determineCoverageOfSignature(List<Integer> byteRange, final int signatureLength, final long fileLen) {
    if (byteRange.size() != 4) {
      return "NOT_DETERMINED";
    }
    long rangeMax = byteRange.get(2) + (long) byteRange.get(3);
    // multiply content length with 2 (because it is in hex in the PDF) and add 2 for < and >
    int contentLen = signatureLength * 2 + 2;
    if (fileLen != rangeMax || byteRange.get(0) != 0 || byteRange.get(1) + contentLen != byteRange.get(2)) {
      return "PARTS";
    } else {
      return "WHOLE_DOCUMENT";
    }
  }

  /**
   * Reads the whole stream to determine the length of it.
   *
   * @param sis stream
   * @return length of given stream or -1 if any error occurred
   */
  protected static long getFileLength(final SeekableInputStream sis) {
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

  /**
   * @param contents      the /Contents field as a COSString
   * @param signedContent the byte sequence that has been signed
   * @param subFilter     the subfilter of the signature
   * @param signDate      the sign date of the signature
   * @return string message which indicates whether the signature could be validated
   */
  public static String verifySignature(final byte[] contents, final InputStream signedContent, final String subFilter,
                                       final Calendar signDate) {
    try {
      if (subFilter == null) return "Could not be validated. Missing subfilter.";
      switch (subFilter) {
        case "adbe.pkcs7.detached":
        case "ETSI.CAdES.detached":
          return verifyPKCS7(contents, signedContent, signDate);
        case "adbe.pkcs7.sha1":
//          CertificateFactory factory = CertificateFactory.getInstance("X.509");
//          ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
//          Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
//          System.out.println("certs=" + certs);
          @SuppressWarnings({"squid:S5542", "lgtm [java/weak-cryptographic-algorithm]"})
          MessageDigest md = MessageDigest.getInstance("SHA1");
          try (DigestInputStream dis = new DigestInputStream(signedContent, md)) {
            while (dis.read() != -1) {
            }
          }
          byte[] hash = md.digest();
          return verifyPKCS7(contents, new ByteArrayInputStream(hash), signDate);
        case "adbe.x509.rsa.sha1":
        case "adbe.x509.rsa_sha1":
          // TODO
          return String.format("Could not be validated. SubFilter %s not yet supported.", subFilter);
        case "ETSI.RFC3161":
          return verifyETSIdotRFC3161(signedContent, contents);
        default:
          return String.format("Could not be validated. Unknown subfilter %s.", subFilter);
      }
    } catch (Exception e) {
      LOGGER.warn("Error occurred while verification of signature.", e);
      return "Could not be validated, error occurred: " + e.getMessage();
    }
  }

  /**
   * Verifies that the pkcs7 signature is valid.
   * Inspired by: <a href="https://github.com/mkl-public/testarea-pdfbox2/blob/master/src/test/java/mkl/testarea/pdfbox2/sign/ValidateSignature.java#L198">...</a>
   * TODO: Could be extended to more complex validation, like here: <a href="https://github.com/apache/pdfbox/blob/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/ShowSignature.java">...</a>
   *
   * @param contents      the /Contents field as a COSString
   * @param signedContent the byte sequence that has been signed
   * @param signDate      the sign date of the signature
   * @return string message which indicates whether the signature could be validated
   */
  public static String verifyPKCS7(final byte[] contents, final InputStream signedContent, final Calendar signDate) {
    try {
      final CMSSignedData cms = new CMSSignedData(new CMSProcessableInputStream(signedContent), contents);
      final SignerInformation signerInfo = cms.getSignerInfos().getSigners().iterator().next();
      X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID())
          .iterator().next();
      SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(cert);

      return signerInfo.verify(verifier) ? "valid" : "Could not be validated";
    } catch (Exception e) {
      LOGGER.warn("Error occurred while verification of signature.", e);
      return "Could not be validated, error occurred: " + e.getMessage();
    }
  }

  /**
   * Verify ETSI.RFC3161 TimeStampToken
   * Copied from: <a href="https://github.com/apache/pdfbox/blob/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/ShowSignature.java">...</a>
   *
   * @param contents      the /Contents field as a COSString
   * @param signedContent the byte sequence that has been signed
   * @return string message which indicates whether the signature could be validated
   */
  public static String verifyETSIdotRFC3161(InputStream signedContent, byte[] contents)
      throws CMSException, NoSuchAlgorithmException, IOException, TSPException, CertificateException {
    TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents));
    TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

    String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
    // compare the hash of the signed content with the hash in the timestamp
    MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
    try (DigestInputStream dis = new DigestInputStream(signedContent, md)) {
      while (dis.read() != -1) {
      }
    }
    return Arrays.equals(md.digest(), timeStampInfo.getMessageImprintDigest()) ? "valid" : "Could not be validated";

//    CertificateFactory factory = CertificateFactory.getInstance("X.509");
//    ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
//    Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
//    System.out.println("certs=" + certs);
//
//    X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
//    SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
//    SigUtils.validateTimestampToken(timeStampToken);
//    SigUtils.verifyCertificateChain(timeStampToken.getCertificates(),
//        certFromTimeStamp,
//        timeStampInfo.getGenTime());
  }

}
