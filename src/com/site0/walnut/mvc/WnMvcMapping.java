package com.site0.walnut.mvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;

/**
 * WnMvc 全局路由表：把「HTTP 方法 + 路径模式」映射到
 * 「控制器类 + 前置过滤器数组（headFilters）+ 尾部过滤器数组（tailFilters）」。
 *
 * <p>路由表在装配期一次性构造完成后<b>只读</b>；每个路径条目只存<b>控制器类与过滤器</b>，
 * 请求命中时才由 {@link WnMvcFactory} born 一个新控制器实例（即用即弃），因此路由表本身
 * 不持有任何控制器实例。</p>
 *
 * <h2>1. 怎么注册路由与 Filter</h2>
 * <p><b>全部走 {@link #route}</b>（没有 get/post/any 等便捷方法）：每条路由显式声明
 * HTTP 方法数组（{@link WnMvcHttpMethod} 枚举）、路径模式、控制器类、前置过滤器数组
 * （headFilters）、尾部过滤器数组（tailFilters）。</p>
 *
 * <pre>
 * WnMvcMapping m = factory.mapping();
 * // 单方法 + 无过滤器：两个过滤器参数都传空数组
 * m.route(new WnMvcHttpMethod[]{ WnMvcHttpMethod.GET },
 *         "/a/login/**",
 *         AppLoginController.class,
 *         new WnMvcFilter[]{},
 *         new WnMvcFilter[]{});
 * // 多方法约束 + 前置/尾部过滤器（显式数组）
 * m.route(new WnMvcHttpMethod[]{ WnMvcHttpMethod.GET, WnMvcHttpMethod.POST },
 *         "/a/captcha/get/?",
 *         CaptchaGetController.class,
 *         new WnMvcFilter[]{ WnCheckSession.INSTANCE },  // 前置（headFilters）
 *         new WnMvcFilter[]{ TempObjsCleaner.INSTANCE }); // 尾部（tailFilters，收尾）
 * </pre>
 *
 * <p><b>过滤器语义</b>：见 {@link WnMvcFilter}——<b>不抛异常即通过</b>，拦截失败抛
 * {@link WnMvcFilterException}。前置过滤器（headFilters）在 controller 之前执行（失败即中断）；
 * 尾部过滤器（tailFilters）在 controller 之后执行（作为 finally 收尾，前置失败也执行）。
 * 全局前置/全局尾部过滤器不属于路由表，由 {@link WnMvcDispatcher} 组装动作链时统一处理。</p>
 *
 * <h2>2. 请求来了怎么经 Mapping 转发</h2>
 *
 * <pre>
 * WnMvcHttpMethod mth = WnMvcHttpMethod.valueOf(ctx.request().getMethod());
 * Route route = m.match(mth, ctx.request());
 * if (route == null) {
 *     // 未命中：回退（静态资源 / 404 错误页），由 Dispatcher 处理
 * } else {
 *     // 路径绑定已完成：{name} 已写入 req.getPathParams()，?/** 已按序写入 req.getPathArgs()
 *     WnMvcController ctl = factory.bornController(route.controllerType());
 *     WnMvcChain chain = new WnMvcChain(route.headFilters(),
 *                                      route.tailFilters(),
 *                                      c -&gt; ctl.handle(c));
 *     WnMvcView view = chain.invoke(ctx);
 *     if (view != null)
 *         view.render(ctx);
 * }
 * </pre>
 *
 * <h2>路径模式与绑定</h2>
 * <ul>
 * <li><code>{name}</code>：匹配一个路径段并<b>按名捕获</b>，命中后写入
 * <code>request.getPathParams()</code>（键 = name）。</li>
 * <li><code>?</code>：匹配一个路径段并<b>按出现顺序</b>加入 <code>request.getPathArgs()</code>。</li>
 * <li><code>**</code>：匹配剩余全部路径段（须在末段），按 <code>/</code> 拼接后加入
 * <code>request.getPathArgs()</code>。</li>
 * <li><code>*</code>：匹配一个路径段，<b>不捕获</b>。</li>
 * </ul>
 * <p>绑定结果写进 {@link WnMvcRequest} 提供的<b>活容器</b>（见其 Javadoc：实现类在构造函数里
 * 预先解析好路径分段与 HTTP Head，并创建好空容器）。</p>
 *
 * <h2>匹配规则（JETTY-03 定稿）</h2>
 * <ul>
 * <li><b>先注册先匹配</b>：映射表不做优先级排序；装配方负责把更具体的模式先注册
 * （例如 <code>/u/version</code> 先于 <code>/u/**</code>）。</li>
 * <li><code>**</code> 匹配剩余 0 到 N 段：模式 <code>/a/login/**</code> 也命中
 * <code>/a/login</code>（此时捕获为空串）。</li>
 * <li>路径按 <code>/</code> 分段，忽略空段（连续斜杠/尾斜杠不产生空段）。</li>
 * <li>方法不匹配 = 未命中（对齐 Nutz：<code>@POST</code> 约束被 GET 请求视为无匹配，
 * 不产生 405），由调度器走未命中回退。</li>
 * </ul>
 */
