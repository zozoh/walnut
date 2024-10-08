package com.site0.walnut.ext.net.xapi.impl;

import java.io.InputStream;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.stream.WnByteInputStream;

public class WnXapiCacheObj extends NilXapiCacheObj {

    private WnObj oHome;

    private WnIo io;

    private String cacheKey;

    private WnObj obj;

    public WnXapiCacheObj(WnIo io, WnObj oHome, XApiRequest req) {
        super(req);
        this.io = io;
        this.oHome = oHome;
        this.req = req;
        this.cacheKey = req.checkCacheKey();
    }

    private WnObj getMatchObj(boolean autoCreate) {
        if (null != obj && !obj.isExpired()) {
            return obj;
        }
        // 自动创建
        if (autoCreate) {
            obj = io.createIfNoExists(oHome, cacheKey, WnRace.FILE);
        }
        // 仅仅获取
        else {
            obj = io.fetch(oHome, cacheKey);
        }
        return obj;
    }

    @Override
    public boolean isMatched() {
        WnObj obj = this.getMatchObj(false);
        if(null == obj) {
            return false;
        }
        // 过期没？
        long cacheExpiAt = obj.getLong("cache_expi_at", -1);
        // 过期了
        if(Wn.now()>cacheExpiAt) {
            return false;
        }
        // 嗯有效
        return true;
    }

    @Override
    public <T> T getOutput(Class<T> classOfT) {
        WnObj obj = this.getMatchObj(false);
        if (null != obj) {
            InputStream ins = io.getInputStream(obj, 0);
            return super.saveAndOutput(ins, classOfT);
        }
        return null;
    }

    @Override
    public <T> T saveAndOutput(InputStream resp, Class<T> classOfT) {
        byte[] bs = this.asBytes(resp);
        // 存入缓存
        WnObj o = this.getMatchObj(true);
        io.writeBytes(o, bs);

        WnByteInputStream ins = new WnByteInputStream(bs);

        // 从接口返回得到过期时间
        NutBean ctx;
        if (req.isDataAsJson()) {
            ctx = doOutput(ins, NutMap.class);
        }
        // XML 我还能再努力一下
        else if (req.isDataAsXml()) {
            // TODO 看来还得用Cheap
            ctx = new NutMap();
        }
        // 其他的就不管了
        else {
            ctx = new NutMap();
        }
        String expiU = req.getCache().getExpiUpdate(ctx);
        Object expi = Wn.fmt_str_macro(expiU);
        long expiInMs = Wn.now() + 3600000L;
        // 提前十分钟过期以策万全
        if (expi != null && expi instanceof Number) {
            expiInMs = ((Number) expi).longValue();
        }
        NutMap meta = new NutMap();
        // 过期1分钟内必然被删除(回收线程周期），即这个缓存如果过期1分钟后没有被命中则必然没用需要回收
        meta.put("expi", expiInMs);
        // 标志一个过期时间，提前5分钟失效
        meta.put("cache_expi_at", expiInMs - 300000L);
        io.appendMeta(o, meta);

        // 输出
        ins.reset();
        return doOutput(ins, classOfT);
    }

}
