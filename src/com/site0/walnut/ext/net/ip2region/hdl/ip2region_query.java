package com.site0.walnut.ext.net.ip2region.hdl;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.lionsoul.ip2region.DbSearcher;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class ip2region_query implements JvmHdl {
    
    DbSearcher searcher;
    
    public void init() throws IOException {
        byte[] buf = Streams.readBytes(getClass().getClassLoader().getResourceAsStream("ip2region/ip2region.db"));
        searcher = new DbSearcher(null, buf);
        searcher.memorySearch("8.8.8.8");
    }

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (hc.params.has("db")) {
            String path = Wn.normalizeFullPath(hc.params.get("db"), sys);
            WnObj wobj = sys.io.check(null, path);
            byte[] buf = Streams.readBytesAndClose(sys.io.getInputStream(wobj, 0));
            searcher = new DbSearcher(null, buf);
            return;
        }
        if (searcher == null) {
            try {
                init();
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
        }
        LinkedHashMap<String, NutMap> list = new LinkedHashMap<>();
        for (String ip : hc.params.vals) {
            if ("self".equals(ip)) {
                ip = Lang.getIP(Mvcs.getReq());
            }
            String region = this.getRegion(ip);
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

    public String getRegion(String ip) {
        try {
            return searcher.memorySearch(ip).getRegion();
        }
        catch (IOException e) {
            return "未知";
        }
    }
}
