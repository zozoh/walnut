package com.site0.walnut.core.io;

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
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnObjFilter;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.util.Wlang;

public class WnIoSaftyWrapper implements WnIo {

    private WnLockApi lockApi;

    private WnIo io;

    public WnIoSaftyWrapper() {}

    public WnIoSaftyWrapper(WnIo io) {
        this.setIo(io);
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
        if (null != io) {
            lockApi = io.getLockApi();
        }
    }

    public WnLockApi getLockApi() {
        return lockApi;
    }

    public void setLockApi(WnLockApi lockApi) {
        this.lockApi = lockApi;
    }

    private void _safe_run(String methodName,
                           WnObj obj,
                           Atom atom,
                           String[] operations) {
        WnIoWriteLocker locker = new WnIoWriteLocker(lockApi,
                                                     obj,
                                                     operations,
                                                     methodName);
        locker.safeRun(atom);
    }

    private <T> T _safe_return(String methodName,
                               WnObj obj,
                               Proton<T> proton,
                               String[] operations) {
        WnIoWriteLocker locker = new WnIoWriteLocker(lockApi,
                                                     obj,
                                                     operations,
                                                     methodName);
        return locker.safeReturn(proton);
    }

    private <T> T _safe_return(String methodName,
                               String objId,
                               Proton<T> proton,
                               String[] operations) {
        WnIoWriteLocker locker = new WnIoWriteLocker(lockApi,
                                                     objId,
                                                     operations,
                                                     methodName);
        return locker.safeReturn(proton);
    }

    public WnIoMappingFactory getMappingFactory() {
        return io.getMappingFactory();
    }

    public boolean exists(WnObj p, String path) {
        return io.exists(p, path);
    }

    public boolean existsId(String id) {
        return io.existsId(id);
    }

    public WnObj checkById(String id) {
        return io.checkById(id);
    }

    public WnObj check(WnObj p, String path) {
        return io.check(p, path);
    }

    public WnObj fetch(WnObj p, String path) {
        return io.fetch(p, path);
    }

