package com.site0.walnut.web2.service.register;

import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.lock.memory.MemoryLockApi;
import com.site0.walnut.impl.lock.redis.QuickRedisLockApi;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配锁：{@code lockApi} + {@code lockApiByMemory}（旧 {@code service.js} 片段）。
 * <p>
 * 过期对象表（{@code expiObjTable}/{@code safeExpiObjTable}）已拆到
 * {@link WnExpiObjTableRegistrar}。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnLockRegistrar {

    private WnLockRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WedisConfig redisConfForLockApi = factory.fetch("redisConfForLockApi", WedisConfig.class);

        QuickRedisLockApi lockApi = new QuickRedisLockApi(redisConfForLockApi);
        factory.register("lockApi", lockApi);

        factory.register("lockApiByMemory", new MemoryLockApi());
    }

}
