package com.site0.walnut.core.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.mapping.support.MountInfo;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.validate.WnMatch;

public class WnIoObj extends NutMap implements WnObj {

    private static final Log log = Wlog.getIO();

    /**
     * 这个索引管理器是当前对象对应的索引管理器
     * <p>
     * 为了能一直查找 Parent，索引管理器也应该有父子结构
     */
    // private WnIoIndexer indexer;
    private WnIo io;

    public WnIoObj() {
        super();
    }

    // public void setIndexer(WnIoIndexer indexer) {
    // this.indexer = indexer;
    // }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public NutMap toMap4Update(String regex) {
        NutMap map = new NutMap();

        // 准备匹配逻辑
        WnMatch m = Wobj.explainObjKeyMatcher(regex);

        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            // id 等是绝对不可以改的
            if (Wn.matchs(key, "^(ph|parent|id|race)$")) {
                continue;
            }
            // "_" 开头（表隐藏），无视
            else if (key.startsWith("_")) {
                continue;
            }
            // 否则只给出正则表达式匹配的部分
            else if (m.match(key)) {
                map.put(key, en.getValue());
            }
        }
        // 如果有 d0 和 d1，那么要重新设置一下确保正确
        if (map.has("d0") || map.has("d1")) {
            this.loadParents(null, false);
            map.put("d0", this.d0());
            map.put("d1", this.d1());
        }
        // 返回
        return map;
    }

    @Override
    public NutMap toMap(String regex) {
        // 准备匹配逻辑
        WnMatch m = Wobj.explainObjKeyMatcher(regex);

        // 准备返回结果
        NutMap map = new NutMap();

        // 依次判断
        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            if (m.match(key)) {
                map.put(key, en.getValue());
            }
        }

        // 搞定
        return map;
    }

    public String id() {
        return getString("id");
    }

    public WnObj id(String id) {
        setv("id", id);
        _oid = null;
        return this;
    }

    @Override
    public Object put(String key, Object value) {
        if ("id".equals(key)) {
            _oid = null;
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m.containsKey("id")) {
            _oid = null;
        }
        super.putAll(m);
    }

    private WnObjId _oid;

    public WnObjId OID() {
        if (null == _oid) {
            String id = this.id();
            if (!Strings.isBlank(id)) {
                _oid = new WnObjId(id);
            } else {
                return new WnObjId(null);
            }
        }
        return _oid;
    }

    public String myId() {
        return OID().getMyId();
    }

    public boolean hasMountRootId() {
        return OID().hasHomeId();
    }

    public boolean hasWriteHandle() {
        throw Wlang.noImplement();
    }

    public String getWriteHandle() {
        throw Wlang.noImplement();
    }

    public WnObj setWriteHandle(String hid) {
        throw Wlang.noImplement();
    }

    public boolean hasID() {
        return !Strings.isBlank(id());
    }

    public boolean isSameId(String id) {
        return id().equals(id);
    }

    public boolean isSameName(String nm) {
        return name().equals(nm);
    }

    public String checkString(String key) {
        String str = getString(key);
        if (null == str)
            throw Er.create("e.io.obj.nokey", key);
        return str;
    }

    public boolean isSameId(WnObj o) {
        return isSameId(o.id());
    }

    public boolean isFromLink() {
        return this.has("fromLink");
    }

    public String fromLink() {
        return this.getString("fromLink");
    }

    public WnObj fromLink(String link) {
        this.put("fromLink", link);
        return this;
    }

    public boolean isLink() {
        return !Strings.isBlank(link());
    }

    public String link() {
        return this.getString("ln");
    }

    public WnObj link(String lid) {
        this.setv("ln", lid);
        return this;
    }

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

    public boolean hasMime() {
        return !Strings.isBlank(mime());
    }

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
        String sha1 = sha1();
        return !Strings.isBlank(sha1);
    }

    public String sha1() {
        String sha1 = this.getString("sha1");
        // if (Wn.Io.isEmptySha1(sha1))
        // return Wn.Io.EMPTY_SHA1;
        return sha1;
    }

    public WnObj sha1(String sha1) {
        if (Wn.Io.isEmptySha1(sha1)) {
            this.setv("sha1", null);
        } else {
            this.setv("sha1", sha1);
        }
        return this;
    }

    public boolean isSameSha1(String sha1) {
        String mySha1 = sha1();
        if (Wn.Io.isEmptySha1(sha1))
            return Wn.Io.isEmptySha1(mySha1);

        if (Wn.Io.isEmptySha1(mySha1))
            return false;

        return mySha1.equals(sha1);
    }

    @Override
    public boolean hasThumbnail() {
        return !Strings.isBlank(thumbnail());
    }

    @Override
    public String thumbnail() {
        return this.getString("thumb");
    }

    @Override
    public WnObj thumbnail(String thumbnail) {
        this.setv("thumb", thumbnail);
        return this;
    }

    public boolean hasData() {
        throw Wlang.makeThrow("data not supported anymore");
    }

    public String data() {
        throw Wlang.makeThrow("data not supported anymore");
    }

    public WnObj data(String data) {
        throw Wlang.makeThrow("data not supported anymore");
    }

    public boolean isSameData(String data) {
        throw Wlang.makeThrow("data not supported anymore");
    }

    public long len() {
        return this.getLong("len", 0);
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

    public String mender() {
        return this.getString("m");
    }

    public WnObj mender(String mender) {
        this.setOrRemove("m", mender);
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

    public String[] dN() {
        String d0 = d0();
        String d1 = d1();

        if (Strings.isBlank(d0))
            return new String[0];
        if (Strings.isBlank(d1))
            return Wlang.array(d0);
        return Wlang.array(d0, d1);
    }

    public WnObj update(Map<? extends String, ? extends Object> map) {
        this.putAll(map);
        return this;
    }

    @Override
    public WnObj updateBy(WnObj o) {
        // 木有必要更新
        if (this == o || null == o) {
            return this;
        }
        // 更新全部元数据
        this.putAll(o);

        // 更新自己的私有属性
        if (o instanceof WnIoObj) {
            // this.indexer = ((WnIoObj) o).indexer;
            this.io = ((WnIoObj) o).io;
            this._parent = ((WnIoObj) o)._parent;
        } else {
            this._parent = o.parent();
        }

        // 确保自己和对方的 parent 不会重复
        if (null != this._parent)
            this._parent = this._parent.clone();

        // 返回自身以便链式赋值
        return this;
    }

    public String[] labels() {
        return this.getArray("lbls", String.class);
    }

    public WnObj labels(String[] lbls) {
        this.setOrRemove("lbls", lbls);
        return this;
    }

    public long createTime() {
        try {
            return this.getLong("ct", -1);
        }
        catch (Exception e) {
            return -1;
        }
    }

    public WnObj createTime(long ct) {
        this.setOrRemove("ct", ct);
        return this;
    }

    @Override
    public long syncTime() {
        try {
            return this.getLong("synt", -1);
        }
        catch (Exception e) {
            return -1;
        }
    }

    @Override
    public WnObj syncTime(long st) {
        this.setOrRemove("synt", st);
        return this;
    }

    public long expireTime() {
        try {
            return this.getLong("expi", -1);
        }
        catch (Exception e) {
            return -1;
        }
    }

    public WnObj expireTime(long expi) {
        this.setOrRemove("expi", expi);
        return this;
    }

    public boolean isExpired() {
        return isExpiredBy(Wn.now());
    }

    @Override
    public boolean isExpiredBy(long now) {
        long expi = expireTime();
        if (expi <= 0)
            return false;
        return expi < now;
    }

    public long lastModified() {
        try {
            return this.getLong("lm");
        }
        catch (Exception e) {
            return -1;
        }
    }

    @Override
    public WnObj lastModified(long lm) {
        this.setv("lm", lm);
        return this;
    }

    public long nanoStamp() {
        try {
            return this.getLong("nano");
        }
        catch (Exception e) {
            return -1;
        }
    }

    public WnObj nanoStamp(long nano) {
        this.setv("nano", nano);
        this.setv("lm", nano / 1000000L);
        return this;
    }

    public boolean equals(Object obj) {
        if (obj instanceof WnIoObj) {
            WnIoObj o = (WnIoObj) obj;
            if (o.size() != size())
                return false;
            for (String key : o.keySet()) {
                if (!Wlang.isEqual(o.get(key), get(key)))
                    return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        String id = id();
        return null == id ? -1 : id.hashCode();
    }

    public String toString() {
        String lnk = "";
        if (this.isLink()) {
            lnk = "->" + this.link();
        }
        String mnt = "";
        if (this.isMount()) {
            mnt = "::" + this.mount();
        }
        // if (null == this.indexer) {
        return String.format("%s;ID(%s)%s%s", name(), id(), mnt, lnk);
        // }
        // return String.format("%s;ID(%s)%s%s", path(), id(), mnt, lnk);
    }

    // -----------------------------------------
    // 下面的属性不要主动设置，用 nd() 方法设置
    // -----------------------------------------
    public String getPath() {
        return getString("ph");
    }

    public String path() {
        String ph = getPath();
        if (Strings.isBlank(ph)) {
            this.loadParents(null, false);
            ph = getString("ph");
        }
        return ph;
    }

    public WnObj path(String path) {
        setv("ph", path);
        return this;
    }

    @Override
    public String getRegularPath() {
        String aph = this.path();
        if (this.isDIR() && !aph.endsWith("/"))
            return aph + "/";
        return aph;
    }

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

    public WnObj appendPath(String path) {
        path(Wn.appendPath(path(), path));
        return this;
    }

    public String name() {
        return getString("nm");
    }

    public WnObj name(String nm) {
        setv("nm", nm);
        String ph = getString("ph");
        if (!Strings.isBlank(ph)) {
            path(Files.renamePath(ph, nm));
        }
        return this;
    }

    public WnRace race() {
        return getEnum("race", WnRace.class);
    }

    public WnObj race(WnRace race) {
        setv("race", race);
        return this;
    }

    public boolean isRace(WnRace race) {
        return race() == race;
    }

    public boolean isDIR() {
        return isRace(WnRace.DIR);
    }

    public boolean isFILE() {
        return isRace(WnRace.FILE);
    }

    public String parentId() {
        return getString("pid");
    }

    @Override
    public boolean hasParent() {
        return !Strings.isBlank(parentId());
    }

    @Override
    public boolean isMount() {
        if (this.has("mnt") || OID().hasHomeId())
            return true;

        // 如果设置了映射管理器，那么看看自己的父
        if (null != this.io && this.hasParent()) {
            WnObj p = this.parent();
            if (null != p) {
                return p.isMount();
            }
        }

        // 那就是木有咯
        return false;
    }

    @Override
    public boolean isMountEntry() {
        return this.has("mnt") && !OID().hasHomeId();
    }

    @Override
    public boolean isMountedObj() {
        return this.has("mnt") && OID().hasHomeId();
    }

    /**
     * null 表示未设置。 "" 表示读取了，但是没有映射
     */
    private String __mnt;

    public String mount() {
        String mnt = getString("mnt");
        if (!Strings.isBlank(mnt)) {
            return mnt;
        }
        if (null == __mnt) {
            // 看看父映射
            WnObj p = this;
            MountInfo mi = new MountInfo();
            while (!mi.hasIndexerAndBM() && p.hasParent()) {
                p = p.parent();
                if (null == p) {
                    break;
                }
                String pmnt = p.mount();
                mi.set(pmnt);
            }
            // 最后得到映射
            __mnt = mi.toString();
        }
        return __mnt;
    }

    public WnObj mount(String mnt) {
        mnt = Strings.sBlank(mnt, null);
        setv("mnt", mnt);
        return this;
    }

    public String mountRootId() {
        return OID().getHomeId();
    }

    public WnObj mountRootId(String mrid) {
        WnObjId oid = OID();
        oid.setHomeId(mrid);
        setv("id", oid.toString());
        return this;
    }

    public boolean isHidden() {
        String nm = name();
        return null != nm && nm.startsWith(".");
    }

    @Override
    public boolean isRootNode() {
        return !this.hasParent();
    }

    private WnObj _parent;

    public WnObj parent() {
        if (null == _parent && hasParent()) {
            String pid = this.parentId();
            if (this.io == null) {
                throw Wlang.makeThrow("NPE inner IO: %s/%s > %s",
                                      pid,
                                      this.id(),
                                      this.name());
            }
            // 自己的父就是根了
            if (this.io.isRoot(pid)) {
                return this.io.getRoot();
            }
            // 尝试获取自己的老父亲
            WnObj oP = io.get(pid);
            if (null == oP) {
                // oP = tree.get(pid);
                // throw Wlang.makeThrow("NPE parent: %s/%s > %s", pid,
                // this.id(), this.name());
                return null;
            }
            // 调用这个函数，触发 pid|ph... 等一系列属性自动修改
            this.setParent(oP);
        }
        return _parent;
    }

    @Override
    public List<WnObj> parents() {
        List<WnObj> list = new LinkedList<>();
        this.loadParents(list, false);
        return list;
    }

    /**
     * 根据给定账户，获取该账户对于当前对象的自定义权限。
     * <p>
     * 权限定义在<code>pvg</code>字段内，它支出如下形式
     * 
     * <ul>
     * <li><code>4a98..a123</code> : 直接是用户的 ID
     * <li><code>@SYS_ADMIN</code> : 角色【域账户】
     * <li><code>@[demo]   </code> : 用户名【域账户】
     * <li><code>+M0A      </code> : 用户所属职位或部门
     * </ul>
     * 
     * <b>权限示例</b>
     * 
     * <pre>
     * pvg : {
     *    "@SYS_ADMIN" : "0777",      // 八进制
     *    "$admin"     : 511,         // 十进制
     *    "@[demo]"    : "rwxr-xr-x", // 全文本
     *    "$[xiaobai]" : "rwx",       // 文本，相当于 "rwxrwxrwx"
     *    "+M0A"       : 7,           // 0-7 整数相当于 "0777"
     * }
     * </pre>
     * 
     * @param u
     *            账户对象
     * @param dftMode
     *            如果没有声明自定义权限，返回的默认权限
     * 
     * @return 一个十进制的权限码
     */
    public NutBean getCustomizedPrivilege() {
        // 防守: 没有指定自定义权限集合
        NutBean pvg = this.getAs("pvg", NutMap.class);
        if (this.hasParent()) {
            pvg = this.parent().joinCustomizedPrivilege(pvg);
        }
        return pvg;
    }

    @Override
    public NutBean joinCustomizedPrivilege(NutBean pvg) {
        // 得到自己的权限
        NutMap selfPvg = this.getAs("pvg", NutMap.class);

        boolean isSubEmpty = null == pvg || pvg.isEmpty();
        boolean isSelfEmpty = null == selfPvg || selfPvg.isEmpty();

        // 防空: 全空
        if (null == pvg) {
            pvg = new NutMap();
        }

        // 两者均不为空，则融合
        if (!isSelfEmpty) {
            for (Map.Entry<String, Object> en : selfPvg.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                WnObjMode om = new WnObjMode(val);
                // 强混合
                if (om.isStrong()) {
                    pvg.put(key, om.getValue());
                }
                // 弱混合
                else if (om.isWeak()) {
                    if (isSubEmpty) {
                        pvg.put(key, om.getValue());
                    }
                }
                // 作为默认值
                else {
                    pvg.putDefault(key, om.getValue());
                }
            }
        }

        // 递归
        if (!this.hasParent()) {
            return pvg;
        }
        return this.parent().joinCustomizedPrivilege(pvg);
    }

    public void setParent(WnObj parent) {
        this._parent = parent;
        String pid = (null == parent ? null : parent.id());
        this.setv("pid", pid);
        this.path(parent.path()).appendPath(name());
        Wn.Io.eval_dn(this);

        // 清除一下缓存
        this.__mnt = null;
    }

    @Override
    public WnObj loadParents(List<WnObj> list, boolean force) {
        // 已经加载过了，且不是强制加载，就啥也不干
        if (null != _parent && !force) {
            if (Strings.isBlank(path())) {
                path(_parent.path()).appendPath(name());
            }
            if (null != list && !_parent.path().equals("/")) {
                _parent.loadParents(list, force);
                list.add(_parent);
            }
            return _parent;
        }

        // 如果自己就是树的根节点则表示到头了
        // 因为 Mount 的树，它的树对象是父树
        if (!this.hasParent()) {
            path("/");
            return this;
        }

        // 得到父节点
        String pid = parentId();
        // 如果引用自身，那么做一下标识
        if (this.isSameId(pid)) {
            this.setv("pid", "!!R");
            return this;
        }

        // 没设置索引管理器
        if (null == io) {
            return null;
        }

        WnObj p = io.isRoot(pid) ? io.getRoot() : io.get(pid);

        // 没有父，是不可能的
        if (null == p) {
            if (log.isWarnEnabled()) {
                log.warnf("NilParent(%s): Obj(%s)", pid, this.id());
            }
            return null;
        }

        // 确保可访问
        p = Wn.WC().whenEnter(p, false);

        // 设置成自己的父
        _parent = p;

        if (!_parent.path().equals("/")) {
            // 递归加载父节点的祖先
            p.loadParents(list, force);

            // 记录到输出列表
            if (null != list)
                list.add(_parent);
        }

        // 更新路径
        path(_parent.path()).appendPath(name());

        // 确保自己的 d0,d1 正确
        Wn.Io.eval_dn(this);

        // 返回父节点
        return _parent;
    }

    public boolean isMyParent(WnObj p) {
        WnObjId myParentId = new WnObjId(this.parentId());
        if (myParentId.isInSubMapping()) {
            return Wlang.isEqual(myParentId.getMyId(), p.myId());
        }
        return Wlang.isEqual(this.parentId(), p.myId());
    }

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
    public boolean isRWMeta() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj setRWMeta(boolean rwmeta) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean hasRWMetaKeys() {
        throw Wlang.noImplement();
    }

    @Override
    public String getRWMetaKeys() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj setRWMetaKeys(String regex) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj clearRWMetaKeys() {
        throw Wlang.noImplement();
    }

    public WnObj clone() {
        return new WnIoObj().updateBy(this);
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
