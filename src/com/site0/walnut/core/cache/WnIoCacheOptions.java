package com.site0.walnut.core.cache;

public class WnIoCacheOptions {

    /**
     * 对象缓存过期时间(秒)
     */
    public int objDuInSec;

    /**
     * 对象缓存清理阈值，超过这个数量缓存应该在加入新对象时自动清理
     */
    public int objCleanThreshold;

    /**
     * 小文件缓冲过期时间(秒)
     */
    public int sha1DuInSec;

    /**
     * 小文件缓冲清理阈值，超过这个数量缓存应该在加入新对象时自动清理
     */
    public int sha1CleanThreshold;

}
