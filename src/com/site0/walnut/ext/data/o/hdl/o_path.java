package com.site0.walnut.ext.data.o.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class o_path extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防空
        if (null == fc.list) {
            return;
        }

        // 确保每个对象都加载了路径
        for (WnObj o : fc.list) {
            o.path();
        }
    }
}
