package com.site0.walnut.ext.data.site.render;

import java.util.HashMap;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.www.JvmWnmlRuntime;
import com.site0.walnut.ext.data.www.WnmlRuntime;
import com.site0.walnut.ext.data.www.WnmlService;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;

public class SiteRendering {

    WnIo io;

    WnAuthSession session;

    WnObj siteHome;

    String appJsonPath;

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

    String before;

    String after;

    WnExecutable run;

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

        // 渲染 _app.json
        if (config.hasAppVars() && !Ws.isBlank(appJsonPath)) {
            LOG("Render _app.json");
            // 读取 _app.json
            WnObj oAppJson = io.check(this.siteHome, appJsonPath);
            NutMap app = io.readJson(oAppJson, NutMap.class);
            // 渲染 appvars
            NutMap gvars = this.getGloabalVars();
            Object appVars = Wn.explainObj(gvars, config.getAppVars());
            app.put("vars", appVars);
            // 写入到目标
            WnObj oAppTa = this.createTargetFile(oAppJson.name());
            String appJson = Json.toJson(app, JsonFormat.nice());
            io.writeText(oAppTa, appJson);
            LOGf(" >> %s :\n%s", oAppTa.path(), appJson);
        }

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
            // 递归链接目录
            WnObj oRealFrom = Wn.real(oFrom, io, new HashMap<>());
            io.copyData(oRealFrom, oTo);
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

    public String getAppJsonPath() {
        return appJsonPath;
    }

    public void setAppJsonPath(String appJsonPath) {
        this.appJsonPath = appJsonPath;
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

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
        if (Ws.isBlank(before)) {
            _before = null;
        } else {
            _before = WnTmpl.parse(before);
        }
    }

    public boolean hasBefore() {
        return null != _before;
    }

    private WnTmpl _before;

    public WnTmpl getBeforeTmpl() {
        return _before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
        if (Ws.isBlank(after)) {
            _after = null;
        } else {
            _after = WnTmpl.parse(after);
        }
    }

    public boolean hasAfter() {
        return null != _after;
    }

    private WnTmpl _after;

    public WnTmpl getAfterTmpl() {
        return _after;
    }

    public boolean canRunCommand() {
        return null != run;
    }

    public String exec2(String cmdText) {
        return run.exec2(cmdText);
    }

    public WnExecutable getRun() {
        return run;
    }

    public void setRun(WnExecutable run) {
        this.run = run;
    }

}
