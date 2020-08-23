package org.nutz.walnut.web.processor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class CreateWnContext extends AbstractProcessor {

    private static final Log log = Logs.get();

    @Override
    public void process(ActionContext ac) throws Throwable {

        WnContext wc = Wn.WC();
        HttpServletRequest req = ac.getRequest();
        HttpServletResponse resp = ac.getResponse();

        // 标识一下响应
        resp.addHeader("X-Powered-By", WnVersion.getName());

        // 设置上下文，主要是从 Cookie 里恢复 SEID
        setupWnContext(wc, req);

        // 设置成可以处理基本的链接
        WnIo io = ac.getIoc().get(WnIo.class, "io");
        wc.setSecurity(new WnEvalLink(io));

        // 继续下一个处理
        doNext(ac);
    }

    public static void setupWnContext(WnContext wc, HttpServletRequest req) {
        //if (!wc.hasSEID()) {
            // 从 cookie 里获取 Session 的 ID
            if (null != req) {
                wc.copyCookieItems(req, Lang.array(Wn.AT_SEID));
            }
            // 显示准备接受调用
            if (log.isInfoEnabled()) {
                wc._timestamp = Wn.now();
                if (log.isDebugEnabled()) {
                    log.debugf("ACCEPT: %s", req.getServletPath());
                }
            } else {
                wc._timestamp = -1;
            }
        //}
    }

}
