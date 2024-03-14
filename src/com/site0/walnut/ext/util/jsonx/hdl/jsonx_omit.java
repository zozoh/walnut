package com.site0.walnut.ext.util.jsonx.hdl;

import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.ext.util.jsonx.util.JsonXFilterIteratee;
import com.site0.walnut.ext.util.jsonx.util.JsonXFilters;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;

public class jsonx_omit extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 准备匹配器
        WnMatch fm = JsonXFilters.prepareMatch(sys, params);

        // 准备迭代器
        JsonXFilterIteratee jfi = new JsonXFilterIteratee(fm, true);

        // 执行过滤
        Wlang.each(fc.obj, jfi);

        // 获得结果
        fc.obj = jfi.getResult();
    }

}
