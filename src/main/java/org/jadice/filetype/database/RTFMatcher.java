package org.jadice.filetype.database;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jadice.filetype.Context;
import org.jadice.filetype.io.SeekableInputStream;

import com.rtfparserkit.parser.RtfListenerAdaptor;
import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;
import com.rtfparserkit.rtf.Command;

/**
 * A {@link Matcher} for RTF documents .
 * 
 * Caveat: for performance reasons, this should only be called from a context where the stream has
 * already be identified as a RTF file/stream.
 * 
 */
public class RTFMatcher extends Matcher {

  /**
   * MIME type of Rich Text Files
   */
  public static final String RTF_MIME_TYPE = "text/rtf";

  /**
   * Key under which RTF metadata is stored in the filetype analyzer result
   */
  public static final String DETAILS_KEY = "RTF_DETAILS";

  /**
   * Key for details of embedded files
   */
  public static final String EMBEDDED_FILE_CLASSES_KEY = "embedded-files";

  /**
   * key for the object class of an embedded file
   */
  public static final String OBJECT_CLASS_KEY = "object-class";

  /**
   * Key for the data length of an embedded file
   */
  public static final String DATA_LENGTH_KEY = "data-length";

  private static class EmbeddedObjectInfoDTO {
    String objClass;
    int objDataLength;

    public Map<String, Object> createDetailMap() {
      final Map<String, Object> result = new HashMap<String, Object>();
      result.put(OBJECT_CLASS_KEY, objClass);
      result.put(DATA_LENGTH_KEY, objDataLength);
      return result;
    }
  }

  private class MyRtfParserListener extends RtfListenerAdaptor {

    private final Context ctx;

    private final Stack<Command> commands = new Stack<Command>();

    private boolean rtfCommandFound = false;

    private int commandCounter = 0;

    private final Map<String, Object> rtfDetails;

    private LinkedList<EmbeddedObjectInfoDTO> embeddedObjects = new LinkedList<RTFMatcher.EmbeddedObjectInfoDTO>();

    public MyRtfParserListener(Map<String, Object> rtfDetails, Context ctx) {
      this.rtfDetails = rtfDetails;
      this.ctx = ctx;
    }

    @Override
    public void processCommand(Command command, int parameter, boolean hasParameter, boolean optional) {
      commands.push(command);
      commandCounter++;

      switch (command){
        case rtf :
          // Is RTF command right at the start?
          if (commandCounter == 1) {
            rtfCommandFound = true;
          }
          break;
        case object :
          // New embedded object found
          embeddedObjects.add(new EmbeddedObjectInfoDTO());
          break;

        default :
          break;
      }
    }

    @Override
    public void processGroupStart() {
      // Caveat: Use "null" as group separator because command enumeration cannot be extended!
      commands.push(null);
    }

    @Override
    public void processGroupEnd() {
      // Pop all element until a group separator (i.e. null) is found
      Command popped = commands.pop();
      while (popped != null && !commands.isEmpty()) {
        popped = commands.pop();
      }
    }

    @Override
    public void processString(String string) {
      if (string == null || string.isEmpty()) {
        return;
      }

      if (commands.contains(Command.info)) {
        processInfo(commands.peek(), string);
      }
      if (commands.contains(Command.object)) {
        processEmbeddedInfo(commands.peek(), string);
      }
    }

    @Override
    public void processBinaryBytes(byte[] data) {
      if (data == null || data.length == 0) {
        return;
      }

      processString(new String(data, Charset.forName("UTF-8")));
    }

    @Override
    public void processCharacterBytes(byte[] data) {
      if (data == null || data.length == 0) {
        return;
      }
      processString(new String(data, Charset.forName("UTF-8")));
    }

    @Override
    public void processDocumentEnd() {
      // Enrich RTF details with embedded document info
      if (embeddedObjects.isEmpty()) {
        return;
      }

      List<Map<String, Object>> embedded = new ArrayList<Map<String, Object>>();
      rtfDetails.put(EMBEDDED_FILE_CLASSES_KEY, embedded);
      for (EmbeddedObjectInfoDTO dto : embeddedObjects) {
        embedded.add(dto.createDetailMap());
      }
    }

    public boolean isRtfCommandFound() {
      return rtfCommandFound;
    }

    public void processInfo(Command cmd, String data) {
      rtfDetails.put(cmd.getCommandName(), data);
    }

    public void processEmbeddedInfo(Command cmd, String data) {
      if (embeddedObjects.isEmpty()) {
        ctx.warning(RTFMatcher.this, "Found details of an embedded file, but none element has started.");
        return;
      }
      final EmbeddedObjectInfoDTO obj = embeddedObjects.getLast();

      // Select only interesting commands
      switch (cmd){
        case objclass :
          obj.objClass = data;
          break;

        case objdata :
          obj.objDataLength = data.length();
          break;

        default :
          break;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jadice.filetype.database.Matcher#matches(org.jadice.filetype.Context)
   */
  @Override
  public boolean matches(Context context) {
    SeekableInputStream sis = context.getStream();
    synchronized (sis) {
      try {
        sis.seek(0);
        final Map<String, Object> rtfDetails = new HashMap<String, Object>();
        final StandardRtfParser parser = new StandardRtfParser();
        final MyRtfParserListener parserListener = new MyRtfParserListener(rtfDetails, context);
        parser.parse(new RtfStreamSource(sis), parserListener);
        if (parserListener.isRtfCommandFound()) {
          context.setProperty(MimeTypeAction.KEY, RTF_MIME_TYPE);
        } else {
          return false;
        }

        context.setProperty(DETAILS_KEY, rtfDetails);

        return true;
      } catch (Exception e) {
        context.error(this, "Exception analyzing RTF document", e);
      }
    }
    return false;
  }

}
