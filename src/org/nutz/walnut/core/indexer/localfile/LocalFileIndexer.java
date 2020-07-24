package org.nutz.walnut.core.indexer.localfile;

import java.io.File;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.AbstractIoIndexer;

public class LocalFileIndexer extends AbstractIoIndexer {

    protected LocalFileIndexer(WnObj root, WnIoMappingFactory mappings, MimeMap mimes) {
        super(root, mappings, mimes);
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
    public void delete(WnObj o) {}

    @Override
    public WnObj get(String id) {
        return null;
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        return 0;
    }

    @Override
    public int eachChild(WnObj o, Each<WnObj> callback) {
        return 0;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        return null;
    }

    @Override
    public long countChildren(WnObj o) {
        return 0;
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
    protected WnObj _create(WnObj o) {
        return null;
    }

    @Override
    protected WnObj _fetch_one_by_name(WnObj p, String name) {
        return null;
    }

    @Override
    protected WnObj _get_by_id(String id) {
        return null;
    }

    @Override
    protected void _set(String id, NutMap map) {}

    @Override
    protected WnIoObj _set_by(WnQuery q, NutMap map, boolean returnNew) {
        return null;
    }

    

}
