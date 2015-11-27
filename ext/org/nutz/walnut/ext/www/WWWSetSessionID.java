package org.nutz.walnut.ext.www;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.walnut.util.Wn;

/**
 * 将 Cookie 里的 DSEID 设置到线程上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WWWSetSessionID implements ActionFilter {

    @Override
    public View match(ActionContext ac) {

        HttpServletRequest req = ac.getRequest();

        // 获取 Session
        Cookie[] cookies = req.getCookies();
        if (null != cookies)
            for (Cookie co : cookies) {
                if (WWW.AT_SEID.equals(co.getName())) {
                    Wn.WC().setv(WWW.AT_SEID, co.getValue());
                    break;
                }
            }

        // 继续下面的操作
        return null;
    }

}
