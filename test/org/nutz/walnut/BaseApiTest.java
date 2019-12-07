package org.nutz.walnut;

import org.junit.After;
import org.junit.Before;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.impl.io.MimeMapImpl;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.web.WnConfig;

public abstract class BaseApiTest {

    // ------------------------------------------------ 这些是测试目标的构建
    protected WnConfig conf;

    protected MongoDB db;

    protected MimeMap mimes;

    @Before
    public void before() {
        // 解析配置文件
        conf = new WnConfig("org/nutz/walnut/junit.properties");

        // 初始化 MongoDB
        db = new MongoDB();
        Mirror.me(db).setValue(db, "host", conf.get("mongo-host"));
        Mirror.me(db).setValue(db, "port", conf.getInt("mongo-port"));
        Mirror.me(db).setValue(db, "usr", conf.get("mongo-usr"));
        Mirror.me(db).setValue(db, "pwd", conf.get("mongo-pwd"));
        Mirror.me(db).setValue(db, "db", conf.get("mongo-db"));
        db.on_create();

        PropertiesProxy ppMime = new PropertiesProxy(conf.check("mime"));
        mimes = new MimeMapImpl(ppMime);

        // 调用子类初始化
        on_before(conf);
    }

    @After
    public void after() {
        on_after(conf);
        db.on_depose();
    }

    protected void on_before(PropertiesProxy pp) {}

    protected void on_after(PropertiesProxy pp) {}

}