public class WnMvcMapping {

    /**
     * 一条匹配结果（不可变值对象）：方法 + 模式 + 控制器类 + 前置/尾部过滤器。
     * <p>路径绑定结果不在这里：已直接写入请求的 <code>getPathParams()/getPathArgs()</code>。</p>
     */
    public static class Route {

        private final WnMvcHttpMethod[] methods;

        private final String pattern;

        private final Class<? extends WnMvcController> controllerType;

        private final WnMvcFilter[] headFilters;

        private final WnMvcFilter[] tailFilters;

        public Route(WnMvcHttpMethod[] methods,
                     String pattern,
                     Class<? extends WnMvcController> controllerType,
                     WnMvcFilter[] headFilters,
                     WnMvcFilter[] tailFilters) {
            this.methods = methods;
            this.pattern = pattern;
            this.controllerType = controllerType;
            this.headFilters = headFilters;
            this.tailFilters = tailFilters;
        }

        /** 本条路由声明的 HTTP 方法（数组，含 {@link WnMvcHttpMethod#ANY}）。 */
        public WnMvcHttpMethod[] methods() {
            return methods;
        }

        /** 命中的路径模式。 */
        public String pattern() {
            return pattern;
        }

        /** 控制器类（请求命中时按此类 born 新实例）。 */
        public Class<? extends WnMvcController> controllerType() {
            return controllerType;
        }

        /** 前置过滤器数组（按执行顺序；全局前置过滤器由 Dispatcher 前置）。 */
        public WnMvcFilter[] headFilters() {
            return headFilters;
        }

        /** 尾部过滤器数组（按执行顺序；作为 finally 收尾，前置失败也执行）。 */
        public WnMvcFilter[] tailFilters() {
            return tailFilters;
        }
    }

    // ======================================================================
    // 注册 API
    // ======================================================================

    /**
     * 注册一条路由（唯一注册入口）：显式声明 HTTP 方法数组、路径模式、控制器类、
     * 前置过滤器数组、尾部过滤器数组。
     *
     * @param methods
     *            HTTP 方法数组（多方法约束；不限方法用 {@link WnMvcHttpMethod#ANY}）
     * @param pattern
     *            路径模式（<code>{name}</code>/<code>?</code>/<code>*</code>/<code>**</code>）
     * @param controllerType
     *            控制器类（一个类一条请求逻辑，入口方法固定为
     *            {@link WnMvcController#handle(WnMvcContext)}）
     * @param headFilters
     *            前置过滤器数组（无则传空数组）
     * @param tailFilters
     *            尾部过滤器数组（无则传空数组；controller 之后执行，收尾保证）
     */
    public WnMvcMapping route(WnMvcHttpMethod[] methods,
                              String pattern,
                              Class<? extends WnMvcController> controllerType,
                              WnMvcFilter[] headFilters,
                              WnMvcFilter[] tailFilters) {
        if (null == methods || methods.length == 0)
            throw Wlang.makeThrow("route methods is empty: %s", pattern);
        if (null == pattern || !pattern.startsWith("/"))
            throw Wlang.makeThrow("route pattern must start with '/': %s", pattern);
        if (null == controllerType)
            throw Wlang.makeThrow("route controllerType is null: %s", pattern);
        Route r = new Route(methods.clone(),
                            pattern,
                            controllerType,
                            null == headFilters ? new WnMvcFilter[0] : headFilters.clone(),
                            null == tailFilters ? new WnMvcFilter[0] : tailFilters.clone());
        routes.add(r);
        return this;
    }

    // ======================================================================
    // 匹配 API（JETTY-03 实现并测试）
    // ======================================================================

