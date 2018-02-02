package org.nutz.walnut.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.sshd.common.util.io.LimitInputStream;
import org.nutz.http.Http;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.mvc.view.RawView.RangeRange;

/**
 * 封装 HTTP 流写出逻辑，提供给诸如 `cmd_httpout` 等命令使用 <br>
 * <b>!!!线程不安全</b>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnHttpResponse {

    /**
     * 响应的头
     */
    private NutMap headers;

    /**
     * 准备要写入的内容流
     */
    private InputStream ins;

    /**
     * 唯一性标识，prepare 前需要设置好
     */
    private String etag;

    /**
     * 响应码，默认会写入 200 如果返现 ETag 相同，则会是 304
     */
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String userAgent;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    private String downloadName;

    public String getDownloadName() {
        return downloadName;
    }

    public void setDownloadName(String downloadName) {
        this.downloadName = downloadName;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 写入前的准备
     * 
     * @param io
     *            IO 接口
     * @param wobj
     *            对象
     * @param range
     *            符合 HTTP 标准 Range 的格式规范。（全本，支持多个）
     */
    public void prepare(WnIo io, WnObj wobj, String range) {
        // 默认采用 obj 的 mime
        if (Strings.isBlank(this.contentType))
            this.contentType = wobj.mime();

        // 默认用 obj 的名称作为下载名
        if (Strings.isBlank(this.downloadName))
            this.downloadName = wobj.name();

        // 准备记录 ETag
        String objETag = Wn.getEtag(wobj);
        headers.put("ETag", objETag);

        // 没给 ETag 那么就直接写咯
        if (Strings.isBlank(etag)) {
            headers.put("Content-Length", "" + wobj.len());
            ins = io.getInputStream(wobj, 0);
        }
        // 看看 ETag 和 Range 的逻辑
        else {
            // 304
            if (null != etag && etag.equalsIgnoreCase(objETag)) {
                this.status = 304;
                this.headers.put("Walnut-Object-Id", wobj.id());
            }
            // 断点续传
            else if (!Strings.isBlank(range)) {
                // 解析 Range
                List<RangeRange> rs = new ArrayList<RawView.RangeRange>();
                if (!RawView2.parseRange(range, rs, (int) wobj.len()) || rs.size() != 1) {
                    this.status = 400;
                    this.headers.put("Walnut-Http-Range-WARN", "Range Not Satisfiable");
                }
                // 解析成功
                else {
                    RangeRange rr = rs.get(0);
                    headers.put("Content-Length", rr.end - rr.start);
                    headers.put("Accept-Ranges", "bytes");
                    headers.put("Content-Range",
                                String.format("bytes %d-%d/%d", rr.start, rr.end - 1, wobj.len()));
                    status = 206;
                    ins = io.getInputStream(wobj, rr.start);
                    ins = new LimitInputStream(ins, rr.end - rr.start);
                }
            }
            // 写全部的流
            else {
                ins = io.getInputStream(wobj, 0);
                headers.put("Content-Length", wobj.len());
            }
        }
    }

    public void prepare(byte[] buf) {
        ins = new ByteInputStream(buf);
        headers.put("Content-Length", buf.length);
    }

    public void prepare(InputStream ins, int len) throws IOException {
        this.ins = ins;
        if (len > 0)
            headers.put("Content-Length", len);
        else if (ins.available() > 0)
            headers.put("Content-Length", ins.available());
    }

    public static interface HandleHeader {
        public void invoke(String key, String val) throws IOException;
    }

    public void __do_header(HandleHeader callback) throws IOException {
        // 然后是确保有 Content-Type
        if (!Strings.isBlank(this.contentType))
            headers.putDefault("Content-Type", this.contentType);

        // 是否声明有下载目标信息呀？
        if (!Strings.isBlank(this.userAgent) && !Strings.isBlank(this.downloadName)) {
            headers.putDefault("Content-Disposition",
                               WnWeb.genHttpRespHeaderContentDisposition(this.downloadName,
                                                                         this.userAgent));
        }

        // 输出Header
        for (Entry<String, Object> en : headers.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null != val) {
                String str = val.toString();
                if (!Strings.isBlank(str)) {
                    callback.invoke(key, str);
                }
            }
        }
    }

    /**
     * 向一个HTTP响应输出流写入数据
     * 
     * @param resp
     *            响应对象
     * 
     * @throws IOException
     */
    public void writeTo(HttpServletResponse resp) throws IOException {
        // 设置状态码
        resp.setStatus(this.status);

        // 30X 就不继续了
        if (status / 100 == 3)
            return;

        // 输出Header
        this.__do_header((key, val) -> {
            resp.setHeader(key, val);
        });

        // 来个空行准备写 Body
        resp.flushBuffer();

        // 写入Body
        if (null != ins) {
            OutputStream ops = resp.getOutputStream();
            Streams.write(ops, ins);
            resp.flushBuffer();
        }
    }

    /**
     * 向一个输出流写入数据（并不会关闭输出流）
     * 
     * @param ops
     *            要被写入的输出流
     * 
     * @throws IOException
     */
    public void writeTo(OutputStream ops) throws IOException {
        // 写入内容
        try {
            OutputStreamWriter w = new OutputStreamWriter(ops);

            // 首先的状态行
            String status_text = Http.getStatusText(status, "FUCK");
            w.write(String.format("HTTP/1.1 %d %s\n", status, status_text));

            // 30X 就不继续了
            if (status / 100 == 3) {
                w.flush();
                return;
            }

            // 输出Header
            this.__do_header((key, val) -> {
                // w.write(URLEncoder.encode(key, "UTF-8"));
                // sys.out.println(URLEncoder.encode(""+en.getValue(),
                // "UTF-8"));
                w.write(key + ": " + val + "\n");
            });

            // 来个空行准备写 Body
            w.write("\n");
            w.flush();

            // 写入Body
            if (null != ins)
                Streams.write(ops, ins);
        }
        finally {
            Streams.safeFlush(ops);
            // Streams.safeClose(ops);
        }
    }

    public WnHttpResponse() {
        this.ins = null;
        this.headers = new NutMap();
        this.status = 200;
    }

    @SuppressWarnings("unchecked")
    public WnHttpResponse(Map<String, ? extends Object> map) {
        this.headers = NutMap.WRAP((Map<String, Object>) map);
    }

    public WnHttpResponse(String headers_str) {
        this.headers = Strings.isBlank(headers_str) ? new NutMap() : Lang.map(headers_str);
    }
}
