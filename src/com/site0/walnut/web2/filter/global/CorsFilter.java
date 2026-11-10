package com.site0.walnut.web2.filter.global;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcShortCircuitException;
import com.site0.walnut.web.util.WnWeb;

/**
 * 统一 CORS 过滤器（RM-NUTZ-16）：跨域逻辑的唯一封装点。
 * <p>
 * 对<b>全部</b>响应加默认跨域头（与旧 {@code WnWeb.setCrossDomainHeaders("*", cb)}
 * 完全一致：Allow-Origin * / Allow-Methods GET,POST,PUT,DELETE,OPTIONS,PATCH /
 * Allow-Headers Origin,Content-Type,Accept,X-Requested-With,X-Walnut-Ticket /
 * Allow-Credentials true）。策略先写死 {@code *}，收紧配置后续再做。
 * <p>
 * OPTIONS 预检<b>在本过滤器内统一短路</b>（用户 2026-11-10 定稿）：非 {@code /api/**}
 * 的 OPTIONS → 加默认头 + {@code status(204)} + flush + 抛
 * {@link WnMvcShortCircuitException}，不进 controller（避免被 SessionCheck 拦 401；
 * 单测直调 dispatcher 同样生效）。{@code /api/**} 的 OPTIONS 例外：只加默认头、不短路，
 * 由 HttpApi 引擎按 API 配置应答（其后写头覆盖默认头，header 为 set 语义）。
 * <p>
 * 行为统一化记录：旧系统各入口对 OPTIONS 处理不一（多数空 200、login_mode 走业务 JSON、
 * chse 未命中兜底 404）；本过滤器统一为「非 /api 的 OPTIONS → 204 + 默认四头」。
 * 浏览器预检不读响应体，业务不受影响。
 */
public class CorsFilter implements WnMvcFilter {

    @Override
    public void doFilter(WnMvcContext ctx) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            ctx.response().header(WnWeb.niceHeaderName(name), value);
        });

        String path = ctx.request().getPath();
        if ("OPTIONS".equalsIgnoreCase(ctx.request().getMethod())
            && (null == path || !path.startsWith("/api/"))) {
            ctx.response().status(204);
            ctx.response().flush();
            throw new WnMvcShortCircuitException();
        }
    }

}
