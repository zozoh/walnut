package com.site0.walnut.ext.xo.io.indexer;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.core.indexer.AbstractIoIndexer;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlog;

public class WnVofsIndexer extends AbstractIoIndexer {

    private static Log log = Wlog.getIO();

    private XoService xos;

    public WnVofsIndexer(WnObj oMntRoot, MimeMap mimes, XoService xos) {
        super(oMntRoot, mimes);
        this.xos = xos;
    }

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean existsId(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WnObj checkById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj fetchByName(WnObj p, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void set(WnObj o, String regex) {
        // TODO Auto-generated method stub

    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getInt(String id, String key, int dft) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLong(String id, String key, long dft) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getString(String id, String key, String dft) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(WnObj o) {
        // TODO Auto-generated method stub

    }

    @Override
    public WnObj get(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnObj getOne(WnQuery q) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long countChildren(WnObj o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long count(WnQuery q) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasChild(WnObj p) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        // TODO Auto-generated method stub

    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        // TODO Auto-generated method stub

    }

}
