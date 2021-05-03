package org.nutz.walnut.ext.old.wiki;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.plugins.zdoc.NutDSet;
import org.nutz.plugins.zdoc.html.AbstractHtmlDSetRender;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class WnHtmlDSetRender extends AbstractHtmlDSetRender {
    
    private static final Log log = Wlog.getEXT();
    protected WnIo io;
    protected WnObj sourceHome;
    protected WnObj dstHome;

    @Override
    protected Tmpl loadTmpl(String tmplDirName, String tmplName, String dftTmplName) {
        Tmpl t = loadTmpl(Disks.appendPath(tmplDirName, tmplName));
        if (t == null) {
            InputStream ins = getClass().getClassLoader().getResourceAsStream("org/nutz/plugins/zdoc/html/" + dftTmplName);
            return Tmpl.parse(Streams.readAndClose(new InputStreamReader(ins)));
        }
        return t;
    }

    @Override
    protected void copyToTarget(String ph, String regex) {
        WnObj tmp = sourceHome;
        if (Strings.isBlank(ph)) {
            ph = ".";
        }
        else {
            tmp = io.fetch(sourceHome, ph);
        }
        if (tmp == null) {
            log.debugf("not such ph=%s", ph);
        }
        else if (!tmp.isDIR()) {
            log.debugf("not dir ph=%s", ph);
        }
        else {
            log.infof("ph=%s regex=%s", ph, regex);
            
            for (WnObj f : io.getChildren(tmp, regex)) {
                if (f.isFILE()) {
                    copyResource(ph + "/" + f.name(), f);
                }
            }
        }
    }

    @Override
    protected void writeToTarget(String ph, String html) {
        io.writeText(io.createIfNoExists(dstHome, ph, WnRace.FILE), html);
    }

    @Override
    protected void copyResource(String rsph) {
        copyResource(rsph, io.fetch(sourceHome, rsph));
    }
    
    protected void copyResource(String rsph, WnObj f) {
        if (f == null) {
            log.infof("no such rsph=%s", rsph);
        }
        if (f.isMount()) {
            io.writeAndClose(io.createIfNoExists(dstHome, rsph, WnRace.FILE), io.getInputStream(f, 0));
        }
        else {
            io.copyData(f, io.createIfNoExists(dstHome, rsph, WnRace.FILE));
        }
    }

    @Override
    protected void checkTarget(String target) {
        log.infof("target=%s", target);
    }

    @Override
    protected Tmpl loadTmpl(String tmplPath) {
        String path = tmplPath;
        if (path.startsWith("/"))
            path = path.substring(1);
        WnObj wobj = io.fetch(sourceHome, path);
        if (wobj == null || !wobj.isFILE()) {
            return null;
        }
        return Tmpl.parse(io.readText(wobj));
    }

    @Override
    protected void checkPrimerObj(NutDSet ds) {
        this.sourceHome = (WnObj) ds.getPrimerObj();
    }

    public void setIo(WnIo io) {
        this.io = io;
    }
    
    public void setDst(WnObj dstHome) {
        this.dstHome = dstHome;
    }
}
