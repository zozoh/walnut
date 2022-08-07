package org.nutz.walnut.ext.data.o.hdl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;

public class o_mime extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        String as = params.get("as", "line");

        // 获取列表
        MimeMap mimes = sys.io.mimes();

        // 全部输出
        String[] keys;
        if (params.vals.length == 0) {
            Set<String> ks = mimes.keys();
            keys = new String[ks.size()];
            int i = 0;
            for (String k : ks) {
                keys[i++] = k;
            }
            Arrays.sort(keys);
        }
        // 部分输出
        else {
            keys = params.vals;
        }

        // 准备输出对象
        Object re = null;

        // 列表输出
        if ("list".equals(as)) {
            List<NutMap> list = new ArrayList<>(keys.length);
            for (String key : keys) {
                String mime = mimes.getMime(key);
                list.add(Wlang.map("type", key).setv("mime", mime));
            }
            re = list;
        }
        // Map 输出
        else if ("map".equals(as)) {
            Map<String, String> map = new LinkedHashMap<>();
            for (String key : keys) {
                String mime = mimes.getMime(key);
                map.put(key, mime);
            }
            re = map;
        }
        // 逐行输出值
        else if ("value".equals(as)) {
            for (String key : keys) {
                String mime = mimes.getMime(key);
                sys.out.println(mime);
            }
        }
        // 逐行打印
        else {
            for (String key : keys) {
                String mime = mimes.getMime(key);
                sys.out.printlnf("%s=%s", key, mime);
            }
        }

        // 禁止全局输出
        fc.quiet = true;

        // 输出对象
        if (null != re) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(re, jfmt);
            sys.out.println(json);
        }
    }

}
