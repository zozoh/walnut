package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * @author zozoh(zozohtnt@gmail.com)
 */
public class jsonx_read extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(list|auto|reset)$");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // ..............................................
        // 读取输入
        Map<String, Object> map = new NutMap();
        for (String ph : params.vals) {
            WnObj o = Wn.checkObj(fc.sys, ph);
            String str = sys.io.readText(o);
            Object obj = Json.fromJson(str);
            String key = Files.getMajorName(o.name());
            map.put(key, obj);
        }
        // ..............................................
        // 自动输出，如果只有一个，就是裸值
        Object data = null;
        if (params.is("auto")) {
            if (map.size() == 1) {
                data = map.values().iterator().next();
            }
        }
        // 输出列表
        else if (params.is("list")) {
            List<Object> list = new ArrayList<Object>(map.size());
            list.addAll(map.values());
            data = list;
        }
        // 默认输出 map
        else {
            data = map;
        }
        // ..............................................
        // 合并
        if (!params.is("reset") && (fc.obj instanceof Map) && (data instanceof Map)) {
            Map fc_map = (Map) fc.obj;
            Map data_map = (Map) data;
            fc_map.putAll(data_map);
        }
        // 替换
        else {
            fc.obj = data;
        }
    }

}
