package org.nutz.walnut.impl.io;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;

public class WnBean extends NutMap implements WnObj {

    private WnTree tree;

    public WnTree tree() {
        return tree;
    }

    public WnObj setTree(WnTree tree) {
        this.tree = tree;
        return this;
    }

    public WnBean() {}

    public NutMap toMap4Update(String regex) {
        NutMap map = new NutMap();

        // 正则表达式支持 "!" 开头
        boolean not = null != regex && regex.startsWith("!");
        if (not)
            regex = regex.substring(1);
        Pattern pattern = regex == null ? null : Pattern.compile(regex);

        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            // id 等是绝对不可以改的
            if (Wn.matchs(key, "^(ph|parent|id|race)$")) {
                continue;
            }
            // 如果 regex 为空，不是 "__" 开头（表隐藏），则全要
            else if (null == pattern) {
                if (!key.startsWith("__"))
                    map.put(key, en.getValue());
            }
            // 否则只给出正则表达式匹配的部分
            else if (pattern.matcher(key).matches() ^ not) {
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
        NutMap map = new NutMap();
        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            if (null == regex) {
                map.put(key, en.getValue());
            }
            // 否则只给出正则表达式匹配的部分，以及几个固定需要更新的字段
            else if (key.matches(regex)) {
                map.put(key, en.getValue());
            }
        }
        return map;
    }

    public String id() {
        return getString("id");
    }

    public WnObj id(String id) {
        setv("id", id);
        return this;
    }

    public boolean hasWriteHandle() {
        return this.has("_write_handle");
    }

    public String getWriteHandle() {
        return this.getString("_write_handle");
    }

    public WnObj setWriteHandle(String hid) {
        this.setv("_write_handle", hid);
        return this;
    }

    public boolean hasID() {
        return !Strings.isBlank(id());
    }

    public boolean isSameId(String id) {
        return id().equals(id);
    }

