package org.nutz.walnut.ext.mt90.hdl;

import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.mt90.Mt90Map;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class mt90_mapp implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WoozMap map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.check("map"), sys));
        sys.out.writeJson(map.points, JsonFormat.compact().setQuoteName(true));
    }

}
