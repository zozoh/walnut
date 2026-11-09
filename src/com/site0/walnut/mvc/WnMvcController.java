package com.site0.walnut.mvc;

/**
 * Web 控制器：<b>一个控制器类 = 一条请求逻辑</b>，只有一个入口方法 {@link #handle(WnMvcContext)}。
 *
 * <h2>职责</h2>
 * <p>{@link #handle(WnMvcContext)} 接收每请求上下文，从 request / response 读取输入，
 * 返回 {@link WnMvcView} 描述输出；或直接写 response 后返回 <code>null</code>（即「void」视图）。</p>
 *
 * <h2>线程模型 / 生命周期</h2>
 * <ul>
 * <li>路由表只存<b>控制器类 + 过滤器</b>（见 {@link WnMvcMapping#route}），不存实例、不存方法名。</li>
 * <li>请求命中路由时，{@link WnMvcFactory} born 一个<b>新控制器实例</b>调用 {@link #handle}，
 * 即用即弃；实例不持有服务字段、不持有请求态，全部数据来自每请求的 {@link WnMvcContext}。</li>
 * </ul>
 *
 * <h2>示例</h2>
 *
 * <pre>
 * public class AppLoginController extends AbstractWnController {
 *     public WnMvcView handle(WnMvcContext ctx) {
 *         String rph = ctx.pathArg(0); // `**` 余段（按序进 pathArgs）
 *         return WnMvcView.html("&lt;html&gt;...&lt;/html&gt;");
 *     }
 * }
 * // 注册（全部走 route）：
 * m.route(new WnMvcHttpMethod[]{ WnMvcHttpMethod.GET },
 *         "/a/login/**",
 *         AppLoginController.class,
 *         new WnMvcFilter[]{}, // headFilters
 *         new WnMvcFilter[]{}); // tailFilters
 * </pre>
 *
 * @see AbstractWnController
 * @see WnMvcMapping
 */
@FunctionalInterface
public interface WnMvcController {

    /**
     * 本条路由的请求逻辑。
     *
     * @param context
     *            每请求上下文
     * @return 视图；<code>null</code> 表示响应已直接写出
     */
    WnMvcView handle(WnMvcContext context);

}
