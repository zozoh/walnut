package org.nutz.walnut.ext.data.site.render;

import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class SiteRendering {

    WnIo io;

    WnAuthSession session;

    WnObj siteHome;

    SitePageRenderConfig config;

    WnOutputable out;

    WnObj targetHome;

    String[] langs;

    public SiteRendering(WnSystem sys, SitePageRenderConfig conf) {
        this.io = sys.io;
        this.out = sys.out;
        this.session = sys.session;
        this.config = conf;
    }

    public void render() {
        // 防守
        if (!this.siteHome.isDIR()) {
            throw Er.create("e.site.render.SiteNotDir",siteHome);
        }
        if (!this.targetHome.isDIR()) {
            throw Er.create("e.site.render.TargetNotDir",targetHome);
        }
        // 遍历归档
        if (config.hasArchives()) {
            for (SiteRenderArchive ar : config.getArchives()) {
                SiteArchiveRendering arr = new SiteArchiveRendering(this, ar);
                arr.renderArchives();
            }
        }
    }

    public WnObj getSiteHome() {
        return siteHome;
    }

    public void setSiteHome(WnObj siteHome) {
        this.siteHome = siteHome;
    }

    public SitePageRenderConfig getConfig() {
        return config;
    }

    public void setConfig(SitePageRenderConfig config) {
        this.config = config;
    }

    public WnOutputable getOut() {
        return out;
    }

    public void setOut(WnOutputable out) {
        this.out = out;
    }

    public WnObj getTargetHome() {
        return targetHome;
    }

    public void setTargetHome(WnObj targetHome) {
        this.targetHome = targetHome;
    }

    public void setTargetHome(String target) {
        this.targetHome = checkObj(target);

    }

    protected WnObj checkObj(String target) {
        return Wn.checkObj(io, session, target);
    }

    public String[] getLangs() {
        return langs;
    }

    public void setLangs(String[] langs) {
        this.langs = langs;
    }

    public void updateLangs(String[] langs) {
        if (null != langs && langs.length > 0) {
            this.langs = langs;
        } else {
            this.langs = config.getLangs();
        }
    }

}
