package org.nutz.walnut.web.filter;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.walnut.util.Wn;

public class WnAsUsr implements ActionFilter {

    private String me;

    private String grp;

    public WnAsUsr(String me, String grp) {
        this.me = me;
        this.grp = grp;
    }

    @Override
    public View match(ActionContext actionContext) {
        Wn.WC().me(me, grp);
        return null;
    }
}
