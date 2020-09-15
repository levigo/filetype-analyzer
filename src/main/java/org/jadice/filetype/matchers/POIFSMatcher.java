package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jadice.filetype.Context;
import org.jadice.filetype.database.DescriptionAction;
import org.jadice.filetype.database.ExtensionAction;
import org.jadice.filetype.database.MimeTypeAction;
import org.jadice.filetype.io.SeekableInputStream;

/**
 * A matcher for Microsoft Office formats up to, but excluding the Office 2007 OOXML formats. Those
 * documents are based on the COM/OLE container "filesystem" which is parsed using apache POI.
 * Documents of the following applications are recognized:
 * <ul>
 * <li>Microsoft Excel</li>
 * <li>Microsoft Word</li>
 * <li>Microsoft PowerPoint</li>
 * <li>Microsoft Visio</li>
 * <li>Microsoft Outlook (Saved eMails in *.msg format)</li>
 * <li>Microsoft Project</li>
 * </ul>
 *
 * <p>
 * Caveat: for performance reasons, the {@link POIFSMatcher} should only be called from a context
 * where the stream has already be identified as a OLE container file/stream.
 * </p>
 * 
 */
public class POIFSMatcher extends Matcher {

  /**
   * Types which are handled by this matcher
   */
  private static enum POIFS_TYPE {

    EXCEL(new String[]{
        "Book", "Workbook"
    }, "application/msexcel", "Microsoft Excel Workbook", "xls"), //
    POWERPOINT("PowerPoint Document", "application/mspowerpoint", "Microsoft Powerpoint Document", "ppt"), //
    POWERPOINT_PP40("PP40", "application/mspowerpoint", "Microsoft Powerpoint Document (v4.0)", "ppt"), //
    WORD("WordDocument", "application/msword", "Microsoft Word Document", "doc"), //
    VISIO("VisioDocument", "application/msvisio", "Microsoft Visio Drawing", "vsd"), //
    OUTLOOK("substg1.0", "application/msoutlook", "Microsoft Outlook Message", "msg"), //
    PROJECT(new String[]{
        "MSProject", "Microsoft Project"
    }, "application/vnd.ms-project", "MS Project Document", "mpp");

    /**
     * POI {@link DirectoryEntry} name
     */
    private final String[] identifiers;

    /**
     * MIME Type
     */
    public final String mimeType;

    /**
     * Description
     */
    public final String description;

    /**
     * Default extension
     */
    public final String extension;


    private POIFS_TYPE(String identifier, String mimeType, String description, String extension) {
      this(new String[]{
          identifier
      }, mimeType, description, extension);
    }

    private POIFS_TYPE(String[] identifiers, String mimeType, String description, String extension) {
      this.identifiers = identifiers;
      this.mimeType = mimeType;
      this.description = description;
      this.extension = extension;
    }

    public boolean matches(String identifier) {
      // MK: A msg chunk contains a tag in a variable format, therefore i
      // changed the case to check if the constant header tag is present instead
      // of equal.

      for (String s : identifiers) {
        // old: if (s.equals(identifier)) {
        if (identifier.contains(s)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public boolean matches(Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);

      POIFSFileSystem fs = new POIFSFileSystem(sis);
      DirectoryEntry root = fs.getRoot();

      traverse(context, root);
      return context.getProperty(MimeTypeAction.KEY) != null;
    } catch (IOException e) {
      context.error(this, "Exception analyzing OLE container file", e);
    }
    return false;
  }

  private void traverse(Context context, DirectoryEntry dir) throws IOException {

    // dir is an instance of DirectoryEntry ...
    for (Iterator<Entry> iter = dir.getEntries(); iter.hasNext();) {
      Entry entry = iter.next();
      if (entry instanceof DirectoryEntry) {
        // recursing would include nested documents
        // traverse(context, (DirectoryEntry) entry, pathName + "$"
        // + entry.getName());
      } else if (entry instanceof DocumentEntry) {
        if (entry.getName().endsWith(DocumentSummaryInformation.DEFAULT_STREAM_NAME)
            || entry.getName().endsWith(SummaryInformation.DEFAULT_STREAM_NAME)) {
          try {
            handleSummaryInformation(context, entry);
          } catch (Exception e) {
            context.error(this, "Exception parsing summary information", e);
          }
        } else {
          detectFileFormat(context, entry);
        }
      }
    }
  }

  private void detectFileFormat(Context context, Entry entry) {
    String name = entry.getName();
    for (POIFS_TYPE type : POIFS_TYPE.values()) {
      if (type.matches(name)) {
        context.setProperty(MimeTypeAction.KEY, type.mimeType);
        context.setProperty(DescriptionAction.KEY, type.description);
        context.setProperty(ExtensionAction.KEY, type.extension);
      }
    }
  }

  /**
   * @param context
   * @param entry
   * @throws IOException
   * @throws NoPropertySetStreamException
   * @throws MarkUnsupportedException
   * @throws UnsupportedEncodingException
   */
  @SuppressWarnings("unchecked")
  private void handleSummaryInformation(Context context, Entry entry) throws IOException, HPSFException {
    DocumentInputStream dis = new DocumentInputStream((DocumentEntry) entry);
    PropertySet dsi = PropertySetFactory.create(dis);

    // XXX: Wuenschenswert: Map, die Einfuege-Reihenfolge beibehaelt. Aber: SD-Marshaller kann damit
    // anscheinend nicht umgehen.
    Map<String, Object> details = new HashMap<String, Object>();
    for (Section s : (List<Section>) dsi.getSections()) {
      for (Property p : s.getProperties()) {
        String name = s.getPIDString(p.getID());
        String contextKey = (dsi.isSummaryInformation() ? "SUMMARY_" : "DOCUMENT_")
            + (name != SectionIDMap.UNDEFINED ? name : "PID_" + p.getID());
        details.put(contextKey, p.getValue());
      }
    }
    detectMSProject(context, details);

    Object savedProps = context.getProperty("OLE_DETAILS");
    if (savedProps != null && savedProps instanceof Map) {
      // Found already some other details -> merge them
      ((Map) savedProps).putAll(details);
    } else {
      context.setProperty("OLE_DETAILS", details);
    }
  }

  /**
   * Detects MS Project from the "APPNAME" field of the details, as Project files can not be
   * distinguished in {@link #detectFileFormat(Context, Entry)} not from other OLE Types.
   * 
   * @param context {@link Context} to store the result
   * @param details The SUMMARY information
   */
  private void detectMSProject(Context context, Map<String, Object> details) {
    final Object appName = details.get("SUMMARY_PID_APPNAME");
    if (appName == null || !(appName instanceof String)) {
      return;
    }

    final String appNameString = ((String) appName).trim();
    if (POIFS_TYPE.PROJECT.matches(appNameString)) {
      context.setProperty(MimeTypeAction.KEY, POIFS_TYPE.PROJECT.mimeType);
      context.setProperty(DescriptionAction.KEY, POIFS_TYPE.PROJECT.description);
      context.setProperty(ExtensionAction.KEY, POIFS_TYPE.PROJECT.extension);
    }

  }


}
