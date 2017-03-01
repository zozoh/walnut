package org.nutz.walnut.web.module;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
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
    
    public boolean checkEtag(WnObj wobj, HttpServletRequest req, HttpServletResponse resp) {
        String etag = "";
        if (Strings.isBlank(wobj.sha1())) {
            etag = String.format("%s-%s-%s", "F", wobj.len(), wobj.lastModified());
        } else {
            etag = String.format("%s-%s-%s", wobj.sha1().substring(0, 6), wobj.len(), wobj.lastModified());
        }
        if (resp != null)
            resp.setHeader("ETag", etag);
        return etag.equals(req.getHeader("If-None-Match"));
    }
}
