package com.site0.walnut.ext.data.o.hdl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;

public class o_put extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(dft)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String key = params.val_check(0);
        boolean asDft = params.is("dft");
        for (WnObj o : fc.list) {
            // 取出对象的内部文档
            NutMap meta0 = o.getAs(key, NutMap.class);
            if (null == meta0) {
                meta0 = new NutMap();
            }
            NutMap meta1 = meta0.duplicate();

            for (int i = 1; i < params.vals.length; i++) {
                NutMap m0 = Wlang.map(params.val(i));
                if (null != m0 && !m0.isEmpty()) {
                    // 仅添加默认值
                    if (asDft) {
                        for (Map.Entry<String, Object> en : m0.entrySet()) {
                            String k = en.getKey();
                            Object v = en.getValue();
                            meta1.putDefault(k, v);
                        }
                    }
                    // 直接覆盖
                    else {
                        meta1.putAll(m0);
                    }
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
