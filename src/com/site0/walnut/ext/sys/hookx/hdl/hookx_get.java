package com.site0.walnut.ext.sys.hookx.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.sys.hookx.HookXContext;
import com.site0.walnut.ext.sys.hookx.HookXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class hookx_get extends HookXFilter {

    @Override
    protected void process(WnSystem sys, HookXContext fc, ZParams params) {
        if (null != params.vals)
            for (String val : params.vals) {
                // 对象 ID
                if (Wn.isFullObjId(val)) {
                    WnObj o = sys.io.get(val);
                    if (null != o) {
                        fc.objs.add(o);
                    }
                }
                // 查询条件
                else if (Strings.isQuoteBy(val, '{', '}')) {
                    NutMap map = Json.fromJson(NutMap.class, val);
                    WnQuery q = new WnQuery();
                    q.add(map);
                    WnObj o = sys.io.getOne(q);
                    if (null != o) {
                        fc.objs.add(o);
                    }
                }
                // 对象路径
                else {
                    WnObj p = fc.getCurrentObj();
                    WnObj o = sys.io.fetch(p, val);
                    if (null != o) {
                        fc.objs.add(o);
                    }
                }
            }
    }

}
