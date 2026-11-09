package com.site0.walnut.mvc;

/**
 * WnMvc 调度器：路由匹配 + born 控制器 + 动作链执行 + 视图渲染 + 未命中回退。
 *
 * <h2>请求管线</h2>
 * <pre>
 * 请求 → HeadFilter → HeadFilter → Controller → TailFilter → TailFilter
 *        (全局 → 路由)                      (路由 → 全局)
 * </pre>
 * <p>HeadFilter = globalHeadFilters → route.headFilters（controller 之前，失败即中断）；
 * TailFilter = route.tailFilters → globalTailFilters（controller 之后，finally 收尾，
 * 前置失败也执行）。二者都是 {@link WnMvcFilter} 实例。</p>
 *
 * <p>由 {@link WnMvcFactory#build()} 产出，持有只读的 {@link WnMvcMapping}、工厂引用与
 * 全局过滤器数组（globalHeadFilters / globalTailFilters，JETTY-05 组装动作链时传入）。</p>
 *
 * <h2>调度流程（JETTY-03 实现）</h2>
 * <ol>
 * <li>从 <code>ctx.request()</code> 取 method/path，经 {@link WnMvcMapping#match(WnMvcHttpMethod, WnMvcRequest)}
 * 匹配路由（命中即完成路径绑定：<code>{name}</code> 进 request.getPathParams()，
 * <code>?</code>/<code>**</code> 进 request.getPathArgs()）；未命中 → 回退（静态资源 / 404 错误页）。</li>
 * <li>经 {@link WnMvcFactory#bornController(Class)} born 一个新控制器实例。</li>
 * <li>构造 {@link WnMvcChain}（headFilters = globalHeadFilters + route.headFilters；
 * tailFilters = route.tailFilters + globalTailFilters；终止动作为 controller.handle），
 * {@link WnMvcChain#invoke(WnMvcContext)} 循环执行；cleanup 必须在 finally。</li>
 * <li>入口方法返回的 {@link WnMvcView} 渲染进 <code>ctx.response()</code>；
 * 过滤器异常走 {@link WnMvcChain#fail(WnMvcContext, Throwable)} 失败视图（fail-closed）。</li>
 * </ol>
 *
 * <h2>线程模型</h2>
 * <p>调度器<b>单例、无状态</b>；{@link #dispatch(WnMvcContext)} 可被并发调用，
 * 每次调用只使用传入的每请求上下文，控制器实例每请求 born、即用即弃。</p>
 */
public class WnMvcDispatcher {

    private final WnMvcMapping mapping;

    private final WnMvcFactory factory;

    private final WnMvcFilter[] globalHeadFilters;

    private final WnMvcFilter[] globalTailFilters;

    private final WnMvcStaticService staticService;

    /**
     * 用给定路由表、工厂与全局过滤器数组构造调度器（无静态文件服务）。
     *
     * @param mapping
     *            只读路由表
     * @param factory
     *            装配工厂（born 控制器）
     * @param globalHeadFilters
     *            全局前置过滤器数组（编码/会话/安全等，对全部路由生效；无则传空数组）
     * @param globalTailFilters
     *            全局尾部过滤器数组（全局收尾，在路由尾部过滤器之后执行；无则传空数组）
     */
    public WnMvcDispatcher(WnMvcMapping mapping,
                           WnMvcFactory factory,
                           WnMvcFilter[] globalHeadFilters,
                           WnMvcFilter[] globalTailFilters) {
        this(mapping, factory, globalHeadFilters, globalTailFilters, null);
    }

    /**
     * 用给定路由表、工厂、全局过滤器数组与静态文件服务构造调度器。
     *
     * @param mapping
     *            只读路由表
     * @param factory
     *            装配工厂（born 控制器）
     * @param globalHeadFilters
     *            全局前置过滤器数组（编码/会话/安全等，对全部路由生效；无则传空数组）
     * @param globalTailFilters
     *            全局尾部过滤器数组（全局收尾，在路由尾部过滤器之后执行；无则传空数组）
     * @param staticService
     *            静态文件服务（未命中回退的「静态」分支委托给它；可为 <code>null</code>，
     *            此时静态形态路径直接走 404 HTML 页）。JETTY-05 提供真实实现。
     */
    public WnMvcDispatcher(WnMvcMapping mapping,
                           WnMvcFactory factory,
                           WnMvcFilter[] globalHeadFilters,
                           WnMvcFilter[] globalTailFilters,
                           WnMvcStaticService staticService) {
        this.mapping = mapping;
        this.factory = factory;
        this.globalHeadFilters =
            null == globalHeadFilters ? new WnMvcFilter[0] : globalHeadFilters;
        this.globalTailFilters =
            null == globalTailFilters ? new WnMvcFilter[0] : globalTailFilters;
        this.staticService = staticService;
    }

    /**
     * 持有的路由表。
     */
    public WnMvcMapping mapping() {
        return mapping;
    }

    /**
     * 全局前置过滤器数组。
     */
    public WnMvcFilter[] globalHeadFilters() {
        return globalHeadFilters;
    }

    /**
     * 全局尾部过滤器数组。
     */
    public WnMvcFilter[] globalTailFilters() {
        return globalTailFilters;
    }

