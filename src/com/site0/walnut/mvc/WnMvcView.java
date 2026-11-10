package com.site0.walnut.mvc;

import java.io.InputStream;

/**
 * 中立 Web 视图：把控制器入口方法返回的结果渲染进 {@link WnMvcContext#response()}。
 *
 * <p>控制器方法返回 {@link WnMvcView} 描述输出；返回 <code>null</code> 等价于「void」视图
 * （响应已直接写出）。视图类型对应旧 Nutz 视图（见 inventory §3.8）：</p>
 * <ul>
 * <li>{@link #json(Object)}：JSON 输出（对应 <code>@Ok("json")</code>）。</li>
 * <li>{@link #ajax(Object)}：Ajax 信封输出（对应 <code>@Ok("ajax")</code> / AjaxView）。</li>
 * <li>{@link #raw}：原始字节/流输出（对应 <code>@Ok("raw")</code>）。</li>
 * <li>{@link #download}：带 Content-Disposition 的下载（对应 WnObjDownloadView）。</li>
 * <li>{@link #redirect(String)}：302 重定向（对应 ServerRedirectView / <code>&gt;&gt;:/</code>）。</li>
 * <li>{@link #html(String)}：HTML 文本输出。</li>
 * <li>{@link #none()}：void 视图（响应已由控制器直接写出）。</li>
 * <li>{@link #error(int)} / {@link #error(int, Object)}：错误视图（对应 <code>http:NNN</code>）。</li>
 * </ul>
 *
 * <p><b>线程模型</b>：视图实例无状态，可被多请求并发渲染（渲染状态全部落到
 * {@link WnMvcContext}）。<b>RM-NUTZ-09</b> 会详细化视图路由与 Cookie 视图（
 * <code>++cookie&gt;&gt;:</code> / <code>--cookie&gt;&gt;:</code> / <code>++cookie-&gt;ajax</code>）。</p>
 */
public interface WnMvcView {

    /**
     * 把本视图渲染进上下文的响应。
     *
     * @param context
     *            每请求上下文
     */
    void render(WnMvcContext context);

    // ======================================================================
    // 静态工厂：各视图类型一个工厂方法（实现见包私有类 {@link WnMvcViews}）。
    // ======================================================================

    /** JSON 视图：把数据序列化为 JSON 写出（Content-Type: application/json）。 */
    static WnMvcView json(Object data) {
        return new WnMvcViews.JsonView(data);
    }

    /** Ajax 视图：把数据包成 Ajax 信封写出（对应旧 AjaxView）。 */
    static WnMvcView ajax(Object data) {
        return new WnMvcViews.AjaxView(data);
    }

    /** Ajax 失败视图：以错误码/消息写出失败信封。 */
    static WnMvcView ajax(String errCode, String message) {
        return new WnMvcViews.AjaxView(errCode, message);
    }

    /** 原始字节视图：按给定 Content-Type 写出字节。 */
    static WnMvcView raw(byte[] data, String contentType) {
        return new WnMvcViews.RawView(data, contentType);
    }

    /** 原始流视图：按给定 Content-Type 写出输入流（不关闭流）。 */
    static WnMvcView raw(InputStream in, String contentType) {
        return new WnMvcViews.RawView(in, contentType);
    }

    /** 下载视图：写出带 Content-Disposition(attachment) 的字节。 */
    static WnMvcView download(byte[] data, String fileName, String contentType) {
        return new WnMvcViews.DownloadView(data, fileName, contentType);
    }

    /** 下载视图：写出带 Content-Disposition(attachment) 的输入流（不关闭流）。 */
    static WnMvcView download(InputStream in, String fileName, String contentType) {
        return new WnMvcViews.DownloadView(in, fileName, contentType);
    }

    /** 重定向视图：302 到目标 URL。 */
    static WnMvcView redirect(String location) {
        return new WnMvcViews.RedirectView(location);
    }

    /** HTML 视图：写出 HTML 文本（Content-Type: text/html）。 */
    static WnMvcView html(String html) {
        return new WnMvcViews.HtmlView(html);
    }

    /** void 视图：不写出任何内容（控制器已直接写 response）。 */
    static WnMvcView none() {
        return WnMvcViews.NoneView.INSTANCE;
    }

    /** 状态视图：只设置状态码（对应 <code>http:NNN</code>）。 */
    static WnMvcView status(int code) {
        return new WnMvcViews.StatusView(code);
    }

    /** 错误视图：设置状态码 + 空错误体。 */
    static WnMvcView error(int code) {
        return new WnMvcViews.ErrorView(code);
    }

    /** 错误视图：设置状态码 + 以 JSON/文本写出错误体。 */
    static WnMvcView error(int code, Object body) {
        return new WnMvcViews.ErrorView(code, body);
    }

}
