package org.nutz.walnut.cheap.css;

import org.nutz.walnut.util.Ws;

public class CheapCss {

    public static CheapStyle parseStyleInCamelCase(String style) {
        return parseStyle(style, name -> Ws.camelCase(name));
    }

    public static CheapStyle parseStyleInKebabCase(String style) {
        return parseStyle(style, name -> Ws.kebabCase(name));
    }

    public static CheapStyle parseStyle(String style, CssKeyFilter filter) {
        CheapStyle re = new CheapStyle();
        if (!Ws.isBlank(style)) {
            String[] ss = Ws.splitIgnoreBlank(style, ";");
            for (String s : ss) {
                if (Ws.isBlank(s))
                    continue;
                int pos = s.indexOf(':');
                if (pos > 0) {
                    String name = s.substring(0, pos).trim();
                    String value = s.substring(pos + 1).trim();
                    if (null != filter) {
                        name = filter.filter(name);
                    }
                    if (!Ws.isBlank(name)) {
                        re.put(name, value);
                    }
                }
            }
        }
        return re;
    }

}
