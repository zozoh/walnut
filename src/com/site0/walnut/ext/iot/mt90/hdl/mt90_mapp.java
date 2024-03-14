package com.site0.walnut.ext.iot.mt90.hdl;

import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.biz.wooz.WoozMap;
import com.site0.walnut.ext.iot.mt90.Mt90Map;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class mt90_mapp implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WoozMap map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.check("map"), sys));
        sys.out.writeJson(map.points, JsonFormat.compact().setQuoteName(true));
    }

}
