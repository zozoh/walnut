package org.nutz.walnut.core;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.util.Disks;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexer;
import org.nutz.walnut.core.indexer.mongo.MongoIndexer;
import org.nutz.walnut.impl.io.MimeMapImpl;
import org.nutz.walnut.impl.io.mongo.MongoDB;

public class IoCoreSetup {

    private static MongoDB mongo;

    private static PropertiesProxy pp;

    private static MimeMap mimes;

    static {
        // 测试配置初始化
        if (null == pp)
            pp = new PropertiesProxy("test.properties");

        // MimeMap 初始化
        if (null == mimes)
            mimes = new MimeMapImpl(new PropertiesProxy("mime.properties"));
    }

    public LocalFileIndexer getLocalFileIndexer() {
        String ph = pp.get("local-bm-home");
        String aph = Disks.normalize(ph);
        File dHome = new File(aph);
        if (!dHome.exists()) {
            Files.createDirIfNoExists(dHome);
        }
        WnObj oHome = this.getRootNode();
        return new LocalFileIndexer(oHome, dHome, mimes);
    }

    public MongoIndexer getMongoIndexer() {
        ZMoCo co = this.getMongoCollection();
        WnObj root = this.getRootNode();
        return new MongoIndexer(root, mimes, co);
    }

    public MimeMap getMimes() {
        return mimes;
    }

    public WnObj getRootNode() {
        WnObj o = new WnIoObj();
        o.id("@WnRoot");
        o.path("/");
        o.race(WnRace.DIR);
        o.name("");
        o.lastModified(System.currentTimeMillis());
        o.createTime(System.currentTimeMillis());
        o.creator("root").mender("root").group("root");
        o.mode(0750);

        return o;
    }

    public ZMoCo getMongoCollection() {
        // MongoDB 连接初始化
        if (null == mongo) {
            mongo = new MongoDB();
            mongo.host = pp.get("mongo-host");
            mongo.port = pp.getInt("mongo-port", 27017);
            mongo.usr = pp.get("mongo-usr");
            mongo.pwd = pp.get("mongo-pwd");
            mongo.db = pp.get("mongo-db", "walnut_unit");
            mongo.on_create();
        }
        String coName = pp.get("mongo-co", "obj");
        ZMoCo co = mongo.getCollection(coName);
        co.drop();
        return co;
    }

}
