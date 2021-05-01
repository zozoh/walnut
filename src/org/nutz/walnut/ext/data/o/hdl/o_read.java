package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_read extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        String toKey = params.getString("to", "content");
        String as = params.getString("as", "text");

        // 循环处理
        for (WnObj o : fc.list) {
            // 依次尝试读取目标文件
            WnObj oF = o;
            for (String path : params.vals) {
                // 明确指明无视
                if ("~ignore~".equals(path)) {
                    oF = null;
                    break;
                }
                // 读取自己
                if ("~self~".equals(path)) {
                    oF = o;
                    break;
                }
                // 尝试读取
                if (null != path) {
                    oF = sys.io.fetch(o, path);
                }
                if (null != oF) {
                    break;
                }
            }

            // 可以读取内容
            if (null == oF || !oF.isFILE()) {
                continue;
            }

            // 读取内容
            String text = sys.io.readText(oF);
            Object val = text;

            // 格式化内容
            if ("json".equals(as)) {
                val = Json.fromJson(text);
            }

            // 存入对象
            o.put(toKey, val);
        }
    }

}
