package org.jadice.filetype.matchers;

import java.io.IOException;
import java.security.Security;
import java.util.Base64;

import org.bouncycastle.cms.CMSSignedData;
import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;

public class PKCS7Matcher extends Matcher {

  @Override
  public boolean matches(final Context context) throws IOException {
    SeekableInputStream sis = context.getStream();
    return isPKCS7(sis, context.getStatedExtension());
  }

  public static boolean isPKCS7(SeekableInputStream sis, String extension) throws IOException {
    byte[] buffer = new byte[5];
    sis.readFully(buffer);
    try {
      if (looksLikeCms(buffer) || (extension != null && ("ps7".equals(extension) || extension.startsWith("p7")))) {
        return readAsCMS(sis);
      }
    } catch (Exception e) {
      // ignore – detection should be resilient
    }
    return false;
  }

  private static boolean looksLikeCms(byte[] data) {
    // ASN.1 DER SEQUENCE
    return data.length > 2 && (data[0] & 0xFF) == 0x30;
  }

  private static boolean readAsCMS(SeekableInputStream sis) throws Exception {
    Security.addProvider(
        new org.bouncycastle.jce.provider.BouncyCastleProvider());
    sis.seek(0);
    final byte[] data = sis.readAllBytes();
    CMSSignedData cms;
    try {
      // first try to decode in case it is base64-encoded
      byte[] decodedBytes = Base64.getDecoder().decode(data);
      cms = new CMSSignedData(decodedBytes);
    } catch (IllegalArgumentException e) {
      cms = new CMSSignedData(data);
    }
    return cms.getSignedContent() != null;
  }
}

