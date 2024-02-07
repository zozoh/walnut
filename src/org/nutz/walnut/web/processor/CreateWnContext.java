package org.nutz.walnut.web.processor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Ws;

public class CreateWnContext extends AbstractProcessor {

    private static final Log log = Wlog.getAC();

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
        // if (!wc.hasSEID()) {
        if (null != req) {
            // 从 header 里获取 Session 的 ID
            String ticket = req.getHeader("X-Walnut-Ticket");
            // System.out.printf("Initial ticket=%s\n", ticket);
            // Enumeration<String> hnms = req.getHeaderNames();
            // while (hnms.hasMoreElements()) {
            // String hnm = hnms.nextElement();
            // String val = req.getHeader(hnm);
            // System.out.printf("!!! %s=%s\n", hnm, val);
            // }
            // ticket = req.getHeader("X-Walnut-Ticket");
            // System.out.printf("=== Get Again=%s\n", ticket);
            // 从 cookie 里获取 Session 的 ID
            if (Ws.isBlank(ticket)) {
                wc.copyCookieItems(req, Lang.array(Wn.AT_SEID));
            }
            // 恢复票据
            else {
                wc.setTicket(ticket);
            }
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
        // }
    }

}
