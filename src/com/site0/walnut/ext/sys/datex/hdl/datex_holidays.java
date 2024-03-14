package com.site0.walnut.ext.sys.datex.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class datex_holidays extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {

        NutMap holidays;
        String holiPath = params.val(0);
        if (!Ws.isBlank(holiPath)) {
            WnObj oHoli = Wn.checkObj(sys, holiPath);
            holidays = sys.io.readJson(oHoli, NutMap.class);
        }
        // 从标准输入读取
        else {
            String json = sys.in.readAll();
            json = Ws.sBlank(json, "{}");
            holidays = Json.fromJson(NutMap.class, json);
        }
        
        fc.holidays.load(holidays);

    }

}
