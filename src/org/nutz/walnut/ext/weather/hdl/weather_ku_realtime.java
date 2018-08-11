package org.nutz.walnut.ext.weather.hdl;

import java.io.File;
import java.util.List;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs(value="cqn", regex="baidu")
public class weather_ku_realtime implements JvmHdl {
    
    String base = "http://api.datamatrices.com/api/v2/realTime/";
    String key = "7RMpY3S1rMtdYT4549KH85EHj";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String lng = hc.params.get("lng");
        String lat = hc.params.get("lat");
        String area = hc.params.get("area");
        String url = base;
        if (Strings.isBlank(area)) {
            if (Strings.isBlank(lng) || Strings.isBlank(lat)) {
                throw Err.create("e.cmd.weather_ku_realtime.need_lng_lat_or_area");
            }
            else {
                url += lng + "/" + lat;
            }
        }
        else {
            url += area;
        }
        if (hc.params.is("baidu"))
            url += "?gpsType=baidu&appKey=" + key;
        else
            url += "?appKey=" + key;
        Response resp = Http.get(url);
        if (resp.isOK())
            sys.out.print(resp.getContent());
    }
}
