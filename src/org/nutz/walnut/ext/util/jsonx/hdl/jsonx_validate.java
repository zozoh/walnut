package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.Collection;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.impl.MapMatch;
import org.nutz.web.WebException;

public class jsonx_validate extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(only|igoreNil)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 分析参数
        boolean only = params.is("only");
        boolean ignoreNil = params.is("ignoreNil");

        // 输入
        String json;
        String ph = params.getString("f");
        if (!Ws.isBlank(ph)) {
            WnObj oIn = Wn.checkObj(sys, ph);
            json = sys.io.readText(oIn);
        } else {
            json = params.val_check(0);
        }

        // 解析
        NutMap vmap = Json.fromJson(NutMap.class, json);
        MapMatch m = new MapMatch(vmap);
        m.setIgnoreNil(ignoreNil);
        m.setOnlyFields(only);

        // 检查列表
        WebException err= null;
        if (fc.obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) fc.obj;
            for (Object ele : col) {
                err = m.matchErr(ele);
                if (null != err) {
                    break;
                }
            }
        }
        // 检查单个对象
        else {
            err = m.matchErr(fc.obj);
        }

        if (null != err) {
            throw err;
        }
    }

}
