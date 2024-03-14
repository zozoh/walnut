package com.site0.walnut.ext.sys.httpapi;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class WnHttpCacheOutputStream extends OutputStream {

    private OutputStream ops;

    private WnIo io;

    private WnObj oCache;

    private WnHttpApiContext apc;

    public WnHttpCacheOutputStream(WnHttpApiContext apc, WnIo io) {
        this.apc = apc;
        this.io = io;
        this.oCache = apc.cacheObj;
        if (null == oCache) {
            this.oCache = io.createIfNoExists(null, apc.cacheObjPath, WnRace.FILE);
        }
        this.ops = io.getOutputStream(oCache, 0);
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public int hashCode() {
        return ops.hashCode();
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public void flush() throws IOException {
        ops.flush();
    }

    public void close() throws IOException {
        ops.close();
        // 流关闭后，需要更新一下请求参数签名
        if (null != apc.reqQuerySign) {
            String fingerKey = apc.oApi.getString("cache-finger-key");
            NutMap meta = Lang.map(fingerKey, apc.reqQuerySign);
            io.appendMeta(oCache, meta);
        }
    }

}
