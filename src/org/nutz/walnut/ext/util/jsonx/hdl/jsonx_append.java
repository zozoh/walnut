package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.Collection;
import java.util.LinkedList;

import org.nutz.json.Json;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class jsonx_append extends JsonXFilter {
    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(path)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 分析参数
        boolean asPath = params.is("path");
        String toKey = params.getString("to");

        // 确保上下文是列表
        LinkedList<Object> list = fc.checkList(toKey, asPath);

        // 逐个解析值，并插入集合
        for (String val : params.vals) {
            Object vo = val;
            try {
                vo = Json.fromJson(val);
            }
            catch (Throwable e) {}
            if (vo instanceof Collection<?>) {
                for (Object ve : (Collection<?>) vo) {
                    list.add(ve);
                }
            } else {
                list.add(vo);
            }
        }
    }

}
