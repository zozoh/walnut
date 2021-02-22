package org.nutz.walnut.ext.o.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AutoMatch;

public class o_enter extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        String[] ss = params.vals;
        if (params.vals.length == 0) {
            ss = Wlang.array("true");
        }

        // 分析参数
        String childBy = params.getString("by", "children");

        // 获得一个过滤器列表
        WnMatch[] ms = new WnMatch[ss.length];
        int i = 0;
        for (String val : ss) {
            Object vo = Json.fromJson(val);
            WnMatch m = new AutoMatch(vo);
            ms[i++] = m;
        }

        // 逐层进入
        List<WnObj> list = fc.list;
        for (i = 0; i < ms.length; i++) {
            if (null == list || list.isEmpty()) {
                break;
            }
            WnMatch m = ms[i];
            for (WnObj o : list) {
                if (m.match(o)) {
                    list = o.getAsList(childBy, WnObj.class);
                }
            }
        }

        // 最后将对象设置进上下文
        fc.clearAll();

        if (null != list)
            fc.list.addAll(list);
    }

}
