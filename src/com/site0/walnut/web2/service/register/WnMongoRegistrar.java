package com.site0.walnut.web2.service.register;

import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 {@code mongoDB}（旧 {@code conf/ioc/mongo.js}）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnMongoRegistrar {

    private WnMongoRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();

        MongoDB mongoDB = new MongoDB();
        mongoDB.host = conf.get("mongo-host");
        mongoDB.port = conf.getInt("mongo-port");
        mongoDB.usr = conf.get("mongo-usr");
        mongoDB.pwd = conf.get("mongo-pwd");
        mongoDB.db = conf.get("mongo-db");
        mongoDB.uri = conf.get("mongo-uri");
        mongoDB.on_create();

        factory.register("mongoDB", mongoDB);
    }

}
