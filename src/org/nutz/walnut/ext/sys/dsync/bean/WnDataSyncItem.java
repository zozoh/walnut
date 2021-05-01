package org.nutz.walnut.ext.sys.dsync.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Ws;

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
        this.beanSha1 = Lang.sha1(beanJson);

        this.meta = o.omit(Wobj.CORE_FIELDS);
        String metaJson = Json.toJson(this.meta, jfmt);
        this.metaSha1 = Lang.sha1(metaJson);

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
