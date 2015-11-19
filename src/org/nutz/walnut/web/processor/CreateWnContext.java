package org.nutz.walnut.web.processor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class CreateWnContext extends AbstractProcessor {

    private static final Log log = Logs.get();

    @Override
    public void process(ActionContext ac) throws Throwable {

        WnContext wc = Wn.WC();
        HttpServletRequest req = ac.getRequest();

        // 设置上下文，主要是从 Cookie 里恢复 SEID
        setupWnContext(wc, req);

        // 继续下一个处理
        doNext(ac);
    }

    public static void setupWnContext(WnContext wc, HttpServletRequest req) {
        if (null == wc.SEID()) {
            // 获取 Session
            if (null != req) {
                Cookie[] cookies = req.getCookies();
                if (null != cookies)
                    for (Cookie co : cookies) {
                        if (Wn.AT_SEID.equals(co.getName())) {
                            wc.SEID(co.getValue());
                            break;
                        }
                    }
            }
            // 显示准备接受调用
            if (log.isInfoEnabled()) {
                wc._timestamp = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debugf("ACCEPT: %s", req.getServletPath());
                }
            } else {
                wc._timestamp = -1;
            }
        }
    }

}
