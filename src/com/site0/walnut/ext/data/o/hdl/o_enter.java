package com.site0.walnut.ext.data.o.hdl;

import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AlwaysMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class o_enter extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        String[] ss = params.vals;
        if (params.vals.length == 0) {
            ss = Wlang.array("!true");
        }

        // 分析参数
        String childBy = params.getString("by", "children");

        // 获得一个过滤器列表
        WnMatch[] ms = new WnMatch[ss.length];
        int i = 0;
        for (String val : ss) {
            WnMatch m;
            // 强制匹配
            if ("!true".equals(val)) {
                m = new AlwaysMatch(true);
            }
            // 强制不匹配
            else if ("!false".equals(val)) {
                m = new AlwaysMatch(false);
            }
            // 自动判断
            else {
                Object vo = Json.fromJson(val);
                m = new AutoMatch(vo);
            }
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