    public WnObj genID() {
        throw Er.create("e.io.obj.forbiden.genid");
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

    public boolean isLink() {
        return !Strings.isBlank(link());
    }

    public String link() {
        return this.getString("ln");
    }

    public WnBean link(String lid) {
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

    public WnBean type(String tp) {
        this.setOrRemove("tp", tp);
        return this;
    }

    public String mime() {
        return this.getString("mime");
    }

    public WnBean mime(String mime) {
        this.setOrRemove("mime", mime);
        return this;
    }

    public boolean hasSha1() {
        return !Strings.isBlank(sha1());
    }

    public String sha1() {
        return this.getString("sha1");
    }

    public WnBean sha1(String sha1) {
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
        return !Strings.isBlank(data());
    }

    public String data() {
        return this.getString("data");
    }

    public WnBean data(String data) {
        this.setv("data", data);
        return this;
    }

    public boolean isSameData(String data) {
        String myData = data();
        if (null == data)
            return null == myData;

        if (null == myData)
            return false;

        return myData.equals(data);
    }

    public long len() {
        return this.getLong("len", 0);
    }

    public WnBean len(long len) {
        this.put("len", len);
        return this;
    }

    public int remain() {
        return this.getInt("remain");
    }

    public WnBean remain(int remain) {
        this.put("remain", remain);
        return this;
    }

    public String creator() {
        return this.getString("c");
    }

    public WnBean creator(String creator) {
        this.setOrRemove("c", creator);
        return this;
    }

    public String mender() {
        return this.getString("m");
    }

    public WnBean mender(String mender) {
        this.setOrRemove("m", mender);
        return this;
    }

    public String group() {
        return this.getString("g");
    }

    public WnBean group(String grp) {
        this.setOrRemove("g", grp);
        return this;
    }

    public int mode() {
        return this.getInt("md");
    }

    public WnBean mode(int md) {
        this.setOrRemove("md", md);
        return this;
    }

    public String d0() {
        return this.getString("d0");
    }

    public WnBean d0(String d0) {
        this.setv("d0", d0);
        return this;
    }

    public String d1() {
        return this.getString("d1");
    }

    public WnBean d1(String d1) {
        this.setv("d1", d1);
        return this;
    }

    public String[] dN() {
        String d0 = d0();
        String d1 = d1();

        if (Strings.isBlank(d0))
            return new String[0];
        if (Strings.isBlank(d1))
            return Lang.array(d0);
        return Lang.array(d0, d1);
    }

    public WnBean update(Map<? extends String, ? extends Object> map) {
        this.putAll(map);
        return this;
    }

    @Override
    public WnObj update2(WnObj o) {
        // 更新全部元数据
        this.putAll(o);

        // 更新自己的私有属性
        if (o instanceof WnBean) {
            this.tree = ((WnBean) o).tree;
            this._parent = ((WnBean) o)._parent;
        } else {
            this.tree = o.tree();
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

    public WnBean labels(String[] lbls) {
        this.setOrRemove("lbls", lbls);
        return this;
    }

    public long createTime() {
        return this.getLong("ct", -1);
    }

    public WnBean createTime(long ct) {
        this.setOrRemove("ct", ct);
        return this;
    }

    @Override
    public long syncTime() {
        return this.getLong("st", -1);
    }

    @Override
    public WnObj syncTime(long st) {
        this.setOrRemove("st", st);
        return this;
    }

    public long expireTime() {
        return this.getLong("expi", -1);
    }

    public WnBean expireTime(long expi) {
        this.setOrRemove("expi", expi);
        return this;
    }

    public boolean isExpired() {
        return isExpiredBy(System.currentTimeMillis());
    }

    @Override
    public boolean isExpiredBy(long now) {
        long expi = expireTime();
        if (expi <= 0)
            return false;
        return expi < now;
    }

    public long lastModified() {
        return this.getLong("lm");
    }

    @Override
    public WnObj lastModified(long lm) {
        this.setv("lm", lm);
        return this;
    }

    public long nanoStamp() {
        return this.getLong("nano");
    }

    public WnBean nanoStamp(long nano) {
        this.setv("nano", nano);
        this.setv("lm", nano / 1000000L);
        return this;
    }

    public boolean equals(Object obj) {
        if (obj instanceof WnBean) {
            WnBean o = (WnBean) obj;
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

    public int hashCode() {
        String id = id();
        return null == id ? -1 : id.hashCode();
    }

    public String toString() {
        return String.format("%s:{%s:%s:%s}", path(), id(), creator(), group());
    }

    // -----------------------------------------
    // 下面的属性不要主动设置，用 nd() 方法设置
    // -----------------------------------------

    public String path() {
        String ph = getString("ph");
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

    public String mount() {
        return getString("mnt");
    }

    public WnObj mount(String mnt) {
        setv("mnt", mnt);
        return this;
    }

    public String mountRootId() {
        String mrid = getString("mrid");
        if (null == mrid)
            return this.isMount() ? id() : null;
        return mrid;
    }

    public WnObj mountRootId(String mrid) {
        setv("mrid", mrid);
        return this;
    }

    @Override
    public boolean isMount() {
        String mnt = mount();
        return !Strings.isBlank(mnt);
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
            this.setParent(tree.getOne(Wn.Q.id(parentId())));
        }
        return _parent;
    }

    public void setParent(WnObj parent) {
        this._parent = parent;
        String pid = (null == parent ? null : parent.id());
        this.setv("pid", pid);
        this.path(parent.path()).appendPath(name());
        Wn.Io.eval_dn(this);
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

        WnObj p = tree.isRoot(pid) ? tree.getRoot() : tree.get(pid);

        // 没有父，是不可能的
        if (null == p) {
            throw Lang.impossible();
        }

        // 递归加载父节点的祖先
        p.loadParents(list, force);

        // 确保可访问
        p = Wn.WC().whenEnter(p);

        // 设置成自己的父
        _parent = p;

        // 记录到输出列表
        if (null != list)
            list.add(_parent);

        // 更新路径
        path(_parent.path()).appendPath(name());

        // 确保自己的 d0,d1 正确
        Wn.Io.eval_dn(this);

        // 返回父节点
        return _parent;
    }

    public boolean isMyParent(WnObj p) {
        return Lang.equals(this.parentId(), p.id());
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
        return getBoolean("__obj_meta_rw");
    }

    @Override
    public WnObj setRWMeta(boolean rwmeta) {
        this.setv("__obj_meta_rw", rwmeta);
        return this;
    }

    @Override
    public boolean hasRWMetaKeys() {
        return has("__store_update_meta");
    }

    @Override
    public String getRWMetaKeys() {
        return getString("__store_update_meta");
    }

    @Override
    public WnObj setRWMetaKeys(String regex) {
        this.setv("__store_update_meta", regex);
        return this;
    }

    @Override
    public WnObj clearRWMetaKeys() {
        this.remove("__store_update_meta");
        return this;
    }

    public WnObj clone() {
        return new WnBean().update2(this);
    }

}
