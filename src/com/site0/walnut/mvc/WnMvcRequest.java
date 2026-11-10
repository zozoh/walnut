package com.site0.walnut.mvc;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.nutz.lang.util.NutMap;

/**
 * 中立 Web 请求：对 HTTP 请求的最小抽象，<b>不出现</b> <code>javax/jakarta.servlet</code>。
 *
 * <p>Jetty9（javax）与 Jetty12（jakarta）各自提供实现类（建议构造器直接接受原生 Servlet
 * 请求，由 {@link WnMvcFactory} 反射 born，见其 Javadoc）；测试用假请求实现同一接口。
 * 确需底层能力时用 {@link #unwrap(Class)} 逃生。</p>
 *
 * <h2>构造函数预解析约定（实现类必须遵守）</h2>
 * <p>实现类在<b>构造函数里预先解析好</b>：</p>
 * <ul>
 * <li>HTTP Head 部分：headers、Cookie（{@link #getHeaders()} / {@link #getCookieMap()}）。</li>
 * <li>query string（{@link #queryStringMap()}）。</li>
 * <li>路径分段（按 <code>/</code> 预拆好），并创建好 {@link #getPathParams()} /
 * {@link #getPathArgs()} 的<b>活容器</b>（NutMap / List）。</li>
 * </ul>
 * <p>路径绑定由 {@link WnMvcMapping#match} 在命中后直接向这两个活容器写入：
 * 声明 <code>{name}</code> 的写入 <code>getPathParams()</code>（<b>只有声明了名称才记录</b>），
 * 声明 <code>?</code> 的按出现顺序加入 <code>getPathArgs()</code>，<code>**</code> 的剩余段按
 * <code>/</code> 拼接后加入 <code>getPathArgs()</code>；<code>*</code> 只匹配不捕获。</p>
 *
 * <h2>请求体读取约定（构建时不动流）</h2>
 * <p>构造函数<b>不读取请求体</b>，body 读取全部惰性：</p>
 * <ul>
 * <li>{@link #getInputStream()}：原生请求体原始流；同一请求多次调用返回同一流，被消费后为空。</li>
 * <li>{@link #getReader()}：按请求 charset 惰性包装的 Reader。</li>
 * <li>{@link #getInputJson()}：把 input 全部读出为字符串（JSON 原文，不解析）。</li>
 * <li>{@link #getInputParams()}：按 form 提交（application/x-www-form-urlencoded）解析 body
 * 为参数表（实现类自建解析类）。</li>
 * <li>{@link #getUploadParts()}：按 multipart 上传表单解析（实现类自建解析类）。</li>
 * </ul>
 * <p>三个解析方法（{@code getInputJson/getInputParams/getUploadParts}）彼此互斥消费流，
 * 建议实现「首次调用读流并缓存结果、重复调用幂等」。</p>
 */
public interface WnMvcRequest {

    /**
     * HTTP 方法（大写），如 <code>GET</code>/<code>POST</code>/<code>PUT</code>。
     */
    String getMethod();

    /**
     * 请求路径（不含 query string）。
     */
    String getPath();

    /**
     * 原始 query string（<code>?</code> 之后部分，不含 <code>?</code>），可能为 <code>null</code>。
     */
    String getQueryString();

    /**
     * 解析后的 query string（构造函数里解析好）：保持顺序；单值 String、多值 String[]。
     */
    NutMap queryStringMap();

    /**
     * 路径参数表：<b>只有声明了名称（<code>{name}</code>）的捕获才记录</b>。
     * <p>返回的是活容器：路由匹配命中后由 {@link WnMvcMapping#match} 写入绑定结果。</p>
     */
    NutMap getPathParams();

    /**
     * 路径参数序列：<code>?</code> 按出现顺序、<code>**</code> 的余段按 <code>/</code> 拼接，
     * 依次加入本表。
     * <p>返回的是活容器：路由匹配命中后由 {@link WnMvcMapping#match} 写入绑定结果。</p>
     */
    List<String> getPathArgs();

    /**
     * 取一个请求头（构造函数里解析好）；键名建议统一大写。
     *
     * @param name
     *            请求头名
     * @return 值，可能为 <code>null</code>
     */
    String getHeader(String name);

    /**
     * 全部请求头（构造函数里解析好；name → 首个值）。
     */
    NutMap getHeaders();

    /**
     * 取一个请求 Cookie 的值（构造函数里解析好）。
     *
     * @param name
     *            Cookie 名
     * @return 值，可能为 <code>null</code>
     */
    String getCookie(String name);

    /**
     * 全部请求 Cookie（构造函数里解析好；name → value）。
     */
    NutMap getCookieMap();

    /**
     * 取一个请求属性（由动作链写入，例如 Host→域 路由、静态站点标记等）。
     *
     * @param name
     *            属性名
     * @return 值，可能为 <code>null</code>
     */
    Object getAttr(String name);

    /**
     * 全部请求属性。
     */
    NutMap getAttrs();

    /**
     * 请求体原始输入流（<b>构建时不动流</b>，惰性）。调用方负责关闭。
     *
     * @return 输入流，无请求体时可为 <code>null</code>
     */
    InputStream getInputStream();

    /**
     * 请求体 Reader（按请求 charset 惰性包装）。调用方负责关闭。
     */
    Reader getReader();

    /**
     * 把请求体全部读出为字符串（JSON 原文，不解析）。
     * <p>与 {@link #getInputParams()} / {@link #getUploadParts()} 互斥消费流。</p>
     *
     * @return 请求体文本，可能为空串
     */
    String getInputJson();

    /**
     * 按 form 提交解析请求体为参数表（实现类自建解析类）。
     * <p>值形态与 {@link #queryStringMap()} 一致（单值 String、多值 String[]）。</p>
     *
     * @return 参数表，无 body 时为空表
     */
    NutMap getInputParams();

    /**
     * 按 multipart 上传表单解析请求体（实现类自建解析类）。
     * <p>每个 part 的约定键：<code>name</code>（字段名）、<code>fileName</code>、
     * <code>contentType</code>、<code>size</code>、<code>inputStream</code>。</p>
     *
     * @return part 列表，无上传时为空表
     */
    List<NutMap> getUploadParts();

    /**
     * 请求的 Content-Type（不含 charset 亦可）。
     *
     * @return 值，可能为 <code>null</code>
     */
    String getContentType();

    /**
     * 客户端来源地址（经 X-Forwarded-For 等反代头解析后的 IP）。
     */
    String getRemoteAddr();

    /**
     * 客户端主机名（不经过反代头解析；实现可能触发反向 DNS，请按需调用）。
     * <p>JETTY-09 批准的最小新增（2026-11-10）：旧 <code>HttpServletRequest#getRemoteHost</code>
     * 的等价物，供 httpapi 填充 <code>http-remote-host</code> 元数据。</p>
     */
    String getRemoteHost();

    /**
     * 客户端来源端口。
     * <p>JETTY-09 批准的最小新增（2026-11-10）：旧 <code>HttpServletRequest#getRemotePort</code>
     * 的等价物，供 httpapi 填充 <code>http-remote-port</code> 元数据。</p>
     */
    int getRemotePort();

    /**
     * 请求 scheme，如 <code>http</code> / <code>https</code>。
     */
    String getScheme();

    /**
     * 请求 Host 头（不含端口）。
     */
    String getHost();

    /**
     * 逃生口：把底层「原生」请求按给定类型取出（例如 <code>HttpServletRequest</code>）。
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
