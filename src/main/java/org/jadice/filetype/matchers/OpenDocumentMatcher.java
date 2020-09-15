package org.jadice.filetype.matchers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.domutil.DOMUtil;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.ziputil.ZipArchiveInput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipFile;

/**
 * A matcher for OpenDocument-based formats and their fore-runners. In particular, the following
 * format families are dealt with:
 * <ul>
 * <li>Open Document Format ODF (OpenOffice 2.x et al.)
 * <li>OpenOffice 1.x/StarOffice 6,7
 * <li>StarOffice 5.x
 * <li>KOffice
 * </ul>
 * <p>
 * The {@link OpenDocumentMatcher} not only sets the MIME type and description appropriately, but
 * also tries to provide the stream meta data.
 * <p>
 * Caveat: for performance reasons, the {@link OpenDocumentMatcher} should only be called from a
 * context where the stream has already been identified as a ZIP file/stream.
 *
 */
public class OpenDocumentMatcher extends Matcher {

  static final class DontCloseFilter extends FilterInputStream {
    DontCloseFilter(InputStream in) {
      super(in);
    }

    @Override
    public void close() throws IOException {
      // don't...
    }
  }

  /**
   * All document types this Matcher recognizes.<br/>
   * <b>Caveat:</b> default extension can be null!
   */
  private enum OpenDocumenType {
    // OpenDocument formats (for OpenOffice 2.x / StarOffice >= 8)
    OPEN_DOCUMENT_WRITER("application/vnd.oasis.opendocument.text", "OpenDocument Writer", "odt"),
    OPEN_DOCUMENT_WRITER_TEMPLATE("application/vnd.oasis.opendocument.text-template", "OpenDocument Writer Template",
        "ott"), //
    OPEN_DOCUMENT_MASTER("application/vnd.oasis.opendocument.text-Master", "OpenDocument Master Document", "odm"), //
    OPEN_DOCUMENT_DRAWING("application/vnd.oasis.opendocument.graphics", "OpenDocument Drawing", "odg"), //
    OPEN_DOCUMENT_DRAWING_TEMPLATE("application/vnd.oasis.opendocument.graphics-template",
        "OpenDocument Drawing Template", "otg"), //
    OPEN_DOCUMENT_PRESENTATION("application/vnd.oasis.opendocument.presentation", "OpenDocument Presentation", "odp"), //
    OPEN_DOCUMENT_PRESENTATION_TEMPLATE("application/vnd.oasis.opendocument.presentation-template",
        "OpenDocument Presentation Template", "otp"), //
    OPEN_DOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet", "OpenDocument Spreadsheet", "ods"), //
    OPEN_DOCUMENT_SPREADSHEET_TEMPLATE("application/vnd.oasis.opendocument.spreadsheet-template",
        "OpenDocument Spreadsheet Template", "ots"), //
    OPEN_DOCUMENT_CHART("application/vnd.oasis.opendocument.chart", "OpenDocument Chart", "odc"), //
    OPEN_DOCUMENT_CHART_TEMPLATE("application/vnd.oasis.opendocument.chart-template", "OpenDocument Chart Template",
        "otc"), //
    OPEN_DOCUMENT_FORMULA("application/vnd.oasis.opendocument.formula", "OpenDocument Formula", "odf"), //
    OPEN_DOCUMENT_FORMULA_TEMPLATE("application/vnd.oasis.opendocument.formula-template",
        "OpenDocument Formula Template", "otf"), //
    OPEN_DOCUMENT_DATABASE("application/vnd.oasis.opendocument.database", "OpenDocument Database", "odd"), //
    OPEN_DOCUMENT_IMAGE("application/vnd.oasis.opendocument.image", "OpenDocument Image", "odi"), //

