package org.nutz.mvc.impl.contextCollector;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.ViewContextCollector;

import javax.servlet.http.HttpServletRequest;

/**
 * 路径参数收集
 */
public class PathargsCollector implements ViewContextCollector {
    @Override
    public Context collect(HttpServletRequest req, Object obj) {
        ActionContext ac = Mvcs.getActionContext();
        if (ac != null){
            return Wlang.context("pathargs", Mvcs.getActionContext().getPathArgs());
        }
        return null;
    }
}
