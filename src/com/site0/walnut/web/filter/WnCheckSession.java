package com.site0.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.web.util.WnWeb;
import org.nutz.web.ajax.AjaxReturn;
import org.nutz.web.ajax.AjaxView;

/**
 * 如果 TL 设置了 SessionId，将其取出，并且如果 Session 过期，则抛错
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCheckSession implements ActionFilter {

    private boolean ajax;

    private boolean allowDead;

    public WnCheckSession() {
        this(false);
    }

    public WnCheckSession(boolean ajax) {
        this.ajax = ajax;
    }

    public WnCheckSession(boolean ajax, boolean allowDead) {
        this.ajax = ajax;
        this.allowDead = allowDead;
    }

    public WnSession testSession(WnContext wc, WnLoginApi auth) {
        // 先看看上下文中的Session
        WnSession se = wc.getSession();

        // 尝试从票据中获取
        if (null == se && wc.hasTicket()) {
            // 看看有没有合法的 Session 对象
            String ticket = wc.getTicket();

            // 获取并更新 Sessoion 对象的最后访问时间
            se = auth.getSession(ticket);
            if (null != se) {
                WnSession se2 = se;
                Wn.WC().hooking(null, () -> {
                    int du = auth.getSessionDuration();
                    auth.touchSession(se2, du);

                });
            }

            // 默认，已经 dead 的会话不被采用，除非特别声明
            if (null == se || (se.isExpired() && !this.allowDead)) {
                return null;
            }
        }

        return se;
    }

    @Override
    public View match(ActionContext ac) {
        // 对于 options 放过
        if (WnWeb.isRequestOptions(ac.getRequest())) {
            return null;
        }

        WnContext wc = Wn.WC();
        Ioc ioc = ac.getIoc();

        // 如果有会话合法，那么就继续下一个操作
        WnLoginApi auth = Wn.Service.auth(ioc);
        WnSession se = testSession(wc, auth);

        if (null != se) {
            // 记录到上下文
            wc.setSession(se);

            // 读取服务类之类的
            WnIo io = Wn.Service.io(ioc);
            WnBoxService boxes = Wn.Service.boxes(ioc);

            WnUser me = se.getUser();

            // 给当前 Session 设置默认的当前路径
            se.getEnv().putDefault("PWD", me.getHomePath());

            // 生成沙盒上下文
            WnServiceFactory services = Wn.Service.services(ac.getIoc());
            WnBoxContext bc = new WnBoxContext(services, new NutMap());
            bc.io = io;
            bc.session = se;

            // 设置钩子上下文
            WnHookContext hc = new WnHookContext(boxes, bc);
            hc.service = ioc.get(WnHookService.class, "hookService");

            wc.setHookContext(hc);

            // 继续下一个操作
            return null;
        }

        // 没有有效 Session 的话，那么看看咋处理
        if (ajax) {
            AjaxReturn re = new AjaxReturn();
            re.setOk(false);
            re.setErrCode("e.se.noexists");
            re.setData(wc.getTicket());
            return new ViewWrapper(new AjaxView(true), re);
        }

        // 重定向到根
        return new ServerRedirectView("/");
    }

}
