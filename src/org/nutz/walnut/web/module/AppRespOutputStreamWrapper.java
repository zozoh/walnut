package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;

public class AppRespOutputStreamWrapper extends OutputStream {

    private int statusCode;

    private HttpRespStatusSetter resp;

    private OutputStream ops;

    public AppRespOutputStreamWrapper(HttpRespStatusSetter resp, int statusCode) {
        this.resp = resp;
        this.ops = resp.getOutputStream();
        this.statusCode = statusCode;
    }

    public void write(int b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);
    }

    public void write(byte[] b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b, off, len);
    }

    public void flush() throws IOException {
        ops.flush();
        resp.flushBuffer();
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
    }

}
