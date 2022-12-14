package org.nutz.walnut.ext.data.site.render;

import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.www.JvmWnmlRuntime;
import org.nutz.walnut.ext.data.www.WnmlRuntime;
import org.nutz.walnut.ext.data.www.WnmlService;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class SiteRendering {

    WnIo io;

    WnAuthSession session;

    WnObj siteHome;

    SitePageRenderConfig config;

    WnOutputable out;

    WnObj targetHome;

    String[] langs;

    WnmlService wnmls;

    WnmlRuntime wnmlRuntime;

    int I; // 输出计数

    public SiteRendering(WnSystem sys, SitePageRenderConfig conf) {
        this.io = sys.io;
        this.out = sys.out;
        this.session = sys.session;
        this.config = conf;
        this.wnmls = new WnmlService();
        this.wnmlRuntime = new JvmWnmlRuntime(sys);
        this.I = 0;
    }

    public void LOGf(String fmt, Object... args) {
        out.printlnf(fmt, args);
    }

    public void LOG(String msg) {
        out.println(msg);
    }

    public void render() {
        // 防守
        if (!this.siteHome.isDIR()) {
            throw Er.create("e.site.render.SiteNotDir", siteHome);
        }
        if (!this.targetHome.isDIR()) {
            throw Er.create("e.site.render.TargetNotDir", targetHome);
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

    public void updateTargetHome(String target) {
        if (Ws.isBlank(target)) {
            target = config.getTarget();
        }
        this.targetHome = checkObj(target);

    }

    protected WnObj checkObj(String target) {
        return Wn.checkObj(io, session, target);
    }

    protected WnObj createTargetFile(String ph) {
        String aph = Wn.normalizeFullPath(ph, session);
        return io.createIfNoExists(targetHome, aph, WnRace.FILE);
    }

    private String _wnml_input;

    public String getWnmlInput() {
        if (null == this._wnml_input) {
            WnObj oF = this.io.check(siteHome, config.getHtml());
            this._wnml_input = this.io.readText(oF);
        }
        return this._wnml_input;
    }

    public boolean hasLangs() {
        return null != langs && langs.length > 0;
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