    // OpenOffice formats (for OpenOffice 1.x / StarOffice 6/7)
    SUN_WRITER("application/vnd.sun.xml.writer", "OpenOffice 1.x / StarOffice 6,7 Writer", "sxw"), //
    SUN_WRITER_TEMPLATE("application/vnd.sun.xml.writer.template", "OpenOffice 1.x / StarOffice 6,7  Writer Template",
        "stw"), //
    SUN_GLOBAL("application/vnd.sun.xml.global", "OpenOffice 1.x / StarOffice 6,7 Global Document", "sxg"), //
    SUN_DRAWING("application/vnd.sun.xml.draw", "OpenOffice 1.x / StarOffice 6,7 Drawing", "sxg"), //
    SUN_DRAWING_TEMPLATE("application/vnd.sun.xml.draw.template", "OpenOffice 1.x / StarOffice 6,7 Drawing Template",
        "stg"), //
    SUN_PRESENTATION("application/vnd.sun.xml.impress", "OpenOffice 1.x / StarOffice 6,7 Presentation", "sxi"), //
    SUN_PRESENTATION_TEMPLATE("application/vnd.sun.xml.impress.template",
        "OpenOffice 1.x / StarOffice 6,7 Presentation Template", "sti"), //
    SUN_SPREADSHEET("application/vnd.sun.xml.calc", "OpenOffice 1.x / StarOffice 6,7 Spreadsheet", "sxc"), //
    SUN_SPREADSHEET_TEMPLATE("application/vnd.sun.xml.calc.template",
        "OpenOffice 1.x / StarOffice 6,7 Spreadsheet Template", "stc"), //
    SUN_FORMULA("application/vnd.sun.xml.math", "OpenOffice 1.x / StarOffice 6,7 Formula", "sxm"), //

    // StarOffice 5.x
    STAR_WRITER("application/vnd.stardivision.writer", "StarOffice 5.x Writer", "sdw"), //
    STAR_GLOBAL("application/vnd.stardivision.writer-global", "StarOffice 5.x Global Document", "sgl"), //
    STAR_DRAWING("application/vnd.stardivision.draw", "StarOffice 5.x Drawing", "sdd"), //
    STAR_PRESENTATION("application/vnd.stardivision.impress", "StarOffice 5.x Presentation", "sdp"), //
    STAR_SPREADSHEET("application/vnd.stardivision.calc", "StarOffice 5.x Spreadsheet", "sdx"), //
    STAR_FORMULA("application/vnd.stardivision.math", "StarOffice 5.x Formula", "smf"), //

    // Koffice
    KOFFICE_DOCUMENT("application/vnd.kde.karbon", "KOffice Karbon Document"), //
    KOFFICE_CHART("application/vnd.kde.kchart", "KOffice Chart"), //
    KOFFICE_FORMULA("application/vnd.kde.kformula", "KOffice Formula"), //
    KOFFICE_DIAGRAM("application/vnd.kde.kivio", "KOffice Diagram"), //
    KOFFICE_DRAWING("application/vnd.kde.kontour", "KOffice Drawing"), //
    KOFFICE_PRESENTATION("application/vnd.kde.kpresenter", "KOffice Presentation"), //
    KOFFICE_SPREADSHEET("application/vnd.kde.kspread", "KOffice Spreadsheet"), //
    KOFFICE_KWORD("application/vnd.kde.kword", "KOffice Kword Document");//

    final String mimeType;
    final String description;
    final String extension;

    private OpenDocumenType(String mimeType, String description, String extension) {
      this.mimeType = mimeType;
      this.description = description;
      this.extension = extension;
    }

    private OpenDocumenType(String mimeType, String description) {
      this.mimeType = mimeType;
      this.description = description;
      this.extension = null;
    }
  }

  @Override
  public boolean matches(Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);

      ZipFile archive = ZipArchiveInput.createZipFile(sis, context);

      detect(context, archive);
      archive.close();

