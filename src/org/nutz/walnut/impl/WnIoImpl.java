package org.nutz.walnut.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
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

    public void _clean_for_unit_test() {
        tree._clean_for_unit_test();
        indexer._clean_for_unit_test();
    }

    @Override
    public WnObj get(String id) {
        WnNode nd = tree.getNode(id);
        nd.loadParents(null, false);
        return indexer.toObj(nd);
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        WnNode nd = tree.fetch(p, path);
        return indexer.toObj(nd);
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        WnNode nd = tree.fetch(p, paths, fromIndex, toIndex);
        return indexer.toObj(nd);
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
    public WnObj move(String srcPath, String destPath) {
        // 得到自身节点
        WnObj ndA = fetch(null, srcPath);

        // 不用移动
        if (srcPath.equals(destPath)) {
            return ndA;
        }

        WnContext wc = Wn.WC();

        // 确保源是可以访问的
        wc.whenAccess(ndA);

        // 确保源的父是可以写入的
        wc.whenWrite(ndA.parent());

        // 看看目标是否存在
        WnNode ta = tree.fetch(null, destPath);

        // 如果不存在
        if (null == ta) {
            ta = tree.fetch(null, Files.getParent(destPath));
        }
        // 如果存在的是一个文件
        else if (ta.isFILE()) {
            throw Er.create("e.io.exists", destPath);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.noexists", destPath);
        }

        // 确认目标能写入
        wc.whenWrite(ta);

        // 默认返回自身
        WnObj re = ndA;

        // 准备好了父在以后判断是否是同一颗树
        WnTree treeA = ndA.tree();
        WnTree treeB = ta.tree();
        // 如果在同一颗树上则简单修改一下节点
        if (treeA.equals(treeB)) {
            // 否则只能允许把对象移动到对象下面
            if (ta.isDIR() || (ta.isOBJ() && ndA.isOBJ())) {
                treeA.append(ta, ndA);
            }
            // 这肯定是一个非法的移动
            else {
                throw Er.create("e.io.move.forbidden", ndA.path() + " >> " + destPath);
            }
        }
        // 如果不是同一棵树，则一棵树上创建一颗树上删除
        else {
            WnNode ndB = treeB.createNode(ta, ndA.id(), ndA.name(), ndA.race());
            // 保持 mount
            if (ndA.isMount()) {
                treeB.setMount(ndB, ndA.mount());
            }
            // copy 内容
            WnObj oB = new WnBean().setNode(ndB);
            WnStore storeA = stores.get(ndA);
            WnStore storeB = stores.get(ndB);

            InputStream ins = storeA.getInputStream(ndA, 0);
            OutputStream ops = storeB.getOutputStream(oB, 0);
            Streams.writeAndClose(ops, ins);

            // 删除原来的内容
            storeA.cleanHistory(ndA, -1);

            // 删除旧节点
            treeA.delete(ndA);

            // 那么返回的就是 oB
            re = oB;
        }

        // 更新一下索引的记录
        re.setv("pid", ta.id());
        indexer.set(re, "^pid$");

        // 返回
        return re;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        WnNode nd = tree.create(p, path, race);
        return indexer.toObj(nd);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        WnNode nd = tree.create(p, paths, fromIndex, toIndex, race);
        return indexer.toObj(nd);
    }

    @Override
    public void delete(WnObj o) {
        WnStore store = stores.get(o);
        store.cleanHistoryBy(o, 0);
        tree.delete(o);
    }

    @Override
    public void remove(String id) {
        WnObj o = this.get(id);
        delete(o);
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
        tree.setMount(o, mnt);
        indexer.set(o, "^mnt$");
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
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
        WnStore store = stores.get(o);
        return store.getOutputStream(o, off);
    }

    @Override
    public String readText(WnObj o) {
        WnStore store = stores.get(o);
        InputStream ins = store.getInputStream(o, 0);
        Reader r = Streams.buffr(new InputStreamReader(ins));
        return Streams.readAndClose(r);
    }

    @Override
    public <T> T readJson(WnObj o, Class<T> classOfT) {
        WnStore store = stores.get(o);
        InputStream ins = store.getInputStream(o, 0);
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
        WnStore store = stores.get(o);
        OutputStream ops = store.getOutputStream(o, 0);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        Streams.writeAndClose(w, cs);
        return o.len();
    }

    @Override
    public long appendText(WnObj o, CharSequence cs) {
        WnStore store = stores.get(o);
        OutputStream ops = store.getOutputStream(o, -1);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        Streams.writeAndClose(w, cs);
        return o.len();
    }

    @Override
    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        WnStore store = stores.get(o);
        OutputStream ops = store.getOutputStream(o, 0);
        Writer w = Streams.buffw(Streams.utf8w(ops));
        try {
            Json.toJson(w, obj, fmt);
        }
        finally {
            Streams.safeClose(w);
        }
        return o.len();
    }

    public WnObj getOne(WnQuery q) {
        return indexer.getOne(q);
    }

    public WnObj toObj(WnNode nd) {
        return indexer.toObj(nd);
    }

    public void set(WnObj o, String regex) {
        indexer.set(o, regex);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return indexer.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return indexer.query(q);
    }

}
