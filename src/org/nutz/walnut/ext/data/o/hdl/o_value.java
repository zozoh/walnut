package org.nutz.walnut.ext.data.o.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_value extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        List<Object> list = new LinkedList<>();
        for (WnObj o : fc.list) {
            for (String key : params.vals) {
                Object v = o.get(key);
                if (null != v) {
                    list.add(v);
                }
            }
        }

        // 输出为 JSON
        String as = params.getString("as", "str");
        if ("json".equals(as)) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(list, jfmt);
            sys.out.println(json);
        }
        // 默认输出为字符串
        else {
            String sep = params.getString("sep", "");
            sep = Ws.unescape(sep);
            String out = Wcol.join(list, sep);
            sys.out.print(out);
        }

        fc.quiet = true;
    }

}
