package com.site0.walnut.core.indexer;

import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wobj;

public abstract class AbstractIoVfsIndexer extends AbstractIoIndexer {

    protected AbstractIoVfsIndexer(WnObj root, MimeMap mimes) {
        super(root, mimes);
    }

    protected String get_path_by_id(String id) {
        return Wobj.decodePathFromBase64(id);
    }

    protected String get_id_by_path(String path) {
        return Wobj.encodePathToBase64(path);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        int len = toIndex - fromIndex;
        String path = Strings.join(fromIndex, len, "/", paths);
        return create(p, path, race);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        return create(p, name, race);
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        return create(p, o.name(), o.race());
    }

    @Override
    public int count(WnQuery q) {
        return this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {}
        });
    }

    @Override
    public int getInt(String id, String key, int dft) {
        return dft;
    }

    @Override
    public long getLong(String id, String key, long dft) {
        return dft;
    }

    @Override
    public String getString(String id, String key, String dft) {
        return dft;
    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        return dft;
    }

    //
    // 下面的就是弄个幌子，啥也不做
    //
    @Override
    public void set(WnObj o, String regex) {}

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        return this.get(id);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        throw Wlang.noImplement();
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        return val;
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        return val;
    }

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        throw Wlang.noImplement();
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        throw Wlang.noImplement();
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        throw Wlang.noImplement();
    }
}
