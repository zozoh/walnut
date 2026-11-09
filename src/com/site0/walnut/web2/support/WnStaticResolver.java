package com.site0.walnut.web2.support;

import com.site0.walnut.api.io.WnObj;

/**
 * 静态资源读取的最小 IO 能力（包装 {@code WnIo}，便于 JUnit 注入假实现）。
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

}
