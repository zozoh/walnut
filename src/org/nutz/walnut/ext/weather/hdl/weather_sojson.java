package org.nutz.walnut.ext.weather.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class weather_sojson implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, "cqn");
        // 得到天气 API
        String weatherApi = Strings.sBlank(params.val(0),
                                           "https://www.sojson.com/open/api/weather/json.shtml?city=北京");

        // 发送请求
        Response resp = Http.get(weatherApi);
        String json = resp.getContent();

        // 格式化
        Object obj = Json.fromJson(json);
        JsonFormat jfmt = Cmds.gen_json_format(params);
        json = Json.toJson(obj, jfmt);

        // 输出
        sys.out.println(json);

    }
}
