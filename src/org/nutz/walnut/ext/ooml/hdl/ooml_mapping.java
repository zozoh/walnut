package org.nutz.walnut.ext.ooml.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.ext.ooml.util.OomlRowMapping;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_mapping extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(reset|only)$");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        fc.onlyMapping = params.is("only");

        // 重置的话，直接清除就好
        if (params.is("reset")) {
            fc.mapping = null;
            return;
        }

        String input = params.val(0);
        // 直接声明映射方式
        if (params.vals.length > 0) {
            input = params.val_check(0);
        }
        // 从文件读取
        else if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            input = sys.io.readText(o);
        }
        // 从标准输入读取
        else {
            input = sys.in.readAll();
        }

        // 设置映射方式
        if (!Ws.isBlank(input)) {
            fc.mapping = Json.fromJson(OomlRowMapping.class, input);
            fc.mapping.ready();
        }
        // 清除映射方式
        else {
            fc.mapping = null;
        }
    }

}
