package org.jadice.filetype.matchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ERezeptMatcher extends Matcher {

  private static final Logger logger = LoggerFactory.getLogger(ERezeptMatcher.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public boolean matches(final Context context) throws IOException {
    SeekableInputStream sis = context.getStream();
    return isERezept(sis, context.getStatedExtension());
  }

  public static boolean isERezept(SeekableInputStream sis, String extension ) throws IOException {
    final byte[] data = sis.readAllBytes();
    try {

      // 1. JSON FHIR
      if (looksLikeJson(data) && isFhirJson(data)) {
        return true;
      }

      // 2. XML FHIR
      if (looksLikeXml(data) && isFhirXml(data)) {
        return true;
      }

      // 3. CMS / PKCS#7
      if (looksLikeCms(data) || "ps7".equals(extension)) {
        byte[] extracted = extractCmsPayload(data);
        if (extracted != null) {
          return isFhirJson(extracted) || isFhirXml(extracted);
        }
      }

    } catch (Exception e) {
      // ignore – detection should be resilient
    }
    return false;
  }

  /* ---------- JSON ---------- */

  private static boolean isFhirJson(byte[] data) {
    try {
      JsonNode root = OBJECT_MAPPER.readTree(data);

      if (!root.has("resourceType")) {
        return false;
      }

      // Typical E-Rezept indicators
      String content = root.toString();
      return content.contains("gematik.de/fhir/erp")
          || content.contains("\"Task\"")
          || content.contains("\"MedicationRequest\"");
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean looksLikeJson(byte[] data) {
    String s = new String(data, StandardCharsets.UTF_8).trim();
    return s.startsWith("{") || s.startsWith("[");
  }

  /* ---------- XML ---------- */

  private static boolean isFhirXml(byte[] data) {
    String xml = new String(data, StandardCharsets.UTF_8);
    return xml.contains("http://hl7.org/fhir")
        && (xml.contains("<Bundle") || xml.contains(":Bundle"))
        && xml.contains("gematik.de/fhir/erp");
  }

  private static boolean looksLikeXml(byte[] data) {
    String s = new String(data, StandardCharsets.UTF_8).trim();
    return s.startsWith("<");
  }

  /* ---------- CMS / PKCS#7 ---------- */

  private static boolean looksLikeCms(byte[] data) {
    // ASN.1 DER SEQUENCE
    return data.length > 2 && (data[0] & 0xFF) == 0x30;
  }

  private static byte[] extractCmsPayload(byte[] cmsData) {
    try {
      Security.addProvider(
          new org.bouncycastle.jce.provider.BouncyCastleProvider());

      byte[] decodedBytes = Base64.getDecoder().decode(cmsData);
      CMSSignedData cms = new CMSSignedData(decodedBytes);
      CMSProcessable signedContent = cms.getSignedContent();

      if (signedContent == null) {
        return null;
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      signedContent.write(out);
      return out.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}

