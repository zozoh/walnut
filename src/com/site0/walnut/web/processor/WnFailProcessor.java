package com.site0.walnut.web.processor;

import com.site0.walnut.log.WnLog;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.ViewProcessor;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import org.nutz.web.WebException;

public class WnFailProcessor extends ViewProcessor {

    private static final WnLog log = Wlog.getAC();

    @Override
    public void init(NutConfig config, ActionInfo ai) throws Throwable {
        view = evalView(config, ai, ai.getFailView());
    }

    public void process(ActionContext ac) throws Throwable {
        Throwable e = ac.getError();
        // 正常的 Web 错误
        if (e instanceof WebException) {
            if (log.isInfoEnabled()) {
                WebException we = (WebException) e;
                log.infof("APP WARN: %s : %s", we.getKey(), we.getReason());
            }
        }
        // 其他异常
        else {
            if (log.isWarnEnabled()) {
                String uri = Mvcs.getRequestPath(ac.getRequest());
                log.warn(String.format("Error@%s :", uri), ac.getError());
            }
        }
        // 显示调试信息
        if (log.isInfoEnabled()) {
            long ts = Wn.WC()._timestamp;
            long du = ts > 0 ? Wn.now() - ts : ts;
            int status = ac.getResponse().getStatus();
            String ph = ac.getRequest().getServletPath();
            String qs = ac.getRequest().getQueryString();
            if (null == qs) {
                qs = "";
            } else if (!Ws.isBlank(qs)) {
                qs = "?" + qs;
            }
            log.infof("🚫KO%d:%dms:%s%s", status, du, ph, qs);
        }

        // 执行清除
        Wn.Ctx.clear();

        // 调用用户视图逻辑
        super.process(ac);
    }

}
