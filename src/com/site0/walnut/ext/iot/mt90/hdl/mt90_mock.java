package com.site0.walnut.ext.iot.mt90.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.biz.wooz.WoozMap;
import com.site0.walnut.ext.biz.wooz.WoozRoute;
import com.site0.walnut.ext.biz.wooz.WoozTools;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.ThQuery;
import com.site0.walnut.ext.iot.mt90.bean.Mt90Raw;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs(value="yr")
public class mt90_mock implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String d = hc.params.val_check(0);
        WnObj dev = null;
        if(d.startsWith("id:")) {
            dev = sys.io.check(null, d);
        }
        else {
            WnThingService thing = new WnThingService(sys.io, sys.io.check(null, Wn.normalizeFullPath("~/device", sys)));
            dev = thing.getOne(new ThQuery("dev_imei", d));
        }
        // comp/data/pak83ovupcg43pkn23qtdeueut/proj/100KM/mars_google.json
        String path = hc.params.get("path");
        if (Strings.isBlank(path)) {
            String cm_id = dev.getString("wz_cm_id");
            String cm_pj = dev.getString("cm_pj");
            if (Strings.isBlank(cm_id) || Strings.isBlank(cm_pj)) {
                sys.err.print("miss cm_id or cm_pj");
                return;
            }
            path = "~/comp/data/"+cm_id+"/proj/"+cm_pj+"/mars_google.json";
        }
        
        
        WnObj mars_google = sys.io.check(null, Wn.normalizeFullPath(path, sys));
        WoozMap map = sys.io.readJson(mars_google, WoozMap.class);
        // 起始百分比,默认从起点开始
        int start = hc.params.getInt("start", 0);
        int startPoint = start * map.route.size() / 100;
        // 结束百分比,默认到终点结束
        int end = hc.params.getInt("end", 100);
        int endPoint = end * map.route.size() / 100;
        // 输出间隔, 默认5秒一次
        int t = hc.params.getInt("t", 5) * 1000;
        // 总共输出多少个点
        int count = hc.params.getInt("c", endPoint - startPoint);
        // 是否添加随机偏移
        //boolean r = hc.params.is("r");
        String url = hc.params.get("url", "http://127.0.0.1:8080/api/wooz/trk/record_raw");
        
        // 差不多了, 输出概要
        sys.out.printlnf("设备IMEI: %s", dev.getString("dev_imei"));
        sys.out.printlnf("赛事id: %s", dev.getString("wz_cm_id"));
        sys.out.printlnf("赛项名称: %s", dev.getString("cm_pj"));
        sys.out.printlnf("起始百分比(start): %d", start);
        sys.out.printlnf("结束百分比(end): %d", end);
        sys.out.printlnf("赛事总点数: %d", map.route.size());
        sys.out.printlnf("模拟输出点数: %d", endPoint - startPoint);
        sys.out.printlnf("限制输出的点数(count): %d", count);
        sys.out.printlnf("输出间隔(t): %dms", t);
        sys.out.printlnf("目标网址(url): %s", url);
        sys.out.println("===========================================");
        if (!hc.params.is("y")) {
            sys.out.println("没有y参数,退出模拟");
            return;
        }
        for (int i = startPoint; i < endPoint; i++) {
            WoozRoute route = map.route.get(i);
            // 坐标系转换
            WoozTools.convert(route, "gcj02", "wgs84");
            sys.out.printf(">> lat=%s, lng=%s, ele=%s", route.lat, route.lng, route.ele);
            Mt90Raw raw = new Mt90Raw();
            raw.timestamp = Wn.now();
            raw.lat = route.lat;
            raw.lng = route.lng;
            raw.ele = (int)route.ele;
            raw.gpsFixed = "A";
            raw.rtimestamp = raw.timestamp;
            
            Response resp = Http.post3(url + "?imei=" + dev.getString("dev_imei"), raw.toGpsRaw() + "\r\n", null, 2000);
            if (resp.isOK()) {
                sys.out.printlnf("       OK");
            }
            else {
                sys.out.printlnf("       FAIL");
            }
            Lang.quiteSleep(t);
        }
    }

}
