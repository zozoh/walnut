package org.nutz.walnut.ext.ip2region.hdl;

import java.util.LinkedHashMap;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.plugins.ip2region.DbSearcher;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ip2region_query implements JvmHdl {
    
    DbSearcher searcher = new DbSearcher();

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        LinkedHashMap<String, NutMap> list = new LinkedHashMap<>();
        for (String ip : hc.params.vals) {
            if ("self".equals(ip)) {
                ip = Lang.getIP(Mvcs.getReq());
            }
            String region = searcher.getRegion(ip);
            String[] tmp = region.split("\\|");
            NutMap map = new NutMap();
            map.put("region", region);
            // 国家
            map.put("country", tmp[0]);
            // 地区
            map.put("zone", "0".equals(tmp[1]) ? "未知" : tmp[1]);
            // 省份
            map.put("province","0".equals(tmp[2]) ? "未知" : tmp[2]);
            // 城市
            map.put("city", "0".equals(tmp[3]) ? "未知" : tmp[3]);
            // 运营商
            map.put("isp", "0".equals(tmp[4]) ? "未知" : tmp[4]);
            list.put(ip, map);
        }
        sys.out.writeJson(list);
    }

}