    /**
     * 处理一次请求：匹配 + born 控制器 + 动作链执行 + 视图渲染 + 未命中回退。
     *
     * @param context
     *            每请求上下文（已由工厂注入 services/io/mimes）
     */
    public void dispatch(WnMvcContext context) {
        WnMvcRequest req = context.request();
        WnMvcHttpMethod mth = __parse_method(req.getMethod());
        // 1) 路由匹配（命中即完成路径绑定）；方法不匹配 = 未命中（对齐 Nutz，不产生 405）
        WnMvcMapping.Route route = null == mth ? null : mapping.match(mth, req);
        if (null == route) {
            __fallback(context);
            return;
        }
        // 2) born 控制器新实例（即用即弃）
        WnMvcController ctl = factory.bornController(route.controllerType());
        // 3) 组装动作链：Head = 全局 → 路由；Tail = 路由 → 全局
        WnMvcFilter[] head = __concat(globalHeadFilters, route.headFilters());
        WnMvcFilter[] tail = __concat(route.tailFilters(), globalTailFilters);
        WnMvcChain chain = new WnMvcChain(head, tail, c -> ctl.handle(c));
        // 4) 执行链 + 渲染视图（链内异常已走失败视图，返回 null 不再渲染）
        WnMvcView view = chain.invoke(context);
        if (null != view)
            view.render(context);
    }

    /**
     * 持有的静态文件服务，可能为 <code>null</code>。
     */
    public WnMvcStaticService staticService() {
        return staticService;
    }

    // ======================================================================
    // 内部实现
    // ======================================================================

    /**
     * 未命中回退的「页面 HTML / API JSON / 静态文件」分流规则（JETTY-03 设计，用户 2026-11-09T04:40 已确认）：
     * <ol>
     * <li><b>API 前缀</b>（一级段为 <code>o</code> / <code>u</code> / <code>api</code>）：
     * 一律 <code>404 + JSON 错误体</code>（<code>{"ok":false,"errCode":"e.mvc.nofound"}</code>），
     * 绝不返回 HTML。</li>
     * <li><b>静态形态路径</b>（带已知静态后缀）：委托 {@link WnMvcStaticService}；
     * 服务不存在或文件不存在 → 404 HTML 页。</li>
     * <li><b>其余（页面路径）</b>：404 + HTML 错误页。</li>
     * </ol>
     */
    private void __fallback(WnMvcContext context) {
        String path = context.request().getPath();
        // 1) API 前缀 → JSON 404
        String seg0 = __first_segment(path);
        if ("o".equals(seg0) || "u".equals(seg0) || "api".equals(seg0)) {
            org.nutz.lang.util.NutMap body = new org.nutz.lang.util.NutMap();
            body.put("ok", false);
            body.put("errCode", "e.mvc.nofound");
            body.put("msg", "no route: " + path);
            WnMvcView.error(404, body).render(context);
            return;
        }
        // 2) 静态形态路径 → 静态文件服务；不存在 → 404 HTML
        if (null != staticService && __has_static_suffix(path)) {
            if (staticService.serve(context))
                return;
        }
        // 3) 页面路径 → 404 HTML 页（错误页资源 + TmplX，JETTY-05 细化）
        WnMvcResponse resp = context.response();
        resp.status(404);
        WnMvcView.html(WnMvcChain.errorHtml(404, "no route: " + path, path)).render(context);
    }

    /**
     * 已知的静态资源后缀（未命中时委托静态文件服务的判定白名单）。
     */
    private static final java.util.Set<String> STATIC_SUFFIXES = new java.util.HashSet<>(
        java.util.Arrays.asList("js", "css", "json", "html", "htm", "txt", "pdf", "gif",
                                "png", "svg", "jpg", "jpeg", "ico", "webp", "map", "xml",
                                "csv", "woff", "woff2", "ttf", "mp4", "webm", "md"));

    private static String __first_segment(String path) {
        if (null == path || path.length() <= 1)
            return "";
        int off = path.startsWith("/") ? 1 : 0;
        int i = path.indexOf('/', off);
        return i < 0 ? path.substring(off) : path.substring(off, i);
    }

    private static boolean __has_static_suffix(String path) {
        int i = null == path ? -1 : path.lastIndexOf('.');
        if (i < 0 || i == path.length() - 1)
            return false;
        int slash = path.lastIndexOf('/');
        if (slash > i)
            return false;
        return STATIC_SUFFIXES.contains(path.substring(i + 1).toLowerCase());
    }

    /**
     * 解析 HTTP 方法枚举；未知方法返回 <code>null</code>（按未命中回退处理）。
     */
    private static WnMvcHttpMethod __parse_method(String method) {
        if (null == method)
            return null;
        try {
            return WnMvcHttpMethod.valueOf(method.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static WnMvcFilter[] __concat(WnMvcFilter[] a, WnMvcFilter[] b) {
        if (a.length == 0)
            return b;
        if (b.length == 0)
            return a;
        WnMvcFilter[] re = new WnMvcFilter[a.length + b.length];
        System.arraycopy(a, 0, re, 0, a.length);
        System.arraycopy(b, 0, re, a.length, b.length);
        return re;
    }

}
