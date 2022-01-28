package org.nutz.walnut.ext.data.o.hdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.ext.data.o.util.WnPop;
import org.nutz.walnut.ext.data.o.util.WnPops;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_pop extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(null)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        if (params.vals.length == 0) {
            return;
        }

        // 分析参数
        boolean isNull = params.is("null");

        // 预先处理压入对象
        Map<String, WnPop> pops = new HashMap<>();
        for (String val : params.vals) {
            String key;
            String popBy;
            int pos = val.indexOf(':');
            if (pos >= 0) {
                key = val.substring(0, pos);
                popBy = Ws.trim(val.substring(pos + 1));
            } else {
                key = val;
                popBy = "i-1";
            }
            WnPop pop = WnPops.parse(popBy);
            pops.put(key, pop);
        }

        // 依次处理上下文对象
        for (WnObj o : fc.list) {

            // 准备要更新的上下文
            NutMap meta = new NutMap();

            // 处理弹出
            for (Map.Entry<String, WnPop> en : pops.entrySet()) {
                String key = en.getKey();
                WnPop pop = en.getValue();

                // 得到列表
                List<Object> list = o.getAsList(key, Object.class);

                // 防守
                if (null == list || list.isEmpty()) {
                    continue;
                }

                // 处理
                list = pop.exec(list);

                // 空变 null
                if (isNull && list.isEmpty()) {
                    list = null;
                }

                // 记入更新元数据
                meta.put(key, list);
            }

            // 记入对象
            if (!meta.isEmpty()) {
                sys.io.appendMeta(o, meta);
            }
        }
    }

}
