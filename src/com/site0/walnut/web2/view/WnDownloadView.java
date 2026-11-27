package com.site0.walnut.web2.view;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.WnHttpResponseWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

/**
 * 下载视图：把 {@link WnHttpResponseWriter} 准备好的一次响应渲染进 MVC 上下文。
 *
 * <p>#495 起仓库里只有一份 {@code WnHttpResponseWriter}（{@code util} 包）：它持有
 * 状态 / 头 / 待写的流，两个出口由调用方决定——{@code cmd_httpout} 走
 * {@code writeTo(OutputStream)}（裸 HTTP），MVC 走这里的 {@link #render}。</p>
 */
public class WnDownloadView implements WnMvcView {

    private final WnHttpResponseWriter re;

    @Override
    public void render(WnMvcContext ctx) {
        WnMvcResponse resp = ctx.response();
        resp.status(re.getStatus());
        for (Map.Entry<String, String> en : re.toHeaders().entrySet()) {
            resp.header(en.getKey(), en.getValue());
        }
        // 与旧实现一致：写完头先 flush（提交响应），正文中途 IO 失败时不能再改状态
        resp.flush();
        try {
            re.writeBodyTo(resp.outputStream());
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

    public WnDownloadView(WnIo io,
                             WnObj o,
                             String downloadName,
                             String ua,
                             String etag,
                             String range) {
        this.re = new WnHttpResponseWriter();
        this.re.setDownloadName(downloadName);
        this.re.setEtag(etag);
        this.re.setUserAgent(ua);
        this.re.prepare(io, o, range);
    }

    /**
     * 直接给定的输入流（不可回退）。
     *
     * <p><b>不支持</b> ETag / Range：流只能从头读一次，没法按偏移重读，也没法算稳定的
     * validator。{@code etag} / {@code range} 形参因此被忽略——行为由
     * {@link WnHttpResponseWriter#prepare(InputStream, int)} 决定（只发 Content-Length）。
     * 需要 304 / 206 的场景请用对象或 byte[] 构造。</p>
     */
    public WnDownloadView(InputStream ins,
                             int len,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range)
            throws IOException {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(ins, len);
    }

    /**
     * 动态生成的字节（例如登录页模板渲染结果）。
     *
     * <p><b>不做 validator</b>：{@code etag} 形参只被记为请求头
     * <code>If-None-Match</code>，但本构造不提供资源 ETag（动态内容每次可能不同），
     * 因此不会命中 304；{@code range} 同样忽略（字节内容不可按偏移复用）。
     * 需要 304 / 206 请用带 {@code bufEtag} 的 7 参构造。</p>
     */
    public WnDownloadView(byte[] buf,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range) {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(buf);
    }

    public WnDownloadView(byte[] buf,
                             String bufEtag,
                             String ua,
                             String mimeType,
                             String downloadName,
                             String etag,
                             String range)
            throws IOException {
        this.re = new WnHttpResponseWriter();
        this.re.setEtag(etag);
        this.re.setContentType(mimeType);
        this.re.setDownloadName(downloadName);
        this.re.setUserAgent(ua);
        this.re.prepare(buf, bufEtag);
    }

}
