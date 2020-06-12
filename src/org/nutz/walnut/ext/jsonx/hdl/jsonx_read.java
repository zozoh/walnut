package org.nutz.walnut.ext.jsonx.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * @author zozoh(zozohtnt@gmail.com)
 */
public class jsonx_read extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(map|list|auto)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext ctx, ZParams params) {
        // 读取输入
        Map<String, Object> map = new NutMap();
        for (String ph : params.vals) {
            WnObj o = Wn.checkObj(ctx.sys, ph);
            String str = sys.io.readText(o);
            Object obj = Json.fromJson(str);
            String key = Files.getMajorName(o.name());
            map.put(key, obj);
        }

        // 自动输出，如果只有一个，就是裸值
        if (params.is("auto")) {
            if (map.size() == 1) {
                ctx.obj = map.values().iterator().next();
                return;
            }
        }
        // 输出列表
        if (params.is("list")) {
            List<Object> list = new ArrayList<Object>(map.size());
            list.addAll(map.values());
            ctx.obj = list;
        }
        // 默认输出 map
        else {
            ctx.obj = map;
        }
    }

}
