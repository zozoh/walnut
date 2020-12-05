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

    private boolean forceFlush;

    public AppRespOpsWrapper(HttpRespStatusSetter resp, int statusCode) {
        this.resp = resp;
        this.ops = resp.getOutputStream();
        this.statusCode = statusCode;
    }

    public boolean isForceFlush() {
        return forceFlush;
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }

    /**
     * 可以设置一个字符串缓冲，当写入输入流时，同时也会写到这个缓冲里
     * 
     * @param sb
     *            字符串缓冲
     */
    public void addStringWatcher(StringBuilder sb) {
        addWatcher(Lang.ops(sb));
    }

    public void addWatcher(OutputStream wo) {
        if (watchs == null)
            watchs = new ArrayList<OutputStream>(1);
        watchs.add(wo);
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (statusCode > 0) {
            resp.setStatus(statusCode);
            statusCode = 0;
        }
        ops.write(b, off, len);

        // 同步写入到观察者流
        if (watchs != null) {
            for (OutputStream wo : watchs)
                wo.write(b, off, len);
        }
    }

    public void flush() throws IOException {
        if (this.forceFlush) {
            ops.flush();
            resp.flushBuffer();
        }
        // 同步刷新观察者流
        if (watchs != null) {
            for (OutputStream wo : watchs)
                wo.flush();
        }
    }

    public void close() throws IOException {
        if (this.forceFlush) {
            ops.flush();
        	resp.flushBuffer();
        }
        ops.close();

        // 同步关闭观察者流
        if (watchs != null) {
            for (OutputStream wo : watchs)
                wo.close();
        }
    }

}
