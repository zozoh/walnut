package org.nutz.walnut.impl.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
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
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnNode;
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

    @Override
    public WnObj get(String id) {
        WnNode nd = tree.getNode(id);
        if (null == nd)
            return null;
        nd.loadParents(null, false);
        return indexer.toObj(nd);
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = get(id);
        if (null == o)
            throw Er.create("e.io.noexists" + "id:" + id);
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
        WnNode nd = tree.fetch(p, path);
        WnObj o = indexer.toObj(nd);
        // 标记一下，如果读写的时候，只写这个对象的索引
        if (rwmeta)
            o.setv(Wn.OBJ_META_RW, rwmeta);
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
        WnObj o = indexer.toObj(nd);
        // 标记一下，如果读写的时候，只写这个对象的索引
        if (rwmeta)
            o.setv(Wn.OBJ_META_RW, rwmeta);
        return o;
    }

    @Override
    public void walk(WnObj p, final Callback<WnObj> callback, WalkMode mode) {
        tree.walk(p, new Callback<WnNode>() {
            public void invoke(WnNode nd) {
                WnObj o = indexer.toObj(nd);
                if (null != callback)
                    callback.invoke(o);
            }
        }, mode);
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
                WnNode newNode = treeA.append(ta, src);
                re.setNode(newNode);
                if (null != newName && !newName.equals(src.name())) {
                    treeA.rename(re, newName);
                }
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
            WnObj oB = indexer.toObj(ndB);

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

        // 更新一下索引的记录
        __set_type(re, src.type());
        re.setv("pid", ta.id());
        indexer.set(re, "^pid|tp|mime$");

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
        WnNode nd = tree.create(p, path, race);
        return __create_index(nd);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        WnNode nd = tree.create(p, paths, fromIndex, toIndex, race);
        return __create_index(nd);
    }

    private WnObj __create_index(WnNode nd) {
        WnObj o = indexer.toObj(nd);
        __set_type(o, null);
        o.nanoStamp(System.nanoTime());
        o.createTime(o.nanoStamp() / 1000000L);
        indexer.set(o, "^lm|ct|nano|tp|mime$");

        return o;
    }

    private void __set_type(WnObj o, String tp) {
        if (Strings.isBlank(tp))
            tp = Files.getSuffixName(o.name());

        if (!o.hasType() || !o.isType(tp)) {
            if (Strings.isBlank(tp)) {
                tp = "txt";
            }
            String mime = mimes.getMime(tp);
            o.type(tp).mime(mime);
        }
    }

    @Override
    public void delete(WnObj o) {
        WnStore store = stores.get(o);
        store.cleanHistoryBy(o, 0);
        indexer.remove(o.id());
        tree.delete(o);
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
        WnNode nd = tree.setMount(o, mnt);
        o.setNode(nd);
        indexer.set(o, "^mnt$");
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
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
            return Json.toJson(Lang.map(str), JsonFormat.compact());
        }
        // 列表和数组是不可以的
        if (meta instanceof Collection<?> || meta.getClass().isArray()) {
            throw Er.create("e.io.meta.aslist", meta);
        }
        // 其他的对象，统统变 JSON 字符串
        return Json.toJson(meta, JsonFormat.compact());
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
    public String readText(WnObj o) {
        InputStream ins = this.getInputStream(o, 0);
        Reader r = Streams.buffr(new InputStreamReader(ins));
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

    public WnObj getOne(WnQuery q) {
        WnObj o = indexer.getOne(q);
        if (null == o)
            return null;

        WnNode nd = tree.getNode(o.id());

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

}
