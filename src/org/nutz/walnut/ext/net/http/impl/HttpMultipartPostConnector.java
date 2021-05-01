package org.nutz.walnut.ext.net.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.walnut.ext.net.http.HttpContext;
import org.nutz.walnut.ext.net.http.bean.HttpFormPart;
import org.nutz.walnut.ext.net.http.bean.WnInputStreamInfo;

public class HttpMultipartPostConnector extends AbstractHttpConnector {

    static final String SEPARATOR = "\r\n";

    public HttpMultipartPostConnector(HttpContext hc) {
        super(hc);
    }

    private String boundary;

    @Override
    public void prepare() {
        // 准备一个分界符
        this.boundary = "------FormBoundary" + R.UU32();

        // 设置头
        this.hc.addHeader("Content-Type", "multipart/form-data;boundary=" + boundary);
    }

    @Override
    public void sendBody() throws IOException {
        // 标记准备开始 body 部分
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // 准备写入流
        OutputStream ops = conn.getOutputStream();
        InputStream ins = null;

        // 输入 Multi-part
        try {

            for (HttpFormPart part : hc.getFormParts()) {
                // 对于文件流，准备一下
                WnInputStreamInfo ini = null;
                if (part.isFile()) {
                    ini = hc.getStreamInfo(part.getValue());
                    ini.name = part.getFileName(ini.name);
                    ini.mime = part.getContentType(ini.mime);
                }

                // 字段的头
                StringBuilder sb = new StringBuilder();
                sb.append("--").append(this.boundary).append(SEPARATOR);
                sb.append("Content-Disposition: form-data; name=\"");
                sb.append(part.getName()).append('"');
                // 文件流，多补充一部分
                if (null != ini) {
                    sb.append("; filename=\"");
                    sb.append(ini.name).append('"');
                    sb.append(SEPARATOR);
                    sb.append("Content-Type: ").append(ini.mime);
                }
                sb.append(SEPARATOR);
                sb.append(SEPARATOR);
                // 写头
                String str = sb.toString();
                writeText(ops, str);

                // 普通字段
                if (null == ini) {
                    if (part.hasValue()) {
                        writeText(ops, part.getValue());
                    }
                }
                // 文件流
                else {
                    ins = ini.stream;
                    Streams.write(ops, ins);
                    Streams.safeClose(ins);
                }

                // 结束
                writeText(ops, SEPARATOR);
            }

            // 最后写一个结尾
            writeText(ops, "--" + boundary + "--" + SEPARATOR);
        }
        // 确保关闭
        finally {
            Streams.safeClose(ins);
            Streams.safeClose(ops);
        }

    }

    private void writeText(OutputStream ops, String str) throws IOException {
        ops.write(str.getBytes(Encoding.CHARSET_UTF8));
    }

}
