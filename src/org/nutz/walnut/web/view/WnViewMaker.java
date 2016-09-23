package org.nutz.walnut.web.view;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.View;
import org.nutz.mvc.ViewMaker;
import org.nutz.web.ajax.AjaxView;

public class WnViewMaker implements ViewMaker {

    @Override
    public View make(Ioc ioc, String type, String value) {
        // 设置 cookie 并重定向
        if ("++cookie>>".equals(type)) {
            return new WnAddCookieViewWrapper(value);
        }
        // 从 cookie 移除并重定向
        else if ("--cookie>>".equals(type)) {
            return new WnDelCookieViewWrapper(value);
        }
        // 设置 cookie 并输出 AJAX 返回
        else if("++cookie->ajax".equals(type)){
            return new WnAddCookieViewWrapper(new AjaxView(value));
        }
        
        // 呃，不认识了 ...
        return null;
    }

}
