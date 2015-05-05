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
        if (log.isInfoEnabled())
            log.infof("--ENE URL: %s", ac.getPath());

        // 执行清除
        Wn.Ctx.clear();

        // 继续下一个处理
        doNext(ac);
    }

}
