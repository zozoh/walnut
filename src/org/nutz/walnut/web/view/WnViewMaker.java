package org.nutz.walnut.web.view;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.View;
import org.nutz.mvc.ViewMaker;

public class WnViewMaker implements ViewMaker {

    @Override
    public View make(Ioc ioc, String type, String value) {
        // 设置 cookie
        if ("++cookie>>".equals(type)) {
            return new WnAddCookieViewWrapper(value);
        }
        // 从 cookie 移除
        else if ("--cookie>>".equals(type)) {
            return new WnDelCookieViewWrapper(value);
        }
        return null;
    }

}
