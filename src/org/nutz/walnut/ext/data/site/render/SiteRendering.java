package org.nutz.walnut.ext.data.site.render;

import java.util.List;
import org.nutz.lang.util.NutMap;
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
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Ws;

public class SiteRendering {

    WnIo io;

    WnAuthSession session;

    WnObj siteHome;

    String archiveSetName;
    String[] archiveIds;

    boolean willCopyFiles;

    boolean willRenderPages;

    SitePageRenderConfig config;

    WnOutputable out;

    WnObj targetHome;

    String[] langs;

    String markKey;
    
    boolean willRecur;

    WnmlService wnmls;

    WnmlRuntime wnmlRuntime;

    int I; // 输出计数
    /**
     * 指定文档渲染模式下，记录渲染的目标文档 <br>
     * <code>{arID:[path1,path2,paht3]</code>
     */
    NutMap results;

    /**
     * JSON 模式将不输出日志，而是最后汇总一个json(results)
     */
    boolean jsonMode;

    public SiteRendering(WnSystem sys, SitePageRenderConfig conf) {
        this.io = sys.io;
        this.out = sys.out;
        this.session = sys.session;
        this.config = conf;
        this.wnmls = new WnmlService();
        this.wnmlRuntime = new JvmWnmlRuntime(sys);
        this.I = 0;
        this.results = new NutMap();
    }

    public boolean isJsonMode() {
        return jsonMode;
    }

    public void setJsonMode(boolean jsonMode) {
        this.jsonMode = jsonMode;
    }

    public void addResult(WnObj oAr, List<String> paths) {
        if (jsonMode) {
            results.put(oAr.id(), paths);
        }
    }

    public boolean hasResults() {
        return null != results && !results.isEmpty();
    }

    public NutMap getResults() {
        return results;
    }

    public void LOGf(String fmt, Object... args) {
        if (!jsonMode) {
            out.printlnf(fmt, args);
        }
    }

    public void LOG(String msg) {
        if (!jsonMode) {
            out.println(msg);
        }
    }

    public void render() {
        // 防守
        if (!this.siteHome.isDIR()) {
            throw Er.create("e.site.render.SiteNotDir", siteHome);
        }
        if (!this.targetHome.isDIR()) {
            throw Er.create("e.site.render.TargetNotDir", targetHome);
        }

        String[] ids = null == archiveIds ? new String[0] : archiveIds;

        // 分析归档ID
        boolean arMode = !Ws.isBlank(this.archiveSetName) || ids.length > 0;

        // 复制文件
        if (config.hasCopyFiles()) {
            if (!arMode || this.willCopyFiles) {
                LOGf("Copy %d file or dirs", config.getCopyFiles().length);
                for (String cpf : config.getCopyFiles()) {
                    String[] ss = Ws.splitIgnoreBlank(cpf, "=>");
                    String phFrom = ss[0];
                    String phTo = ss.length > 1 ? ss[1] : null;
                    doCopyFile(phFrom, phTo);
                }
            }
        }

        // 渲染所有归档
        if (config.hasArchives()) {
            LOGf("Render %d archive set", config.getArchives().length);
            for (SiteRenderArchive ar : config.getArchives()) {
                if (null != archiveSetName && !ar.isSameName(archiveSetName)) {
                    continue;
                }
                SiteArchiveRendering arr = new SiteArchiveRendering(this, ar);
                arr.renderArchives(ids);
            }
        }
    }

    void doCopyFile(String phFrom, String phTo) {
        // 读取源文件
        WnObj oFrom;
        // 绝对位置
        if (phFrom.startsWith("/") || phFrom.startsWith("~/")) {
            String aph = Wn.normalizeFullPath(phFrom, session);
            oFrom = io.check(null, aph);
        }
        // 来自站点
        else {
            oFrom = io.check(this.siteHome, phFrom);
            if (Ws.isBlank(phTo)) {
                phTo = phFrom;
            }
        }

        // 创建目标
        WnObj oTo = io.createIfNoExists(targetHome, phTo, oFrom.race());
        _copy_obj(oFrom, oTo);

    }

    void _copy_obj(WnObj oFrom, WnObj oTo) {
        LOGf(" - copy : %s >> %s", oFrom.path(), oTo.path());
        // 目录递归
        if (oFrom.isDIR() && oTo.isDIR()) {
            List<WnObj> children = io.getChildren(oFrom, null);
            for (WnObj child : children) {
                String nm = child.name();
                WnObj oToSub = io.createIfNoExists(oTo, nm, child.race());
                _copy_obj(child, oToSub);
            }
        }
        // 文件复制
        else if (oFrom.isFILE() && oTo.isFILE()) {
            io.copyData(oFrom, oTo);
        }
    }

    public String getArchiveSetName() {
        return archiveSetName;
    }

    public void setArchiveSetName(String archiveSetName) {
        this.archiveSetName = archiveSetName;
    }

    public String[] getArchiveIds() {
        return archiveIds;
    }

    public void setArchiveIds(String[] archiveIds) {
        this.archiveIds = archiveIds;
    }

    public boolean isWillCopyFiles() {
        return willCopyFiles;
    }

    public void setWillCopyFiles(boolean willCopyFiles) {
        this.willCopyFiles = willCopyFiles;
    }

    public boolean isWillRenderPages() {
        return willRenderPages;
    }

    public void setWillRenderPages(boolean willRenderPages) {
        this.willRenderPages = willRenderPages;
    }

    public NutMap getGloabalVars() {
        NutMap re = new NutMap();
        if (config.hasVars()) {
            re.putAll(config.getVars());
        }
        re.put("CURRENT_DIR", siteHome.path());
        re.put("rs", "/");
        re.put("grp", session.getMyGroup());
        return re;
    }

    public WnObj getSiteHome() {
        return siteHome;
    }

    public void setSiteHome(WnObj siteHome) {
        this.siteHome = siteHome;
    }

    public void updateSiteHome(String path) {
        if (Ws.isBlank(path)) {
            path = config.getHome();
        }
        this.siteHome = checkObj(path);

    }

    public void updateSiteHome(WnObj oSiteHome) {
        if (null == oSiteHome) {
            String path = config.getHome();
            oSiteHome = checkObj(path);
        }
        this.siteHome = oSiteHome;

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
        // 绝对位置
        if (target.startsWith("~/") || target.startsWith("/")) {
            return Wn.checkObj(io, session, target);
        }
        // 站内文件
        else {
            return io.check(this.siteHome, target);
        }
    }

    protected WnObj createTargetFile(String ph) {
        return io.createIfNoExists(targetHome, ph, WnRace.FILE);
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

    public boolean hasMarkKey() {
        return !Ws.isBlank(markKey);
    }

    public String getMarkKey() {
        return markKey;
    }

    public void setMarkKey(String markKey) {
        // 标记键不能是标准字段
        if (null != markKey && Wobj.isReserveKey(markKey)) {
            throw Er.create("e.cmd.site.render.MarkReserveKey", markKey);
        }
        this.markKey = markKey;
    }

    public boolean isWillRecur() {
        return willRecur;
    }

    public void setWillRecur(boolean willRecur) {
        this.willRecur = willRecur;
    }

    
}

