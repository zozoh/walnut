package org.nutz.walnut.impl.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.ObjIndexStrategy;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnNodeCallback;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreFactory;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnIoImpl implements WnIo {

    private WnTree tree;

    private WnStoreFactory stores;

    private WnIndexer indexer;

    private MimeMap mimes;

    public void _clean_for_unit_test() {
        tree._clean_for_unit_test();
        indexer._clean_for_unit_test();
    }

    private WnTree tree(WnNode p) {
        if (null == p)
            return tree;
        WnTree re = p.tree();
        if (null == re)
            return tree;
        return re;
    }

    @Override
    public boolean exists(WnObj p, String path) {
        return tree(p).fetch(p, path) != null;
    }

    @Override
    public boolean existsId(String id) {
        return tree(null).getNode(id) != null;
    }

    @Override
    public WnObj toObj(WnNode nd) {
        return indexer.toObj(nd, ObjIndexStrategy.PARENT);
    }

    @Override
    public WnObj get(String id) {
        WnNode nd = tree(null).getNode(id);
        if (null == nd)
            return null;
        nd.loadParents(null, false);
        return indexer.toObj(nd, ObjIndexStrategy.PARENT);
    }

    @Override
    public WnObj get(WnNode nd) {
        return indexer.toObj(nd, ObjIndexStrategy.PARENT);
    }

    @Override
    public WnObj getRootObj() {
        WnNode nd = tree.getTreeNode().clone();
        WnBean o = new WnBean();
        o.setNode(nd);
        o.name("");
        o.race(WnRace.DIR);
        return o;
    }

    @Override
    public WnObj getParent(WnObj o) {
        if (null == o)
            return null;
        return indexer.toObj(o.parent(), ObjIndexStrategy.PARENT);
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = get(id);
        if (null == o)
            throw Er.create("e.io.noexists", "id:" + id);
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.noexists", path);
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        // 判断是否是获取对象索引
        String nm = Files.getName(path);
        boolean rwmeta = false;
        if (nm.startsWith(Wn.OBJ_META_PREFIX)) {
            path = Files.renamePath(path, nm.substring(Wn.OBJ_META_PREFIX.length()));
            rwmeta = true;
        }

        // 获取对象
        WnNode nd = tree(p).fetch(p, path);
        WnObj o = indexer.toObj(nd, ObjIndexStrategy.PARENT);
        // 标记一下，如果读写的时候，只写这个对象的索引
        if (rwmeta) {
            o.setv(Wn.OBJ_META_RW, rwmeta);
            o.mime(mimes.getMime("json"));
        }
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {

        // 判断是否是获取对象索引
        String nm = paths[toIndex - 1];
        boolean rwmeta = false;
        if (nm.startsWith(Wn.OBJ_META_PREFIX)) {
            paths[toIndex - 1] = nm.substring(Wn.OBJ_META_PREFIX.length());
            rwmeta = true;
        }

        // 获取对象
        WnNode nd = tree(p).fetch(p, paths, fromIndex, toIndex);
        WnObj o = indexer.toObj(nd, ObjIndexStrategy.PARENT);
        // 标记一下，如果读写的时候，只写这个对象的索引
        if (rwmeta) {
            o.setv(Wn.OBJ_META_RW, rwmeta);
            o.mime(mimes.getMime("json"));
        }
        return o;
    }

    @Override
    public void walk(WnObj p, final Callback<WnObj> callback, WalkMode mode) {
        tree(p).walk(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                WnObj o = indexer.toObj(nd, ObjIndexStrategy.PARENT);
                if (null != callback)
                    callback.invoke(o);
            }
        }, mode);
    }

    @Override
    public int eachChildren(WnObj p, String str, final Each<WnObj> callback) {
        return tree(p).eachChildren(p, str, new Each<WnNode>() {
            public void invoke(int index, WnNode nd, int length) {
                WnObj o = indexer.toObj(nd, ObjIndexStrategy.PARENT);
                callback.invoke(index, o, length);
            }
        });
    }

    @Override
    public List<WnObj> getChildren(WnObj p, String str) {
        final List<WnObj> list = new LinkedList<WnObj>();
        eachChildren(p, str, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                list.add(o);
            }
        });
        return list;
    }

    @Override
    public void rename(WnObj o, String newName) {
        WnNode nd = o.tree().rename(o, newName);
        o.setNode(nd);
        __set_type(o, null);
        indexer.set(o, "^nm|tp|mime$");
    }

    @Override
    public void changeType(WnObj o, String tp) {
        __set_type(o, tp);
        indexer.set(o, "^tp|mime$");
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        // 保存之前的 d0,d1
        String old_d0 = src.d0();
        String old_d1 = src.d1();

        // 得到自身节点
        src.loadParents(null, false);

        // 不用移动
        if (src.path().equals(destPath)) {
            return src;
        }

        WnContext wc = Wn.WC();

        // 确保源是可以访问的
        wc.whenAccess(src);

        // 确保源的父是可以写入的
        wc.whenWrite(src.parent());

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        WnNode ta = tree(null).fetch(null, taPath);

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            taPath = Files.getParent(taPath);
            ta = tree(null).fetch(null, taPath);
            newName = Files.getName(destPath);
        }
        // 如果存在的是一个文件
        else if (ta.isFILE()) {
            throw Er.create("e.io.exists", destPath);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.noexists", taPath);
        }

        // 确认目标能写入
        wc.whenWrite(ta);

        // 默认返回自身
        WnObj re = src;

        // 准备好了父在以后判断是否是同一颗树
        WnTree treeA = src.tree();
        WnTree treeB = ta.tree();
        // 如果在同一颗树上则简单修改一下节点
        if (treeA.equals(treeB)) {
            // 否则只能允许把对象移动到对象下面
            if (ta.isDIR() || (ta.isOBJ() && src.isOBJ())) {
                WnNode newNode = treeA.append(ta, src, newName);
                re.setNode(newNode);
            }
            // 这肯定是一个非法的移动
            else {
                throw Er.create("e.io.move.forbidden", src.path() + " >> " + destPath);
            }
        }
        // 如果不是同一棵树，则一棵树上创建一颗树上删除
        else {
            WnNode ndB;
            if (null == newName) {
                ndB = treeB.createNode(ta, src.id(), src.name(), src.race());
            } else {
                ndB = treeB.createNode(ta, src.id(), newName, src.race());
            }
            WnObj oB = indexer.toObj(ndB, ObjIndexStrategy.WC);

            // 保持 mount
            if (src.isMount(treeA)) {
                treeB.setMount(ndB, src.mount());
            }

            // copy 内容
            if (src.len() > 0) {
                WnStore storeA = stores.get(src);
                WnStore storeB = stores.get(ndB);

                InputStream ins = storeA.getInputStream(src, 0);
                OutputStream ops = storeB.getOutputStream(oB, 0);
                Streams.writeAndClose(ops, ins);

                // 删除原来的内容
                try {
                    wc.setv("store:clean_not_update_indext", true);
                    storeA.cleanHistory(src, -1);
                }
                finally {
                    wc.remove("store:clean_not_update_indext");
                }
            }
            // 删除旧节点
            treeA.delete(src);

            // 那么返回的就是 oB
            re = oB;
        }

        // 修改新对象的 d0, d1
        Wn.Io.eval_dn(re);

        // 更新一下索引的记录
        __set_type(re, src.type());
        re.setv("pid", ta.id());
        indexer.set(re, "^d0|d1|pid|tp|mime$");

        // 如果是目录，且d0,d1 改变了，需要递归
        if (re.isDIR()) {
            final String d0 = re.d0();
            final String d1 = re.d1();
            if (!Lang.equals(d0, old_d0) || !Lang.equals(d1, old_d1)) {
                this.walk(re, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        obj.d0(d0).d1(d1);
                        indexer.set(obj, "^d0|d1$");
                    }
                }, WalkMode.DEPTH_NODE_FIRST);
            }
        }
        // 返回
        return re;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        WnObj o = fetch(p, path);
        if (null == o)
            return create(p, path, race);

        if (!o.isRace(race))
            throw Er.create("e.io.create.invalid.race", path + " ! " + race);

        return o;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        // 判断是否是获取对象索引
        String nm = Files.getName(path);
        if (nm.startsWith(Wn.OBJ_META_PREFIX)) {
            return fetch(p, path);
        }

        // 创建时，附加创建索引的方法
        WnContext wc = Wn.WC();
        final WnNodeCallback old = wc.onCreate();

        try {
            // 给树设定回调，保证每次创建节点都会创建索引
            wc.onCreate(new WnNodeCallback() {
                public WnNode invoke(WnNode nd) {
                    WnObj re = __create_index(nd);
                    if (null != old)
                        return old.invoke(re);
                    return re;
                }
            });

            // 执行创建
            WnNode nd = tree(p).create(p, path, race);
            return indexer.toObj(nd, ObjIndexStrategy.WC);
        }
        finally {
            wc.onCreate(old);
        }
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        // 判断是否是获取对象索引
        String nm = paths[toIndex - 1];
        if (nm.startsWith(Wn.OBJ_META_PREFIX)) {
            return fetch(p, paths, fromIndex, toIndex);
        }

        // 创建时，附加创建索引的方法
        WnContext wc = Wn.WC();
        final WnNodeCallback old = wc.onCreate();

        try {
            // 给树设定回调，保证每次创建节点都会创建索引
            wc.onCreate(new WnNodeCallback() {
                public WnNode invoke(WnNode nd) {
                    WnObj re = __create_index(nd);
                    if (null != old)
                        return old.invoke(re);
                    return re;
                }
            });

            // 执行创建
            WnNode nd = tree(p).create(p, paths, fromIndex, toIndex, race);
            return indexer.toObj(nd, ObjIndexStrategy.WC);
        }
        finally {
            wc.onCreate(old);
        }
    }

    private WnObj __create_index(WnNode nd) {
        WnObj o = indexer.toObj(nd, ObjIndexStrategy.WC);
        return o;
    }

    private void __set_type(WnObj o, String tp) {
        Wn.set_type(mimes, o, tp);
    }

    @Override
    public void delete(WnObj o) {
        // 链接的话，就删了吧
        if (!o.isLink()) {
            // 目录的话，删除不能为空
            if (hasChild(o)) {
                throw Er.create("e.io.rm.noemptynode", o);
            }
            // 文件删除
            if (!o.isDIR()) {
                WnStore store = stores.get(o);
                store.cleanHistoryBy(o, 0);
            }
        }
        // 删除树节点和索引
        indexer.remove(o.id());
        tree(o).delete(o);
    }

    @Override
    public boolean hasChild(WnObj p) {
        if (p.isFILE())
            return false;
        final boolean[] re = new boolean[1];
        tree(p).eachChildren(p, null, new Each<WnNode>() {
            public void invoke(int index, WnNode ele, int length) {
                re[0] = true;
                Lang.Break();
            }
        });
        return re[0];
    }

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        WnStore store = stores.get(o);
        return store.eachHistory(o, nano, callback);
    }

    @Override
    public List<WnHistory> getHistoryList(WnObj o, long nano) {
        WnStore store = stores.get(o);
        return store.getHistoryList(o, nano);
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        WnStore store = stores.get(o);
        return store.getHistory(o, nano);
    }

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        throw Lang.noImplement();
    }

    @Override
    public List<WnHistory> cleanHistory(WnObj o, long nano) {
        WnStore store = stores.get(o);
        return store.cleanHistory(o, nano);
    }

    @Override
    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        WnStore store = stores.get(o);
        return store.cleanHistoryBy(o, remain);
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        WnNode nd = tree(o).setMount(o, mnt);
        o.setNode(nd);
        indexer.set(o, "^mnt$");
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // 得到节点检查的回调接口
        o = indexer.toObj(Wn.WC().whenRead(o), ObjIndexStrategy.STRICT);

        // 是读取元数据的
        if (o.getBoolean(Wn.OBJ_META_RW)) {
            WnObj o2 = (WnObj) o.duplicate();
            o2.remove(Wn.OBJ_META_RW);
            return new WnObjMetaInputStream(o2);
        }
        // 读取内容的
        WnStore store = stores.get(o);
        return store.getInputStream(o, off);
    }

    @Override
    public InputStream getInputStream(WnObj o, WnHistory his, long off) {
        WnStore store = stores.get(o);
        return store.getInputStream(o, his, off);
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        // 得到节点检查的回调接口
        o = indexer.toObj(Wn.WC().whenWrite(o), ObjIndexStrategy.STRICT);

        // 是写入元数据的
        if (o.getBoolean(Wn.OBJ_META_RW)) {
            return new WnObjMetaOutputStream(o, indexer, off < 0);
        }
        // 写入内容
        WnStore store = stores.get(o);
        return store.getOutputStream(o, off);
    }

    @Override
    public void writeMeta(WnObj o, Object meta) {
        // 得到之前的配置
        boolean rw = o.getBoolean(Wn.OBJ_META_RW);

        // 确保是写 Meta
        o.setv(Wn.OBJ_META_RW, true);

        // 写入
        String json = __to_meta_json(o, meta);
        writeText(o, json);

        // 恢复
        if (!rw)
            o.remove(Wn.OBJ_META_RW);
    }

    private String __to_meta_json(WnObj o, Object meta) {
        // 空
        if (null == meta) {
            return "{}";
        }

        // 准备格式化
        JsonFormat fmt = JsonFormat.compact().setIgnoreNull(false);

        // 字符串
        if (meta instanceof CharSequence) {
            String str = Strings.trim(meta.toString());
            // 空字符串，当做空对象
            if (Strings.isBlank(str)) {
                return "{}";
            }
            // 正则表达式
            if (str.startsWith("^")) {
                return Json.toJson(o.toMap(str), JsonFormat.compact());
            }
            // 如果是 JSON 对象
            if (Strings.isQuoteBy(str, '{', '}')) {
                return str;
            }
            // 否则，试图给其包裹上 map
            return Json.toJson(Lang.map(str), fmt);
        }
        // 列表和数组是不可以的
        if (meta instanceof Collection<?> || meta.getClass().isArray()) {
            throw Er.create("e.io.meta.aslist", meta);
        }
        // 其他的对象，统统变 JSON 字符串
        return Json.toJson(meta, fmt);
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        // 得到之前的配置
        boolean rw = o.getBoolean(Wn.OBJ_META_RW);

        // 确保是写 Meta
        o.setv(Wn.OBJ_META_RW, true);

        // 写入
        String json = __to_meta_json(o, meta);
        appendText(o, json);

        // 恢复
        if (!rw)
            o.remove(Wn.OBJ_META_RW);
    }

    @Override
    public long readAndClose(WnObj o, OutputStream ops) {
        InputStream ins = this.getInputStream(o, 0);
        return Streams.writeAndClose(ops, ins);
    }

    @Override
    public String readText(WnObj o) {
        InputStream ins = this.getInputStream(o, 0);
        Reader r = Streams.buffr(Streams.utf8r(ins));
        return Streams.readAndClose(r);
    }

    @Override
    public <T> T readJson(WnObj o, Class<T> classOfT) {
        InputStream ins = this.getInputStream(o, 0);
        Reader r = Streams.buffr(Streams.utf8r(ins));
        try {
            return Json.fromJson(classOfT, r);
        }
        finally {
            Streams.safeClose(r);
        }
    }

    @Override
    public Reader getReader(WnObj o, long off) {
        InputStream ins = this.getInputStream(o, off);
        return Streams.utf8r(ins);
    }

    @Override
    public Writer getWriter(WnObj o, long off) {
        OutputStream ops = this.getOutputStream(o, off);
        return Streams.utf8w(ops);
    }

    @Override
    public long writeText(WnObj o, CharSequence cs) {
        OutputStream ops = this.getOutputStream(o, 0);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        Streams.writeAndClose(w, cs);
        return o.len();
    }

    @Override
    public long appendText(WnObj o, CharSequence cs) {
        OutputStream ops = this.getOutputStream(o, -1);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        Streams.writeAndClose(w, cs);
        return o.len();
    }

    @Override
    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        if (null == fmt)
            fmt = JsonFormat.full().setQuoteName(true);

        Object json = obj;
        if (obj instanceof CharSequence) {
            json = Json.fromJson(obj.toString());
        }

        OutputStream ops = this.getOutputStream(o, 0);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        try {
            Json.toJson(w, json, fmt);
        }
        finally {
            Streams.safeClose(w);
        }
        return o.len();
    }

    @Override
    public long writeAndClose(WnObj o, InputStream ins) {
        OutputStream ops = this.getOutputStream(o, 0);
        return Streams.writeAndClose(ops, ins);
    }

    public WnObj getOne(WnQuery q) {
        WnObj o = indexer.getOne(q);
        if (null == o)
            return null;

        WnNode nd = tree(null).getNode(o.id());

        if (null == nd) {
            throw Er.create("e.io.lostnode", o);
        }

        return o.setNode(nd);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return indexer.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return indexer.query(q);
    }

    @Override
    public long countChildren(WnObj p, String tp, boolean withHidden) {
        return tree(p).countNode(p, tp, withHidden);
    }

}
