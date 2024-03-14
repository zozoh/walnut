package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

/**
 * 将 `{a:{x:1},b:{y:2}}` 变成 `[{key:a,x:1},{key:b,y:2}]`
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class jsonx_map2list extends JsonXFilter {

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext ctx, ZParams params) {
        // 防守
        if (null == ctx.obj)
            return;

        String keyName = params.getString("key", "key");
        boolean joinKey = !"-nil-".equals(keyName);

        String[] ignores = params.getAs("ignore", String[].class);

        // 只处理 map
        if (ctx.obj instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) ctx.obj);
            List<NutMap> list = new ArrayList<>(map.size());
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                // 忽略不是 Map 的对象
                if (val instanceof Map) {
                    NutMap valMap = NutMap.WRAP((Map<String, Object>) val);
                    boolean ignoreIt = false;
                    // 判断一下是否忽略
                    if (ignores != null) {

                        for (String ignore : ignores) {
                            boolean not = false;
                            String igKey = ignore;
                            if (igKey.startsWith("!")) {
                                igKey = Strings.trim(igKey.substring(1));
                                not = true;
                            }
                            // 不存在就忽略
                            if (not) {
                                if (!valMap.has(igKey)) {
                                    ignoreIt = true;
                                    break;
                                }
                            }
                            // 存在就忽略
                            else {
                                if (valMap.has(igKey)) {
                                    ignoreIt = true;
                                    break;
                                }
                            }
                        }
                    }

                    // 计入结果
                    if (!ignoreIt) {
                        if (joinKey) {
                            valMap.put(keyName, key);
                        }
                        list.add(valMap);
                    }
                }
            }
            // 计入结果
            ctx.obj = list;
        }
    }

}
