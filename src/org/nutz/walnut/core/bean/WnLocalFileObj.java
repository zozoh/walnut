package org.nutz.walnut.core.bean;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.json.ToJson;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

@ToJson
public class WnLocalFileObj extends NutMap implements WnObj {

    private WnObj oHome;

    private File dHome;

    private File file;

    private MimeMap mimes;

    private String _id; // 用来缓存一下

    private String rph;

    public WnLocalFileObj(WnObj oHome, File dHome, File f, MimeMap mimes) {
        this.oHome = oHome;
        this.dHome = dHome;
        this.file = f;
        this.mimes = mimes;
        this.rph = Disks.getRelativePath(dHome, file);
        this._id = oHome.id() + ":" + rph;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isRootNode() {
        return false;
    }

    @Override
    public String id() {
        return _id;
    }

    @Override
    public String myId() {
        return rph;
    }

    @Override
    public boolean hasMountHomeId() {
        return true;
    }

    @Override
    public boolean hasID() {
        return true;
    }

    @Override
    public boolean isSameId(WnObj o) {
        return id().equals(o.id());
    }

    @Override
    public boolean isSameId(String id) {
        return id().equals(id);
    }

    @Override
    public boolean isSameName(String nm) {
        return name().equals(nm);
    }

    @Override
    public boolean isMyParent(WnObj p) {
        return parent().isSameId(p);
    }

    @Override
    public boolean isMyAncestor(WnObj an) {
        if (null == an)
            return false;
        if (this.isMyParent(an))
            return true;
        if (this.hasParent()) {
            WnObj p = this.parent();
            if (null != p) {
                return p.isMyAncestor(an);
            }
        }
        return false;
    }

    @Override
    public String path() {
        if (null != _parent) {
            String pph = _parent.path();
            String nm = this.name();
            return Wn.appendPath(pph, nm);
        }
        String ph = oHome.path();
        return Wn.appendPath(ph, rph);
    }

    @Override
    public String getRegularPath() {
        String aph = this.path();
        if (this.isDIR() && !aph.endsWith("/"))
            return aph + "/";
        return aph;
    }

    @Override
    public String getFormedPath(boolean isRegular) {
        String path = isRegular ? this.getRegularPath() : this.path();
        String d0 = this.d0();
        if ("home".equals(d0)) {
            String d1 = this.d1();
            String home = Wn.appendPath("/", d0, d1);
            if (path.startsWith(home)) {
                return "~" + path.substring(home.length());
            }
        }
        return path;
    }

    @Override
    public String name() {
        String nm = this.getString("nm");
        if (!Strings.isBlank(nm))
            return nm;
        return file.getName();
    }

    @Override
    public WnObj name(String nm) {
        if (Strings.isBlank(nm)) {
            this.remove("nm");
        } else {
            this.put("nm", nm);
        }
        return this;
    }

    @Override
    public WnRace race() {
        if (file.isFile())
            return WnRace.FILE;
        if (file.isDirectory())
            return WnRace.DIR;
        throw Er.create("e.io.localfile.weirdFile", this.file);
    }

    @Override
    public boolean isRace(WnRace race) {
        return race() == race;
    }

    @Override
    public boolean isDIR() {
        return isRace(WnRace.DIR);
    }

    @Override
    public boolean isFILE() {
        return isRace(WnRace.FILE);
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean hasParent() {
        return true;
    }

    @Override
    public WnObj parent() {
        if ("./".equals(rph)) {
            return oHome;
        }
        File p = file.getParentFile();
        return new WnLocalFileObj(oHome, dHome, p, mimes);
    }

    /**
     * 有时候链接文件，需要修改这个文件的父，以便呈现链接后的目录
     */
    private WnObj _parent;

    @Override
    public void setParent(WnObj parent) {
        this._parent = parent;
    }

    @Override
    public int getCustomizedPrivilege(WnAccount u) {
        return oHome.getCustomizedPrivilege(u);
    }

    @Override
    public WnObj loadParents(List<WnObj> list, boolean force) {
        WnObj p = this.parent();
        p.loadParents(list, force);
        list.add(p);
        return p;
    }

    @Override
    public String parentId() {
        return parent().id();
    }

    @Override
    public String mount() {
        return oHome.mount();
    }

    @Override
    public String mountRootId() {
        return oHome.id();
    }

    @Override
    public boolean isMount() {
        return true;
    }

    @Override
    public long len() {
        return file.length();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public WnObj clone() {
        return new WnLocalFileObj(oHome, dHome, file, mimes);
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String link() {
        return null;
    }

    @Override
    public boolean isType(String tp) {
        String mytp = type();
        if (null == mytp)
            return null == tp;
        if (null == tp)
            return false;
        // 用正则
        if (tp.startsWith("^")) {
            return mytp.matches(tp);
        }
        // 精确匹配
        return mytp.equals(tp);
    }

    @Override
    public boolean hasType() {
        return !Strings.isBlank(type());
    }

    @Override
    public String type() {
        if (this.isFILE())
            return Files.getSuffixName(file);
        return null;
    }

    @Override
    public String mime() {
        if (this.isFILE())
            return mimes.getMime(type());
        return null;
    }

    @Override
    public boolean hasMime() {
        return !Strings.isBlank(mime());
    }

    @Override
    public boolean isMime(String mime) {
        String myMime = mime();
        if (null == myMime)
            return null == mime;
        if (null == mime)
            return false;
        // 用正则
        if (mime.startsWith("^")) {
            return myMime.matches(mime);
        }
        // 精确匹配
        return myMime.equals(mime);
    }

    public boolean hasSha1() {
        return !Strings.isBlank(sha1());
    }

    public String sha1() {
        return this.getString("sha1");
    }

    public WnObj sha1(String sha1) {
        this.setv("sha1", sha1);
        return this;
    }

    public boolean isSameSha1(String sha1) {
        String mySha1 = sha1();
        if (null == sha1)
            return null == mySha1;

        if (null == mySha1)
            return false;
        return mySha1.equals(sha1);
    }

    @Override
    public boolean hasThumbnail() {
        return false;
    }

    @Override
    public String thumbnail() {
        return null;
    }

    @Override
    public String creator() {
        return oHome.creator();
    }

    @Override
    public String mender() {
        return oHome.mender();
    }

    @Override
    public String group() {
        return oHome.group();
    }

    @Override
    public int mode() {
        return oHome.mode();
    }

    @Override
    public String d0() {
        return oHome.d0();
    }

    @Override
    public String d1() {
        return oHome.d1();
    }

    @Override
    public String[] dN() {
        return oHome.dN();
    }

    @Override
    public String[] labels() {
        return null;
    }

    @Override
    public long createTime() {
        return file.lastModified();
    }

    @Override
    public long expireTime() {
        return -1;
    }

    @Override
    public long syncTime() {
        return -1;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isExpiredBy(long now) {
        return false;
    }

    @Override
    public NutMap toMap4Update(String regex) {
        return toMap(regex);
    }

    @Override
    public NutMap toMap(String regex) {
        NutMap map = new NutMap();
        map.put("m", mender());
        map.put("c", creator());
        map.put("g", group());
        map.put("id", id());
        map.put("nm", name());

        map.put("ph", path());
        map.put("ct", createTime());
        map.put("lm", lastModified());
        map.put("tp", type());
        map.put("d0", d0());
        map.put("d1", d1());
        map.put("md", mode());
        map.put("len", len());
        map.put("mnt", oHome.mount());
        map.put("race", race());
        map.put("mime", mime());

        if (null != regex)
            return map.pickBy(regex);
        return map;
    }

    public String toJson(JsonFormat jfmt) {
        NutMap map = toMap(null);
        return Json.toJson(map, jfmt);
    }

    public String toString() {
        return String.format("%s;ID(%s)<%s/%s>", path(), id(), creator(), group());
    }

    @Override
    public boolean hasWriteHandle() {
        throw Lang.noImplement();
    }

    @Override
    public String getWriteHandle() {
        throw Lang.noImplement();
    }

    @Override
    public WnObj setWriteHandle(String hid) {
        throw Lang.noImplement();
    }

    @Override
    public boolean isRWMeta() {
        throw Lang.noImplement();
    }

    @Override
    public WnObj setRWMeta(boolean rwmeta) {
        throw Lang.noImplement();
    }

    @Override
    public boolean hasRWMetaKeys() {
        throw Lang.noImplement();
    }

    @Override
    public String getRWMetaKeys() {
        throw Lang.noImplement();
    }

    @Override
    public WnObj setRWMetaKeys(String regex) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj clearRWMetaKeys() {
        throw Lang.noImplement();
    }

    @Override
    public WnObj id(String id) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj path(String path) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj appendPath(String path) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj race(WnRace race) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj mount(String mnt) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj mountRootId(String mrid) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj lastModified(long lm) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj update(Map<? extends String, ? extends Object> map) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj update2(WnObj o) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj link(String lid) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj type(String tp) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj mime(String mime) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj thumbnail(String thumbnail) {
        throw Lang.noImplement();
    }

    @Override
    public boolean hasData() {
        throw Lang.makeThrow("data not supported anymore");
    }

    @Override
    public String data() {
        throw Lang.makeThrow("data not supported anymore");
    }

    @Override
    public boolean isSameData(String data) {
        throw Lang.makeThrow("data not supported anymore");
    }

    @Override
    public WnObj data(String data) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj len(long len) {
        throw Lang.noImplement();
    }

    @Override
    public int remain() {
        throw Lang.noImplement();
    }

    @Override
    public WnObj remain(int remain) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj creator(String creator) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj mender(String mender) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj group(String grp) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj mode(int md) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj d0(String d0) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj d1(String d1) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj labels(String[] lbls) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj createTime(long ct) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj expireTime(long expi) {
        throw Lang.noImplement();
    }

    @Override
    public WnObj syncTime(long st) {
        throw Lang.noImplement();
    }

}
