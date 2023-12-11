package org.jadice.filetype.matchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
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

import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.Message;
import net.freeutils.tnef.msg.Msg;

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


    private POIFS_TYPE(final String identifier, final String mimeType, final String description, final String extension) {
      this(new String[]{
          identifier
      }, mimeType, description, extension);
    }

    private POIFS_TYPE(final String[] identifiers, final String mimeType, final String description, final String extension) {
      this.identifiers = identifiers;
      this.mimeType = mimeType;
      this.description = description;
      this.extension = extension;
    }

    public boolean matches(final String identifier) {
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
  public boolean matches(final Context context) {
    SeekableInputStream sis = context.getStream();
    try {
      sis.seek(0);

      try (POIFSFileSystem fs = new POIFSFileSystem(sis)) {
        DirectoryEntry root = fs.getRoot();

        traverse(context, root);

        if (context.getProperty(MimeTypeAction.KEY).equals(POIFS_TYPE.OUTLOOK.mimeType) && (isSignedMessage(context, root))) {
          context.setProperty(MimeTypeAction.KEY, POIFS_TYPE.OUTLOOK.mimeType + ";signed=true");
        }
      }
      return context.getProperty(MimeTypeAction.KEY) != null;
    } catch (IOException e) {
      context.error(this, "Exception analyzing OLE container file", e);
    }
    return false;
  }

  private void traverse(final Context context, final DirectoryEntry dir) throws IOException {

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

  private void detectFileFormat(final Context context, final Entry entry) {
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
   * @throws UnsupportedEncodingException
   */
  @SuppressWarnings("unchecked")
  private void handleSummaryInformation(final Context context, final Entry entry) throws IOException, HPSFException {
    DocumentInputStream dis = new DocumentInputStream((DocumentEntry) entry);
    PropertySet dsi = PropertySetFactory.create(dis);

    Map<String, Object> details = new HashMap<String, Object>();
    for (Section s : (List<Section>) dsi.getSections()) {
      for (Property p : s.getProperties()) {
        String name = s.getPIDString(p.getID());
        String contextKey = (dsi.isSummaryInformation() ? "SUMMARY_" : "DOCUMENT_")
            + (PropertyIDMap.UNDEFINED.equals(name) ? name : "PID_" + p.getID());
        details.put(contextKey, p.getValue());
      }
    }
    detectMSProject(context, details);

    Object savedProps = context.getProperty("OLE_DETAILS");
    if (savedProps != null && savedProps instanceof Map) {
      // Found already some other details -> merge them
      ((Map<String, Object>) savedProps).putAll(details);
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
  private void detectMSProject(final Context context, final Map<String, Object> details) {
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

  /**
   * Return whether the input is a clear-signed or opaque-signed .msg format message.
   *
   * @param context
   * @param root    the root of the {@link POIFSFileSystem}
   * @return true if signed, false if not or an error occurred
   * @see <a href="https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxosmime/e6f63b02-c679-4752-9302-9c4641749e95?redirectedfrom=MSDN">Recognizing a Message Object that Represents an Opaque-Signed or Encrypted S/MIME Message</a>
   * @see <a href="https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxosmime/b5ab0318-ff00-4be2-a02a-6b8201a1d6b3?redirectedfrom=MSDN">Recognizing a Message Object that Represents a Clear-Signed Message</a>
   */
  private boolean isSignedMessage(Context context, DirectoryEntry root){
    try(Message message = Msg.processMessage(root)) {
      if (message.getAttachments().size() == 1) {
        MAPIProps mapiProps = message.getMAPIProps();
        MAPIProp prop = mapiProps.getProp(MAPIProp.PR_MESSAGE_CLASS);
        if (prop != null) {
          Object value = prop.getValue();
          return value != null && (value.equals("IPM.Note.SMIME") || value.equals("IPM.Note.SMIME.MultipartSigned"));
        }
      }
    } catch (IOException e) {
      context.error(this, "Exception parsing " + POIFS_TYPE.OUTLOOK.description, e);
    }
    return false;
  }


}
