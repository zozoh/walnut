package com.site0.walnut.mvc;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.util.Wlang;

/**
 * WnMvc 装配工厂：负责服务装配 + 请求/响应实现类装配，并产出
 * {@link WnMvcDispatcher}。实例由启动装配（JETTY-05）创建并持有，<b>无全局静态入口</b>。
 *
 * <h2>职责</h2>
 * <ul>
 * <li>服务装配：持有服务集合（{@link WnServiceHolder}）、{@link WnIo}、{@link MimeMap}
 * （对应旧 Nutz Ioc 的 conf/io/service 装配，JETTY-04 完成替换）。</li>
 * <li>路由装配：持有全局 {@link WnMvcMapping}（{@link #mapping()}），装配代码直接
 * {@link WnMvcMapping#route} 注册「控制器类 + 过滤器」；路由表<b>不存控制器实例、不存方法名</b>
 * （入口方法固定为 {@link WnMvcController#handle(WnMvcContext)}）。</li>
 * <li>请求/响应实现类装配：注册 {@link WnMvcRequest}/{@link WnMvcResponse} 的<b>实现类</b>
 * （{@link #requestType(Class)}/{@link #responseType(Class)}），每请求由
 * {@link #bornRequest(Object)}/{@link #bornResponse(Object)} 反射 born。</li>
 * </ul>
 *
 * <h2>Borning 与容器切换</h2>
 * <p>borning 采用反射（参考 <code>Mirror.getBorningByArgTypes</code>；本任务不实现）：</p>
 * <ul>
 * <li>控制器：无参构造器（实例不注入服务，服务经 {@link WnMvcContext} 取用）；
 * <b>每请求命中路由时 born 一个新实例</b>，即用即弃。</li>
 * <li>请求/响应实现类：构造器直接接受原生 Servlet 对象——Jetty9 实现类构造器收
 * <code>javax.servlet.http.HttpServletRequest/Response</code>；换 Jetty12 时<b>只改工厂的
 * requestType/responseType 注册与这两个实现类</b>，controller 与业务代码零改动。</li>
 * </ul>
 *
 * <h2>线程模型 / 生命周期</h2>
 * <p>工厂实例在装配期由启动装配（JETTY-05）创建并持有，传给 {@link WnMvcDispatcher}；
 * <b>不提供全局静态入口</b>（服务获取一律经每请求的 {@link WnMvcContext}，见「能力门」）。
 * 装配期一次性写入服务与「类」，之后只读；运行期每请求 born
 * 新控制器/新请求/新响应实例，不持有任何请求态。</p>
 *
 * <h2>能力门</h2>
 * <p>{@link #newContext(WnMvcRequest, WnMvcResponse)} 是服务进入请求上下文的唯一装配点：
 * 它把 services/io/mimes 注入 {@link WnMvcContext}（包私有 setter），包外无法自造带服务的上下文。</p>
 */
public class WnMvcFactory {

    private WnServiceHolder services;

    private WnIo io;

    private MimeMap mimes;

    private WnMvcMapping mapping;

    private Class<? extends WnMvcRequest> requestType;

    private Class<? extends WnMvcResponse> responseType;

    // ======================================================================
    // 服务装配（装配期一次性调用）
    // ======================================================================

    /** 设置服务集合。 */
    public WnMvcFactory services(WnServiceHolder services) {
        this.services = services;
        return this;
    }

    /** 设置 IO 能力。 */
    public WnMvcFactory io(WnIo io) {
        this.io = io;
        return this;
    }

    /** 设置 MIME 映射。 */
    public WnMvcFactory mimes(MimeMap mimes) {
        this.mimes = mimes;
        return this;
    }

    /**
     * 服务集合。
     */
    public WnServiceHolder services() {
        return services;
    }

    /**
     * IO 能力。
     */
    public WnIo io() {
        return io;
    }

    /**
     * MIME 映射。
     */
    public MimeMap mimes() {
        return mimes;
    }

    // ======================================================================
    // 路由与请求/响应实现类装配（装配期一次性调用）
    // ======================================================================

    /**
     * 全局路由表（装配代码在此表上 {@link WnMvcMapping#route} 注册路由，
     * 装配完成后只读）。
     */
    public WnMvcMapping mapping() {
        if (null == mapping)
            mapping = new WnMvcMapping();
        return mapping;
    }

