package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.TimeZone;

import org.nutz.json.Json;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.ext.util.jsonx.hdl.ttl.TPLLField;
import com.site0.walnut.ext.util.jsonx.hdl.ttl.TPLLParsing;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class jsonx_load extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(ignore_nil)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        String as = params.getString("as", "json");
        String loadPath = params.val_check(0);

        // 普通 JSON
        if ("json".equals(as)) {
            WnObj o = Wn.checkObj(sys, loadPath);
            String json = sys.io.readText(o);
            fc.obj = Json.fromJson(json);
        }
        // TPLL 文件
        else if ("tpll".equals(as)) {
            WnObj o = Wn.checkObj(sys, loadPath);
            String json = sys.io.readText(o);
            TPLLField[] flds = Json.fromJson(TPLLField[].class, json);
            TPLLParsing ing = new TPLLParsing(flds);
            String raw = params.getString("raw");
            ing.setupRawTypes(raw);
            ing.setIgnoreNil(params.is("ignore_nil"));

            // 解析用的默认时区
            String tzs = params.getString("tz", "UTC");
            TimeZone timezone = TimeZone.getTimeZone(tzs);
            ing.setTimezone(timezone);

            String input = fc.input;
            String from = params.get("from");
            if (!Ws.isBlank(from)) {
                WnObj oFrom = Wn.checkObj(sys, from);
                input = sys.io.readText(oFrom);
            }
            fc.obj = ing.parse(input);
        }

    }

}
