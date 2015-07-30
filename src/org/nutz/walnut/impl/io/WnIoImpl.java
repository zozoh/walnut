package org.nutz.walnut.impl.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnNodeCallback;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnIoImpl implements WnIo {

    private WnTree tree;

    private WnStore store;

    WnIndexer indexer;

    private MimeMap mimes;

    public void _clean_for_unit_test() {
        tree._clean_for_unit_test();
        indexer._clean_for_unit_test();
    }


    @Override
    public boolean exists(WnObj p, String path) {
        return tree.fetch(p, path) != null;
    }

    @Override
    public boolean existsId(String id) {
        return tree.existsId(id);
    }


    @Override
    public WnObj get(String id) {
        if (null == id)
            return null;

        // 如果是不完整的 ID
        if (!id.matches("[0-9a-v]{26}")) {
            WnQuery q = new WnQuery().limit(2);
            q.setv("id", Pattern.compile("^" + id));
            List<WnObj> objs = this.query(q);
            if (objs.isEmpty())
                return null;
            if (objs.size() > 1)
                throw Er.create("e.io.obj.get.shortid", id);
            WnObj o = objs.get(0);
            WnNode nd = tree.getNode(o.id());
            nd.loadParents(null, false);
            o.setNode(nd);
            return o;
        }

        // 如果是完整的 ID
        WnNode nd = tree.getNode(id);
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
            path = Files.renamePath(path, nm.substring(Wn.OBJ_META_PREFIX.length())).replace('\\',
                                                                                             '/');
            rwmeta = true;
        }

        // 获取对象
        WnNode nd = tree.fetch(p, path);
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
        WnNode nd = tree.fetch(p, paths, fromIndex, toIndex);
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
        tree.walk(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                WnObj o = indexer.toObj(nd, ObjIndexStrategy.PARENT);
                if (null != callback)
                    callback.invoke(o);
            }
        }, mode);
    }

    @Override
    public int eachChildren(WnObj p, String str, final Each<WnObj> callback) {
        return tree.eachChildren(p, str, new Each<WnNode>() {
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
    public WnObj rename(WnObj o, String newName) {
        String destPath = Wn.appendPath(Files.getParent(o.path()), newName);
        return this.move(o, destPath);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        WnContext wc = Wn.WC();

        // 调用钩子
        src.setv("_mv_dest", destPath);
        wc.doHook("move", src);

        // 保存之前的 d0,d1
        String old_d0 = src.d0();
        String old_d1 = src.d1();

        // 得到自身节点
        src.loadParents(null, false);

        // 得到自身的原始的父
        WnObj oldSrcParent = toObj(src.parent());

        // 不用移动
        if (src.path().equals(destPath)) {
            return src;
        }

        // 确保源是可以访问的
        wc.whenAccess(src);

        // 确保源的父是可以写入的
        wc.whenWrite(src.parent());

        // 看看目标是否存在
        String newName = null;
        String taPath = destPath;
        WnNode ta = tree.fetch(null, taPath);

        // 如果不存在，看看目标的父是否存在，并且可能也同时要改名
        if (null == ta) {
            taPath = Files.getParent(taPath);
            ta = tree.fetch(null, taPath);
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
        // 如果不是同一棵树，由于不能确认是怎么存储的，抛错
        else {
            throw Er.create("e.io.move.forbidden", src.path() + " >> " + destPath);
        }

        // 修改新对象的 d0, d1
        Wn.Io.eval_dn(re);

        // 更新一下索引的记录
        if (null != newName) {
            // __set_type(re, src.type());
            re.name(newName);
            Wn.set_type(mimes, re, null);
        }
        re.setv("pid", ta.id());
        indexer.set(re, "^d0|d1|nm|pid|tp|mime$");

        // 如果是目录，且d0,d1 改变了，需要递归
        __check_dn(old_d0, old_d1, re);

        // 触发同步时间修改
        Wn.Io.update_ancestor_synctime(this, re, false);
        // 如果对象换了父节点，之前的父节点也要被触发修改时间
        if (!oldSrcParent.isSameId(re.parentId())) {
            Wn.Io.update_ancestor_synctime(this, oldSrcParent, true);
        }

        // 返回
        return re;
    }

    private void __check_dn(String old_d0, String old_d1, WnObj o) {
        if (o.isDIR()) {
            final String d0 = o.d0();
            final String d1 = o.d1();
            if (!Lang.equals(d0, old_d0) || !Lang.equals(d1, old_d1)) {
                this.walk(o, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        obj.d0(d0).d1(d1);
                        indexer.set(obj, "^d0|d1$");
                    }
                }, WalkMode.DEPTH_NODE_FIRST);
            }
        }
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
                    // System.out.println("~~~~~~~~~~~~~~!!!!!!!!!! " +
                    // nd.id());
                    WnObj re = __create_index(nd);
                    if (null != old)
                        return old.invoke(re);
                    return re;
                }
            });

            // 执行创建
            WnNode nd = tree.create(p, path, race);
            WnObj o = indexer.toObj(nd, ObjIndexStrategy.WC);

            // 触发同步时间修改
            Wn.Io.update_ancestor_synctime(this, o, false);

            // 触发钩子
            o = wc.doHook("create", o);

            // 搞定，返回
            return o;
        }
        finally {
            wc.onCreate(old);
        }

    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // 执行创建
        WnNode nd = tree.createNode(p, id, name, race);
        WnObj o = indexer.toObj(nd, ObjIndexStrategy.WC);

        // 触发同步时间修改
        Wn.Io.update_ancestor_synctime(this, o, false);

        // 触发钩子
        o = Wn.WC().doHook("create", o);

        // 搞定，返回
        return o;
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
            WnNode nd = tree.create(p, paths, fromIndex, toIndex, race);
            WnObj o = indexer.toObj(nd, ObjIndexStrategy.WC);

            // 触发同步时间修改
            Wn.Io.update_ancestor_synctime(this, o, false);

            // 触发钩子
            o = wc.doHook("create", o);

            // 搞定，返回
            return o;
        }
        finally {
            wc.onCreate(old);
        }
    }

    private WnObj __create_index(WnNode nd) {
        return indexer.toObj(nd, ObjIndexStrategy.WC);
    }

    @Override
    public void delete(WnObj o) {
        // 调用回调
        o = Wn.WC().doHook("delete", o);

        // 链接的话，就删了吧
        if (null != o) {
            if (!o.isLink()) {
                // 目录的话，删除不能为空
                if (hasChild(o)) {
                    throw Er.create("e.io.rm.noemptynode", o);
                }
                // 文件删除
                if (!o.isDIR()) {
                    store.delete(o);
                }
            }
            // 删除树节点和索引
            indexer.remove(o.id());
            tree.delete(o);

            // 触发同步时间修改
            Wn.Io.update_ancestor_synctime(this, o, false);
        }
    }

    @Override
    public boolean hasChild(WnObj p) {
        if (p.isFILE())
            return false;
        final boolean[] re = new boolean[1];
        tree.eachChildren(p, null, new Each<WnNode>() {
            public void invoke(int index, WnNode ele, int length) {
                re[0] = true;
                Lang.Break();
            }
        });
        return re[0];
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        // 保存旧 mount 信息
        o.setv("_old_mnt", o.mount());

        // 挂载
        WnNode nd = tree.setMount(o, mnt);
        o.setNode(nd);

        // 如果是 unmount，则恢复到父节点的 mount
        if (null == mnt) {
            o.mount(getParent(o).mount());
        }
        indexer.set(o, "^mnt$");

        // 调用钩子
        Wn.WC().doHook("mount", o);
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
        return store.getInputStream(o, off);
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        // 得到节点检查的回调接口
        o = indexer.toObj(Wn.WC().whenWrite(o), ObjIndexStrategy.STRICT);

        // 是写入元数据的
        if (o.getBoolean(Wn.OBJ_META_RW)) {
            return new WnObjMetaOutputStream(o, this, off < 0);
        }
        // 写入内容
        OutputStream ops = store.getOutputStream(o, off);
        return new WnIoOutputStreamWrapper(this, o, ops);
        // return ops;
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
                return Json.toJson(o.toMap(str), fmt);
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

        WnNode nd = tree.getNode(o.id());

        if (null == nd) {
            throw Er.create("e.io.lostnode", o);
        }

        nd.loadParents(null, false);
        return o.setNode(nd);
    }

    public int each(WnQuery q, final Each<WnObj> callback) {
        if (null == callback)
            return 0;

        return indexer.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                WnNode nd = tree.getNode(o.id());
                if (null == nd)
                    throw Er.create("e.io.q.nd.noexists", o);
                nd.loadParents(null, false);
                o.setNode(nd);
                callback.invoke(index, o, length);
            }
        });
    }

    public List<WnObj> query(WnQuery q) {
        final List<WnObj> list = new LinkedList<WnObj>();
        this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                list.add(o);
            }
        });
        return list;
    }

    

    @Override
    public MimeMap mimes() {
        return this.mimes;
    }

    @Override
    public String open(WnObj o, int mode) {
        return store.open(o, mode);
    }

    @Override
    public void close(String hid) {
        store.close(hid);
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        return store.read(hid, bs, off, len);
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        store.write(hid, bs, off, len);
    }

    @Override
    public int read(String hid, byte[] bs) {
        return store.read(hid, bs);
    }

    @Override
    public void write(String hid, byte[] bs) {
        store.write(hid, bs);
    }

    @Override
    public void seek(String hid, long pos) {
        store.seek(hid, pos);
    }

    @Override
    public void flush(String hid) {
        store.flush(hid);
    }

}
