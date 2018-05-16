package org.nutz.walnut.web.module;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Mirror;
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
        // 生成上下文
        NutMap context = new NutMap();

        // .........................................
        // 计入请求头
        NutMap header = new NutMap();

        Enumeration<String> hnms = req.getHeaderNames();
        while (hnms.hasMoreElements()) {
            String hnm = hnms.nextElement();
            String hval = req.getHeader(hnm);
            header.put(hnm.toUpperCase(), hval);
        }
        context.put("header", header);

        // .........................................
        // 计入请求 Cookie
        NutMap cookie = new NutMap();

        Cookie[] coos = req.getCookies();
        if (null != coos && coos.length > 0) {
            for (Cookie coo : coos) {
                cookie.putDefault(coo.getName(), coo.getValue());
            }
            context.put("cookies", cookie);
        }

        // .........................................
        // 计入请求属性
        NutMap attrs = new NutMap();
        Enumeration<String> anms = req.getAttributeNames();
        while (anms.hasMoreElements()) {
            String key = anms.nextElement();
            Object val = req.getAttribute(key);
            if (null != val) {
                Mirror<Object> mi = Mirror.me(val);
                // 简单的值才记录，以防止可怕的事情发生
                if (mi.isNumber() || mi.isStringLike()) {
                    attrs.put(key, val);
                }
            }
        }
        context.put("attrs", attrs);

        // .........................................
        // 计入参数
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

        // 得到 duser 会话 ID (TODO zozoh:这个最后会被抛弃掉)
        context.put("sessionId", Wn.WC().getString(WWW.AT_SEID));

        // 返回
        return context;
    }
}
