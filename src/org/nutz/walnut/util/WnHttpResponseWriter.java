package org.nutz.walnut.util;

import java.io.File;
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
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.RawView.RangeRange;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.range.WnGetInputStream;
import org.nutz.walnut.util.range.WnGetLocalFileInputStream;
import org.nutz.walnut.util.range.WnGetObjInputStream;
import org.nutz.walnut.util.stream.WnByteInputStream;
import org.nutz.walnut.web.util.WnWeb;
import org.subethamail.smtp.util.Base64;

/**
 * 封装 HTTP 流写出逻辑，提供给诸如 `cmd_httpout` 等命令使用 <br>
 * <b>!!!线程不安全</b>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnHttpResponseWriter {

    /**
     * 响应的头
     */
    private NutMap headers;

    /**
     * 准备要写入的内容流
     */
    private InputStream ins;

    /**
     * 是否将输出转换位 base64
     */
    private boolean asBase64;

    /**
     * 唯一性标识，prepare 前需要设置好
     */
    private String etag;

    /**
     * 响应码，默认会写入 200 如果返现 ETag 相同，则会是 304
     */
    private int status;

    private String userAgent;

    private String downloadName;

    private String contentType;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isAsBase64() {
        return asBase64;
    }

    public void setAsBase64(boolean asBase64) {
        this.asBase64 = asBase64;
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
        if (Strings.isBlank(this.downloadName)) {
            this.downloadName = wobj.getString("title");
            this.downloadName = Ws.sBlank(this.downloadName, wobj.name());
            // 确保有正确的扩展名作为结尾
            String suffixName = Files.getSuffixName(this.downloadName);
            if (wobj.hasType() && !wobj.isType(suffixName)) {
                this.downloadName += "." + wobj.type();
            }
        }

        // 准备对象
        WnGetInputStream getInput = new WnGetObjInputStream(io, wobj);

        // 准备
        this.prepare(range, getInput);
    }

    // public void __prepare(WnIo io, WnObj wobj, String range) {
    // // 默认采用 obj 的 mime
    // if (Strings.isBlank(this.contentType))
    // this.contentType = wobj.mime();
    //
    // // 默认用 obj 的名称作为下载名
    // if (Strings.isBlank(this.downloadName)) {
    // this.downloadName = wobj.name();
    // // 确保有正确的扩展名作为结尾
    // String suffixName = Files.getSuffixName(this.downloadName);
    // if (wobj.hasType() && !wobj.isType(suffixName)) {
    // this.downloadName += "." + wobj.type();
    // }
    // }
    //
    // // 准备记录 ETag
    // String objETag = Wn.getEtag(wobj);
    //
    // // etag存在且相等, 304搞定
    // if (objETag.equalsIgnoreCase(etag)) {
    // this.status = 304;
    // // this.headers.put("Walnut-Object-Id", wobj.id());
    // return;
    // }
    // headers.put("ETag", objETag);
    //
    // if (Strings.isBlank(range)) {
    // ins = io.getInputStream(wobj, 0);
    // headers.put("Content-Length", wobj.len());
    // } else {
    // // 解析 Range
    // List<RangeRange> rs = new ArrayList<RawView.RangeRange>();
    // if (!RawView2.parseRange(range, rs, (int) wobj.len()) || rs.size() != 1)
    // {
    // this.status = 400;
    // this.headers.put("Walnut-Http-Range-WARN", "Range Not Satisfiable");
    // }
    // // 解析成功
    // else {
    // RangeRange rr = rs.get(0);
    // headers.put("Content-Length", rr.end - rr.start);
    // headers.put("Accept-Ranges", "bytes");
    // headers.put("Content-Range",
    // String.format("bytes %d-%d/%d", rr.start, rr.end - 1, wobj.len()));
    // status = 206;
    // ins = io.getInputStream(wobj, rr.start);
    // ins = new LimitInputStream(ins, rr.end - rr.start);
    // }
    // }
    // }

    /**
     * 写入前的准备（通用）
     * 
     * @param range
     *            断点续传
     * @param getInput
     *            一个封装获取对象流/ETag/内容长度的接口
     * 
     */
    public void prepare(String range, WnGetInputStream getInput) {
        String objETag = getInput.getETag();
        long objLen = getInput.getContentLenth();

        // etag存在且相等, 304搞定
        if (null != objETag && objETag.equalsIgnoreCase(etag)) {
            this.status = 304;
            // this.headers.put("Walnut-Object-Id", wobj.id());
            return;
        }
        headers.put("ETag", objETag);

        try {
            if (Strings.isBlank(range)) {
                ins = getInput.getStream(0);
                headers.put("Content-Length", objLen);
            } else {
                // 解析 Range
                List<RangeRange> rs = new ArrayList<RawView.RangeRange>();
                if (!RawView2.parseRange(range, rs, objLen) || rs.size() != 1) {
                    this.status = 400;
                    this.headers.put("Walnut-Http-Range-WARN", "Range Not Satisfiable");
                }
                // 解析成功
                else {
                    RangeRange rr = rs.get(0);
                    headers.put("Content-Length", rr.end - rr.start);
                    headers.put("Accept-Ranges", "bytes");
                    headers.put("Content-Range",
                                String.format("bytes %d-%d/%d", rr.start, rr.end - 1, objLen));
                    status = 206;
                    ins = getInput.getStream(rr.start);
                    ins = new LimitInputStream(ins, rr.end - rr.start);
                }
            }
        }
        catch (IOException e) {
            throw Er.create(e, "e.http.range");
        }
    }

    /**
     * 写入前的准备
     * 
     * @param f
     *            内容文件
     * @param mime
     *            输入流内容类型。 如果为空，需要另行 setContentType
     * @param sha1
     *            文件 SHA1 指纹
     * @param range
     *            符合 HTTP 标准 Range 的格式规范。（全本，支持多个）
     */
    public void prepare(File f, String sha1, String range) {
        // 准备对象
        WnGetInputStream getInput = new WnGetLocalFileInputStream(f, sha1);

        // 准备
        this.prepare(range, getInput);
    }

    public void prepare(byte[] buf) {
        prepare(buf, null);
    }

    public void prepare(byte[] buf, String bufEtag) {
        // 记录 ETag
        if (!Strings.isBlank(bufEtag)) {
            headers.put("ETag", bufEtag);
        }

        // 304
        if (null != etag && etag.equalsIgnoreCase(bufEtag)) {
            this.status = 304;

        }
        // 否则就准备写吧
        else {
            ins = new WnByteInputStream(buf);
            headers.put("Content-Length", buf.length);
        }
    }

    public void prepare(String content, String contentETag) {
        this.prepare(content.getBytes(Encoding.CHARSET_UTF8), contentETag);
    }

    public void prepare(String content) {
        this.prepare(content, Lang.sha1(content));
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
        if (this.asBase64) {
            headers.putDefault("Content-Type", "text/plain");
        } else if (!Strings.isBlank(this.contentType)) {
            headers.putDefault("Content-Type", this.contentType);
        }

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

        // 输出Header
        this.__do_header((key, val) -> {
            resp.setHeader(key, val);
        });

        // 解决iphone读取视频失败的问题
        String cntType = resp.getContentType();
        if (cntType != null) {
            if (cntType.startsWith("video/")
                || cntType.startsWith("image/")
                || cntType.startsWith("audio")) {
                resp.setCharacterEncoding(null);
            }
        }

        // 来个空行准备写 Body
        resp.flushBuffer();

        // 写入Body
        if (null != ins) {
            try {
                OutputStream ops = resp.getOutputStream();
                __write_to_output(ops, ins);
                resp.flushBuffer();
            }
            finally {
                Streams.safeClose(ins);
            }
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
            if (null != ins) {
                __write_to_output(ops, ins);
            }
            // 写入空
            else {
                Streams.write(ops, " ".getBytes());
            }
        }
        finally {
            Streams.safeFlush(ops);
            Streams.safeClose(ins);
        }
    }

    private void __write_to_output(OutputStream ops, InputStream ins) throws IOException {
        // Covert base 64
        if (this.asBase64) {
            // Load bytes
            byte[] bytes;
            if (this.ins instanceof WnByteInputStream) {
                bytes = ((WnByteInputStream) this.ins).getBytes();
            } else {
                bytes = ins.readAllBytes();
            }
            // encode base64
            byte[] base64 = Base64.encodeToByte(bytes, false);
            // write
            Streams.write(ops, base64);
        }
        // Write raw stream
        else {
            Streams.write(ops, ins);
        }
    }

    public WnHttpResponseWriter() {
        this.ins = null;
        this.headers = new NutMap();
        this.status = 200;
    }

    @SuppressWarnings("unchecked")
    public WnHttpResponseWriter(Map<String, ? extends Object> map) {
        this.headers = NutMap.WRAP((Map<String, Object>) map);
    }

    public WnHttpResponseWriter(String headers_str) {
        this.headers = Strings.isBlank(headers_str) ? new NutMap() : Lang.map(headers_str);
    }
}
