package com.site0.walnut.ext.data.val.hdl;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import com.site0.walnut.ext.data.val.ValContext;
import com.site0.walnut.ext.data.val.ValFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.ValueMakers;

public class val_gen extends ValFilter {

    @Override
    protected void process(WnSystem sys, ValContext fc, ZParams params) {
        String input = params.val_check(0);
        int n = params.getInt("n", 1);
        boolean asJson = params.is("json", n > 1);

        // 获取值的生成器
        ValueMaker vmk = ValueMakers.build(sys, input);

        // 生成值
        Date hint = new Date();
        Set<Object> vals = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            Object v = vmk.make(hint, fc.context);
            vals.add(v);
        }

        // 输出
        if (asJson) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            fc.result = Json.toJson(vals, jfmt);
        }
        // 直接输出
        else {
            fc.result = Ws.join(vals, ",");
        }

    }

}
