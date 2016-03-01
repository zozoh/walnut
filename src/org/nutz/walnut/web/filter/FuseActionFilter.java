package org.nutz.walnut.web.filter;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

// TODO 与WnCheckSession合并
@IocBean
public class FuseActionFilter implements ActionFilter {

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
        } else {
            // 不允许新建
            return HttpStatusView.HTTP_502;
        }
        if (se != null) {
            sess.touch(se.id());

            // 记录到上下文
            wc.SE(se);
            wc.me(se.me(), se.group());

            // 设置钩子
            WnIo io = ioc.get(WnIo.class, "io");
            WnBoxService boxes = ioc.get(WnBoxService.class, "boxService");
            WnUsrService usrs = sess.usrs();
            WnUsr me = usrs.check(se.me());

            WnBoxContext bc = new WnBoxContext();
            bc.io = io;
            bc.me = me;
            bc.session = se;
            bc.usrService = usrs;
            bc.sessionService = sess;

            WnHookContext hc = new WnHookContext(boxes, bc);
            hc.io = io;
            hc.me = me;
            hc.se = se;
            hc.service = ioc.get(WnHookService.class, "hookService");

            wc.setHookContext(hc);

            String path = ac.getPath();
            if (!path.endsWith("mkdir") && !path.endsWith("create")) {
                HttpServletRequest req = ac.getRequest();
                if (req.getParameter("path") != null) {
                    WnObj obj = io.fetch(null, req.getParameter("path"));
                    if (obj == null)
                        return HttpStatusView.HTTP_404;
                    req.setAttribute("fuse_obj", obj);
                }
                if (req.getParameter("source") != null && !req.getRequestURI().endsWith("symlink")) {
                    WnObj obj = io.fetch(null, req.getParameter("source"));
                    if (obj == null)
                        return HttpStatusView.HTTP_404;
                    req.setAttribute("fuse_obj", obj);
                }

            }
            // 返回空，继续下面的逻辑
            return null;
        } else {
            // 必须存在
            return HttpStatusView.HTTP_502;
        }

    }

}