    /**
     * 按 HTTP 方法 + 请求匹配一条路由。
     * <p>命中后完成路径绑定：<code>{name}</code> 捕获写入 <code>request.getPathParams()</code>，
     * <code>?</code>/<code>**</code> 捕获按序加入 <code>request.getPathArgs()</code>。</p>
     *
     * @param method
     *            HTTP 方法（枚举）
     * @param request
     *            中立请求（取其 getPath() 匹配，并向其写入路径绑定）
     * @return 匹配结果；未命中时返回 <code>null</code>
     */
    public Route match(WnMvcHttpMethod method, WnMvcRequest request) {
        String[] rsegs = __split_path(request.getPath());
        for (Route r : routes) {
            if (!__match_method(r.methods, method))
                continue;
            NutMap params = new NutMap();
            List<String> args = new ArrayList<>(4);
            if (__match_pattern(__split_path(r.pattern), rsegs, params, args)) {
                // 命中后才写入请求的活容器，避免部分匹配污染绑定结果
                request.getPathParams().putAll(params);
                request.getPathArgs().addAll(args);
                return r;
            }
        }
        return null;
    }

    /**
     * 是否存在能匹配的路由（不产生绑定开销的快速判定）。
     */
    public boolean has(WnMvcHttpMethod method, String path) {
        String[] rsegs = __split_path(path);
        for (Route r : routes) {
            if (!__match_method(r.methods, method))
                continue;
            if (__match_pattern(__split_path(r.pattern), rsegs, null, null))
                return true;
        }
        return false;
    }

    /**
     * 全部已注册的路径模式。
     */
    public List<String> patterns() {
        List<String> re = new ArrayList<>(routes.size());
        for (Route r : routes)
            re.add(r.pattern());
        return Collections.unmodifiableList(re);
    }

    /**
     * 已注册路由条数。
     */
    public int size() {
        return routes.size();
    }

    // ======================================================================
    // 内部实现
    // ======================================================================

    /**
     * 已注册路由（按注册顺序，先注册先匹配）。
     * <p>装配方负责把<b>更具体</b>的模式先注册（例如 <code>/u/version</code> 先于
     * <code>/u/**</code>）；映射表不做优先级排序。</p>
     */
    private final List<Route> routes = new ArrayList<>();

    /**
     * 把路径按 <code>/</code> 拆成段（忽略空段）。<code>"/"</code> 拆为空数组。
     */
    private static String[] __split_path(String path) {
        if (null == path || path.isEmpty() || "/".equals(path))
            return new String[0];
        List<String> segs = new ArrayList<>(8);
        int off = 0;
        int len = path.length();
        while (off < len) {
            int i = path.indexOf('/', off);
            if (i < 0)
                i = len;
            if (i > off)
                segs.add(path.substring(off, i));
            off = i + 1;
        }
        return segs.toArray(new String[0]);
    }

    /**
     * 方法约束：声明含 {@link WnMvcHttpMethod#ANY} 或显式包含请求方法即通过。
     */
    private static boolean __match_method(WnMvcHttpMethod[] declared, WnMvcHttpMethod method) {
        if (null == method)
            return false;
        for (WnMvcHttpMethod m : declared) {
            if (m == WnMvcHttpMethod.ANY || m == method)
                return true;
        }
        return false;
    }

    /**
     * 模式段匹配。命中时把绑定写入 params/args（可为 <code>null</code> 表示只判定不收集）。
     *
     * @param psegs
     *            模式段
     * @param rsegs
     *            请求路径段
     * @param params
     *            命名捕获收集（<code>{name}</code>），可为 <code>null</code>
     * @param args
     *            位置捕获收集（<code>?</code>/<code>**</code>），可为 <code>null</code>
     * @return 是否命中
     */
    private static boolean __match_pattern(String[] psegs,
                                           String[] rsegs,
                                           NutMap params,
                                           List<String> args) {
        int j = 0;
        for (int i = 0; i < psegs.length; i++) {
            String p = psegs[i];
            // `**`：匹配剩余全部段（含 0 段），按 `/` 拼接捕获；须在末段
            if ("**".equals(p)) {
                if (i != psegs.length - 1)
                    throw Wlang.makeThrow("path pattern '**' must be the last segment");
                if (null != args)
                    args.add(String.join("/", java.util.Arrays.asList(rsegs)
                                                        .subList(j, rsegs.length)));
                return true;
            }
            // 其余段要求请求还有剩余段
            if (j >= rsegs.length)
                return false;
            String r = rsegs[j++];
            // `?`：匹配一段并捕获
            if ("?".equals(p)) {
                if (null != args)
                    args.add(r);
                continue;
            }
            // `*`：匹配一段，不捕获
            if ("*".equals(p))
                continue;
            // `{name}`：匹配一段并按名捕获
            if (p.length() > 2 && p.startsWith("{") && p.endsWith("}")) {
                if (null != params)
                    params.put(p.substring(1, p.length() - 1), r);
                continue;
            }
            // 精确段
            if (!p.equals(r))
                return false;
        }
        // 模式用完，请求也必须刚好用完
        return j == rsegs.length;
    }

}
