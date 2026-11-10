package com.site0.walnut.web2.controller.app;

import java.io.OutputStream;
import com.site0.walnut.mvc.WnMvcResponse;

public class WnHttpRespStatusSetter {

    private final WnMvcResponse resp;
    private final OutputStream ops;
    private boolean done;

    public WnHttpRespStatusSetter(WnMvcResponse resp) {
        this.resp = resp;
        this.ops = resp.outputStream();
    }

    public synchronized void setStatus(int status) {
        if (!done) {
            resp.status(status);
            done = true;
        }
    }

    public synchronized OutputStream getOutputStream() {
        return ops;
    }

    public synchronized void flushBuffer() {
        resp.flush();
    }
}
