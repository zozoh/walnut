package com.site0.walnut.web2.filter.global;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.support.WnAuthLookup;

/**
 * 全局上下文过滤器（head）：对照旧 {@code web/processor/CreateWnContext}。
 * <p>
 * 每请求初始化线程上下文 {@link WnContext}（IPv4/票据/会话/安全），并从
 * header {@code X-Walnut-Ticket}、query {@code _wn_ticket_}、Cookie {@code SEID}
 * 依次恢复票据；成功取到未过期会话时写入 {@link WnMvcContext#setSession} 与
 * {@code Wn.WC().setSession(...)}，供后续过滤器/控制器使用。
 * <p>
 * 注意：{@link WnMvcContext} 本身由 {@code WnMvcJettyHandler} 用
 * {@code WnMvcFactory.newContext} 创建；本过滤器只做上下文初始化。
 */
public class CreateMvcContextFilter implements WnMvcFilter {

    private final WnAuthLookup auth;

    private final WnIo io;

    public CreateMvcContextFilter(WnAuthLookup auth, WnIo io) {
        this.auth = auth;
        this.io = io;
    }

    @Override
    public void doFilter(WnMvcContext ctx) {
        WnContext wc = Wn.WC();
        wc.setLogOn();
        wc.setIPv4(ctx.request().getRemoteAddr());

        // 票据：header → query → Cookie(SEID)
        String ticket = ctx.request().getHeader("X-Walnut-Ticket");
        if (Ws.isBlank(ticket)) {
            Object v = ctx.request().queryStringMap().get("_wn_ticket_");
            ticket = null == v ? null : v.toString();
        }
        if (Ws.isBlank(ticket))
            ticket = ctx.request().getCookie(Wn.AT_SEID);
        if (!Ws.isBlank(ticket))
            wc.setTicket(ticket);

        // 恢复会话（过期的不采用）
        if (null != auth && !Ws.isBlank(ticket)) {
            WnSession se = auth.getSession(ticket);
            if (null != se && !se.isExpired()) {
                ctx.setSession(se);
                wc.setSession(se);
                auth.touchSession(se);
            }
        }

        // 基础安全（旧 CreateWnContext 语义）：后续 SessionCheck 可换成 WnSecurityImpl
        if (null != io)
            wc.setSecurity(new WnEvalLink(io));

        ctx.attrs().put("wn.mvc.filter.create", Boolean.TRUE);
        // 页面错误体开关：web2 请求一律按页面 HTML 渲染错误（API 前缀仍 JSON）
        ctx.attrs().put("wn.mvc.error.page", Boolean.TRUE);
    }

}