    /**
     * 注册 {@link WnMvcRequest} 的实现类（Jetty9：构造器接受
     * <code>javax.servlet.http.HttpServletRequest</code>）。
     */
    public WnMvcFactory requestType(Class<? extends WnMvcRequest> type) {
        this.requestType = type;
        return this;
    }

    /**
     * 注册 {@link WnMvcResponse} 的实现类（Jetty9：构造器接受
     * <code>javax.servlet.http.HttpServletResponse</code>）。
     */
    public WnMvcFactory responseType(Class<? extends WnMvcResponse> type) {
        this.responseType = type;
        return this;
    }

    // ======================================================================
    // Borning：每请求调用（反射 born，参考 Mirror.getBorningByArgTypes）
    // ======================================================================

    /**
     * born 一个控制器新实例（无参构造器）。每请求命中路由时调用。
     */
    public WnMvcController bornController(Class<? extends WnMvcController> controllerType) {
        try {
            java.lang.reflect.Constructor<? extends WnMvcController> c = controllerType.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        }
        catch (ReflectiveOperationException e) {
            throw Wlang.makeThrow("fail to born controller: %s", controllerType.getName());
        }
    }

    /**
     * born 一个中立请求对象：用注册的实现类、以原生请求为构造参数反射 born。
     *
     * @param nativeRequest
     *            原生请求对象（如 <code>HttpServletRequest</code>）
     */
    public WnMvcRequest bornRequest(Object nativeRequest) {
        if (null == requestType)
            throw Wlang.makeThrow("WnMvcFactory.requestType not registered");
        return __born_by_arg(requestType, nativeRequest);
    }

    /**
     * born 一个中立响应对象：用注册的实现类、以原生响应为构造参数反射 born。
     *
     * @param nativeResponse
     *            原生响应对象（如 <code>HttpServletResponse</code>）
     */
    public WnMvcResponse bornResponse(Object nativeResponse) {
        if (null == responseType)
            throw Wlang.makeThrow("WnMvcFactory.responseType not registered");
        return __born_by_arg(responseType, nativeResponse);
    }

    /**
     * 找一个单参构造器（参数类型接受原生对象）反射 born。
     */
    private static <T> T __born_by_arg(Class<T> type, Object arg) {
        for (java.lang.reflect.Constructor<?> c : type.getDeclaredConstructors()) {
            Class<?>[] pts = c.getParameterTypes();
            if (pts.length == 1 && (null == arg || pts[0].isInstance(arg))) {
                try {
                    c.setAccessible(true);
                    return type.cast(c.newInstance(arg));
                }
                catch (ReflectiveOperationException e) {
                    throw Wlang.makeThrow("fail to born %s with arg %s",
                                          type.getName(),
                                          null == arg ? "null" : arg.getClass().getName());
                }
            }
        }
        throw Wlang.makeThrow("no 1-arg constructor on %s accepts %s",
                              type.getName(),
                              null == arg ? "null" : arg.getClass().getName());
    }

    // ======================================================================
    // 产出与每请求上下文
    // ======================================================================

    /**
     * 装配完成，产出调度器（路由匹配 + born 控制器 + 动作链执行 + 未命中回退）。
     * <p>产出的调度器<b>不带全局过滤器、不带静态文件服务</b>；需要二者时直接用
     * {@link WnMvcDispatcher#WnMvcDispatcher(WnMvcMapping, WnMvcFactory, WnMvcFilter[], WnMvcFilter[], WnMvcStaticService)}
     * 装配（JETTY-05）。</p>
     */
    public WnMvcDispatcher build() {
        return new WnMvcDispatcher(mapping(), this, new WnMvcFilter[0], new WnMvcFilter[0]);
    }

    /**
     * 创建一个每请求上下文，并把 services/io/mimes 注入（能力门装配点）。
     *
     * @param request
     *            中立请求
     * @param response
     *            中立响应
     * @return 新上下文（session 由动作链后续恢复）
     */
    public WnMvcContext newContext(WnMvcRequest request, WnMvcResponse response) {
        WnMvcContext ctx = new WnMvcContext();
        ctx.setRequest(request);
        ctx.setResponse(response);
        ctx.setAttrs(new java.util.HashMap<String, Object>());
        ctx.setServices(services);
        ctx.setIo(io);
        ctx.setMimes(mimes);
        return ctx;
    }

}
