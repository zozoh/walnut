package org.nutz.walnut.core.io;

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
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnExpiObjTable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnObjFilter;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.lock.WnLockApi;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnIoWrapper implements WnIo {

    protected WnExpiObjTable expiTable;

    protected WnLockApi locks;

    protected WnIo io;

    protected AbstractWnIoWrapper() {}

    public AbstractWnIoWrapper(WnIo io) {
        this.io = io;
    }

    protected void tryAddExpiObj(WnObj o) {
        if (null != expiTable && null != o) {
            this.tryAddExpiObj(o.id(), o.expireTime());
        }
    }

    protected void tryAddExpiObj(String id, long expi) {
        if (null != expiTable && null != id && expi > 0) {
            expiTable.insertOrUpdate(id, expi);
        }
    }

    protected void tryRemoveExpiObj(WnObj o) {
        if (null != expiTable && null != o) {
            expiTable.remove(o.id());
        }
    }

    public long copyData(WnObj a, WnObj b) {
        return io.copyData(a, b);
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

    public String open(WnObj o, int mode) {
        return io.open(o, mode);
    }

    public WnObj check(WnObj p, String path) {
        return io.check(p, path);
    }

    public WnObj fetch(WnObj p, String path) {
        return io.fetch(p, path);
    }

    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        return io.fetch(p, paths, fromIndex, toIndex);
    }

    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        return io.createIfNoExists(p, path, race);
    }

    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        return io.createIfExists(p, path, race);
    }

    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        io.walk(p, callback, mode);
    }

    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode, WnObjFilter filter) {
        io.walk(p, callback, mode, filter);
    }

    public WnObj flush(String hid) {
        return io.flush(hid);
    }

    public WnObj move(WnObj src, String destPath) {
        return io.move(src, destPath);
    }

    public WnObj move(WnObj src, String destPath, int mode) {
        return io.move(src, destPath, mode);
    }

    public WnObj rename(WnObj o, String nm) {
        return io.rename(o, nm);
    }

    public WnObj rename(WnObj o, String nm, boolean keepType) {
        return io.rename(o, nm, keepType);
    }

    public WnObj close(String hid) {
        return io.close(hid);
    }

    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        WnObj o2 = io.setBy(id, key, val, returnNew);
        if ("expi".equals(key) && (val instanceof Long)) {
            long expi = (Long) val;
            this.tryAddExpiObj(id, expi);
        }
        return o2;
    }

    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        WnObj o = io.setBy(q, key, val, returnNew);
        if (null != o && "expi".equals(key) && (val instanceof Long)) {
            long expi = (Long) val;
            this.tryAddExpiObj(o.id(), expi);
        }
        return o;
    }

    public WnObj rename(WnObj o, String nm, int mode) {
        return io.rename(o, nm, mode);
    }

    public void set(WnObj o, String regex) {
        io.set(o, regex);
        NutBean map = o.pickBy(regex);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
    }

    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        WnObj o = io.setBy(id, map, returnNew);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(id, map.getLong("expi"));
        }
        return o;
    }

    public void setMount(WnObj o, String mnt) {
        io.setMount(o, mnt);
    }

    public void writeMeta(WnObj o, Object meta) {
        NutBean map = Wn.anyToMap(o, meta);
        io.writeMeta(o, map);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
    }

    public void appendMeta(WnObj o, Object meta) {
        NutBean map = Wn.anyToMap(o, meta);
        io.appendMeta(o, map);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
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

    public int read(String hid, byte[] bs, int off, int len) {
        return io.read(hid, bs, off, len);
    }

    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        WnObj o = io.setBy(q, map, returnNew);
        if (null != o && map.containsKey("expi")) {
            this.tryAddExpiObj(o.id(), map.getLong("expi"));
        }
        return o;
    }

    public <T> T readJson(WnObj o, Class<T> classOfT) {
        return io.readJson(o, classOfT);
    }

    public long writeImage(WnObj o, RenderedImage im) {
        return io.writeImage(o, im);
    }

    public long writeText(WnObj o, CharSequence cs) {
        return io.writeText(o, cs);
    }

    public long appendText(WnObj o, CharSequence cs) {
        return io.appendText(o, cs);
    }

    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        return io.writeJson(o, obj, fmt);
    }

    public long writeBytes(WnObj o, byte[] buf) {
        return io.writeBytes(o, buf);
    }

    public long writeBytes(WnObj o, byte[] buf, int off, int len) {
        return io.writeBytes(o, buf, off, len);
    }

    public long writeAndClose(WnObj o, InputStream ins) {
        return io.writeAndClose(o, ins);
    }

    public void write(String hid, byte[] bs, int off, int len) {
        io.write(hid, bs, off, len);
    }

    public int inc(String id, String key, int val, boolean returnNew) {
        return io.inc(id, key, val, returnNew);
    }

    public Reader getReader(WnObj o, long off) {
        return io.getReader(o, off);
    }

    public Writer getWriter(WnObj o, long off) {
        return io.getWriter(o, off);
    }

    @Override
    public WnIoIndexer getIndexer(WnObj o) {
        return io.getIndexer(o);
    }

    public WnIoHandle openHandle(WnObj o, int mode) throws WnIoHandleMutexException, IOException {
        return io.openHandle(o, mode);
    }

    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return io.inc(q, key, val, returnNew);
    }

    public InputStream getInputStream(WnObj o, long off) {
        return io.getInputStream(o, off);
    }

    public int read(String hid, byte[] bs) {
        return io.read(hid, bs);
    }

    public void write(String hid, byte[] bs) {
        io.write(hid, bs);
    }

    public OutputStream getOutputStream(WnObj o, long off) {
        return io.getOutputStream(o, off);
    }

    public void seek(String hid, long pos) {
        io.seek(hid, pos);
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

    public WnObj create(WnObj p, String path, WnRace race) {
        return io.create(p, path, race);
    }

    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        return io.create(p, paths, fromIndex, toIndex, race);
    }

    public WnObj create(WnObj p, WnObj o) {
        WnObj o2 = io.create(p, o);
        this.tryAddExpiObj(o2);
        return o2;
    }

    public MimeMap mimes() {
        return io.mimes();
    }

    public void delete(WnObj o) {
        io.delete(o);
        this.tryRemoveExpiObj(o);
    }

    public void delete(WnObj o, boolean r) {
        io.delete(o, r);
        this.tryRemoveExpiObj(o);
    }

    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return io.createById(p, id, name, race);
    }

    public void trancate(WnObj o, long len) {
        io.trancate(o, len);
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

    public long getPos(String hid) {
        return io.getPos(hid);
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

    @Override
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        return io.eachChild(o, name, callback);
    }

    public List<WnObj> getChildren(WnObj o, String name) {
        return io.getChildren(o, name);
    }

    public long count(WnQuery q) {
        return io.count(q);
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

}
