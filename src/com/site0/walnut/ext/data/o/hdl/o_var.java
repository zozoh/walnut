package com.site0.walnut.ext.data.o.hdl;

import java.util.Map;

import org.nutz.json.Json;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_var extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(view|autojava)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        boolean isView = params.is("view");
        boolean autoJava = params.is("autojava");

        // 设置模式
        if (!isView && params.vals.length > 0) {
            String dft = params.getString("dft", null);
            String varName = params.val_check(0);
            String varValue = params.val(1, dft);
            Object val = varValue;

            if (autoJava) {
                val = Ws.toJavaValue(varValue);
            }

            fc.vars.put(varName, val);
        }
        // 查看模式
        else {
            // 指定具体的变量
            if (params.vals.length > 0) {
                for (String varName : params.vals) {
                    Object v = fc.vars.get(varName);
                    sys.out.printlnf("%s = %s", varName, Json.toJson(v));
                }
            }
            // 查看全部变量
            else {
                if (fc.vars.isEmpty()) {
                    sys.out.println("~ empty ~");
                } else {
                    for (Map.Entry<String, Object> en : fc.vars.entrySet()) {
                        String varName = en.getKey();
                        Object varValue = en.getValue();
                        sys.out.printlnf("%s = %s", varName, Json.toJson(varValue));
                    }
                }
            }
        }
    }

}
