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
        // 获取 Session
        HttpServletRequest req = ac.getRequest();
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
        // 显示调试信息
        if (log.isInfoEnabled())
            log.infof("BEGIN URL: %s", ac.getRequest().getServletPath());

        // 继续下一个处理
        doNext(ac);
    }

}