      return context.getProperty(MimeTypeAction.KEY) != null;
    } catch (Exception e) {
      context.error(this, "Exception analyzing ODF", e);
    }

    return false;
  }

  private void detect(Context ctx, ZipFile archive) throws IOException {
    boolean gotMimeType = false;
    boolean gotMetaData = false;

    try {
      Map<String, Object> results = new HashMap<String, Object>();
      Enumeration<? extends ZipEntry> en = archive.entries();
      while (en.hasMoreElements()) {
        ZipEntry entry = en.nextElement();

        if ("mimetype".equals(entry.getName())) {
          detectMimeType(ctx, results, archive.getInputStream(entry));
          gotMimeType = true;
        } else if ("meta.xml".equals(entry.getName())) {
          try {
            readMetaXml(results, archive.getInputStream(entry));
            gotMetaData = true;
          } catch (Exception e) {
            ctx.error(this, "Exception parsing meta.xml", e);
          }
        } else if ("META-INF/manifest.xml".equals(entry.getName())) {
          try {
            readManifestXml(ctx, results, archive.getInputStream(entry));
            gotMimeType = true;
          } catch (Exception e) {
            ctx.error(this, "Exception parsing manifest.xml", e);
          }
        }

        if (gotMetaData && gotMimeType) {
          break;
        }
      }

      // we add the results to the context only if the detection
      // yielded at least a MIME type.
      if (!results.isEmpty() && results.containsKey(MimeTypeAction.KEY)) {
        for (Iterator<Map.Entry<String, Object>> i = results.entrySet().iterator(); i.hasNext();) {
          Map.Entry<String, Object> e = i.next();
          if (!e.getKey().startsWith("ODF_")) {
            ctx.setProperty(e.getKey(), e.getValue());
            i.remove();
          }
        }
        ctx.setProperty("ODF_DETAILS", results);
      }
    } catch (IOException e) {
      // DON'T propagate IOExceptions from here, since it is most likely the
      // ZipInputStream causing it, not the source data.
      ctx.error(this, "Exception analyzing ODF container file", e);
    }
  }

  private void detectMimeType(Context ctx, Map<String, Object> results, InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    int read = 0;
    byte[] buffer = new byte[1024];
    while ((read = is.read(buffer)) > 0) {
      sb.append(new String(buffer, 0, read, "ASCII"));
    }

    // parse mime type
    detectFileFormat(ctx, results, sb.toString().trim());
  }

  private void readMetaXml(Map<String, Object> results, InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilder builder = DOMUtil.createSimpleDocumentBuilder();
    Document document = builder.parse(new DontCloseFilter(is));

    Node root = DOMUtil.findChildByName(document, "office:document-meta");
    if (null == root) {
      throw new IOException("malformed meta.xml: missing office:document-meta node");
    }

    Node meta = DOMUtil.findChildByName(root, "office:meta");
    if (null == meta) {
      throw new IOException("malformed meta.xml: missing office:meta node");
    }

    Node n = meta.getFirstChild();
    while (n != null) {
      if (n instanceof Element) {
        if (!"meta:document-statistic".equals(n.getNodeName()) && !"meta:user-defined".equals(n.getNodeName())) {
          results.put("ODF_" + n.getNodeName(), DOMUtil.getNodeText(n));
        } else if ("meta:document-statistic".equals(n.getNodeName())) {
          NamedNodeMap m = n.getAttributes();
          for (int j = 0; j < m.getLength(); j++) {
            Node a = m.item(j);
            results.put("ODF_DOCUMENT_STATISTIC_" + a.getNodeName(), DOMUtil.getNodeText(a));
          }
        } else if ("meta:user-defined".equals(n.getNodeName())) {
          Node nameAttribute = n.getAttributes().getNamedItem("meta:name");
          if (null == nameAttribute) {
            throw new IOException("malformed meta.xml: user-defined/meta:name attribute is missing.");
          }
          results.put("ODF_USER_DEFINED_" + nameAttribute.getNodeValue(), DOMUtil.getNodeText(n));
        }
      }

      n = n.getNextSibling();
    }
  }

  private void readManifestXml(Context ctx, Map<String, Object> results, InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilder builder = DOMUtil.createSimpleDocumentBuilder();
    Document document = builder.parse(new DontCloseFilter(is));

    Node root = DOMUtil.findChildByName(document, "manifest:manifest");
    if (null == root) {
      throw new IOException("malformed manifest.xml");
    }

    Node n = root.getFirstChild();
    while (null != n) {
      if ("manifest:file-entry".equals(n.getNodeName())) {
        Node pathAttribute = n.getAttributes().getNamedItem("manifest:full-path");
        if (null == pathAttribute) {
          throw new IOException("malformed manifest.xml: manifest:full-path attribute is missing.");
        }
        if ("/".equals(pathAttribute.getNodeValue())) {
          Node mediaType = n.getAttributes().getNamedItem("manifest:media-type");
          if (null == mediaType) {
            throw new IOException("malformed manifest.xml: manifest:media-type attribute is missing.");
          }

          detectFileFormat(ctx, results, mediaType.getNodeValue());
        }
      }
      n = n.getNextSibling();
    }
  }

  private void detectFileFormat(Context ctx, Map<String, Object> results, String t) {
    if (!t.startsWith("application/")) {
      ctx.warning(this, "Unsupported MIME type: " + t);
      return;
    }

    for (OpenDocumenType type : OpenDocumenType.values()) {
      if (t.startsWith(type.mimeType)) {
        // XXX: warum nicht direkt in ctx stecken?
        results.put(MimeTypeAction.KEY, type.mimeType);
        results.put(DescriptionAction.KEY, type.description);

        if (type.extension != null) {
          results.put(ExtensionAction.KEY, type.extension);
        }

        // We found a type
        return;
      }
    }
  }
}
