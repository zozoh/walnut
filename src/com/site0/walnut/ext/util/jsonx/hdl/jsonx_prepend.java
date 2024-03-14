package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Collection;
import java.util.LinkedList;

import org.nutz.json.Json;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_prepend extends JsonXFilter {

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
                list.addAll(0, (Collection<?>) vo);
            } else {
                list.addFirst(vo);
            }
        }
    }
}
