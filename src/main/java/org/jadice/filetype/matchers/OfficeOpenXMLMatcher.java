package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.domutil.DOMUtil;
import org.jadice.filetype.io.SeekableInputStream;
import org.jadice.filetype.ziputil.ZipArchiveInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipFile;

/**
 * A {@link Matcher} for Office Open XML (i.e. MS Office 2007) documents.
 * <p>
 * Caveat: for performance reasons, the {@link OpenDocumentMatcher} should only be called from a
 * context where the stream has already been identified as a ZIP file/stream.
 * </p>
 * @see <a href=
 *      "http://www.ecma-international.org/news/TC45_current_work/Office%20Open%20XML%20Part%202%20-%20Open%20Packaging%20Conventions_final.docx"
 *      >Office Open XML Specification (Ecma TC45) / Part 2: Open Packaging Conventions</a>
 * 
 */
public class OfficeOpenXMLMatcher extends Matcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeOpenXMLMatcher.class);

  // 8.3.4 Representing Relationships
  private static final String RELATIONSSHIP_FILENAME = "_rels/.rels";

  // 9.2.6 Mapping Part Content Type
  private static final String CONTENT_TYPES_FILENAME = "[Content_Types].xml";

  private static final String OFFICE_DOCUMENT_REL_TYPE_SCHEMA = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";

  private static final String XPS_REL_TYPE_SCHEMA = "http://schemas.microsoft.com/xps/2005/06/fixedrepresentation";

  private static final String VISIO_REL_TYPE_SCHEMA = "http://schemas.microsoft.com/visio/2010/relationships/document";

  private static final String CORE_PROPERTIES_SCHEMA = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

  private static final class Relationship implements Comparable<Relationship> {
    public final String id;
    public final String type;
    public final String target;

    public Relationship(final String id, final String type, final String target) {
      this.id = id;
      this.type = type;
      this.target = target.startsWith("/") ? target.substring(1) : target;
    }

    @Override
    public int compareTo(final Relationship other) {
      return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(final Object other) {
      return other instanceof Relationship //
          && id.equals(((Relationship) other).id);
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public String toString() {
      return "[" + id + "] " + type + " -> " + target;
    }
  }

  interface ContentType {
    boolean matches(String partName);

    String getMimeType();
  }

  private static final class DefaultContentType implements ContentType {
    private final String mimeType;
    private final String extension;

    public DefaultContentType(final String mimeType, final String extension) {
      this.mimeType = mimeType;
      this.extension = extension;
    }

    @Override
    public String getMimeType() {
      return mimeType;
    }

    @Override
    public boolean matches(final String partName) {
      return partName.endsWith("." + extension);
    }

    @Override
    public String toString() {
      return extension + " -> " + mimeType;
    }
  }

  private static final class OverrideContentType implements ContentType {
    private final String mimeType;
    private final String partName;

    public OverrideContentType(final String mimeType, final String partName) {
      this.mimeType = mimeType;
      this.partName = partName.startsWith("/") ? partName.substring(1) : partName;
    }

    @Override
    public String getMimeType() {
      return mimeType;
    }

    @Override
    public boolean matches(final String partName) {
      return this.partName.equals(partName);
    }

    @Override
    public String toString() {
      return partName + " -> " + mimeType;
    }
  }

  /**
   * Types that this Matcher can handle.
   * 
   */
  public enum OfficeOpenType {
    WORD_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "Microsoft Word 2007 Document", "docx"),
    WORD_TEMPLATE("application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        "Microsoft Word 2007 Template", "dotx"),
    WORD_DOCUMENT_MACRO("application/vnd.ms-word.document.macroEnabled", "Microsoft Word 2007 Document with macros",
        "docm"),
    EXCEL_WORKSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "Microsoft Excel 2007 Worksheet", "xlsx"),
    EXCEL_TEMPLATE("application/vnd.openxmlformats-officedocument.spreadsheetml.template",
        "Microsoft Excel 2007 Template", "xltx"),
    EXCEL_WORKSHEET_MACRO("application/vnd.ms-excel.sheet.macroEnabled", "Microsoft Excel 2007 Worksheet with macros",
        "xlsm"),
    POWERPOINT_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "Microsoft PowerPoint 2007 Presentation", "pptx"),
    POWERPOINT_TEMPLATE("application/vnd.openxmlformats-officedocument.presentationml.template",
        "Microsoft PowerPoint 2007 Template", "potx"),
    POWERPOINT_PRESENTATION_MACRO("application/vnd.ms-powerpoint.presentation.macroEnabled",
        "Microsoft PowerPoint 2007 Presentation with macros", "pptm"),
    POWERPOINT_SLIDESHOW("application/vnd.openxmlformats-officedocument.presentationml.slideshow",
        "Microsoft PowerPoint 2007 Slideshow", "ppsx"),
    XPS_DOCUMENT("application/vnd.ms-xpsdocument", "XPS (XML Paper Specification) Document", "xps",
        "application/vnd.ms-package.xps-fixeddocumentsequence+xml"),
    VISIO_DRAWING("application/vnd.ms-visio.drawing", "Microsoft Visio 2013 Drawing", "vsdx",
        "application/vnd.ms-visio.drawing.main+xml"),
    VISIO_DRAWING_MACRO_ENABLED("application/vnd.ms-visio.drawing.macroEnabled.12",
        "Microsoft Visio 2013 macro-enabled Drawing", "vsdm", "application/vnd.ms-visio.drawing.macroEnabled.main+xml"),
    VISIO_STENCIL("application/vnd.ms-visio.stencil", "Microsoft Visio 2013 Stencil", "vssx",
        "application/vnd.ms-visio.stencil.main+xml"),
    VISIO_STENCIL_MACRO_ENABLED("application/vnd.ms-visio.stencil.macroEnabled.12",
        "Microsoft Visio 2013 macro-enabled Stencil", "vssm", "application/vnd.ms-visio.stencil.macroEnabled.main+xml"),
    VISIO_TEMPLATE("application/vnd.ms-visio.template", "Microsoft Visio 2013 Template", "vstx",
        "application/vnd.ms-visio.template.main+xml"),
    VISIO_TEMPLATE_MACRO_ENABLED("application/vnd.ms-visio.template.macroEnabled.12",
        "Microsoft Visio 2013 macro-enabled Template", "vstm",
        "application/vnd.ms-visio.template.macroEnabled.main+xml");


    final String mimeType;
    final String description;
    final String extension;
    /**
     * The key in the XML index files which reveals the content type
     */
    final String xmlIdentifier;

    /**
     * Constr. w/ mimeType == xmlIdentifier
     */
    OfficeOpenType(final String mimeType, final String description, final String extension) {
      this.mimeType = mimeType;
      this.description = description;
      this.extension = extension;
      this.xmlIdentifier = mimeType;
    }

    OfficeOpenType(final String mimeType, final String description, final String extension, final String xmlIdentifier) {
      this.mimeType = mimeType;
      this.description = description;
      this.extension = extension;
      this.xmlIdentifier = xmlIdentifier;
    }

    public boolean matches(final ContentType docType) {
      return docType != null && docType.getMimeType().startsWith(xmlIdentifier);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jadice.filetype.database.Matcher#matches(org.jadice.filetype.Context)
   */
  @Override
  public boolean matches(final Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);

      try (ZipFile archive = ZipArchiveInput.createZipFile(sis, context)) {
        detect(context, archive);
      }
      return context.getProperty(MimeTypeAction.KEY) != null;
    } catch (IOException e) {
      context.error(this, "Exception analyzing Office Open XML Container", e);
    }
    return false;
  }

  private void detect(final Context ctx, final ZipFile archive) throws IOException {
    final SortedSet<Relationship> rels = getRelationships(archive);
    final List<ContentType> contentTypes = getContentTypes(archive);
    if (rels.isEmpty() || contentTypes.isEmpty()) {
      return;
    }

    final OfficeOpenType mimeType = findDocumentType(rels, contentTypes);
    LOGGER.debug("Detected OfficeOpenType: {}", mimeType);
    if (mimeType == null) {
      return;
    }

    ctx.setProperty(MimeTypeAction.KEY, mimeType.mimeType);
    ctx.setProperty(DescriptionAction.KEY, mimeType.description);
    ctx.setProperty(ExtensionAction.KEY, mimeType.extension);

    Map<String, Object> metaData;
    try {
      metaData = findMetaData(rels, archive);
      if (!metaData.isEmpty()) {
        ctx.setProperty("OLE_DETAILS", metaData);
      }
    } catch (Exception e) {
      ctx.error(this, "Exception parsing MetaData", e);
    }

  }

  private OfficeOpenType findDocumentType(final SortedSet<Relationship> rels, final List<ContentType> contentTypes) {

    final Relationship mainDocRel = getMainDocument(rels);
    if (mainDocRel == null) {
      return null;
    }

    ContentType docType = null;
    for (ContentType ct : contentTypes) {
      if (ct.matches(mainDocRel.target)) {
        docType = ct;
        break;
      }
    }
    if (docType == null) {
      return null;
    }

    for (OfficeOpenType type : OfficeOpenType.values()) {
      if (type.matches(docType)) {
        return type;
      }
    }
    return null;
  }

  private Relationship getMainDocument(final SortedSet<Relationship> rels) {
    for (Relationship r : rels) {
      if (OFFICE_DOCUMENT_REL_TYPE_SCHEMA.equalsIgnoreCase(r.type) || XPS_REL_TYPE_SCHEMA.equalsIgnoreCase(r.type)
          || VISIO_REL_TYPE_SCHEMA.equalsIgnoreCase(r.type)) {
        return r;
      }
    }
    return null;
  }

  private Map<String, Object> findMetaData(final SortedSet<Relationship> rels, final ZipFile archive) throws IOException {
    for (Relationship r : rels) {
      // XXX What about extended-properties?
      if (CORE_PROPERTIES_SCHEMA.equalsIgnoreCase(r.type)) {
        final InputStream is = getSafeInputStream(r.target, archive);
        if (is == null) {
          continue;
        }
        LOGGER.debug("Parsing document meta data from {}", r.target);
        return readXMLMetaData(is);
      }
    }
    return Collections.emptyMap();
  }

  private Map<String, Object> readXMLMetaData(final InputStream inputStream) throws IOException {
    Map<String, Object> result = new HashMap<>();
    final Document doc;
    try {
      DocumentBuilder builder = DOMUtil.createSimpleDocumentBuilder();
      doc = builder.parse(inputStream);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Could not read XML meta data", e);
    }

    Node root = DOMUtil.findChildByName(doc, "cp:coreProperties");
    if (null == root) {
      throw new IOException("malformed core-properties: missing cp:coreProperties node");
    }

    Node n = root.getFirstChild();
    while (n != null) {
      if (n instanceof Element) {
        String key = n.getNodeName();
        String val = DOMUtil.getNodeText(n);
        if (val != null && val.length() > 0) {
          // Ignore empty information
          result.put(key, val);
        }
      }
      n = n.getNextSibling();
    }
    return result;
  }

  /**
   * Get the content types from an OfficeOpenXML archive
   * @param archive an OfficeOpenXML archive
   * @return a list of content types or an empty {@link List<ContentType>}
   * @throws IOException if parsing of the content types fails
   */
  private List<ContentType> getContentTypes(final ZipFile archive) throws IOException {
    List<ContentType> result = new LinkedList<>();
    final InputStream typesIS = getSafeInputStream(CONTENT_TYPES_FILENAME, archive);
    if (typesIS == null) {
      return result;
    }

    final Document doc;
    try {
      LOGGER.debug("Detecting ContentTypes. Parsing " + CONTENT_TYPES_FILENAME + " now");
      DocumentBuilder dBuilder = DOMUtil.createSimpleDocumentBuilder();
      doc = dBuilder.parse(typesIS);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Could not parse " + CONTENT_TYPES_FILENAME, e);
    }

    // Important: First the specialized overrides, ...
    NodeList overrideNodes = doc.getElementsByTagName("Override");
    for (int i = 0; i < overrideNodes.getLength(); i++) {
      Node nNode = overrideNodes.item(i);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) nNode;
        final OverrideContentType ct = new OverrideContentType(e.getAttribute("ContentType"),
            e.getAttribute("PartName"));
        LOGGER.debug("Detected override content type: {}", ct);
        result.add(ct);
      }
    }

    // .. then the "defaults"
    NodeList defaultNodes = doc.getElementsByTagName("Default");
    for (int i = 0; i < defaultNodes.getLength(); i++) {
      Node nNode = defaultNodes.item(i);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) nNode;
        final DefaultContentType ct = new DefaultContentType(e.getAttribute("ContentType"),
            e.getAttribute("Extension"));
        LOGGER.debug("Detected default content type: {}", ct);
        result.add(ct);
      }
    }

    return result;
  }

  /**
   * Get the relationships from an OfficeOpenXML archive
   * @param archive an OfficeOpenXML archive
   * @return a set of relationships or an empty {@link SortedSet<Relationship>}
   * @throws IOException if parsing of the relationship part fails
   */
  private SortedSet<Relationship> getRelationships(final ZipFile archive) throws IOException {
    SortedSet<Relationship> result = new TreeSet<>();
    final InputStream relationsIS = getSafeInputStream(RELATIONSSHIP_FILENAME, archive);
    if (relationsIS == null) {
      return result;
    }

    Document doc;
    try {
      LOGGER.debug("Detecting Relationships. Parsing " + RELATIONSSHIP_FILENAME + " now");
      DocumentBuilder dBuilder = DOMUtil.createSimpleDocumentBuilder();
      doc = dBuilder.parse(relationsIS);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Could not parse " + RELATIONSSHIP_FILENAME, e);
    }
    NodeList nList = doc.getElementsByTagName("Relationship");

    for (int i = 0; i < nList.getLength(); i++) {

      Node nNode = nList.item(i);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) nNode;
        final Relationship rel = new Relationship(e.getAttribute("Id"), e.getAttribute("Type"),
            e.getAttribute("Target"));
        LOGGER.debug("Detected relationship: {}", rel);
        result.add(rel);
      }
    }
    return result;
  }

  private InputStream getSafeInputStream(String fileName, final ZipFile archive) throws IOException {
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }

    final ZipEntry entry = archive.getEntry(fileName);
    if (entry != null && !entry.isDirectory()) {
      LOGGER.debug("Get '{}' from 1 piece", fileName);
      return archive.getInputStream(entry);
    }

    // try directory browsing:
    // Assemble stream from "[0].piece"..."[$n].last.piece";
    // see Office Open XML, Part 2: Open Packaging Conventions, sec 9.1.3.1 Logical Item Names
    List<InputStream> streams = new LinkedList<>();
    int i = 0;
    ZipEntry piece;
    while ((piece = archive.getEntry(fileName + "/[" + i + "].piece")) != null) {
      final InputStream is = archive.getInputStream(piece);
      if (is == null) {
        break;
      }

      streams.add(is);
      i++;
    }

    final ZipEntry last = archive.getEntry(fileName + "/[" + i + "].last.piece");
    if (last == null) {
      return null;
    }

    final InputStream is = archive.getInputStream(last);
    if (is == null) {
      return null;
    }
    streams.add(is);

    LOGGER.debug("Get '{}' from {} pieces", fileName, streams.size());
    return new SequenceInputStream(Collections.enumeration(streams));
  }
}
