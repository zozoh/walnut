package org.nutz.walnut.ext.o.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_read extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        String path = params.val(0, "~self~");
        String toKey = params.getString("to", "content");
        String as = params.getString("as", "text");

        // 判断特殊的 path 值
        if (null != path) {
            // 明确指明无视
            if ("~ignore~".equals(path))
                return;

            // 读取自己
            if ("~self~".equals(path))
                path = null;
        }

        // 循环处理
        for (WnObj o : fc.list) {
            // 目录必须有 path，否则不能读取内容
            if (o.isDIR() && null == path) {
                continue;
            }

            // 读取目标文件
            WnObj oF = o;
            if (null != path) {
                oF = sys.io.fetch(o, path);
            }
            if (null == oF) {
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
