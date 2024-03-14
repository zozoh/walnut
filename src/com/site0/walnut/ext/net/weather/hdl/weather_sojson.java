package com.site0.walnut.ext.net.weather.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class weather_sojson implements JvmHdl {
    
    protected String base = "https://www.sojson.com/open/api/weather/json.shtml?city=";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, "cqn");
        // 得到天气 API
        String city = Strings.sBlank(params.val(0), "北京");

        String url = base + city;
        // 发送请求
        Response resp = Http.get(url);
        String json = resp.getContent();

        // 格式化
        Object obj = Json.fromJson(json);
        JsonFormat jfmt = Cmds.gen_json_format(params);
        json = Json.toJson(obj, jfmt);

        // 输出
        sys.out.println(json);

    }
}
