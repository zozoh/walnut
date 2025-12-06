package com.site0.walnut.login;

public class WnLoginCacheOptions {

    /**
     * 缓存清理所有过期对象后，还会多清理一些对象，以免导致频繁的清理操作。 缓存自己的权限排序算法，会清理哪些命中率较低的低权重项目。
     * 它会一直清理到本属性声明的数量，如果不指定，这个数量应该是清理阈值的一半
     */
    public int minKeep;

    /**
     * 缓存对象清理阈值，超过这个数量缓存应该在加入新对象时自动清理
     */
    public int cleanThreshold;

    /**
     * 对象缓存过期时间（秒）
     */
    public int duInSec;

}
