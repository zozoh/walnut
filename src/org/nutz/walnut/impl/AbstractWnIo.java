package org.nutz.walnut.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnStoreFactory;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.security.WnSecurity;

public abstract class AbstractWnIo implements WnIo {

    protected WnTree tree;

    protected WnStoreFactory stores;

    protected WnIndexer indexer;

    protected WnSecurity security(int modeMask) {
        return new WnSecurity(indexer, modeMask);
    }

    public void _clean_for_unit_test() {
        tree._clean_for_unit_test();
        indexer._clean_for_unit_test();
    }

    public WnObj getBy(String id, String... keys) {
        return indexer.getBy(id, keys);
    }

    public void set(String id, NutMap map) {
        indexer.set(id, map);
    }

    public void set(WnObj o) {
        indexer.set(o);
    }

    public Object getValue(String id, String key) {
        return indexer.getValue(id, key);
    }

    public void setValue(String id, String key, Object val) {
        indexer.setValue(id, key, val);
    }

    public <T> T getAs(String id, Class<T> type, String key) {
        return indexer.getAs(id, type, key);
    }

    public <T> T getAs(String id, Class<T> type, String key, T dft) {
        return indexer.getAs(id, type, key, dft);
    }

    public int getInt(String id, String key) {
        return indexer.getInt(id, key);
    }

    public int getInt(String id, String key, int dft) {
        return indexer.getInt(id, key, dft);
    }

    public long getLong(String id, String key) {
        return indexer.getLong(id, key);
    }

    public long getLong(String id, String key, long dft) {
        return indexer.getLong(id, key, dft);
    }

    public String getString(String id, String key) {
        return indexer.getString(id, key);
    }

    public String getString(String id, String key, String dft) {
        return indexer.getString(id, key, dft);
    }

    public boolean getBoolean(String id, String key) {
        return indexer.getBoolean(id, key);
    }

    public boolean getBoolean(String id, String key, boolean dft) {
        return indexer.getBoolean(id, key, dft);
    }

    public float getFloat(String id, String key) {
        return indexer.getFloat(id, key);
    }

    public float getFloat(String id, String key, float dft) {
        return indexer.getFloat(id, key, dft);
    }

    public double getDouble(String id, String key) {
        return indexer.getDouble(id, key);
    }

    public double getDouble(String id, String key, double dft) {
        return indexer.getDouble(id, key, dft);
    }

    public Date getTime(String id, String key) {
        return indexer.getTime(id, key);
    }

    public Date getTime(String id, String key, Date dft) {
        return indexer.getTime(id, key, dft);
    }

    public <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum) {
        return indexer.getEnum(id, key, classOfEnum);
    }

    public <T extends Enum<T>> T getEnum(String id, String key, Class<T> classOfEnum, T dft) {
        return indexer.getEnum(id, key, classOfEnum, dft);
    }

    public boolean isEnum(String id, String key, Enum<?>... eus) {
        return indexer.isEnum(id, key, eus);
    }

    public int each(WnQuery q, Each<WnObj> callback) {
        return indexer.each(q, callback);
    }

    public List<WnObj> query(WnQuery q) {
        return indexer.query(q);
    }

    public WnObj getOne(WnQuery q) {
        return indexer.getOne(q);
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        tree.setMount(o, mnt);
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        WnStore store = stores.get(o);
        return store.getInputStream(o, off);
    }

    @Override
    public InputStream getInputStream(WnHistory his, long off) {
        WnObj o = get(his.oid());
        WnStore store = stores.get(o);
        return store.getInputStream(o, off);
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        WnStore store = stores.get(o);
        return store.getOutputStream(o, off);
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
        WnObj o = get(oid);
        WnStore store = stores.get(o);
        return store.addHistory(oid, data, sha1, len);
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
}
