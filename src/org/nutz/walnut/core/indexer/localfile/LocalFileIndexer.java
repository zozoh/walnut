package org.nutz.walnut.core.indexer.localfile;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.indexer.AbstractIoIndexer;

public class LocalFileIndexer extends AbstractIoIndexer {

    @Override
    public boolean exists(WnObj p, String path) {
        return false;
    }

    @Override
    public boolean existsId(String id) {
        return false;
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
    public void delete(WnObj o) {}

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

}
