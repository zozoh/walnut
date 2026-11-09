package com.site0.walnut.mvc;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.session.WnSession;

/**
 * 一次 Web 请求的上下文：聚合中立请求/响应、会话、请求属性与服务能力。
 *
 * <h2>线程模型 / 生命周期</h2>
 * <p><b>每请求创建一次</b>，仅存活于本次请求的处理线程内，不跨线程共享、不可复用。
 * 由动作链（或 {@link WnMvcFactory}/{@link WnMvcDispatcher}）在请求开始时创建、
 * 在请求结束时丢弃。</p>
 *
 * <h2>能力门（services / io / mimes）</h2>
 * <p>服务能力（{@link #services()}、{@link #io()}、{@link #mimes()}）只能由
 * <b>本包内的动作链 / 工厂 / 调度器</b>通过包私有 setter 注入；包外代码（含具体控制器）只能读取，
 * 无法自造上下文以获取服务。因此没有上下文的线程拿不到服务（fail-closed）。</p>
 *
 * <h2>路径参数（pathParams / pathArgs）</h2>
 * <p>本类不保存路径绑定：{@link #pathParams()} / {@link #pathArgs()} 直接透传
 * {@link WnMvcRequest#getPathParams()} / {@link WnMvcRequest#getPathArgs()}——绑定结果由
 * {@link WnMvcMapping#match} 在命中后写入请求（<code>{name}</code> 进 pathParams、
 * <code>?</code>/<code>**</code> 按序进 pathArgs）。</p>
 */
public class WnMvcContext {

    private WnMvcRequest request;

    private WnMvcResponse response;

    private WnSession session;

    private Map<String, Object> attrs;

    private WnServiceHolder services;

    private WnIo io;

    private MimeMap mimes;

    /**
     * 中立请求对象（method/path/headers/queryStringMap/cookies/body），不含 Servlet API。
     */
    public WnMvcRequest request() {
        return request;
    }

    /**
     * 中立响应对象（status/headers/cookies/outputStream），不含 Servlet API。
     */
    public WnMvcResponse response() {
        return response;
    }

    /**
     * 当前会话，可能为 <code>null</code>（未登录 / 尚未恢复票据）。
     */
    public WnSession session() {
        return session;
    }

    /**
     * 请求属性（由动作链写入，例如 Host→域 路由、静态站点标记等）。
     * <p>返回的 Map 可读可写；写入应遵循旧 <code>@Attr</code> 的语义（简单值）。</p>
     */
    public Map<String, Object> attrs() {
        return attrs;
    }

    /**
     * 路径参数表：<b>声明了名称（<code>{name}</code>）的捕获</b>（透传请求的
     * {@link WnMvcRequest#getPathParams()}）。
     */
    public NutMap pathParams() {
        return null == request ? null : request.getPathParams();
    }

    /**
     * 取一个命名路径参数。
     *
     * @param name
     *            声明在路径模式里的名称，如 <code>"appName"</code>
     * @return 捕获值，可能为 <code>null</code>
     */
    public Object pathParam(String name) {
        NutMap pp = pathParams();
        return null == pp ? null : pp.get(name);
    }

    /**
     * 路径参数序列：<code>?</code> 按出现顺序、<code>**</code> 余段（按 <code>/</code> 拼接）
     * （透传请求的 {@link WnMvcRequest#getPathArgs()}）。
     */
    public List<String> pathArgs() {
        return null == request ? null : request.getPathArgs();
    }

    /**
     * 取一个按序路径参数。
     *
     * @param index
     *            0 起始的捕获序号
     * @return 捕获值，可能为 <code>null</code>
     */
    public String pathArg(int index) {
        List<String> args = pathArgs();
        if (null != args && index >= 0 && index < args.size())
            return args.get(index);
        return null;
    }

    /**
     * 服务集合（能力门）：{@link WnServiceHolder}（由用户完成的 IDE 改名而来）。
     * <p>只能由动作链 / 工厂设置，包外代码只读。</p>
     */
    public WnServiceHolder services() {
        return services;
    }

    /**
     * IO 能力（对象树读写）。只能由动作链 / 工厂设置。
     */
    public WnIo io() {
        return io;
    }

    /**
     * MIME 映射。只能由动作链 / 工厂设置。
     */
    public MimeMap mimes() {
        return mimes;
    }

    /**
     * 逃生口：把底层「原生」能力按给定类型取出（例如 Jetty9 的
     * <code>HttpServletRequest</code> / Jetty12 的 <code>jakarta.servlet...Request</code>）。
     * <p>仅 <code>HttpApiModule</code> 等确需底层能力的场景使用；优先取 request、再取 response。</p>
     *
     * @param <T>
     *            目标类型
     * @param type
     *            目标类型
     * @return 底层对象，不支持时为 <code>null</code>
     */
    public <T> T unwrap(Class<T> type) {
        if (null != request) {
            T t = request.unwrap(type);
            if (null != t)
                return t;
        }
        if (null != response) {
            return response.unwrap(type);
        }
        return null;
    }

    // ======================================================================
    // 以下为能力门 setter：包私有，仅允许本包内的动作链 / 工厂 / 调度器调用。
    // ======================================================================

    void setRequest(WnMvcRequest request) {
        this.request = request;
    }

    void setResponse(WnMvcResponse response) {
        this.response = response;
    }

    /**
     * 设置当前会话（供动作链过滤器在票据恢复后写入）。
     * <p>2026-11-10 用户批准为第 3 处 mvc 改动：由包私有改为 public，使
     * {@code web2.filter} 能写入会话（原 Javadoc 已约定「session 由动作链后续恢复」）。</p>
     */
    public void setSession(WnSession session) {
        this.session = session;
    }

    void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    void setServices(WnServiceHolder services) {
        this.services = services;
    }

    void setIo(WnIo io) {
        this.io = io;
    }

    void setMimes(MimeMap mimes) {
        this.mimes = mimes;
    }

}
