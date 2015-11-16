package org.nutz.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
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

/**
 * 如果 TL 设置了 SessionId，将其取出，并且如果 Session 过期，则抛错
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCheckSession implements ActionFilter {

    protected boolean needSession;

    @Override
    public View match(ActionContext ac) {

        WnContext wc = Wn.WC();
        String seid = wc.SEID();
        Ioc ioc = ac.getIoc();

        // 如果有会话 ID，则检查一下有效性
        WnSessionService sess = ioc.get(WnSessionService.class, "sessionService");
        WnSession se = null;
        if (seid != null) {
            se = sess.fetch(seid);
        }
        if (se != null) {
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

            // 返回空，继续下面的逻辑
            return null;
        } else {
            return new ServerRedirectView("/");
        }

    }

}
