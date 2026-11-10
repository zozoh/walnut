package com.site0.walnut.mvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.tmpl.WnTmpl;

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
 * <p>具体失败视图渲染（错误页/错误码映射）由 RM-NUTZ-05 实现；cleanup 必须在 finally。</p>
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
        catch (WnMvcShortCircuitException e) {
            // 过滤器已写完响应（如 CORS 预检 204）：静默短路，不渲染失败视图、不打日志
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
     * <p><b>页面/API 区分（RM-NUTZ-05）</b>：一级段为 <code>o</code>/<code>u</code>/<code>api</code>
     * 的请求保持 JSON 错误体；其余页面请求输出 HTML 错误页（资源
     * <code>mvc/error/{status}.html</code>，TmplX 渲染；渲染异常回退最小 HTML）。</p>
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
        WnLog log = Wlog.getMAIN();
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

        String path = context.request().getPath();
        // 页面请求：HTML 错误页（RM-NUTZ-05；web2 由 CreateMvcContextFilter 打标）；
        // 未打标的库内单测保持 RM-NUTZ-03 的 JSON 断言（兼容既有 64 用例）
        if (!isApiPath(path) && __is_page_error(context)) {
            try {
                context.response().status(status);
                WnMvcView.html(errorHtml(status, err.getMessage(), path)).render(context);
            }
            catch (Throwable e2) {
                try {
                    context.response().sendError(status);
                }
                catch (Throwable e3) {
                    log.warn("WnMvcChain: fail to render error page", e2);
                }
            }
            return;
        }

        // API 请求：保持 JSON 错误体
        NutMap body = new NutMap();
        body.put("ok", false);
        if (null != errCode)
            body.put("errCode", errCode);
        body.put("msg", err.getMessage());
        WnMvcView.error(status, body).render(context);
    }

    // ======================================================================
    // 错误页渲染（RM-NUTZ-05；WnMvcDispatcher.__fallback 也复用）
    // ======================================================================

    /**
     * 一级段是否为 API 前缀（<code>o</code>/<code>u</code>/<code>api</code>）。
     */
    static boolean isApiPath(String path) {
        String seg0 = __first_segment(path);
        return "o".equals(seg0) || "u".equals(seg0) || "api".equals(seg0);
    }

    /**
     * 是否按页面错误体渲染：由 web2 的上下文过滤器打标
     * <code>wn.mvc.error.page=true</code>；库内旧单测未打标时保持 RM-NUTZ-03 JSON 行为。
     */
    private static boolean __is_page_error(WnMvcContext context) {
        if (null == context.attrs())
            return false;
        return Boolean.TRUE.equals(context.attrs().get("wn.mvc.error.page"));
    }

    /**
     * 渲染错误页 HTML：读 <code>mvc/error/{status}.html</code>，用 TmplX（{@link WnTmpl}）
     * 注入 status/title/message/path；资源缺失或渲染异常时回退最小静态 HTML。
     */
    static String errorHtml(int status, String message, String path) {
        String title = __error_title(status);
        String text = null;
        try (InputStream in = WnMvcChain.class.getResourceAsStream("/com/site0/walnut/mvc/error/"
                                                                    + status
                                                                    + ".html")) {
            if (null != in)
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Throwable e) {
            // 资源读取失败 → 回退最小 HTML
        }
        if (null != text) {
            try {
                NutMap vars = new NutMap();
                vars.put("status", status);
                vars.put("title", title);
                vars.put("message", null == message ? title : message);
                vars.put("path", null == path ? "" : path);
                return WnTmpl.parse(text).render(vars);
            }
            catch (Throwable e) {
                // 渲染失败 → 回退最小 HTML
            }
        }
        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>"
               + status
               + " "
               + __escape(title)
               + "</title></head><body><h1>"
               + status
               + " "
               + __escape(title)
               + "</h1><p>"
               + __escape(null == message ? "" : message)
               + "</p><p>"
               + __escape(null == path ? "" : path)
               + "</p></body></html>";
    }

    private static String __first_segment(String path) {
        if (null == path || path.length() <= 1)
            return "";
        int off = path.startsWith("/") ? 1 : 0;
        int i = path.indexOf('/', off);
        return i < 0 ? path.substring(off) : path.substring(off, i);
    }

    private static String __error_title(int status) {
        switch (status) {
        case 400:
            return "Bad Request";
        case 401:
            return "Unauthorized";
        case 403:
            return "Forbidden";
        case 404:
            return "Not Found";
        case 500:
            return "Internal Server Error";
        default:
            return "Error";
        }
    }

    private static String __escape(String s) {
        if (null == s)
            return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}
