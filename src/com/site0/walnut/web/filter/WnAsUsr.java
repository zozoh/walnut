package com.site0.walnut.web.filter;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;

public class WnAsUsr implements ActionFilter {

    private String name;

    public WnAsUsr(String name) {
        this.name = name;
    }

    @Override
    public View match(ActionContext ac) {
        Ioc ioc = ac.getIoc();
        WnLoginApi auth = Wn.Service.auth(ioc);
        WnUser me = auth.checkUser(name);
        Wn.WC().setMe(me);
        return null;
    }
}
