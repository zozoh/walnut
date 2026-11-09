package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.eot.WnSafeExpiObjTable;
import com.site0.walnut.core.eot.mongo.MongoExpiObjTable;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配过期对象表：{@code expiObjTable} + {@code safeExpiObjTable}
 * （旧 {@code service.js} 片段）。
 * <p>
 * 依赖已注册的 {@code mongoDB} 与 {@code lockApi}；必须在 IO 主链之前调用
 * （{@code hookedIo}/{@code __io} 需要 {@code safeExpiObjTable}）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnExpiObjTableRegistrar {

    private WnExpiObjTableRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        MongoDB mongoDB = factory.fetch("mongoDB", MongoDB.class);
        WnLockApi lockApi = factory.fetch("lockApi", WnLockApi.class);

        MongoExpiObjTable expiObjTable = new MongoExpiObjTable(mongoDB.getCollection("expi"));
        factory.register("expiObjTable", expiObjTable);

        WnSafeExpiObjTable safeExpiObjTable = new WnSafeExpiObjTable();
        safeExpiObjTable.setTable(expiObjTable);
        safeExpiObjTable.setLocks(lockApi);
        safeExpiObjTable.setTryLockDuration(conf.getLong("sys-expi-obj-table-lockdu", 5000));
        factory.register("safeExpiObjTable", safeExpiObjTable);
    }

}
