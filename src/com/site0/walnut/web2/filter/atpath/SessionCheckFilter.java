package com.site0.walnut.web2.filter.atpath;

import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.support.WnAuthLookup;

/**
 * 路线级会话校验过滤器（atpath）：对照旧 {@code web/filter/WnCheckSession}。
 * <p>
 * 语义：
 * <ul>
 * <li>OPTIONS 直接放行（对齐旧实现）。</li>
 * <li>已有会话（{@code ctx.session()}）→ 写入 {@code Wn.WC()}；否则按票据再取一次。</li>
 * <li>无有效会话：<b>ajax/args=true</b> → 抛 401（{@code e.se.noexists}），不走重定向；
 * 默认模式 → 302 重定向 <code>/</code> 并中断链（fail-closed）。</li>
 * </ul>
 */
public class SessionCheckFilter implements WnMvcFilter {

    private final WnAuthLookup auth;

    private final boolean ajax;

    private final boolean allowDead;

    public SessionCheckFilter(WnAuthLookup auth) {
        this(auth, false, false);
    }

    public SessionCheckFilter(WnAuthLookup auth, boolean ajax, boolean allowDead) {
        this.auth = auth;
        this.ajax = ajax;
        this.allowDead = allowDead;
    }

    @Override
    public void doFilter(WnMvcContext ctx) {
        if ("OPTIONS".equalsIgnoreCase(ctx.request().getMethod()))
            return;

        WnSession se = ctx.session();
        if (null == se && null != auth) {
            String ticket = Wn.WC().getTicket();
            if (Ws.isBlank(ticket))
                ticket = ctx.request().getHeader("X-Walnut-Ticket");
            if (Ws.isBlank(ticket)) {
                Object v = ctx.request().queryStringMap().get("_wn_ticket_");
                ticket = null == v ? null : v.toString();
            }
            if (!Ws.isBlank(ticket)) {
                se = auth.getSession(ticket);
                if (null != se && (allowDead || !se.isExpired())) {
                    ctx.setSession(se);
                    auth.touchSession(se);
                }
                else {
                    se = null;
                }
            }
        }
        else if (null != se && se.isExpired() && !allowDead) {
            se = null;
        }

        if (null != se) {
            WnContext wc = Wn.WC();
            wc.setSession(se);
            ctx.attrs().put("wn.mvc.filter.session", se.getTicket());
            return;
        }

        // 未通过：ajax/true → 401；否则重定向 "/" 并中断
        if (ajax) {
            throw new WnMvcFilterException(401, "e.se.noexists", "no session");
        }
        ctx.response().sendRedirect("/");
        throw new WnMvcFilterException(302, null, "no session: redirect to /");
    }

}
