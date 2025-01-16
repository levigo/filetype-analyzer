package org.jadice.filetype.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import org.jadice.filetype.Context;

/**
 * {@link Action} to set a stream's description. An existing description may optionally be replaced
 * by specifying 'replace="true"'.
 *
 */
@XmlRootElement(name = "description")
public class DescriptionAction extends Action {
  public static final String KEY = "description";

  /**
   * List of maps from language to Description
   */
  public static class Description implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Map<String, String>> content = new ArrayList<>();

    private final Locale locale;

    public Description(Locale locale) {
      this.locale = locale;
    }

    public void append(String lang, String description, boolean replace) {
      if (replace) {
        content.clear();
      }

      if (null == lang) {
        lang = DEFAULT_LANG;
      }

      // default desc. ist der indikator für eine neue map,
      // alle lok. sprachen müssen danach in der xml datei definiert werden
      if (content.isEmpty() || lang.equalsIgnoreCase(DEFAULT_LANG)) {
        content.add(new HashMap<>());
      }

      Map<String, String> map = content.get(content.size() - 1);

      map.put(lang, description);
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      String lang = locale.getLanguage();

      for (Map<String, String> map : content) {
        String desc = map.get(lang);

        // fall back to default language if not found
        if (null == desc) {
          desc = map.get(DEFAULT_LANG);
        }

        if (null != desc) {
          if (result.length() > 0)
            result.append(", ");

          result.append(desc);
        }
      }

      return result.toString();
    }
  }

  @XmlAttribute
  private String lang;

  @XmlValue
  private String description;

  @XmlAttribute
  private final boolean replace = false;

  private static final String DEFAULT_LANG = "default";

  @Override
  public void perform(Context ctx) {
    Description desc = null;

    if (ctx.getProperty(KEY) instanceof Description) {
      desc = (Description) ctx.getProperty(KEY);
    } else if (ctx.getProperty(KEY) instanceof String) {
      desc = new Description(ctx.getLocale());
      desc.append(null, (String) ctx.getProperty(KEY), replace);
    }

    if (desc == null) {
      desc = new Description(ctx.getLocale());
      ctx.setProperty(KEY, desc);
    }

    desc.append(lang, description, replace);
  }
}
