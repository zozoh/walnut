package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.util.validate.impl.MapMatch;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;

public class jsonx_validate extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(only|igoreNil)$");
    }

    @SuppressWarnings("unchecked")
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
        Object match = Json.fromJson(json);
        WnMatch m;
        if (match instanceof Map<?, ?>) {
            NutMap vmap = NutMap.WRAP((Map<String, Object>) match);
            MapMatch mm = new MapMatch(vmap);
            mm.setIgnoreNil(ignoreNil);
            mm.setOnlyFields(only);
            m = mm;
        } else {
            m = AutoMatch.parse(match);
        }

        // 检查列表
        List<WebException> errs = AutoMatch.matchErrors(m, fc.obj);
        List<NutMap> errList = new ArrayList<NutMap>();
        for (WebException err : errs) {
            if (null != err) {
                errList.add(err.toBean());
            }
        }

        // 成功
        if (errList.isEmpty()) {
            fc.obj = Ajax.ok();
        }
        // 失败
        else {
            fc.obj = Ajax.fail().setData(errList);
        }
    }

}
