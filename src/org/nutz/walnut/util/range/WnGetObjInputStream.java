package org.nutz.walnut.util.range;

import java.io.InputStream;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnGetObjInputStream implements WnGetInputStream {

    private WnIo io;

    private WnObj obj;

    public WnGetObjInputStream(WnIo io, WnObj obj) {
        this.io = io;
        this.obj = obj;
    }

    @Override
    public InputStream getStream(long offset) {
        return io.getInputStream(obj, offset);
    }

    @Override
    public long getContentLenth() {
        return obj.len();
    }

    @Override
    public String getETag() {
        return Wn.getEtag(obj);
    }

}
