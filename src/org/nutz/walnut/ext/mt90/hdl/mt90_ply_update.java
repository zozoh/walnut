package org.nutz.walnut.ext.mt90.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.ext.wooz.AbstraceWoozPoint;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(fixed)$")
public class mt90_ply_update implements JvmHdl {
    
    private static final Log log = Logs.get();
    
    private static Map<String, WoozMap> maps = new HashMap<>();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String line = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        
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

        // 把更新数据准备好
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
        meta.put("u_tkr_rssi", raw.gsmRssi); // 信号强度
        meta.put("u_tkr_satellite", raw.satellite); // 卫星数量
        meta.put("u_tkr_voltage", raw.powerVoltage); // 电池电压
        
        if (hc.params.has("map")) {
            try {
                WnObj tmp = sys.io.fetch(null, Wn.normalizeFullPath(hc.params.get("map"), sys));
                WoozMap map = maps.get(tmp.sha1());
                if (map == null) {
                    map = sys.io.readJson(tmp, WoozMap.class);
                    maps.put(tmp.sha1(), map);
                }
                int[] re = WoozTools.findClosest(map.route, point.lat, point.lng, 50);
                meta.put("u_trk_route_index", re[0]);
                meta.put("u_trk_route_distance", re[1]);
            }
            catch (Throwable e) {
                log.warn("尝试匹配选手轨迹点到线路时报错了", e);
            }
        }
        
        for (String val : hc.params.vals) {
            WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(val, sys));
            // 过滤跟踪时间
            if (wobj.getLong("u_trk_tm_gps", 0) > raw.timestamp) {
                // 属于补传数据,跳过
                continue;
            }
            sys.io.appendMeta(wobj, meta);
        }
    }
}
