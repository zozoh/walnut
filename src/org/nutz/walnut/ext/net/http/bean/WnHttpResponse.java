package org.nutz.walnut.ext.net.http.bean;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.lang.util.NutMap;

public class WnHttpResponse extends InputStream {

    private int statusCode;

    private NutMap headers;

    private InputStream body;

    public WnHttpResponse() {
        headers = new NutMap();
    }

    public WnHttpResponse(int code, Map<String, Object> headers) {
        this.statusCode = code;
        this.headers = NutMap.WRAP(headers);
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream ins, String encoding) throws IOException {
        // 处理压缩流
        if (encoding != null) {
            // GZIP
            if (encoding.contains("gzip")) {
                ins = new GZIPInputStream(ins);
            }
            // deflate 压缩
            else if (encoding.contains("deflate")) {
                ins = new InflaterInputStream(ins, new Inflater(true));
            }
        }

        // 缓冲一下
        this.body = new BufferedInputStream(ins);
    }

    public byte[] readAllBytes() throws IOException {
        LinkedByteBuffer bytes = new LinkedByteBuffer();
        byte[] buf = new byte[8192];
        int len;
        while ((len = body.read(buf)) >= 0) {
            if (len > 0) {
                bytes.write(buf, 0, len);
            }
        }
        return bytes.toArray();
    }

    public String readAll() throws IOException {
        LinkedByteBuffer bytes = new LinkedByteBuffer();
        byte[] buf = new byte[8192];
        int len;
        while ((len = body.read(buf)) >= 0) {
            if (len > 0) {
                bytes.write(buf, 0, len);
            }
        }
        return bytes.toString();
    }

    public boolean isStatus(int code) {
        return this.statusCode == code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    public NutMap getHeaders() {
        return headers;
    }

    public void setHeaders(NutMap headers) {
        this.headers = headers;
    }

    public int read() throws IOException {
        return body.read();
    }

    public int hashCode() {
        return body.hashCode();
    }

    public int read(byte[] b) throws IOException {
        return body.read(b);
    }

    public boolean equals(Object obj) {
        return body.equals(obj);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return body.read(b, off, len);
    }

    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return body.readNBytes(b, off, len);
    }

    public long skip(long n) throws IOException {
        return body.skip(n);
    }

    public int available() throws IOException {
        return body.available();
    }

    public void close() throws IOException {
        body.close();
    }

    public void mark(int readlimit) {
        body.mark(readlimit);
    }

    public void reset() throws IOException {
        body.reset();
    }

    public boolean markSupported() {
        return body.markSupported();
    }

    public long transferTo(OutputStream out) throws IOException {
        return body.transferTo(out);
    }

}
