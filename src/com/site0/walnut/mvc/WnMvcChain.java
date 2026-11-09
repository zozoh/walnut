package com.site0.walnut.mvc;

import org.nutz.log.Log;

import com.site0.walnut.util.Wlog;

/**
 * 动作链执行器：用<b>共享上下文的循环</b>驱动过滤器与 controller，最后进入失败视图。
 *
 * <h2>执行模型（单步调试友好）</h2>
 *
 * <pre>
 * // 1) 前置过滤器（headFilters）：不抛异常 = 通过；抛异常 → fail + 中断（fail-closed）
 * for (WnMvcFilter flt : headFilters) {
 *     try {
 *         flt.doFilter(context);
 *     }
 *     catch (Throwable e) {
 *         fail(context, e);
 *         return null;              // 不进 controller
 *     }
 * }
 * // 2) controller 请求逻辑
 * WnMvcView view = action.invoke(context);
 * // 3) 尾部过滤器（tailFilters）：finally 收尾保证，前置失败也执行
 * for (WnMvcFilter flt : tailFilters) {
 *     try {
 *         flt.doFilter(context);
 *     }
 *     catch (Throwable e) {
 *         // 收尾失败：记日志；尚未进失败视图且响应未提交时，按异常状态渲染失败视图
 *     }
 * }
 * return view;
 * </pre>
 *
 * <h2>执行顺序</h2>
 * <p>链只按给定数组顺序执行：headFilters → controller → tailFilters。两个数组<b>如何组合</b>
 * （全局过滤器与路由级过滤器的先后）由 {@link WnMvcDispatcher} 构造链时决定，
 * 链本身不关心过滤器来自哪里。</p>
 *
 * <p>具体失败视图渲染（错误页/错误码映射）由 JETTY-05 实现；cleanup 必须在 finally。</p>
 *
 * <p><b>生命周期</b>：每个请求由 {@link WnMvcDispatcher} 构造一条链实例，携带该请求匹配到的
 * 前置/尾部过滤器数组与终止动作（controller.handle）；链实例仅本请求使用。</p>
 */
public class WnMvcChain {

    /**
     * 终止动作：前置过滤器全部通过后执行的 controller 请求逻辑。
     */
    @FunctionalInterface
    public interface Action {

        /**
         * 执行请求逻辑。
         *
         * @param context
         *            每请求上下文
         * @return 视图；<code>null</code> 表示响应已直接写出
         */
        WnMvcView invoke(WnMvcContext context);
    }

    private final WnMvcFilter[] headFilters;

    private final WnMvcFilter[] tailFilters;

    private final Action action;

    /**
     * 构造一条请求链。
     *
     * @param headFilters
     *            前置过滤器数组（已按执行顺序组装好）
     * @param tailFilters
     *            尾部过滤器数组（已按执行顺序组装好；作为 finally 收尾，前置失败也执行）
     * @param action
     *            终止动作（controller 的 handle）
     */
    public WnMvcChain(WnMvcFilter[] headFilters,
                      WnMvcFilter[] tailFilters,
                      Action action) {
        this.headFilters = headFilters;
        this.tailFilters = tailFilters;
        this.action = action;
    }

    /**
     * 前置过滤器数组。
     */
    public WnMvcFilter[] headFilters() {
        return headFilters;
    }

    /**
     * 尾部过滤器数组（收尾保证：前置失败也执行）。
     */
    public WnMvcFilter[] tailFilters() {
        return tailFilters;
    }

    /**
     * 终止动作。
     */
    public Action action() {
        return action;
    }

    /**
     * 执行链：前置过滤器循环（不抛异常 = 通过）→ controller 请求逻辑 → 尾部过滤器循环
     * （finally 收尾，无论成功失败都执行）。
     *
     * @param context
     *            每请求上下文
     * @return controller 返回的视图（交由调度器渲染）；前置过滤器异常中断时为 <code>null</code>
     */
    public WnMvcView invoke(WnMvcContext context) {
        WnMvcView view = null;
        // 是否已进入失败视图（失败视图渲染后不再返回 controller 视图）
        boolean failed = false;
        try {
            // 1) 前置过滤器：不抛异常 = 通过；抛异常 → fail + 中断（fail-closed）
            for (WnMvcFilter flt : headFilters)
                flt.doFilter(context);
            // 2) controller 请求逻辑；controller 异常同样走失败视图
            view = action.invoke(context);
        }
        catch (Throwable e) {
            failed = true;
            fail(context, e);
        }
        finally {
            // 3) 尾部过滤器：finally 收尾保证，前置/controller 失败也执行
            for (WnMvcFilter flt : tailFilters) {
                try {
                    flt.doFilter(context);
                }
                catch (Throwable e) {
                    Wlog.getMAIN().warn("WnMvcChain: tail filter failed", e);
                    // 收尾失败：尚未进失败视图且响应未提交时，按异常状态渲染失败视图
                    if (!failed && !context.response().committed()) {
                        failed = true;
                        fail(context, e);
                    }
                }
            }
        }
        return failed ? null : view;
    }

    /**
     * 失败分支：把异常记录日志并渲染为失败视图（fail-closed）。
     * <p>{@link WnMvcFilterException} 携带的状态码/错误码决定失败视图的 HTTP 状态与错误体；
     * 其他异常一律 500。响应已提交时只记日志不再渲染。</p>
     * <p><b>页面/API 的错误体区分（错误页/TmplX）由 JETTY-05 细化</b>，本实现统一输出
     * JSON 错误体（<code>{"ok":false,"errCode":...,"msg":...}</code>）。</p>
     *
     * @param context
     *            每请求上下文
     * @param err
     *            异常（通常是 {@link WnMvcFilterException}）
     */
    public void fail(WnMvcContext context, Throwable err) {
        int status = 500;
        String errCode = null;
        if (err instanceof WnMvcFilterException) {
            WnMvcFilterException fe = (WnMvcFilterException) err;
            status = fe.getStatus();
            errCode = fe.getErrCode();
        }
        Log log = Wlog.getMAIN();
        if (status >= 500)
            log.warn(String.format("WnMvcChain: request failed @%s", context.request().getPath()),
                     err);
        else
            log.warnf("WnMvcChain: request rejected(%d) @%s : %s",
                      status,
                      context.request().getPath(),
                      err.getMessage());
        // 响应已提交：无法再渲染失败视图
        if (context.response().committed())
            return;
        org.nutz.lang.util.NutMap body = new org.nutz.lang.util.NutMap();
        body.put("ok", false);
        if (null != errCode)
            body.put("errCode", errCode);
        body.put("msg", err.getMessage());
        WnMvcView.error(status, body).render(context);
    }

}
