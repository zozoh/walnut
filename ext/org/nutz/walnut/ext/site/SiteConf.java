package org.nutz.walnut.ext.site;

import java.io.Reader;
import java.util.regex.Pattern;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;

public class SiteConf {

    private String title;

    private String theme;

    private SiteData[] data;

    private SiteRule[] rules;

    public SiteConf(Reader reader) {
        try {
            PropertiesProxy pp = new PropertiesProxy(reader);
            title = pp.get("title", "A Site");
            theme = pp.get("theme");

            // data
            String[] sss = Strings.splitIgnoreBlank(pp.get("data"), "\n");
            data = new SiteData[sss.length];
            for (int i = 0; i < sss.length; i++) {
                String[] ss = Strings.splitIgnoreBlank(sss[i], ":");
                SiteData sd = new SiteData();
                sd.type = ss[0];
                sd.name = ss[1];
                data[i] = sd;
            }

            // rules
            sss = Strings.splitIgnoreBlank(pp.get("rules"), "\n");
            rules = new SiteRule[sss.length];
            for (int i = 0; i < sss.length; i++) {
                String[] ss = Strings.splitIgnoreBlank(sss[i], ":");
                SiteRule sr = new SiteRule();
                sr.regex = Pattern.compile(ss[0]);
                sr.templateName = ss[1];
                rules[i] = sr;
            }
        }
        finally {
            Streams.safeClose(reader);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getTheme() {
        return theme;
    }

    public SiteData[] getData() {
        return data;
    }

    public SiteRule[] getRules() {
        return rules;
    }

    public String getTemplateName(String rph) {
        for (SiteRule sr : rules) {
            if (sr.regex.matcher(rph).find()) {
                return sr.templateName;
            }
        }
        throw Er.create("e.site.tmpl.nofound", rph);
    }

}
