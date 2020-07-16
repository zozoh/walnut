package org.nutz.walnut.core;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;

public class WnIoMapping {

    private WnIoIndexer indexer;

    private WnIoBM BM;

    public WnIoMapping(WnIoIndexer indexer, WnIoBM BM) {
        this.indexer = indexer;
        this.BM = BM;
    }

    public boolean exists(WnObj p, String path) {
        return indexer.exists(p, path);
    }

    public boolean existsId(String id) {
        return indexer.existsId(id);
    }

    public WnObj fetch(WnObj p, String path) {
        return indexer.fetch(p, path);
    }

    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        return indexer.fetch(p, paths, fromIndex, toIndex);
    }

    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        indexer.walk(p, callback, mode);
    }

    public WnObj move(WnObj src, String destPath) {
        return indexer.move(src, destPath);
    }

    public WnObj move(WnObj src, String destPath, int mode) {
        return indexer.move(src, destPath, mode);
    }

    public WnObj rename(WnObj o, String nm) {
        return indexer.rename(o, nm);
    }

    public WnObj rename(WnObj o, String nm, boolean keepType) {
        return indexer.rename(o, nm, keepType);
    }

    public WnObj rename(WnObj o, String nm, int mode) {
        return indexer.rename(o, nm, mode);
    }

    public void set(WnObj o, String regex) {
        indexer.set(o, regex);
    }

    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        return indexer.setBy(id, map, returnNew);
    }

    public WnObj setBy(WnQuery q, NutMap map, boolean returnNew) {
        return indexer.setBy(q, map, returnNew);
    }

    public int inc(String id, String key, int val, boolean returnNew) {
        return indexer.inc(id, key, val, returnNew);
    }

    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return indexer.inc(q, key, val, returnNew);
    }

    public int getInt(String id, String key, int dft) {
        return indexer.getInt(id, key, dft);
    }

    public long getLong(String id, String key, long dft) {
        return indexer.getLong(id, key, dft);
    }

    public String getString(String id, String key, String dft) {
        return indexer.getString(id, key, dft);
    }

    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return indexer.getAs(id, key, classOfT, dft);
    }

    public WnObj create(WnObj p, String path, WnRace race) {
        return indexer.create(p, path, race);
    }

    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        return indexer.create(p, paths, fromIndex, toIndex, race);
    }

    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return indexer.createById(p, id, name, race);
    }

    public void delete(WnObj o) {
        indexer.delete(o);
        BM.delete(o);
    }

    public WnObj get(String id) {
        return indexer.get(id);
    }

    public WnObj getOne(WnQuery q) {
        return indexer.getOne(q);
    }

    public WnObj getRoot() {
        return indexer.getRoot();
    }

    public String getRootId() {
        return indexer.getRootId();
    }

    public boolean isRoot(String id) {
        return indexer.isRoot(id);
    }

    public boolean isRoot(WnObj o) {
        return indexer.isRoot(o);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return indexer.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return indexer.query(q);
    }

    public List<WnObj> getChildren(WnObj o, String name) {
        return indexer.getChildren(o, name);
    }

    public long count(WnQuery q) {
        return indexer.count(q);
    }

    public boolean hasChild(WnObj p) {
        return indexer.hasChild(p);
    }

    public WnObj push(String id, String key, Object val, boolean returnNew) {
        return indexer.push(id, key, val, returnNew);
    }

    public void push(WnQuery query, String key, Object val) {
        indexer.push(query, key, val);
    }

    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        return indexer.pull(id, key, val, returnNew);
    }

    public void pull(WnQuery query, String key, Object val) {
        indexer.pull(query, key, val);
    }

    public boolean isSameBM(WnIoMapping mapping) {
        return this.BM.isSame(mapping.BM);
    }

    public long copyData(WnObj a, WnObj b) {
        return BM.copyData(a, b);
    }

    public WnIoHandle open(WnObj o, int mode) {
        return BM.open(o, mode);
    }

    public int read(String hid, byte[] bs, int off, int len) {
        return BM.read(hid, bs, off, len);
    }

    public void write(String hid, byte[] bs, int off, int len) {
        BM.write(hid, bs, off, len);
    }

    public int read(String hid, byte[] bs) {
        return BM.read(hid, bs);
    }

    public void write(String hid, byte[] bs) {
        BM.write(hid, bs);
    }

    public void seek(String hid, long pos) {
        BM.seek(hid, pos);
    }

    public void trancate(WnObj o, long len) {
        BM.trancate(o, len);
    }

    public long getPos(String hid) {
        return BM.getPos(hid);
    }

}
