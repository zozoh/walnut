package org.nutz.walnut.ext.site;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public class ShCtx {

    public String _rph;

    public String homePath;

    public String hdlName;

    public String[] args;

    public WnSystem sys;

    public WnObj oCurrent;

    public WnObj oHome;

    public WnObj oConf;

    public SiteConf conf;

    private Map<String, Document> _tmpls;

    private Map<String, Document> _libs;;

    public Map<String, String> jsLibs;

    public File dRenderHome;

    public ShCtx() {
        _tmpls = new HashMap<String, Document>();
        _libs = new HashMap<String, Document>();
        jsLibs = new LinkedHashMap<String, String>();
    }

    public Document getTemplateDom() {
        String key = conf.getTemplateName(_rph);
        Document doc = _tmpls.get(key);
        if (null == doc) {
            synchronized (this) {
                doc = _tmpls.get(key);
                if (null == doc) {
                    WnObj oTmpl = sys.io.check(oHome, "template/" + key + "/" + key + ".html");
                    String html = sys.io.readText(oTmpl);
                    doc = Jsoup.parse(html);
                    _tmpls.put(key, doc);
                }
            }
        }
        return doc.clone();
    }

    public Document getLibraryDom(String name) {
        Document doc = _libs.get(name);
        if (null == doc) {
            synchronized (this) {
                doc = _libs.get(name);
                if (null == doc) {
                    WnObj oLib = sys.io.check(oHome, "lib/" + name + "/" + name + ".html");
                    String html = sys.io.readText(oLib);
                    doc = Jsoup.parse(html);
                    _libs.put(name, doc);
                }
            }
        }
        return doc.clone();
    }

}
