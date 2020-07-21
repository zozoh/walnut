package org.nutz.walnut.core;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class WnIoImpl2 implements WnIo {

    private static final Log log = Logs.get();

    private WnIoIndexer indexer;

    private WnIoMappingFactory mappings;

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
        im.delete(o);
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
        return false;
    }

    @Override
    public boolean existsId(String id) {
        return false;
    }

    @Override
    public WnObj checkById(String id) {
        return null;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        return null;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        return null;
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {}

    @Override
    public WnObj move(WnObj src, String destPath) {
        return null;
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        return null;
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
    public void set(WnObj o, String regex) {}

    @Override
    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        return null;
    }

    @Override
    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        return null;
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        return 0;
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
        return null;
    }

    @Override
    public WnObj getOne(WnQuery q) {
        return null;
    }

    @Override
    public WnObj getRoot() {
        return null;
    }

    @Override
    public String getRootId() {
        return null;
    }

    @Override
    public boolean isRoot(String id) {
        return false;
    }

    @Override
    public boolean isRoot(WnObj o) {
        return false;
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
        return 0;
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
        return null;
    }

    @Override
    public void _clean_for_unit_test() {
        throw Lang.noImplement();
    }

}
