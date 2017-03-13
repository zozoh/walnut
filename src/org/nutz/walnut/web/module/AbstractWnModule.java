package org.nutz.walnut.web.module;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.WWW;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.web.WnConfig;

public abstract class AbstractWnModule extends WnRun {

    public static View HTTP_304 = new HttpStatusView(304);

    @Inject("refer:conf")
    protected WnConfig conf;

    @Inject("refer:mimes")
    protected MimeMap mimes;

    protected WnObj _find_app_home(String appName) {
        String appPaths = Wn.WC().checkSE().vars().getString("APP_PATH");
        String[] bases = Strings.splitIgnoreBlank(appPaths, ":");
        for (String base : bases) {
            String ph = Wn.appendPath(base, appName);
            WnObj o = io.fetch(null, ph);
            if (null != o)
                return o;
        }
        return null;
    }

    protected WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }

    protected NutMap _gen_context_by_req(HttpServletRequest req) {
        NutMap context = new NutMap();
        // 生成上下文
        NutMap params = new NutMap();

        // 寻找一个请求里的所有参数
        Map<String, String[]> paramMap = req.getParameterMap();
        for (Map.Entry<String, String[]> en : paramMap.entrySet()) {
            String key = en.getKey();
            String[] val = en.getValue();

            if (null == val || val.length == 0)
                continue;

            if (val.length == 1)
                params.put(key, val[0]);
            else
                params.put(key, val);
        }
        context.put("params", params);

        // 得到会话 ID
        context.put("sessionId", Wn.WC().getString(WWW.AT_SEID));

        // 返回
        return context;
    }

    public boolean checkEtag(WnObj wobj, HttpServletRequest req, HttpServletResponse resp) {
        String etag = "";
        if (Strings.isBlank(wobj.sha1())) {
            etag = String.format("%s-%s-%s", "F", wobj.len(), wobj.lastModified());
        } else {
            etag = String.format("%s-%s-%s",
                                 wobj.sha1().substring(0, 6),
                                 wobj.len(),
                                 wobj.lastModified());
        }
        if (resp != null)
            resp.setHeader("ETag", etag);
        return etag.equals(req.getHeader("If-None-Match"));
    }
}
