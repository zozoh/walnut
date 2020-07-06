package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;

public class AppRespOutputStreamWrapper extends OutputStream {

    private int statusCode;

    private HttpRespStatusSetter resp;

    private OutputStream ops;

    private OutputStream watch_ops;

    public AppRespOutputStreamWrapper(HttpRespStatusSetter resp, int statusCode) {
        this.resp = resp;
        this.ops = resp.getOutputStream();
        this.statusCode = statusCode;
    }

    /**
     * 可以设置一个字符串缓冲，当写入输入流时，同时也会写到这个缓冲里
     * 
     * @param sb
     *            字符串缓冲
     */
    public void addStringWatcher(StringBuilder sb) {
        watch_ops = Lang.ops(sb);
    }

    public void write(int b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);
        if (null != watch_ops) {
            watch_ops.write(b);
        }
    }

    public void write(byte[] b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);
        if (null != watch_ops) {
            watch_ops.write(b);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b, off, len);
        if (null != watch_ops) {
            watch_ops.write(b, off, len);
        }
    }

    public void flush() throws IOException {
        ops.flush();
        resp.flushBuffer();
        if (null != watch_ops) {
            watch_ops.flush();
        }
        // ops.close();
        // try {
        // ops = resp.getOutputStream();
        // }
        // catch (IOException e) {
        // throw Lang.wrapThrow(e);
        // }
    }

    public void close() throws IOException {
        ops.close();
        resp.flushBuffer();
        if (null != watch_ops) {
            watch_ops.close();
        }
    }

}
