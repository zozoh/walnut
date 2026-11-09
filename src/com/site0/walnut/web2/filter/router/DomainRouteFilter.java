package com.site0.walnut.web2.filter.router;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web2.support.WnDomainResolver;
import com.site0.walnut.web2.support.WnPathRewriter;

/**
 * Host→域 路由过滤器（router 包；对照旧 {@code WalnutFilter} 的域名路由段）。
 * <p>
 * 行为：
 * <ul>
 * <li>写请求属性 <code>wn_www_path_org/host/ip</code>。</li>
 * <li>{@code main-host} 命中的 host 不路由（与旧实现一致；为 null 表示统统不路由）。</li>
 * <li>取 <code>/domain/{host}</code> 记录：过期 → 403；B 记录 → 只写
 * <code>wn_www_grp/site</code>；C 记录 → 额外写 <code>wn_www_static=yes</code>；
 * A 记录 → 按 hostmap 规则重写路径（{@link WnPathRewriter}）。</li>
 * </ul>
 * <p><b>预分发</b>：JETTY-03 的 Dispatcher 在匹配路由之后才执行 globalHead，而路径重写必须
 * 发生在其前；因此 {@code WnMvcJettyHandler} 会先调用本过滤器一次，链内再次执行时用
 * {@code wn.mvc.domain.routed} 幂等跳过。
 */
public class DomainRouteFilter implements WnMvcFilter {

    private static final String ROUTED = "wn.mvc.domain.routed";

    private final Pattern mainHost;

    private final WnDomainResolver resolver;

    private final List<DmnRule> rules;

    public DomainRouteFilter(Pattern mainHost, WnDomainResolver resolver, List<DmnRule> rules) {
        this.mainHost = mainHost;
        this.resolver = resolver;
        this.rules = null == rules ? Collections.<DmnRule> emptyList() : rules;
    }

    @Override
    public void doFilter(WnMvcContext ctx) {
        if (Boolean.TRUE.equals(ctx.attrs().get(ROUTED)))
            return;
        ctx.attrs().put(ROUTED, Boolean.TRUE);

        String path = ctx.request().getPath();
        String host = ctx.request().getHost();

        // 一定记录属性
        ctx.request().getAttrs().put("wn_www_path_org", path);
        ctx.request().getAttrs().put("wn_www_host", host);
        ctx.request().getAttrs().put("wn_www_ip", ctx.request().getRemoteAddr());

        // main-host 命中 → 不路由；mainHost 为 null 表示统统不路由
        if (null == mainHost || (null != host && mainHost.matcher(host).find()))
            return;

        WnObj oDmn = null == resolver ? null : resolver.fetchDomain(host);
        if (null == oDmn)
            return;

        // 过期记录（未声明 expi_at 表示无限期）
        long expiAt = oDmn.getLong("expi_at", 0);
        if (expiAt > 0 && expiAt < Wn.now()) {
            ctx.request().getAttrs().put("wn_www_error", "域名转发过期");
            throw new WnMvcFilterException(403, "e.domain.expired", "域名转发过期: " + host);
        }

        String grp = __str(oDmn.getFallback("domain", "dmn_grp"));
        String site = __str(oDmn.getFallback("site", "dmn_site"));
        ctx.request().getAttrs().put("wn_www_grp", grp);
        ctx.request().getAttrs().put("wn_www_site", site);

        // B：只记录
        if (oDmn.isType("B"))
            return;
        // C：静态站点，由 WebRootController/StaticRouter 处理
        if (oDmn.isType("C")) {
            ctx.request().getAttrs().put("wn_www_static", "yes");
            return;
        }

        // A：hostmap 重写
        for (DmnRule rule : rules) {
            String newPath = rule.render(path, grp);
            if (null == newPath)
                continue;
            if (newPath.startsWith("/websocket"))
                return;
            ctx.request().getAttrs().put("wn_www_path_new", newPath);
            if (ctx.request() instanceof WnPathRewriter)
                ((WnPathRewriter) ctx.request()).setPath(newPath);
            return;
        }
    }

    private static String __str(Object o) {
        return null == o ? "" : o.toString();
    }

}
