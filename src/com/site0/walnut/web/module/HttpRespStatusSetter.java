package com.site0.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;

/**
 * 专门弄个类，只有第一次设置 status 才会有用，其他的统统的无视
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HttpRespStatusSetter {

    private HttpServletResponse resp;

    private OutputStream ops;

    private boolean done;

    public HttpRespStatusSetter(HttpServletResponse resp) {
        this.resp = resp;
        this.done = false;
        try {
            this.ops = resp.getOutputStream();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public synchronized void setStatus(int status) {
        if (!done) {
            resp.setStatus(status);
            done = true;
        }
    }

    public synchronized OutputStream getOutputStream() {
        return ops;
    }
    
    public synchronized void flushBuffer(){
        try {
            resp.flushBuffer();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

}
