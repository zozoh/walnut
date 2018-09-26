package org.nutz.walnut.ext.mt90.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.ext.wooz.AbstraceWoozPoint;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(fixed)$")
public class mt90_ply_update implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String line = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(1), sys));
        Mt90Raw raw = Mt90Raw.mapping(line.trim());
        if (raw == null) {
            return;
        }
        if (!"A".equals(raw.gpsFixed)) {
            return; // 不是gps定位成功的,不认
        }
        if (1 == raw.eventKey) {
            // TODO 报警信息
        }
        // 过滤跟踪时间
        String conv_from = "wgs84";
        String conv_to = hc.params.get("conv_to", "gcj02");
        AbstraceWoozPoint point = new AbstraceWoozPoint();
        point.lat = raw.lat;
        point.lng = raw.lng;
        point.ele = raw.ele;
        WoozTools.convert(point, conv_from, conv_to);
        NutMap meta = new NutMap();
        meta.put("u_lat", point.lat);
        meta.put("u_lng", point.lng);
        meta.put("u_ele", point.ele);
        meta.put("u_trk_tm", System.currentTimeMillis());
        meta.put("u_trk_tm_gps", raw.timestamp);
        sys.io.appendMeta(wobj, meta);
    }
}
