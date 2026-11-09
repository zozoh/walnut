package com.site0.walnut.web2.filter.atpath;

import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.support.WnAuthLookup;

/**
 * 路线级身份切换过滤器（atpath）：对照旧 {@code web/filter/WnAsUsr}。
 * <p>
 * 以指定用户名执行：{@code auth.checkUser(name)} → {@code Wn.WC().setMe(user)}；
 * 用户不存在或鉴权异常一律 403（fail-closed），不进入 controller。
 */
public class AsUsrFilter implements WnMvcFilter {

    private final WnAuthLookup auth;

    private final String name;

    public AsUsrFilter(WnAuthLookup auth, String name) {
        this.auth = auth;
        this.name = name;
    }

    @Override
    public void doFilter(WnMvcContext ctx) {
        if (null == auth || Ws.isBlank(name))
            throw new WnMvcFilterException(403, "e.se.forbid", "no auth for as-usr");
        WnUser user;
        try {
            user = auth.checkUser(name);
        }
        catch (Throwable e) {
            throw new WnMvcFilterException(403, "e.se.forbid", "user not found: " + name, e);
        }
        if (null == user)
            throw new WnMvcFilterException(403, "e.se.forbid", "user not found: " + name);
        Wn.WC().setMe(user);
        ctx.attrs().put("wn.mvc.filter.asusr", name);
    }

}
