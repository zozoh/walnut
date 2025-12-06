package com.site0.walnut.ext.media.ooml.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.ext.media.ooml.util.OomlRowMapping;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

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
            // 准备字段过滤器
            Object keysBy = params.getString("keys", null);
            Object namesBy = params.getString("names", null);
            WnMatch keys = null;
            WnMatch names = null;
            if (null != keysBy) {
                keys = AutoMatch.parse(keysBy);
            }
            if (null != namesBy) {
                names = AutoMatch.parse(namesBy);
            }

            // 准备表格映射规则
            fc.mapping = Json.fromJson(OomlRowMapping.class, input);
            fc.mapping.setPickingFields(keys, names);

            // 编译规则
            NutBean vars = sys.session.getEnv();
            Map<String, NutMap[]> caches = new HashMap<>();
            fc.mapping.ready(sys.io, vars, caches);
        }
        // 清除映射方式
        else {
            fc.mapping = null;
        }

        // 设置自定义的默认字段
        if (params.has("defaults")) {
            NutMap defaults = params.getMap("defaults");
            fc.mapping.setDefaultMeta(defaults);
        }

        // 设置自定义的强制覆盖值
        if (params.has("override")) {
            NutMap override = params.getMap("override");
            fc.mapping.setOverrideMeta(override);
        }

    }

}
