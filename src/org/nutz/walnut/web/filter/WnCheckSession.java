package org.nutz.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.web.ajax.AjaxReturn;
import org.nutz.web.ajax.AjaxView;

/**
 * 如果 TL 设置了 SessionId，将其取出，并且如果 Session 过期，则抛错
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCheckSession implements ActionFilter {

    private boolean ajax;

    public WnCheckSession() {
        this(false);
    }

    public WnCheckSession(boolean ajax) {
        this.ajax = ajax;
    }

    public static WnSession testSession(WnContext wc, Ioc ioc) {
        // 如果上下文已经有了 Session，就直接返回
        WnSession se = wc.SE();
        if (null != se)
            return se;

        // 获取 SessionID
        String seid = wc.SEID();
        if (null == seid)
            return null;

        // 看看有没有合法的 Session 对象
        return _sess(ioc).fetch(seid);

    }

    private static WnSessionService _sess(Ioc ioc) {
        return ioc.get(WnSessionService.class, "sessionService");
    }

    @Override
    public View match(ActionContext ac) {

        WnContext wc = Wn.WC();
        Ioc ioc = ac.getIoc();

        // 如果有会话合法，那么就继续下一个操作
        WnSession se = testSession(wc, ioc);

        if (null != se) {
            WnSessionService sess = _sess(ioc);

            // 更行 Sessoion 对象的最后访问时间
            sess.touch(se.id());

            // 记录到上下文
            wc.SE(se);
            wc.me(se.me(), se.group());

            // 读取服务类之类的
            WnIo io = ioc.get(WnIo.class, "io");
            WnBoxService boxes = ioc.get(WnBoxService.class, "boxService");
            WnUsrService usrs = sess.usrs();
            WnUsr me = usrs.check(se.me());

            // 给当前 Session 设置默认的当前路径
            se.var("PWD", me.home());

            // 生成沙盒上下文
            WnBoxContext bc = new WnBoxContext();
            bc.io = io;
            bc.me = me;
            bc.session = se;
            bc.usrService = usrs;
            bc.sessionService = sess;

            // 设置钩子上下文
            WnHookContext hc = new WnHookContext(boxes, bc);
            hc.io = io;
            hc.me = me;
            hc.se = se;
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
            re.setData(wc.SEID());
            return new ViewWrapper(new AjaxView(true), re);
        }

        // 重定向到根
        return new ServerRedirectView("/");
    }

}
