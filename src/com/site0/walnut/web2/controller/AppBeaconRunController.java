package com.site0.walnut.web2.controller;

import java.io.InputStreamReader;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlog;

public class AppBeaconRunController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            String json = Streams.readAndClose(new InputStreamReader(ctx.request().getInputStream(),
                                                                     Encoding.CHARSET_UTF8));
            NutMap params = Json.fromJson(NutMap.class, json);
            if (null != params) {
                params.put("ffb", params.getBoolean("ffb"));
                params.put("log_off", params.getBoolean("log_off"));
                return run(ctx, params);
            }
        }
        catch (Exception e) {
            Wlog.getAPP().warn("App beacon command failed", e);
        }
        return WnMvcView.none();
    }
}
