package org.nutz.walnut.ext.util.jsonx.hdl;

import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.ext.util.jsonx.util.JsonXFilterIteratee;
import org.nutz.walnut.ext.util.jsonx.util.JsonXFilters;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;

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
