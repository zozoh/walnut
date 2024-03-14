package com.site0.walnut.ext.data.o.hdl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_remove extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String key = params.val_check(0);
        for (WnObj o : fc.list) {
            // 取出对象的内部文档
            NutMap meta0 = o.getAs(key, NutMap.class);
            if (null == meta0) {
                meta0 = new NutMap();
            }
            NutMap meta1 = meta0.duplicate();

            for (int i = 1; i < params.vals.length; i++) {
                String[] ks = Ws.splitIgnoreBlank(params.val(i));
                for (String k : ks) {
                    meta1.remove(k);
                }
            }
            // 更新
            if (!meta1.equals(meta0)) {
                o.put(key, meta1);
                sys.io.set(o, "^(" + key + ")$");
            }
        }
    }

}
