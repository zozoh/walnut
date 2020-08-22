package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Lang;

public class AppRespOpsWrapper extends OutputStream {

    private int statusCode;

    private HttpRespStatusSetter resp;

    private OutputStream ops;

    private List<OutputStream> watchs;

    public AppRespOpsWrapper(HttpRespStatusSetter resp, int statusCode) {
        this.resp = resp;
        this.ops = resp.getOutputStream();
        this.statusCode = statusCode;
        this.watchs = new ArrayList<>(3);
    }

    /**
     * 可以设置一个字符串缓冲，当写入输入流时，同时也会写到这个缓冲里
     * 
     * @param sb
     *            字符串缓冲
     */
    public void addStringWatcher(StringBuilder sb) {
        watchs.add(Lang.ops(sb));
    }

    public void addWatcher(OutputStream wo) {
        watchs.add(wo);
    }

    public void write(int b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);

        // 同步写入到观察者流
        if (!watchs.isEmpty()) {
            for (OutputStream wo : watchs)
                wo.write(b);
        }
    }

    public void write(byte[] b) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b);

        // 同步写入到观察者流
        if (!watchs.isEmpty()) {
            for (OutputStream wo : watchs)
                wo.write(b);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = -1;
        }
        ops.write(b, off, len);

        // 同步写入到观察者流
        if (!watchs.isEmpty()) {
            for (OutputStream wo : watchs)
                wo.write(b);
        }
    }

    public void flush() throws IOException {
        ops.flush();
        resp.flushBuffer();

        // 同步刷新观察者流
        if (!watchs.isEmpty()) {
            for (OutputStream wo : watchs)
                wo.flush();
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

        // 同步关闭观察者流
        if (!watchs.isEmpty()) {
            for (OutputStream wo : watchs)
                wo.close();
        }
    }

}
