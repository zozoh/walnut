package com.site0.walnut.ext.util.jsonx.hdl;

import java.lang.reflect.Array;
import java.util.List;

import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wnum;
import com.site0.walnut.util.ZParams;

public class jsonx_item extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj) {
            return;
        }

        // 获取下标
        int index = params.val_int(0, 0);

        // 列表
        if (fc.obj instanceof List<?>) {
            List<?> list = (List<?>) fc.obj;
            int i = Wnum.scrollIndex(index, list.size());
            if (i >= 0) {
                fc.obj = list.get(i);
            } else {
                fc.obj = null;
            }
        }
        // 数组
        else if (fc.obj.getClass().isArray()) {
            int len = Array.getLength(fc.obj);
            int i = Wnum.scrollIndex(index, len);
            if (i >= 0) {
                fc.obj = Array.get(fc.obj, i);
            } else {
                fc.obj = null;
            }
        }
        // 那么就是空的
        else {
            fc.obj = null;
        }
    }

}
