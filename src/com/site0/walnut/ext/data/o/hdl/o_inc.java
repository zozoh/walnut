package com.site0.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_inc extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(quiet|json)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        if (fc.list.isEmpty()) {
            return;
        }
        // 分析参数
        boolean asList = params.is("l");
        boolean quiet = params.is("quiet");
        boolean asJson = params.is("json");
        int pad = params.getInt("pad", 0);
        String key = params.getString("key", "value");
        int v = params.val_int(0, 1);

        // 准备输出对象
        List<Object> list = new ArrayList<>(fc.list.size());
        for (WnObj o : fc.list) {
            String oid = o.id();
            int n = sys.io.inc(oid, key, v, true);
            o.put(key, n);
            // 是否 pad
            if (pad > 0) {
                String sN = Integer.toString(n);
                sN = Ws.padStart(sN, pad, '0');
                list.add(sN);
            }
            // 直接存值
            else {
                list.add(n);
            }
        }

        // 不直接输出序号
        if (quiet) {
            return;
        }

        // 阻止默认输出
        fc.quiet = true;

        // 按照 JSON 输出
        if (asJson) {
            Object jsonOut;
            if (asList || list.size() > 1) {
                jsonOut = Json.toJson(list, fc.jfmt);
            } else {
                Object li0 = list.get(0);
                jsonOut = Json.toJson(li0, fc.jfmt);
            }
            sys.out.println(jsonOut);
        }
        // 按行打印
        else {
            for (Object li : list) {
                sys.out.println(li);
            }
        }
    }

}
