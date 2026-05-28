package com.site0.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

public class WnSetSecurity implements ActionFilter {

    @Override
    public View match(ActionContext ac) {
        Ioc ioc = ac.getIoc();

        WnLoginApi auth = Wn.Service.auth(ioc);
        WnIo io = Wn.Service.io(ioc);

        WnContext wc = Wn.WC();
        wc.setSecurity(new WnSecurityImpl(io, auth));

        throw Wlang.makeThrow("no-support-anymore");

        // 继续下一个操作
        // return null;
    }

}
