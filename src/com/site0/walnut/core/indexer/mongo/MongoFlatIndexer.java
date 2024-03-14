package com.site0.walnut.core.indexer.mongo;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutBean;
import org.nutz.mongo.ZMoCo;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.core.bean.WnIoObj;

public class MongoFlatIndexer extends MongoIndexer {

    public MongoFlatIndexer(WnObj root, MimeMap mimes, ZMoCo co) {
        super(root, mimes, co);
    }

    @Override
    protected WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew) {
        q.setv("pid", root.id());
        return super._set_by(q, map, returnNew);
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        q.setv("pid", root.id());
        return super.inc(q, key, val, returnNew);
    }

    @Override
    protected int _each(WnQuery q, WnObj pHint, Each<WnObj> callback) {
        q.setv("pid", root.id());
        return super._each(q, pHint, callback);
    }

    @Override
    public long count(WnQuery q) {
        q.setv("pid", root.id());
        return super.count(q);
    }

    @Override
    public void push(WnQuery q, String key, Object val) {
        q.setv("pid", root.id());
        super.push(q, key, val);
    }

    @Override
    public void pull(WnQuery q, String key, Object val) {
        q.setv("pid", root.id());
        super.pull(q, key, val);
    }

}
