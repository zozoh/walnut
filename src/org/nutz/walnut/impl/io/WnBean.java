package org.nutz.walnut.impl.io;

import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;

// import org.nutz.walnut.util.WnContext;

public class WnBean extends NutMap implements WnObj {

    public WnBean() {}

    public WnBean(WnBean o) {
        this.putAll(o);
        if (null != o._nd)
            this.setNode(o._nd.duplicate());
    }

    private WnNode _nd;

    public WnNode nd() {
        return _nd;
    }

    public WnObj setNode(WnNode nd) {
        if (nd == this || nd == _nd)
            return this;

        // 如果已经设置了 ID, 那么必须一致
        String id = id();
        if (!Strings.isBlank(id)) {
            if (!nd.isSameId(id)) {
                throw Er.create("e.io.obj.nd.NoSameId", id + " != " + nd.id());
            }
        } else {
            id(nd.id());
        }

        // 设置节点
        this._nd = nd;

        // 更新其他字段用作冗余记录
        this.setv("nm", nd.name());
        this.name(nd.name());
        this.path(nd.path());
        this.parentId(nd.parentId());
        this.race(nd.race());
        this.setv("mnt", Strings.sBlank(nd.mount(), nd.tree().getMount()));

        // 如果节点没有给出创建者，则默认用线程当前的用户
        // WnContext wc = Wn.WC();
        // if (Strings.isBlank(this.creator()))
        // this.creator(Strings.sBlank(nd.creator(), wc.checkMe()));
        //
        // if (Strings.isBlank(this.mender()))
        // this.mender(Strings.sBlank(nd.mender(), wc.checkMe()));
        //
        // if (Strings.isBlank(this.group()))
        // this.group(Strings.sBlank(nd.group(), wc.checkGroup()));

        // 如果节点给出了大小，也复用
        long len = nd.len();
        if (len > 0) {
            this.len(len);
        }

        // 没有设定权限码，用节点的
        // if (this.mode() <= 0)
        // this.mode(nd.mode());

        // 看看节点有没有给出时间
        if (nd.nanoStamp() > 0) {
            this.nanoStamp(nd.nanoStamp());
        }
        // 没给时间的话如果自己也没有，用当前时间
        else if (this.nanoStamp() <= 0) {
            this.nanoStamp(System.nanoTime());
        }

        // 返回
        return this;
    }

    public NutMap toMap4Update(String regex) {
        NutMap map = new NutMap();
        for (Map.Entry<String, Object> en : this.entrySet()) {
            String key = en.getKey();
            // 如果 regex 为空，只要不是 id 且不是 "__" 开头（表隐藏），则全要
            if (null == regex) {
                if (!"id".equals(key) && !key.startsWith("__"))
                    map.put(key, en.getValue());
            }
            // 否则只给出正则表达式匹配的部分，以及几个固定需要更新的字段
            else if (key.matches("^nm|race|pid|mnt$") || key.matches(regex)) {
                map.put(key, en.getValue());
            }
        }
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

    public WnNode id(String id) {
        // 如果已经设置了 Node，那么必须一致
        WnNode nd = nd();
        if (null != nd) {
            if (!nd.isSameId(id)) {
                throw Er.create("e.io.obj.id.NoSameId", id + " != " + nd.id());
            }
        }
        setv("id", id);
        return this;
    }

    public boolean hasID() {
        return !Strings.isBlank(id());
    }

    public boolean isSameId(WnNode o) {
        return o.isSameId(id());
    }

    public boolean isSameId(String id) {
        return id().equals(id);
    }

    public WnNode genID() {
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

    public WnBean parentId(String pid) {
        this.setv("pid", pid);
        return this;
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
        if (null == sha1)
            return false;
        String mySha1 = sha1();
        if (null == mySha1)
            return false;
        return mySha1.equals(sha1);
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
        if (null == data)
            return false;
        String myData = data();
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

    public WnBean update(NutMap map) {
        this.putAll(map);
        return this;
    }

    public String[] labels() {
        return this.getArray("lbs", String.class);
    }

    public WnBean labels(String[] lbs) {
        this.setOrRemove("lbs", lbs);
        return this;
    }

    public long createTime() {
        return this.getLong("ct", -1);
    }

    public WnBean createTime(long ct) {
        this.setOrRemove("ct", ct);
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
        if (expi < 0)
            return false;
        return expi < now;
    }

    public long lastModified() {
        return this.getLong("lm");
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

    public String toString() {
        return String.format("%s:%s[%s] %dbytes -> %s", id(), name(), sha1(), len(), mount());
    }

    // -----------------------------------------
    // 下面的属性不要主动设置，用 nd() 方法设置
    // -----------------------------------------

    public String path() {
        return getString("ph");
    }

    public WnNode path(String path) {
        setv("ph", path);
        return this;
    }

    @Override
    public String realPath() {
        return _nd.realPath();
    }

    public WnNode appendPath(String path) {
        path(Wn.appendPath(path(), path));
        return this;
    }

    public String name() {
        return getString("nm");
    }

    public WnNode name(String nm) {
        setv("nm", nm);
        return this;
    }

    public WnRace race() {
        return getEnum("race", WnRace.class);
    }

    public WnNode race(WnRace race) {
        setv("race", race);
        return this;
    }

    public boolean isRace(WnRace race) {
        return race() == race;
    }

    public boolean isOBJ() {
        return isRace(WnRace.OBJ);
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

    public WnNode mount(String mnt) {
        setv("mnt", mnt);
        if (null != _nd)
            _nd.mount(mnt);
        return this;
    }

    // -----------------------------------------
    // 下面是委托 _nd 属性的方法
    // -----------------------------------------

    public boolean isHidden() {
        return nd().isHidden();
    }

    @Override
    public boolean isRootNode() {
        return nd().isRootNode();
    }

    @Override
    public boolean isMount(WnTree myTree) {
        return nd().isMount(myTree);
    }

    public WnNode parent() {
        return nd().parent();
    }

    public void setParent(WnNode parent) {
        nd().setParent(parent);
    }

    @Override
    public WnNode loadParents(List<WnNode> list, boolean force) {
        return nd().loadParents(list, force);
    }

    public boolean isMyParent(WnNode p) {
        return nd().isMyParent(p);
    }

    public WnTree tree() {
        return nd().tree();
    }

    public void setTree(WnTree tree) {
        nd().setTree(tree);
    }

    public void assertTree(WnTree tree) {
        nd().assertTree(tree);
    }

    public WnNode clone() {
        return duplicate();
    }

    @Override
    public WnNode duplicate() {
        return new WnBean(this);
    }

}
