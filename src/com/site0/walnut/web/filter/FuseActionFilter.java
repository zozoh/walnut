package com.site0.walnut.web.filter;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

// TODO 与WnCheckSession合并
@IocBean
public class FuseActionFilter implements ActionFilter {

    @Override
    public View match(ActionContext ac) {

        WnContext wc = Wn.WC();
        String ticket = wc.getTicket();
        Ioc ioc = ac.getIoc();

        // 如果有会话 ID，则检查一下有效性
        WnAuthService auth = Wn.Service.auth(ioc);
        WnAuthSession se = null;
        if (ticket != null) {
            se = auth.checkSession(ticket);
        } else {
            // 不允许新建
            return HttpStatusView.HTTP_502;
        }

        if (se != null) {
            // 记录到上下文
            wc.setSession(se);

            // 设置钩子
            WnIo io = ioc.get(WnIo.class, "io");
            WnBoxService boxes = ioc.get(WnBoxService.class, "boxService");

            WnServiceFactory services = Wn.Service.services(ioc);
            WnBoxContext bc = new WnBoxContext(services, new NutMap());
            bc.io = io;
            bc.session = se;
            bc.auth = auth;

            WnHookContext hc = new WnHookContext(boxes, bc);
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
                if (req.getParameter("source") != null
                    && !req.getRequestURI().endsWith("symlink")) {
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