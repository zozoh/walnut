package com.site0.walnut.ext.net.weather.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs(value="cqn", regex="baidu")
public class weather_ku implements JvmHdl {
    
    protected String base = "http://api.datamatrices.com/api/v2/";
    protected String key = "7RMpY3S1rMtdYT4549KH85EHj";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String lng = hc.params.get("lng");
        String lat = hc.params.get("lat");
        String area = hc.params.get("area");
        String type = hc.params.get("tp", "realTime");
        String url = base + type + "/";
        String key = hc.params.get("k", this.key);
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
        if (resp.isOK()) {
            NutMap re = Json.fromJson(NutMap.class, resp.getReader());
            if (re.getInt("code", 1) == 0) {
                sys.out.writeJson(re.get("data"), Cmds.gen_json_format(hc.params));
            }
            else {
                sys.err.print("e.cmd.weather.code_"+re.get("code"));
            }
            return;
        }
        else {
            sys.err.print("e.cmd.weather.recode_"+resp.getStatus());
        }
    }
}
