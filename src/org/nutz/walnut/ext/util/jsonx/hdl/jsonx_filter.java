package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.each.WnEachIteratee;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class jsonx_filter extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 准备过滤条件
        String input = params.val(0);
        if (params.has("-f")) {
            WnObj oFile = Wn.checkObj(sys, params.getString("f"));
            input = sys.io.readText(oFile);
        }

        // 防守
        if (null == input) {
            return;
        }

        // 转换过滤条件
        Object obj = Json.fromJson(input);
        WnMatch wm = new AutoMatch(obj);

        // 执行过滤
        List<Object> list = new LinkedList<>();

        Wlang.each(fc.obj, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                if (wm.match(ele)) {
                    list.add(ele);
                }
            }
        });

        fc.obj = list;
    }

}
