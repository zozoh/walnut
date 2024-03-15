package com.site0.walnut.ext.sys.hookx.hdl;

import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.sys.hookx.HookXContext;
import com.site0.walnut.ext.sys.hookx.HookXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class hookx_query extends HookXFilter {

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        String val = params.val_check(0);
        NutMap map = Wlang.map(val);
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
