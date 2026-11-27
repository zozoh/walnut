package com.site0.walnut.mvc;

import java.util.HashMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.range.WnGetInputStream;
import com.site0.walnut.util.range.WnGetObjInputStream;

/**
 * {@link WnStaticResolver} 的正式实现：薄包装 {@code WnIo}。
 * <p>
 * RM-NUTZ-14 自 {@code web2.support} 移入本包（与 {@link WnDefaultStaticService} 同层）。
 */
public class WnIoStaticResolver implements WnStaticResolver {

    private final WnIo io;

    public WnIoStaticResolver(WnIo io) {
        this.io = io;
    }

    @Override
    public WnObj check(String path) {
        if (null == io)
            return null;
        return io.check(null, path);
    }

    @Override
    public WnObj fetch(WnObj parent, String path) {
        if (null == io)
            return null;
        return io.fetch(parent, path);
    }

    @Override
    public byte[] readBytes(WnObj obj) {
        if (null == io || null == obj)
            return null;
        return io.readBytes(obj);
    }

    @Override
    public WnGetInputStream open(WnObj obj) {
        if (null == io || null == obj)
            return null;
        // 解开链接对象：ETag 与长度都取真实文件，流仍由 io 按偏移打开
        WnObj oReal = Wn.real(obj, io, new HashMap<>());
        if (null == oReal)
            return null;
        return new WnGetObjInputStream(io, oReal);
    }

}
