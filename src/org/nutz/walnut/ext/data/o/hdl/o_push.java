package org.nutz.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_push extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(uniq|raw)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        if (params.vals.length == 0) {
            return;
        }

        // 分析参数
        String toKey = params.check("to");
        boolean isRaw = params.is("raw");
        boolean isUniq = params.is("uniq");

        // 预先处理压入对象
        List<Object> vals = new ArrayList<>(params.vals.length);
        for (String val : params.vals) {
            if (isRaw) {
                vals.add(val);
            } else {
                Object v2 = Ws.toJavaValue(val);
                vals.add(v2);
            }
        }

        // 依次处理上下文对象
        for (WnObj o : fc.list) {
            List<Object> list = o.getAsList(toKey, Object.class);
            if (null == list) {
                list = new ArrayList<>(vals.size());
            }
            // 确保唯一
            if (isUniq) {
                LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                for (Object li : list) {
                    map.put(li, li);
                }
                for (Object v : vals) {
                    map.put(v, v);
                }
                list = new ArrayList<>(map.size());
                list.addAll(map.keySet());
            }
            // 正常推入
            else {
                for (Object v : vals) {
                    list.add(v);
                }
            }
            // 记入对象
            NutMap meta = Wlang.map(toKey, list);
            sys.io.appendMeta(o, meta);
        }
    }

}
