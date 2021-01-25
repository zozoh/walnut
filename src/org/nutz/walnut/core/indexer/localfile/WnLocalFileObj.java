package org.nutz.walnut.core.indexer.localfile;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.json.ToJson;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnObjId;
import org.nutz.walnut.util.Wn;

@ToJson
public class WnLocalFileObj extends NutMap implements WnObj {

    private WnObj oHome;

    private File dHome;

    private File file;

    private MimeMap mimes;

    private String _id; // 用来缓存一下

    private String phHome;

    private String phFile;

    private String rph;

    public WnLocalFileObj(WnObj oHome, File dHome, File f, MimeMap mimes) {
        this.oHome = oHome;
        this.dHome = dHome;
        this.file = f;
        this.mimes = mimes;
        this.phHome = Files.getAbsPath(dHome);
        this.phFile = Files.getAbsPath(file);
        // 整理路径中的 ..
        this.phHome = Disks.getCanonicalPath(this.phHome);
        this.phFile = Disks.getCanonicalPath(this.phFile);
        this.rph = Disks.getRelativePath(phHome, phFile);
        if (file.isDirectory() && !this.rph.endsWith("/")) {
            this.rph += "/";
        }
        this._id = oHome.id() + ":" + rph;

        // 填充一下自己，防止有贱人 get(key)
        this._fill_vals(this);
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
    public WnObjId OID() {
        return new WnObjId(oHome.id(), this.rph);
    }

    @Override
    public String myId() {
        return rph;
    }

    @Override
    public boolean hasMountRootId() {
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
        if (null != _parent && !_parent.isSameId(oHome)) {
            String pph = _parent.path();
            String rph;
            /**
             * 父也是一个本地文件，那么就比较一下rph 来获取 自己真正相对与父的路径
             * <p>
             * 这个特殊的判断是针对下面的深层链接做的优化
             * 
             * <pre>
             * /rs/ti/
             *     +----+
             *          | link
             *          v
             * /mnt/ti/src/view/creation.o
             *      +->mount: file://d:/git/github/titnaium/
             * </pre>
             * 
             * 当获取 fetch(null, "/rs/ti/view/creation.o") 时导致的一系列问题
             * 
             */
            if (_parent instanceof WnLocalFileObj) {
                WnLocalFileObj lfp = (WnLocalFileObj) _parent;
                // 如果映射不一致，那么不能够啊
                // 如果是顶级，那么两个 mount 都是 null
                if (!Lang.equals(lfp.mount(), this.mount())) {
                    throw Lang.impossible();
                }
                // 我的相对路径竟然不是以父的相对路径开始的？ 不能够啊
                if (!this.rph.startsWith(lfp.rph)) {
                    throw Lang.impossible();
                }
                // 得到自己的相对路径
                rph = this.rph.substring(lfp.rph.length());
            }
            // 直接将自己的名字拼合上父的路径
            else {
                rph = this.name();
            }
            return Wn.appendPath(pph, rph);
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
        if (null != _parent) {
            /**
             * 父也是一个本地文件，那么就比较一下rph 来获取 自己真正相对与父的路径
             * <p>
             * 这个特殊的判断是针对下面的深层链接做的优化
             * 
             * <pre>
             * /rs/ti/
             *     +----+
             *          | link
             *          v
             * /mnt/ti/src/view/creation.o
             *      +->mount: file://d:/git/github/titnaium/
             * </pre>
             * 
             * 当获取 fetch(null, "/rs/ti/view/creation.o") 时导致的一系列问题
             * 
             */
            if (_parent instanceof WnLocalFileObj) {
                WnLocalFileObj lfp = (WnLocalFileObj) _parent;
                // 如果映射不一致，那么不能够啊
                if (!lfp.mount().equals(this.mount())) {
                    throw Lang.impossible();
                }
                // 我的相对路径比父的还长？ 不能够啊
                if (!this.rph.startsWith(lfp.rph)) {
                    throw Lang.impossible();
                }
                // 得到自己的相对父路径
                String rph2 = this.rph.substring(lfp.rph.length());

                // 如果这个路径还没有用尽，则搞一个新的 parent 出来
                int pos = rph2.lastIndexOf('/', rph2.length() - 2);
                if (pos > 0) {
                    File d = this.file.getParentFile();
                    WnLocalFileObj p2 = new WnLocalFileObj(oHome, dHome, d, mimes);
                    p2.setParent(_parent);
                    return p2;
                }
                // 如果用尽了
                else {
                    return this._parent;
                }
            }
            // 计算自己相对于父的相对路径
            String _p_rph = Disks.getRelativePath(this._parent.path(), this.path());
            // 看看自己的 rph 是否用尽
            int pos = _p_rph.lastIndexOf('/', _p_rph.length() - 1);
            // rph 用尽了，直接返回自己的父就好了
            if (pos <= 0) {
                return _parent;
            }
            // rph 未用尽，那么试图搞一个自己的父目录对象
            File fP = this.file.getParentFile();
            WnLocalFileObj oP = new WnLocalFileObj(oHome, dHome, fP, mimes);
            oP.setParent(this._parent);
            return oP;
        }
        // 已经到达根了
        File p = file.getParentFile();
        if (p.equals(this.dHome)) {
            return oHome;
        }
        return new WnLocalFileObj(oHome, dHome, p, mimes);
    }

    /**
     * 有时候链接文件，需要修改这个文件的父，以便呈现链接后的目录
     */
    WnObj _parent;

    @Override
    public void setParent(WnObj parent) {
        this._parent = parent;
    }

    @Override
    public List<WnObj> parents() {
        List<WnObj> list = new LinkedList<>();
        this.loadParents(list, false);
        return list;
    }

    @Override
    public int getCustomizedPrivilege(WnAccount u) {
        return oHome.getCustomizedPrivilege(u);
    }

    @Override
    public WnObj loadParents(List<WnObj> list, boolean force) {
        WnObj p = this.parent();
        p.loadParents(list, force);
        if (null != list) {
            list.add(p);
        }
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
    public WnObj mount(String mnt) {
        return this;
    }

    @Override
    public WnObj mountRootId(String mrid) {
        return this;
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
        if (null == sha1) {
            this.remove("sha1");
        } else {
            this.setv("sha1", sha1);
        }
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
        _fill_vals(map);

        if (null != regex)
            return map.pickBy(regex);

        return map;
    }

    private void _fill_vals(NutBean map) {
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
    }

    public String toJson(JsonFormat jfmt) {
        NutMap map = toMap(null);
        return Json.toJson(map, jfmt);
    }

    public String toString() {
        return String.format("%s;ID(%s)==%s", path(), id(), mount());
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
    public WnObj lastModified(long lm) {
        // 啥都不干就对了
        return this;
    }

    @Override
    public WnObj update(Map<? extends String, ? extends Object> map) {
        // 啥都不干就对了
        return this;
    }

    @Override
    public WnObj updateBy(WnObj o) {
        // 啥都不干就对了
        return this;
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
        // 啥都不干就对了
        return this;
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

    @Override
    public int compareTo(WnObj o) {
        String nm1 = this.name();
        String nm2 = o.name();
        if (null == nm1) {
            if (null == nm2) {
                return 0;
            }
            return -1;
        }
        if (null == nm2) {
            return 1;
        }
        return nm1.compareTo(nm2);
    }
}
