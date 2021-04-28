package org.nutz.walnut.ext.net.sendmail.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class WnObjMailDataSource implements javax.activation.DataSource {

    private WnIo io;

    private WnObj obj;

    public WnObjMailDataSource(WnIo io, WnObj obj) {
        this.io = io;
        this.obj = obj;
    }

    @Override
    public String getContentType() {
        return obj.mime();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return io.getInputStream(obj, 0);
    }

    @Override
    public String getName() {
        return obj.name();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return io.getOutputStream(obj, 0);
    }

}
