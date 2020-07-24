package org.nutz.walnut.core;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnObjId;
import org.nutz.walnut.util.Wn;

public class WnIoImpl2 implements WnIo {

    private static final Log log = Logs.get();

    /**
     * 根索引管理器
     */
    private WnIoIndexer indexer;

    /**
     * 映射工厂类
     */
    private WnIoMappingFactory mappings;

    /**
     * 句柄管理器
     */
    private WnIoHandleManager handles;

    public WnIoImpl2(WnIoIndexer indexer, WnIoMappingFactory mappings, WnIoHandleManager handles) {
        this.indexer = indexer;
        this.mappings = mappings;
        this.handles = handles;
    }

    @Override
    public long copyData(WnObj a, WnObj b) {
        WnIoMapping ma = mappings.check(a);
        WnIoMapping mb = mappings.check(b);
        // 调试日志
        if (log.isDebugEnabled()) {
            log.debugf("copyData ma:%s, mb:%s",
                       ma.getClass().getSimpleName(),
                       mb.getClass().getSimpleName());
        }
        // 相同桶管理器可以快速 Copy
        if (ma.isSameBM(mb)) {
            if (log.isDebugEnabled()) {
                log.debug("Quick copy");
            }
            return ma.copyData(a, b);
        }

        // 否则只能硬 COPY 了
        long re = 0;
        WnIoHandle h_a = ma.open(a, Wn.S.R);
        WnIoHandle h_b = mb.open(b, Wn.S.W);
        if (log.isDebugEnabled()) {
            log.debugf("copyData open h_a:%s, h_b:%s", h_a, h_b);
        }
        try {
            byte[] buf = new byte[8192];
            int len;
            while ((len = h_a.read(buf, 0, buf.length)) > 0) {
                re += len;
                h_b.write(buf, 0, len);
            }
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        // 确保关闭
        finally {
            // 关闭读
            try {
                h_a.close();
            }
            catch (Exception e) {
                log.warn("Fail to close A:" + h_a, e);
            }
            // 刷新写缓冲
            try {
                h_b.flush();
            }
            catch (Exception e) {
                log.warn("Fail to flush B:" + h_b, e);
            }
            // 关闭写缓冲
            try {
                h_b.close();
            }
            catch (Exception e) {
                log.warn("Fail to close B:" + h_b, e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debugf("copyData done: %d", re);
        }
        return re;
    }

    @Override
    public String open(WnObj o, int mode) {
        WnIoMapping im = mappings.check(o);
        WnIoHandle h = im.open(o, mode);
        return h.getId();
    }

    @Override
    public WnObj flush(String hid) {
        WnIoHandle h = handles.check(hid);
        try {
            h.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        return h.getObj();
    }

    @Override
    public WnObj close(String hid) {
        WnIoHandle h = handles.check(hid);
        try {
            h.close();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        return h.getObj();
    }

    @Override
    public int read(String hid, byte[] bs, int off, int len) {
        WnIoHandle h = handles.check(hid);
        try {
            return h.read(bs, off, len);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public void write(String hid, byte[] bs, int off, int len) {
        WnIoHandle h = handles.check(hid);
        try {
            h.write(bs, off, len);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public int read(String hid, byte[] bs) {
        return this.read(hid, bs, 0, bs.length);
    }

    @Override
    public void write(String hid, byte[] bs) {
        write(hid, bs, 0, bs.length);
    }

    @Override
    public void seek(String hid, long pos) {
        WnIoHandle h = handles.check(hid);
        h.setOffset(pos);
    }

    @Override
    public long getPos(String hid) {
        WnIoHandle h = handles.check(hid);
        return h.getOffset();
    }

    @Override
    public void delete(WnObj o) {
        WnIoMapping im = mappings.check(o);
        im.delete(o, false);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        if (o.isDIR() && r) {
            // 递归
            each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    delete(child, true);
                }
            });
        }
        // 删除自己
        delete(o);
    }

    @Override
    public void trancate(WnObj o, long len) {
        WnIoMapping im = mappings.check(o);
        im.truncate(o, len);
    }

    @Override
    public boolean exists(WnObj p, String path) {
        return null != fetch(p, path);
    }

    @Override
    public boolean existsId(String id) {
        return indexer.existsId(id);
    }

    @Override
    public WnObj checkById(String id) {
        // 直接来吧
        WnObj o = this.get(id);
        if (null == o) {
            throw Er.create("e.io.obj.noexists", "id:" + id);
        }
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.obj.noexists", path);
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        if (path.startsWith("/")) {
            p = null;
        }
        String[] ss = Strings.splitIgnoreBlank(path, "[/]");
        return fetch(p, ss, 0, ss.length);
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        return indexer.fetch(p, paths, fromIndex, toIndex);
    }

    @Override
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        // DEPTH_LEAF_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            __walk_DEPTH_LEAF_FIRST(p, callback);
        }
        // DEPTH_NODE_FIRST
        else if (WalkMode.DEPTH_NODE_FIRST == mode) {
            __walk_DEPATH_NODE_FIRST(p, callback);
        }
        // 广度优先
        else if (WalkMode.BREADTH_FIRST == mode) {
            __walk_BREADTH_FIRST(p, callback);
        }
        // 仅叶子节点
        else if (WalkMode.LEAF_ONLY == mode) {
            __walk_LEAF_ONLY(p, callback);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    protected void _do_walk_children(WnObj p, final Callback<WnObj> callback) {
        List<WnObj> list = this.getChildren(p, null);
        for (WnObj o : list) {
            try {
                callback.invoke(o);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {
                continue;
            }
        }
    }

    private void __walk_LEAF_ONLY(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (nd.isFILE())
                    callback.invoke(nd);
                else
                    __walk_LEAF_ONLY(nd, callback);
            }
        });
    }

    private void __walk_BREADTH_FIRST(WnObj p, final Callback<WnObj> callback) {
        final List<WnObj> list = new LinkedList<WnObj>();
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE())
                    list.add(nd);
            }
        });
        for (WnObj nd : list)
            __walk_BREADTH_FIRST(nd, callback);
    }

