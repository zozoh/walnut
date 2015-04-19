package org.nutz.walnut.api.io;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

public class WnObj extends NutMap {

    public String checkString(String key) {
        String str = getString(key);
        if (null == str)
            throw Er.create("e.io.obj.nokey", key);
        return str;
    }

    public String id() {
        return this.getString("id");
    }

    public WnObj id(String id) {
        this.setOrRemove("id", id);
        return this;
    }

    public boolean isSameId(WnObj o) {
        return isSameId(o.id());
    }

    public boolean isSameId(String id) {
        return id().equals(id);
    }

    public String parentId() {
        return this.getString("pid");
    }

    public WnObj parentId(String pid) {
        this.setv("pid", pid);
        return this;
    }

    public boolean hasParent() {
        return null != parentId();
    }

    public String path() {
        return this.getString("path");
    }

    public WnObj path(String path) {
        this.setv("path", path);
        return this;
    }

    public String link() {
        return this.getString("ln");
    }

    public WnObj link(String lid) {
        this.setOrRemove("ln", lid);
        return this;
    }

    public String name() {
        return this.getString("nm");
    }

    private static final Pattern P_NM = Pattern.compile("[/\\\\]");

    public WnObj name(String nm) {
        if (Strings.isBlank(nm))
            throw Er.create("e.io.obj.nm.blank");

        if (nm.equals(".") || nm.equals("..") || P_NM.matcher(nm).find())
            throw Er.create("e.io.obj.nm.invalid", nm);

        this.setOrRemove("nm", nm);
        return this;
    }

    public boolean isType(String tp) {
        String mytp = type();
        if (null == mytp)
            return null == tp;
        if (null == tp)
            return false;
        return mytp.equals(tp);
    }

    public String type() {
        return this.getString("tp");
    }

    public WnObj type(String tp) {
        this.setOrRemove("tp", tp);
        return this;
    }

    public String mime() {
        return this.getString("mime");
    }

    public WnObj mime(String mime) {
        this.setOrRemove("mime", mime);
        return this;
    }

    public boolean hasSha1() {
        return this.containsKey("sha1");
    }

    public String sha1() {
        return this.getString("sha1");
    }

    public WnObj sha1(String sha1) {
        this.setOrRemove("sha1", sha1);
        return this;
    }

    public boolean isSameSha1(String sha1) {
        if (null == sha1)
            return false;
        String mySha1 = sha1();
        if (null == mySha1)
            return false;
        return mySha1.equals(sha1);
    }

    public boolean hasData() {
        return this.containsKey("data");
    }

    public String data() {
        return this.getString("data");
    }

    public WnObj data(String data) {
        this.setOrRemove("data", data);
        return this;
    }

    public long len() {
        return this.getLong("len");
    }

    public WnObj len(long len) {
        this.put("len", len);
        return this;
    }

    public int remain() {
        return this.getInt("remain");
    }

    public WnObj remain(int remain) {
        this.put("remain", remain);
        return this;
    }

    public String creator() {
        return this.getString("c");
    }

    public WnObj creator(String creator) {
        this.setOrRemove("c", creator);
        return this;
    }

    public String group() {
        return this.getString("g");
    }

    public WnObj group(String grp) {
        this.setOrRemove("g", grp);
        return this;
    }

    public int mode() {
        return this.getInt("md");
    }

    public WnObj mode(int md) {
        this.setOrRemove("md", md);
        return this;
    }

    public String d0() {
        return this.getString("d0");
    }

    public WnObj d0(String d0) {
        this.setv("d0", d0);
        return this;
    }

    public String d1() {
        return this.getString("d1");
    }

    public WnObj d1(String d1) {
        this.setv("d1", d1);
        return this;
    }

    public WnObj update(NutMap map) {
        this.putAll(map);
        return this;
    }

    public String[] labels() {
        return this.getArray("lbs", String.class);
    }

    public WnObj labels(String[] lbs) {
        this.setOrRemove("lbs", lbs);
        return this;
    }

    public Date createTime() {
        return this.getAs("ct", Date.class);
    }

    public WnObj createTime(Date ct) {
        this.setOrRemove("ct", ct);
        return this;
    }

    public Date lastModified() {
        return this.getAs("lm", Date.class);
    }

    public long nanoStamp() {
        return this.getLong("nano");
    }

    public WnObj nanoStamp(long nano) {
        this.setv("nano", nano);
        this.setv("lm", new Date(nano / 1000000L));
        return this;
    }

    public boolean equals(Object obj) {
        if (obj instanceof WnObj) {
            WnObj o = (WnObj) obj;
            if (o.size() != size())
                return false;
            for (String key : o.keySet()) {
                if (!Lang.equals(o.get(key), get(key)))
                    return false;
            }
            return true;
        }
        return false;
    }

    public NutMap toMap(String regex) {
        NutMap map = new NutMap();
        if (Strings.isBlank(regex)) {
            map.putAll(this);
        } else {
            for (Map.Entry<String, Object> en : this.entrySet()) {
                String key = en.getKey();
                if (key.matches(regex))
                    map.put(key, en.getValue());
            }
        }
        return map;
    }

    public String toString() {
        return String.format("%s:%s[%s]", id(), name(), type());
    }
}
