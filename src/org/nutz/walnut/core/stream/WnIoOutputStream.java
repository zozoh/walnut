package org.nutz.walnut.core.stream;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoActionCallback;
import org.nutz.walnut.core.WnIoHandle;

public class WnIoOutputStream extends OutputStream {

    private WnObj obj;

    private WnIoActionCallback callback;

    private WnIoHandle h;

    public WnIoOutputStream(WnIoHandle h, WnIoActionCallback callback) {
        this.h = h;
        this.callback = callback;
        this.obj = h.getObj();
        if (null != callback) {
            WnObj o = callback.on_before(obj);
            this.obj.update2(o);
        }
    }

    @Override
    public void write(int b) throws IOException {
        throw Lang.noImplement();
    }

    @Override
    public void write(byte[] b) throws IOException {
        h.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        h.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        h.flush();
    }

    @Override
    public void close() throws IOException {
        h.close();

        if (null != callback) {
            WnObj o = callback.on_after(obj);
            this.obj.update2(o);
        }
    }

}
