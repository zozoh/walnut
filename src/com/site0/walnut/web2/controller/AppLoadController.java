package com.site0.walnut.web2.controller;

import java.io.IOException;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web2.view.WnDownloadView;

public class AppLoadController extends AbstractAppController {

    private static final Log log = Wlog.getAPP();

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            NutMap params = params(ctx.request());
            WnIo io = io(ctx);
            String appName = arg(ctx, 0);
            WnObj appHome = _check_app_home(ctx, appName);
            String rsName = arg(ctx, 1);
            String mimeType = params.getString("mime");
            String download = params.getString("d");
            String ua = ctx.request().getHeader("User-Agent");
            String etag = ctx.request().getHeader("If-None-Match");
            String range = ctx.request().getHeader("Range");
            Stopwatch sw = null;
            if (log.isDebugEnabled()) {
                log.debugf("APPLoad(%s) : %s", appName, rsName);
                sw = Stopwatch.begin();
            }
            try {
                WnObj o = io.check(appHome, rsName);
                o = Wn.WC().whenRead(o, false);
                String text = null;
                if (log.isDebugEnabled())
                    sw.tag("check_rs " + rsName);
                ua = WnWeb.autoUserAgent(o, ua, download);
                if ("application/json".equals(mimeType)) {
                    NutMap json = Json.fromJson(NutMap.class, text);
                    text = Json.toJson(json, JsonFormat.nice());
                }
                if (null != text) {
                    StringInputStream ins = new StringInputStream(text);
                    return new WnDownloadView(ins, -1, ua, Strings.sBlank(mimeType, o.mime()),
                                              o.name(), etag, range);
                }
                return new WnDownloadView(io, o, null, ua, etag, range);
            }
            finally {
                if (log.isDebugEnabled()) {
                    sw.stop();
                    log.debugf("APPLoad(%s) : %s DONE %s", appName, rsName, sw);
                }
            }
        }
        catch (IOException | RuntimeException e) {
            throw new WnMvcFilterException(404, "e.mvc.nofound", e.getMessage(), e);
        }
    }
}
