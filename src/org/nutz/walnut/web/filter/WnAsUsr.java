package org.nutz.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.util.Wn;

public class WnAsUsr implements ActionFilter {

    private String name;

    public WnAsUsr(String name) {
        this.name = name;
    }

    @Override
    public View match(ActionContext ac) {
        Ioc ioc = ac.getIoc();
        WnAuthService auth = Wn.Service.auth(ioc);
        WnAccount me = auth.checkAccount(name);
        Wn.WC().setMe(me);
        return null;
    }
}
