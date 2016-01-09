package org.nutz.walnut.web.processor;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import org.nutz.walnut.util.Wn;

public class DeposeWnContext extends AbstractProcessor {

    private static final Log log = Logs.get();

    @Override
    public void process(ActionContext ac) throws Throwable {

        // 显示调试信息
        if (log.isInfoEnabled()) {
            long ts = Wn.WC()._timestamp;
            long du = ts > 0 ? System.currentTimeMillis() - ts : ts;
            log.infof("HTTPok:%3dms: %s", du, ac.getRequest().getServletPath());
        }

        // 执行清除
        Wn.Ctx.clear();

        // 继续下一个处理
        doNext(ac);
    }

}
