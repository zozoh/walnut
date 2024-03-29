package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Mirror;
import org.nutz.mapl.Mapl;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.each.WnEachIteratee;

public class jsonx_get extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        // 准备路径
        String keyPath = params.val_check(0);
        Mirror<?> mi = Mirror.me(fc.obj);

        // 上下文是列表
        if (mi.isColl()) {
            List<Object> vals = new LinkedList<>();
            Wlang.each(fc.obj, new WnEachIteratee<Object>() {
                public void invoke(int index, Object ele, Object src) {
                    Object v = Mapl.cell(ele, keyPath);
                    vals.add(v);
                }
            });
            fc.obj = vals;
        }

        // 就是普通对象
        else {
            Object val = Mapl.cell(fc.obj, keyPath);
            fc.obj = val;
        }
    }

}
