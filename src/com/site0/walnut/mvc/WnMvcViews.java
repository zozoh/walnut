package com.site0.walnut.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;

/**
 * {@link WnMvcView} 各静态工厂的实现类集合（包私有）。
 *
 * <p>视图实例无状态，可被多请求并发渲染（渲染状态全部落到 {@link WnMvcContext}）。</p>
 */
final class WnMvcViews {

    private WnMvcViews() {}

    /** JSON 视图：把数据序列化为 JSON 写出。 */
    static class JsonView implements WnMvcView {

        private final Object data;

        JsonView(Object data) {
            this.data = data;
        }

        @Override
        public void render(WnMvcContext context) {
            WnMvcResponse resp = context.response();
            resp.contentType("application/json;charset=utf-8");
            try {
                Writer w = resp.writer();
                w.write(Json.toJson(data, JsonFormat.compact()));
                w.flush();
            }
            catch (IOException e) {
                throw Wlang.wrapThrow(e);
            }
        }
    }

    /** Ajax 信封视图：成功 <code>{"ok":true,"data":...}</code>，失败带 errCode/msg。 */
    static class AjaxView implements WnMvcView {

        private final Object data;

        private final String errCode;

        private final String message;

        AjaxView(Object data) {
            this.data = data;
            this.errCode = null;
            this.message = null;
        }

        AjaxView(String errCode, String message) {
            this.data = null;
            this.errCode = errCode;
            this.message = message;
        }

        @Override
        public void render(WnMvcContext context) {
            NutMap re = new NutMap();
            if (null == errCode) {
                re.put("ok", true);
                re.put("data", data);
            } else {
                re.put("ok", false);
                re.put("errCode", errCode);
                re.put("msg", message);
            }
            new JsonView(re).render(context);
        }
    }

    /** 原始字节/流视图：按给定 Content-Type 写出。 */
    static class RawView implements WnMvcView {

        private final byte[] data;

        private final InputStream in;

        private final String contentType;

        RawView(byte[] data, String contentType) {
            this.data = data;
            this.in = null;
            this.contentType = contentType;
        }

        RawView(InputStream in, String contentType) {
            this.data = null;
            this.in = in;
            this.contentType = contentType;
        }

        @Override
        public void render(WnMvcContext context) {
            WnMvcResponse resp = context.response();
            if (null != contentType)
                resp.contentType(contentType);
            __write_body(resp);
        }

        /** 供 {@link DownloadView} 复用。 */
        void __write_body(WnMvcResponse resp) {
            try {
                OutputStream out = resp.outputStream();
                if (null != data) {
                    out.write(data);
                } else if (null != in) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) >= 0)
                        out.write(buf, 0, n);
                }
                out.flush();
            }
            catch (IOException e) {
                throw Wlang.wrapThrow(e);
            }
        }
    }

    /** 下载视图：带 Content-Disposition(attachment) 的原始输出。 */
    static class DownloadView implements WnMvcView {

        private final RawView raw;

        private final String fileName;

        DownloadView(byte[] data, String fileName, String contentType) {
            this.raw = new RawView(data, contentType);
            this.fileName = fileName;
        }

        DownloadView(InputStream in, String fileName, String contentType) {
            this.raw = new RawView(in, contentType);
            this.fileName = fileName;
        }

        @Override
        public void render(WnMvcContext context) {
            WnMvcResponse resp = context.response();
            // RFC5987：filename* 带 UTF-8 编码；filename 做 ASCII 回退
            StringBuilder sb = new StringBuilder("attachment");
            if (null != fileName) {
                sb.append("; filename=\"").append(fileName.replace('"', '_')).append('"');
                sb.append("; filename*=UTF-8''")
                  .append(encodeRFC5987(fileName));
            }
            resp.header("Content-Disposition", sb.toString());
            raw.render(context);
        }

        private static String encodeRFC5987(String s) {
            StringBuilder sb = new StringBuilder(s.length() * 2);
            for (byte b : s.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                int c = b & 0xFF;
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~')
                    sb.append((char) c);
                else
                    sb.append('%').append(String.format("%02X", c));
            }
            return sb.toString();
        }
    }

    /** 重定向视图：302 到目标 URL。 */
    static class RedirectView implements WnMvcView {

        private final String location;

        RedirectView(String location) {
            this.location = location;
        }

        @Override
        public void render(WnMvcContext context) {
            context.response().sendRedirect(location);
        }
    }

    /** HTML 视图：写出 HTML 文本。 */
    static class HtmlView implements WnMvcView {

        private final String html;

        HtmlView(String html) {
            this.html = html;
        }

        @Override
        public void render(WnMvcContext context) {
            WnMvcResponse resp = context.response();
            resp.contentType("text/html;charset=utf-8");
            try {
                Writer w = resp.writer();
                w.write(null == html ? "" : html);
                w.flush();
            }
            catch (IOException e) {
                throw Wlang.wrapThrow(e);
            }
        }
    }

    /** void 视图：不写出任何内容。 */
    static class NoneView implements WnMvcView {

        static final NoneView INSTANCE = new NoneView();

        @Override
        public void render(WnMvcContext context) {}
    }

    /** 状态视图：只设置状态码（对应 <code>http:NNN</code>）。 */
    static class StatusView implements WnMvcView {

        private final int code;

        StatusView(int code) {
            this.code = code;
        }

        @Override
        public void render(WnMvcContext context) {
            context.response().status(code);
        }
    }

    /** 错误视图：设置状态码 + 错误体（无体时走 sendError）。 */
    static class ErrorView implements WnMvcView {

        private final int code;

        private final Object body;

        ErrorView(int code) {
            this.code = code;
            this.body = null;
        }

        ErrorView(int code, Object body) {
            this.code = code;
            this.body = body;
        }

        @Override
        public void render(WnMvcContext context) {
            WnMvcResponse resp = context.response();
            if (null == body) {
                resp.sendError(code);
                return;
            }
            resp.status(code);
            if (body instanceof String) {
                resp.contentType("text/plain;charset=utf-8");
                try {
                    Writer w = resp.writer();
                    w.write((String) body);
                    w.flush();
                }
                catch (IOException e) {
                    throw Wlang.wrapThrow(e);
                }
            } else {
                new JsonView(body).render(context);
            }
        }
    }

}
