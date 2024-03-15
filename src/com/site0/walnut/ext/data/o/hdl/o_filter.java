package com.site0.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.util.validate.impl.MapMatch;
import com.site0.walnut.util.validate.impl.ParallelMatch;

public class o_filter extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        if (params.vals.length == 0)
            return;

        // 准备过滤器
        WnMatch[] ms = new WnMatch[params.vals.length];
        WnMatch test;
        int i = 0;
        for (String val : params.vals) {
            Object vo = Json.fromJson(val);
            WnMatch m = new AutoMatch(vo);
            ms[i++] = m;
        }
        if (ms.length == 0) {
            test = new MapMatch(Wlang.map("race", "DIR"));
        } else if (ms.length == 1) {
            test = ms[0];
        } else {
            test = new ParallelMatch(ms);
        }

        // 逐层进入
        List<WnObj> list = new ArrayList<>(fc.list.size());
        for (WnObj o : fc.list) {
            if (test.match(o)) {
                list.add(o);
            }
        }

        // 最后将对象设置进上下文
        fc.clearAll();
        fc.list.addAll(list);
    }

}
