package com.site0.walnut.ext.sys.dsync.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;

public class WnDataSyncItem {

    private WnRace race;

    private String path;

    private WnObj obj;

    private NutBean bean;

    // private String beanJson;

    private String beanSha1;

    private NutBean meta;

    // private String metaJson;

    private String metaSha1;

    private String sha1;

    private long len;

    public WnDataSyncItem() {}

    public WnDataSyncItem(String input) {
        this.parse(input);
    }

    public WnDataSyncItem(WnObj o, String homePath) {
        this.setObj(o, homePath);
    }

    /**
     * @return 一个用来过滤本记录的对象
     */
    public NutMap getTestMap() {
        NutMap map = new NutMap();
        map.put("race", race.toString());
        map.put("path", path);
        return map;
    }

    /**
     * 从包里加载的记录通常没有obj对象，这时候，可以通过这个函数加载出来。
     * <p>
     * 通常是，从压缩包里加载的树，恢复后，通过遍历树可以得到新的ID映射关系
     * 
     * @return 加载后的对象
     */
    public WnObj loadObj(WnIo io, NutBean vars) {
        String aph = Wn.normalizeFullPath(path, vars);
        this.obj = io.fetch(null, aph);
        return this.obj;
    }

    public WnObj loadObj(WnIo io, WnSession se) {
        NutBean vars = se.getEnv();
        return loadObj(io, vars);
    }

    public WnObj loadObj(WnSystem sys) {
        return loadObj(sys.io, sys.session);
    }

    /**
     * @param o
     * @param homePath
     *            主目录路径，必须以 "/" 结尾
     */
    public void setObj(WnObj o, String homePath) {
        this.race = o.race();
        this.path = o.path();
        this.obj = o;
        if (!this.path.startsWith(homePath)) {
            throw Er.create("e.dsync.out-of-home", this.path + " <-> " + homePath);
        }
        this.path = "~/" + this.path.substring(homePath.length());

        JsonFormat jfmt = JsonFormat.compact().setQuoteName(true);

        this.bean = o.pick(Wobj.CORE_FIELDS);
        String beanJson = Json.toJson(this.bean, jfmt);
        this.beanSha1 = Wlang.sha1(beanJson);

        this.meta = o.omit(Wobj.CORE_FIELDS);
        String metaJson = Json.toJson(this.meta, jfmt);
        this.metaSha1 = Wlang.sha1(metaJson);

        this.sha1 = Ws.sBlank(o.sha1(), null);
        this.len = o.len();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isf = this.isFile();
        sb.append(isf ? 'F' : 'D');
        sb.append(':').append(this.path);
        sb.append(";=BEAN(").append(beanSha1).append(')');
        sb.append(";=META(").append(metaSha1).append(')');
        if (isf) {
            sb.append(";=SHA1(").append(sha1).append(')');
            sb.append(";=LEN(").append(len).append(')');
        }
        return sb.toString();
    }

    private static String REG = "^([FD])"
                                + ":(.+)"
                                + ";=BEAN\\(([0-9a-z]{40})\\)"
                                + ";=META\\(([0-9a-z]{40})\\)"
                                + "("
                                + ";=SHA1\\(([a-z0-9]{4,})\\)"
                                + ";=LEN\\(([0-9]+)\\)"
                                + ")?$";

    private static Pattern _P = Pattern.compile(REG);

    public void parse(String input) {
        Matcher m = _P.matcher(input);
        if (!m.find())
            throw Er.create("e.dsync.input.invalid", input);

        // RACE
        this.race = "F".equals(m.group(1)) ? WnRace.FILE : WnRace.DIR;
        this.path = m.group(2);
        this.beanSha1 = m.group(3);
        this.metaSha1 = m.group(4);
        if (this.isFile()) {
            this.sha1 = m.group(6);
            if ("null".equals(this.sha1)) {
                this.sha1 = null;
            }
            this.len = Long.parseLong(m.group(7));
        }
    }

    public boolean isFile() {
        return WnRace.FILE == this.race;
    }

    public boolean isDir() {
        return WnRace.DIR == this.race;
    }

    public WnRace getRace() {
        return race;
    }

    public void setRace(WnRace race) {
        this.race = race;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasObj() {
        return null != obj;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

    public NutBean getBean() {
        return bean;
    }

    public void setBean(NutBean obj) {
        this.bean = obj;
    }

    public String getBeanSha1() {
        return beanSha1;
    }

    public void setBeanSha1(String objSha1) {
        this.beanSha1 = objSha1;
    }

    public NutBean getMeta() {
        return meta;
    }

    public void setMeta(NutBean meta) {
        this.meta = meta;
    }

    public String getMetaSha1() {
        return metaSha1;
    }

    public void setMetaSha1(String metaSha1) {
        this.metaSha1 = metaSha1;
    }

    public boolean hasSha1() {
        return !Ws.isBlank(sha1);
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }

}
