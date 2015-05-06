package org.nutz.walnut;

import org.junit.After;
import org.junit.Before;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnStoreFactory;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.io.MimeMapImpl;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.util.Wn;

public abstract class BaseApiTest {

    // ------------------------------------------------ 这些是测试目标的构建
    protected WnIndexer indexer;

    protected WnTreeFactory treeFactory;

    protected WnStoreFactory storeFactory;

    protected PropertiesProxy pp;

    protected MongoDB db;
    
    protected MimeMap mimes;

    @Before
    public void before() {
        // 解析配置文件
        pp = new PropertiesProxy("org/nutz/walnut/junit.properties");

        // 初始化 MongoDB
        db = new MongoDB();
        Mirror.me(db).setValue(db, "host", pp.get("mongo-host"));
        Mirror.me(db).setValue(db, "port", pp.getInt("mongo-port"));
        Mirror.me(db).setValue(db, "usr", pp.get("mongo-usr"));
        Mirror.me(db).setValue(db, "pwd", pp.get("mongo-pwd"));
        Mirror.me(db).setValue(db, "db", pp.get("mongo-db"));
        db.on_create();
        
        PropertiesProxy ppMime = new PropertiesProxy(pp.check("mime"));
        mimes = new MimeMapImpl(ppMime);

        // 创建当前线程操作的用户
        Wn.WC().me("root","root");

        // 调用子类初始化
        on_before(pp);
    }

    @After
    public void after() {
        on_after(pp);
    }

    protected void on_before(PropertiesProxy pp) {};

    protected void on_after(PropertiesProxy pp) {}
    
    protected abstract WnNode _create_top_tree_node();

}