    private void __walk_DEPATH_NODE_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE()) {
                    __walk_DEPATH_NODE_FIRST(nd, callback);
                }
            }
        });
    }

    private void __walk_DEPTH_LEAF_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (!nd.isFILE()) {
                    __walk_DEPTH_LEAF_FIRST(nd, callback);
                }
                callback.invoke(nd);
            }
        });
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        if (src.isMount()) {
            WnIoMapping mapping = mappings.check(src);
            return mapping.move(src, destPath);
        }
        return indexer.move(src, destPath);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        if (src.isMount()) {
            WnIoMapping mapping = mappings.check(src);
            return mapping.move(src, destPath, mode);
        }
        return indexer.move(src, destPath, mode);
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        return null;
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        return null;
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        return null;
    }

    @Override
    public void set(WnObj o, String regex) {
        if (o.isMount()) {
            WnIoMapping mapping = mappings.check(o);
            mapping.set(o, regex);
        }
        // 根
        else {
            indexer.set(o, regex);
        }
    }

    @Override
    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        WnObjId oid = new WnObjId(id);
        // 两段式 ID, 前段必有 mount
        if (oid.hasHomeId()) {
            WnObj oHome = this.indexer.checkById(oid.getHomeId());
            if (!oHome.isMount()) {
                throw Er.create("e.io.weirdid.HomeNotMount", id);
            }
            WnIoMapping mapping = mappings.check(oHome);
            return mapping.setBy(oid.getMyId(), map, returnNew);
        }

        // 直接来吧
        return this.indexer.setBy(id, map, returnNew);
    }

    @Override
    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        // 如果声明了 pid ，则看看有木有映射
        String pid = q.first().getString("pid");
        if (!Strings.isBlank(pid)) {
            WnObj p = this.checkById(pid);
            if (p.isMount()) {
                WnIoMapping mapping = mappings.check(p);
                return mapping.setBy(q, map, returnNew);
            }
        }
        // 采用根索引管理器
        return indexer.setBy(q, map, returnNew);
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        WnObjId oid = new WnObjId(id);
        // 两段式 ID, 前段必有 mount
        if (oid.hasHomeId()) {
            WnObj oHome = this.indexer.checkById(oid.getHomeId());
            if (!oHome.isMount()) {
                throw Er.create("e.io.weirdid.HomeNotMount", id);
            }
            WnIoMapping mapping = mappings.check(oHome);
            return mapping.inc(oid.getMyId(), key, val, returnNew);
        }

        // 直接来吧
        return this.indexer.inc(id, key, val, returnNew);
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return 0;
    }

    @Override
    public int getInt(String id, String key, int dft) {
        return 0;
    }

    @Override
    public long getLong(String id, String key, long dft) {
        return 0;
    }

    @Override
    public String getString(String id, String key, String dft) {
        return null;
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return null;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        return null;
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        return null;
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return null;
    }

    @Override
    public WnObj get(String id) {
        WnObjId oid = new WnObjId(id);
        // 两段式 ID, 前段必有 mount
        if (oid.hasHomeId()) {
            WnObj oHome = this.indexer.checkById(oid.getHomeId());
            if (!oHome.isMount()) {
                throw Er.create("e.io.weirdid.HomeNotMount", id);
            }
            WnIoMapping mapping = mappings.check(oHome);
            return mapping.checkById(oid.getMyId());
        }

        // 直接来吧
        return this.indexer.get(id);
    }

    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (q == null)
            q = new WnQuery();
        q.limit(1);

        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
            }
        });
        return re[0];
    }

    @Override
    public WnObj getRoot() {
        return indexer.getRoot();
    }

    @Override
    public String getRootId() {
        return indexer.getRootId();
    }

    @Override
    public boolean isRoot(String id) {
        return indexer.isRoot(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return indexer.isRoot(o);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        return 0;
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        return null;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        return null;
    }

    @Override
    public long count(WnQuery q) {
        // 如果指定了父，且有映射，则尝试用对应的索引管理器
        String pid = q.first().getString("pid");
        WnObj p = this.checkById(pid);
        if (p.isMount()) {
            WnIoMapping mapping = mappings.check(p);
            return mapping.count(q);
        }

        // 否则用根索引管理器
        return this.indexer.count(q);
    }

    @Override
    public boolean hasChild(WnObj p) {
        return false;
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public void push(WnQuery query, String key, Object val) {}

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {}

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        return null;
    }

    @Override
    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        return null;
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        return null;
    }

    @Override
    public void setMount(WnObj o, String mnt) {}

    @Override
    public void writeMeta(WnObj o, Object meta) {}

    @Override
    public void appendMeta(WnObj o, Object meta) {}

    @Override
    public String readText(WnObj o) {
        return null;
    }

    @Override
    public BufferedImage readImage(WnObj o) {
        return null;
    }

    @Override
    public long readAndClose(WnObj o, OutputStream ops) {
        return 0;
    }

    @Override
    public <T> T readJson(WnObj o, Class<T> classOfT) {
        return null;
    }

    @Override
    public long writeImage(WnObj o, RenderedImage im) {
        return 0;
    }

    @Override
    public long writeText(WnObj o, CharSequence cs) {
        return 0;
    }

    @Override
    public long appendText(WnObj o, CharSequence cs) {
        return 0;
    }

    @Override
    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        return 0;
    }

    @Override
    public long writeAndClose(WnObj o, InputStream ins) {
        return 0;
    }

    @Override
    public Reader getReader(WnObj o, long off) {
        return null;
    }

    @Override
    public Writer getWriter(WnObj o, long off) {
        return null;
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        return null;
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        return null;
    }

    @Override
    public MimeMap mimes() {
        return indexer.mimes();
    }

    @Override
    public void _clean_for_unit_test() {
        throw Lang.noImplement();
    }

}
