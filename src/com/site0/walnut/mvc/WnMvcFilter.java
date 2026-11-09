package com.site0.walnut.mvc;

/**
 * 动作链节点（过滤器）：在 controller 前后做拦截，对应旧 Nutz 的 <code>ActionFilter</code>
 * 与动作链 processor。
 *
 * <p>同一接口用于两组过滤：<b>前置过滤器</b>（controller 之前，失败即中断）与
 * <b>尾部过滤器</b>（controller 之后，作为 finally 收尾，前置失败也执行）。</p>
 *
 * <h2>通过/拦截语义</h2>
 * <p><b>返回值为 void：不抛异常即通过。</b>拦截失败（无会话、无权限等）抛出专有异常
 * {@link WnMvcFilterException}，由 {@link WnMvcChain} 捕获后记日志、按异常携带的状态码渲染
 * 失败视图并中断（fail-closed，不进入 controller）。</p>
 *
 * <p>执行顺序（主题 README §4.2）：<code>请求 → HeadFilter(全局 → 路由) → controller →
 * TailFilter(路由 → 全局)</code>；HeadFilter 负责创建上下文/放入 services → 编码 → 会话/安全，
 * TailFilter 负责 view/cleanup；cleanup 必须在 finally。</p>
 *
 * <p><b>线程模型</b>：过滤器实例<b>单例、无状态</b>；实现体不得保存请求态，全部状态放进
 * {@link WnMvcContext}。</p>
 *
 * <h2>示例</h2>
 *
 * <pre>
 * public class CheckSession implements WnMvcFilter {
 *     public void doFilter(WnMvcContext ctx) {
 *         // 无会话：抛专有异常（401），链捕获后走失败视图
 *         if (ctx.session() == null)
 *             throw new WnMvcFilterException(401, "e.se.noexists", "no session");
 *         // 不抛异常 = 通过
 *     }
 * }
 * </pre>
 */
@FunctionalInterface
public interface WnMvcFilter {

    /**
     * 执行本节点逻辑。
     * <p><b>不抛出异常即视为通过</b>；拦截失败抛 {@link WnMvcFilterException}
     * （携带 HTTP 状态码与业务错误码）。</p>
     *
     * @param context
     *            每请求上下文
     */
    void doFilter(WnMvcContext context);

}