    public WnObj fetch(WnObj p,
                       String[] paths,
                       boolean isForDir,
                       int fromIndex,
                       int toIndex) {
        return io.fetch(p, paths, isForDir, fromIndex, toIndex);
    }

    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        io.walk(p, callback, mode);
    }

    public void walk(WnObj p,
                     Callback<WnObj> callback,
                     WalkMode mode,
                     WnObjFilter filter) {
        io.walk(p, callback, mode, filter);
    }

    public WnObj move(WnObj src, String destPath) {
        return _safe_return("move", src, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.move(src, destPath);
            }
        }, new String[]{"meta"});
    }

    public WnObj move(WnObj src, String destPath, int mode) {
        return _safe_return("move_with_mode", src, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.move(src, destPath, mode);
            }
        }, new String[]{"meta"});
    }

    public WnObj rename(WnObj o, String nm) {
        return _safe_return("rename", o, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.rename(o, nm);
            }
        }, new String[]{"meta"});
    }

    public WnObj rename(WnObj o, String nm, boolean keepType) {
        return _safe_return("rename_keepType", o, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.rename(o, nm, keepType);
            }
        }, new String[]{"meta"});
    }

    public WnObj rename(WnObj o, String nm, int mode) {
        return _safe_return("rename_with_mode", o, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.rename(o, nm, mode);
            }
        }, new String[]{"meta"});
    }

    public void set(WnObj o, String regex) {
        _safe_run("set", o, new Atom() {
            public void run() {
                io.set(o, regex);
            }
        }, new String[]{"meta"});
    }

    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        return _safe_return("setBy", id, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.setBy(id, map, returnNew);
            }
        }, new String[]{"meta"});
    }

    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        return io.setBy(q, map, returnNew);
    }

    public int inc(String id, String key, int val, boolean returnNew) {
        return io.inc(id, key, val, returnNew);
    }

    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return io.inc(q, key, val, returnNew);
    }

    public int getInt(String id, String key, int dft) {
        return io.getInt(id, key, dft);
    }

    public long getLong(String id, String key, long dft) {
        return io.getLong(id, key, dft);
    }

    public String getString(String id, String key, String dft) {
        return io.getString(id, key, dft);
    }

    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return io.getAs(id, key, classOfT, dft);
    }

    public WnObj get(String id) {
        return io.get(id);
    }

    public WnObj getIn(WnObj p, String id) {
        return io.getIn(p, id);
    }

    public WnObj getOne(WnQuery q) {
        return io.getOne(q);
    }

    public WnObj getRoot() {
        return io.getRoot();
    }

    public String getRootId() {
        return io.getRootId();
    }

    public boolean isRoot(String id) {
        return io.isRoot(id);
    }

    public boolean isRoot(WnObj o) {
        return io.isRoot(o);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return io.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return io.query(q);
    }

    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        return io.eachChild(o, name, callback);
    }

    public List<WnObj> getChildren(WnObj o, String name) {
        return io.getChildren(o, name);
    }

    public int count(WnQuery q) {
        return io.count(q);
    }

    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        return io.aggregate(q, agg);
    }

    public boolean hasChild(WnObj p) {
        return io.hasChild(p);
    }

    public WnObj push(String id, String key, Object val, boolean returnNew) {
        return io.push(id, key, val, returnNew);
    }

    public void push(WnQuery query, String key, Object val) {
        io.push(query, key, val);
    }

    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        return io.pull(id, key, val, returnNew);
    }

    public void pull(WnQuery query, String key, Object val) {
        io.pull(query, key, val);
    }

    public WnObj create(WnObj p, WnObj o) {
        return io.create(p, o);
    }

    public WnObj createIfNoExists(WnObj p, WnObj o) {
        return io.createIfNoExists(p, o);
    }

    public WnObj createIfExists(WnObj p, WnObj o) {
        return io.createIfExists(p, o);
    }

    public WnObj create(WnObj p, String path, WnRace race) {
        return io.create(p, path, race);
    }

    public WnObj create(WnObj p,
                        String[] paths,
                        int fromIndex,
                        int toIndex,
                        WnRace race) {
        return io.create(p, paths, fromIndex, toIndex, race);
    }

    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return io.createById(p, id, name, race);
    }

    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        return io.createIfNoExists(p, path, race);
    }

    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        return io.createIfExists(p, path, race);
    }

    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        return _safe_return("setBy2", id, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.setBy(id, key, val, returnNew);
            }
        }, new String[]{"meta"});
    }

    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        return io.setBy(q, key, val, returnNew);
    }

    public void setMount(WnObj o, String mnt) {
        _safe_run("setMount", o, new Atom() {
            public void run() {
                io.setMount(o, mnt);
            }
        }, new String[]{"meta"});
    }

    public void writeMeta(WnObj o, Object meta) {
        _safe_run("writeMeta", o, new Atom() {
            public void run() {
                io.writeMeta(o, meta);
            }
        }, new String[]{"content"});
    }

    public void appendMeta(WnObj o, Object meta) {
        _safe_run("appendMeta", o, new Atom() {
            public void run() {
                io.appendMeta(o, meta);
            }
        }, new String[]{"content"});
    }

    public void appendMeta(WnObj o, Object meta, boolean keepType) {
        _safe_run("appendMeta_keepType", o, new Atom() {
            public void run() {
                io.appendMeta(o, meta, keepType);
            }
        }, new String[]{"content"});
    }

    public String readText(WnObj o) {
        return io.readText(o);
    }

    public byte[] readBytes(WnObj o) {
        return io.readBytes(o);
    }

    public BufferedImage readImage(WnObj o) {
        return io.readImage(o);
    }

    public long readAndClose(WnObj o, OutputStream ops) {
        return io.readAndClose(o, ops);
    }

    public <T> T readJson(WnObj o, Class<T> classOfT) {
        return io.readJson(o, classOfT);
    }

    public long writeImage(WnObj o, RenderedImage im) {
        return _safe_return("writeImage", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeImage(o, im);
            }
        }, new String[]{"content"});
    }

    public long writeText(WnObj o, CharSequence cs) {
        return _safe_return("writeText", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeText(o, cs);
            }
        }, new String[]{"content"});
    }

    public long appendText(WnObj o, CharSequence cs) {
        return _safe_return("appendText", o, new Proton<Long>() {
            protected Long exec() {
                return io.appendText(o, cs);
            }
        }, new String[]{"content"});
    }

    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        return _safe_return("writeJson", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeJson(o, obj, fmt);
            }
        }, new String[]{"content"});
    }

    public long writeBytes(WnObj o, byte[] buf) {
        return _safe_return("writeBytes", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeBytes(o, buf);
            }
        }, new String[]{"content"});
    }

    public long writeBytes(WnObj o, byte[] buf, int off, int len) {
        return _safe_return("writeBytes", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeBytes(o, buf, off, len);
            }
        }, new String[]{"content"});
    }

    public long write(WnObj o, InputStream ins) {
        return _safe_return("writeAndClose", o, new Proton<Long>() {
            protected Long exec() {
                return io.write(o, ins);
            }
        }, new String[]{"content"});
    }

    public long writeAndClose(WnObj o, InputStream ins) {
        return _safe_return("writeAndClose", o, new Proton<Long>() {
            protected Long exec() {
                return io.writeAndClose(o, ins);
            }
        }, new String[]{"content"});
    }

    public Reader getReader(WnObj o, long off) {
        return io.getReader(o, off);
    }

    public Writer getWriter(WnObj o, long off) {
        return io.getWriter(o, off);
    }

    public WnIoIndexer getIndexer(WnObj o) {
        return io.getIndexer(o);
    }

    public WnIoHandle openHandle(WnObj o, int mode)
            throws WnIoHandleMutexException, IOException {
        return io.openHandle(o, mode);
    }

    public long copyData(WnObj a, WnObj b) {
        return _safe_return("copyData", b, new Proton<Long>() {
            protected Long exec() {
                return io.copyData(a, b);
            }
        }, new String[]{"content"});
    }

    public String open(WnObj o, int mode) {
        return io.open(o, mode);
    }

    public WnObj flush(String hid) {
        return io.flush(hid);
    }

    public WnObj close(String hid) {
        return io.close(hid);
    }

    public int read(String hid, byte[] bs, int off, int len) {
        return io.read(hid, bs, off, len);
    }

    public void write(String hid, byte[] bs, int off, int len) {
        io.write(hid, bs, off, len);
    }

    public int read(String hid, byte[] bs) {
        return io.read(hid, bs);
    }

    public void write(String hid, byte[] bs) {
        io.write(hid, bs);
    }

    public void seek(String hid, long pos) {
        io.seek(hid, pos);
    }

    public void delete(WnObj o) {
        _safe_run("delete", o, new Atom() {
            public void run() {
                io.delete(o);
            }
        }, new String[]{"meta", "content"});
    }

    public void delete(WnObj o, boolean r) {
        _safe_run("delete", o, new Atom() {
            public void run() {
                io.delete(o, r);
            }
        }, new String[]{"meta", "content"});
    }

    public void trancate(WnObj o, long len) {
        _safe_run("trancate", o, new Atom() {
            public void run() {
                io.trancate(o, len);
            }
        }, new String[]{"meta", "content"});
    }

    public long getPos(String hid) {
        return io.getPos(hid);
    }

    public InputStream getInputStream(WnObj o, long off) {
        return io.getInputStream(o, off);
    }

    public InputStream getInputStream(WnObj o) {
        return this.getInputStream(o, 0);
    }

    public OutputStream getOutputStream(WnObj o, long off) {
        WnIoWriteLocker locker = new WnIoWriteLocker(lockApi,
                                                     o,
                                                     new String[]{"meta",
                                                                  "content"},
                                                     "getOutputStream");
        while (true) {
            try {
                WnLock lock = locker.tryLock();
                OutputStream ops = io.getOutputStream(o, off);
                return new WnIoSaftyWrappedOutputStream(ops, lockApi, lock);
            }
            // 加锁失败的话，需要等待一会而
            catch (WnLockFailException e) {
                Wlang.wait(WnIoWriteLocker.class, 3000);
            }
        }
    }

    public MimeMap mimes() {
        return io.mimes();
    }

}
