package com.site0.walnut.web.processor;

import org.nutz.log.Log;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class DeposeWnContext extends AbstractProcessor {

    private static final Log log = Wlog.getAC();

    @Override
    public void process(ActionContext ac) throws Throwable {

        // 显示调试信息
        if (log.isInfoEnabled()) {
            long ts = Wn.WC()._timestamp;
            long du = ts > 0 ? Wn.now() - ts : ts;
            String ph = ac.getRequest().getServletPath();
            int status = ac.getResponse().getStatus();
            // 这种 URL 暂时先不打印，因为负载均衡会狂请求 ...
            if (!"/".equals(ph)) {
                String qs = ac.getRequest().getQueryString();
                if (!Ws.isBlank(qs)) {
                    qs = "?" + qs;
                } else {
                    qs = "";
                }
                log.infof("✔️OK%d:%dms:%s%s", status, du, ph, qs);
            }
        }

        // 执行清除
        Wn.Ctx.clear();

        // 继续下一个处理
        doNext(ac);
    }

}
