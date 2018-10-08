package org.nutz.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnSetSecurity implements ActionFilter {

    @Override
    public View match(ActionContext ac) {
        WnContext wc = Wn.WC();
        Ioc ioc = ac.getIoc();
        WnSessionService sess = ioc.get(WnSessionService.class, "sessionService");
        WnIo io = ioc.get(WnIo.class, "io");
        WnUsrService usrs = sess.usrs();
        wc.setSecurity(new WnSecurityImpl(io, usrs));

        // 继续下一个操作
        return null;
    }

}
