package org.nutz.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.Wn;

public class WnSetSecurity implements ActionFilter {

    @Override
    public View match(ActionContext ac) {
        Ioc ioc = ac.getIoc();
        
        WnAuthService auth = Wn.Service.auth(ioc);
        WnIo io = Wn.Service.io(ioc);
        
        Wn.WC().setSecurity(new WnSecurityImpl(io, auth));

        // 继续下一个操作
        return null;
    }

}
