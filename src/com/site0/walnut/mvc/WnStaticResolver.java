package com.site0.walnut.mvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.range.WnGetInputStream;

/**
 * 静态资源读取的最小 IO 能力（包装 {@code WnIo}，便于 JUnit 注入假实现）。
 * <p>
 * RM-NUTZ-14 自 {@code web2.support} 移入本包：静态服务是 mvc 层概念，
 * 默认实现 {@link WnDefaultStaticService} 依赖本接口与 {@code WnIo}。
 */
public interface WnStaticResolver {

    /**
     * 按绝对路径取对象（不存在返回 <code>null</code>）。
     */
    WnObj check(String path);

    /**
     * 在父对象下按相对路径取对象（不存在返回 <code>null</code>）。
     */
    WnObj fetch(WnObj parent, String path);

    /**
     * 读取对象内容字节。
     */
    byte[] readBytes(WnObj obj);

    /**
     * 取「流 + 长度 + ETag」三件套（静态资源响应要用）。
     *
     * <p>默认实现把 {@link #readBytes(WnObj)} 的字节包成流，只给测试假实现兜底；
     * 真实实现应覆盖成按偏移读的流（见 {@link WnIoStaticResolver}）。</p>
     *
     * @param obj
     *            目标对象
     * @return 取流入口；对象不可读时返回 <code>null</code> 由调用方当未命中处理
     */
    default WnGetInputStream open(WnObj obj) {
        final byte[] data = readBytes(obj);
        if (null == data)
            return null;
        final byte[] buf = data;
        final String etag = null == obj ? null : Wn.getEtag(obj);
        return new WnGetInputStream() {

            @Override
            public InputStream getStream(long offset) {
                int off = (int) Math.max(0, Math.min(offset, buf.length));
                return new ByteArrayInputStream(buf, off, buf.length - off);
            }

            @Override
            public long getContentLenth() {
                return buf.length;
            }

            @Override
            public String getETag() {
                return etag;
            }
        };
    }

}
