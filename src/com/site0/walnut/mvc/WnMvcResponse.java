package com.site0.walnut.mvc;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * 中立 Web 响应：对 HTTP 响应的最小抽象，<b>不出现</b> <code>javax/jakarta.servlet</code>。
 *
 * <p>Jetty9（javax）与 Jetty12（jakarta）各自提供适配实现；测试用假响应实现同一接口。
 * 视图（{@link WnMvcView}）通过本接口写出 status / headers / cookies / body。
 * 确需底层能力时用 {@link #unwrap(Class)} 逃生。</p>
 *
 * <h2>状态模型</h2>
 * <p>响应一旦开始写出正文即 <b>committed</b>（{@link #committed()} 返回 <code>true</code>），
 * 此后不得再改 status / headers。写正文前应先设置好 status 与 headers。</p>
 */
public interface WnMvcResponse {

    /**
     * 当前 HTTP 状态码；未显式设置时为 200。
     */
    int status();

    /**
     * 设置 HTTP 状态码。committed 后调用无效或抛错（由实现决定）。
     */
    void status(int code);

    /**
     * 取一个已设置的响应头。
     *
     * @param name
     *            响应头名
     * @return 值，可能为 <code>null</code>
     */
    String header(String name);

    /**
     * 设置一个响应头（覆盖同名）。committed 后调用无效或抛错（由实现决定）。
     */
    void header(String name, String value);

    /**
     * 追加一个响应头（同名不覆盖，用于 <code>Set-Cookie</code> 等多值头）。
     */
    void addHeader(String name, String value);

    /**
     * 全部已设置的响应头（name → 首个值）。
     */
    Map<String, String> headers();

    /**
     * 设置 Content-Type。
     */
    void contentType(String contentType);

    /**
     * 添加一个响应 Cookie（完整属性）。
     *
     * @param cookie
     *            不可变 Cookie 值对象
     */
    void cookie(WnMvcCookie cookie);

    /**
     * 添加一个简单响应 Cookie（<code>Path=/</code>，会话期）。
     */
    void cookie(String name, String value);

    /**
     * 删除一个 Cookie（等价设置同名过期 Cookie）。
     */
    void removeCookie(String name);

    /**
     * 响应正文输出流。调用方负责 flush/close。
     */
    OutputStream outputStream();

    /**
     * 响应正文 Writer（文本输出）。
     */
    Writer writer();

    /**
     * 是否已提交（正文已开始写出）。
     */
    boolean committed();

    /**
     * 刷新缓冲。
     */
    void flush();

    /**
     * 设置响应体缓冲大小（等价 Servlet <code>setBufferSize</code>）；须在正文写出前调用。
     * <p>RM-NUTZ-09 批准的最小新增（2026-11-10）：旧 httpapi <code>http-resp-buff-size</code>
     * 元数据用。</p>
     */
    void setBufferSize(int size);

    /**
     * 复位响应（等价 Servlet <code>reset</code>）：清除 status / headers 与缓冲正文，
     * 回到未提交状态；已 committed 时行为由实现决定（通常抛错）。
     * <p>RM-NUTZ-09 批准的最小新增（2026-11-10）：动态渲染 errOut 分支用。</p>
     */
    void reset();

    /**
     * 302 重定向。
     *
     * @param location
     *            目标 URL
     */
    void sendRedirect(String location);

    /**
     * 以给定状态码 + 默认错误体写出错误响应。
     */
    void sendError(int code);

    /**
     * 以给定状态码 + 消息写出错误响应。
     */
    void sendError(int code, String message);

    /**
     * 逃生口：把底层「原生」响应按给定类型取出（例如 <code>HttpServletResponse</code>）。
     * <p>仅 <code>HttpApiModule</code> 等确需底层能力的场景使用。</p>
     *
     * @param <T>
     *            目标类型
     * @param type
     *            目标类型
     * @return 底层对象，不支持时为 <code>null</code>
     */
    <T> T unwrap(Class<T> type);

}
