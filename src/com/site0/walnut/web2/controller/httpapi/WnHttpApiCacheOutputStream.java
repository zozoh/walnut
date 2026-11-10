package com.site0.walnut.web2.controller.httpapi;

import java.io.IOException;
import java.io.OutputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

/**
 * RM-NUTZ-09：照抄旧 {@code ext/sys/httpapi/WnHttpCacheOutputStream.java}
 * （逐行复制；{@code apc} 换为同包的新上下文类）。
 */
public class WnHttpApiCacheOutputStream extends OutputStream {

    private OutputStream ops;

    private WnIo io;

    private WnObj oCache;

    private WnHttpApiContext apc;

    public WnHttpApiCacheOutputStream(WnHttpApiContext apc, WnIo io) {
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
            NutMap meta = Wlang.map(fingerKey, apc.reqQuerySign);
            io.appendMeta(oCache, meta);
        }
    }

}
