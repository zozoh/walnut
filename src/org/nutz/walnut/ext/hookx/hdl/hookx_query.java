package org.nutz.walnut.ext.hookx.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.hookx.HookXContext;
import org.nutz.walnut.ext.hookx.HookXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class hookx_query extends HookXFilter {

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        String val = params.val_check(0);
        NutMap map = Lang.map(val);
        int limit = params.getInt("limit", 10);
        int skip = params.getInt("skip", 0);
        WnQuery q = new WnQuery();
        q.add(map);
        q.limit(limit).skip(skip);
        List<WnObj> list = sys.io.query(q);
        if (!list.isEmpty()) {
            fc.objs.addAll(list);
        }
    }

}
