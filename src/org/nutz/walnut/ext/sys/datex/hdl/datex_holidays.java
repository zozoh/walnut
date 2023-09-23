package org.nutz.walnut.ext.sys.datex.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.datex.DatexContext;
import org.nutz.walnut.ext.sys.datex.DatexFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

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
