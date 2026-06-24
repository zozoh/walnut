package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Map;
import java.util.TimeZone;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;

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

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        String as = params.getString("as", "json");
        String loadPath = params.val_check(0);

        Object loaded = null;

        // 普通 JSON
        if ("json".equals(as)) {
            WnObj o = Wn.checkObj(sys, loadPath);
            String json = sys.io.readText(o);
            loaded = Json.fromJson(json);
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
            loaded = ing.parse(input);
        }

        // 对于 Map 对象，可以进一步处理结果
        if (null != loaded && loaded instanceof Map<?, ?>) {

            NutMap map = NutMap.WRAP((Map<String, Object>) loaded);

            // 获取值
            String getKey = params.getString("get");
            if (!Ws.isBlank(getKey)) {
                loaded = Mapl.cell(map, getKey);
            }
        }

        // 过滤
        if (null != loaded && loaded instanceof Map<?, ?>) {
            String omit = params.getString("omit");
            String pick = params.getString("pick");
            boolean hasOmit = !Ws.isBlank(omit);
            boolean hasPick = !Ws.isBlank(pick);
            if (hasOmit || hasPick) {
                NutMap map = NutMap.WRAP((Map<String, Object>) loaded);
                if (hasOmit) {
                    String[] omitKeys = Ws.splitIgnoreBlank(omit);
                    map = map.omit(omitKeys);
                }
                if (hasPick) {
                    String[] pickKeys = Ws.splitIgnoreBlank(pick);
                    map = map.pick(pickKeys);
                }
                loaded = map;
            }
        }

        String put = params.getString("put");

        // 融合方式: 融合
        if (null != fc.obj
            && (fc.obj instanceof Map<?, ?>)
            && !Ws.isBlank(put)) {
            Mapl.put(fc.obj, put, loaded);
        }
        // 融合方式: 替换
        else {
            fc.obj = loaded;
        }
    }

}
