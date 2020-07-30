package org.nutz.walnut.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.bm.localbm.LocalIoBM;
import org.nutz.walnut.core.bm.localfile.LocalFileBM;
import org.nutz.walnut.core.bm.localfile.LocalFileWBM;
import org.nutz.walnut.core.hdl.redis.RedisIoHandleManager;
import org.nutz.walnut.core.indexer.localfile.LocalFileIndexer;
import org.nutz.walnut.core.indexer.localfile.LocalFileWIndexer;
import org.nutz.walnut.core.indexer.mongo.MongoIndexer;
import org.nutz.walnut.core.io.WnIoImpl2;
import org.nutz.walnut.core.mapping.WnBMFactory;
import org.nutz.walnut.core.mapping.WnIndexerFactory;
import org.nutz.walnut.core.mapping.WnIoMappingFactoryImpl;
import org.nutz.walnut.core.mapping.bm.LocalFileBMFactory;
import org.nutz.walnut.core.mapping.bm.LocalFileWBMFactory;
import org.nutz.walnut.core.mapping.bm.LocalIoBMFactory;
import org.nutz.walnut.core.mapping.indexer.LocalFileIndexerFactory;
import org.nutz.walnut.core.refer.redis.RedisReferService;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.io.MimeMapImpl;
import org.nutz.walnut.impl.io.mongo.MongoDB;
import org.nutz.walnut.util.Wn;

public class IoCoreSetup {

    private static MongoDB mongo;

    private static ZMoCo co;

    private static PropertiesProxy pp;

    private static MimeMap mimes;

    private static WedisConfig wedisConf;

    private static LocalIoBM globalBM;

    private static MongoIndexer globalIndexer;

    private static WnIoMappingFactoryImpl mappings;

    private static WnIoHandleManager handles;

    private static WnReferApi refers;

    private static WnIo io;

    static {
        // 测试配置初始化
        if (null == pp)
            pp = new PropertiesProxy("test.properties");

        // MimeMap 初始化
        if (null == mimes)
            mimes = new MimeMapImpl(new PropertiesProxy("mime.properties"));
    }

    public WnAccount genAccount(String name) {
        WnAccount u = new WnAccount(name);
        u.setGroupName(name);
        u.setId(Wn.genId());
        return u;
    }

    public WnIo getIo() {
        if (null == io) {
            WnIoMappingFactory mappings = this.getWnIoMappingFactory();
            this.setupWnIoMappingFactory();
            io = new WnIoImpl2(mappings);
        }
        return io;
    }

    public void setupWnIoMappingFactory() {
        // 全局索引和桶管理器
        mappings.setGlobalIndexer(this.getGlobalIndexer());
        mappings.setGlobalBM(this.getGlobalIoBM());

        // 索引管理器工厂映射
        Map<String, WnIndexerFactory> indexers = new HashMap<>();
        indexers.put("file", new LocalFileIndexerFactory(mimes));
        // TODO 还有 "dao|mem|redis|mq" 几种索引管理器
        // ...
        mappings.setIndexers(indexers);

        // 桶管理器工厂映射
        WnIoHandleManager handles = this.getWnIoHandleManager();
        Map<String, WnBMFactory> bmfs = new HashMap<>();
        bmfs.put("lbm", new LocalIoBMFactory());
        bmfs.put("file", new LocalFileBMFactory(handles));
        bmfs.put("filew", new LocalFileWBMFactory(handles));
        mappings.setBms(bmfs);
    }

    public WnIoMapping getGlobalIoMapping() {
        WnIoIndexer indexer = this.getGlobalIndexer();
        WnIoBM bm = this.getGlobalIoBM();
        return new WnIoMapping(indexer, bm);
    }

    public WnIoMapping getLocalFileMapping() {
        WnIoIndexer indexer = this.getLocalFileIndexer();
        WnIoBM bm = this.getLocalFileBM();
        return new WnIoMapping(indexer, bm);
    }

    public LocalIoBM getGlobalIoBM() {
        if (null == globalBM) {
            String bmHome = Disks.normalize(pp.get("io-bm-home"));
            WnIoHandleManager handles = this.getWnIoHandleManager();
            WnReferApi refers = this.getWnReferApi();
            globalBM = new LocalIoBM(handles, bmHome, true, refers);
        }
        return globalBM;
    }

    public WnIoHandleManager getWnIoHandleManager() {
        if (null == handles) {
            WnIoMappingFactory mf = this.getWnIoMappingFactory();
            int timeout = pp.getInt("hdl-timeout", 20);
            WedisConfig conf = this.getWedisConfig();
            handles = new RedisIoHandleManager(mf, timeout, conf);
        }
        return handles;
    }

    public WnIoMappingFactory getWnIoMappingFactory() {
        if (null == mappings) {
            mappings = new WnIoMappingFactoryImpl();
        }
        return mappings;
    }

    public WnReferApi getWnReferApi() {
        if (null == refers) {
            refers = new RedisReferService(this.getWedisConfig());
        }
        return refers;
    }

    public WedisConfig getWedisConfig() {
        if (null == wedisConf) {
            wedisConf = new WedisConfig();
            wedisConf.setHost(pp.get("redis-host"));
            wedisConf.setPort(pp.getInt("redis-port"));
            wedisConf.setSsl(pp.getBoolean("redis-ssl"));
            wedisConf.setPassword(pp.get("redis-password", null));
            wedisConf.setDatabase(pp.getInt("redis-database", 0));
        }
        return wedisConf;
    }

    public LocalFileBM getLocalFileBM() {
        WnIoHandleManager handles = this.getWnIoHandleManager();
        String ph = pp.get("local-file-home");
        String aph = Disks.normalize(ph);
        File dHome = new File(aph);
        if (!dHome.exists()) {
            Files.createDirIfNoExists(dHome);
        }
        return new LocalFileWBM(handles, dHome);
    }

    public LocalFileIndexer getLocalFileIndexer() {
        File dHome = getLocalFileHome();
        if (!dHome.exists()) {
            Files.createDirIfNoExists(dHome);
        }
        WnObj oHome = this.getRootNode();
        return new LocalFileWIndexer(oHome, dHome, mimes);
    }

    public File getLocalFileHome() {
        String ph = pp.get("local-file-home");
        String aph = Disks.normalize(ph);
        File dHome = new File(aph);
        return dHome;
    }

    public MongoIndexer getGlobalIndexer() {
        if (null == globalIndexer) {
            ZMoCo co = this.getMongoCollection();
            WnObj root = this.getRootNode();
            globalIndexer = new MongoIndexer(root, mimes, co);
        }
        return globalIndexer;
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
        o.mode(0755);

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
            String coName = pp.get("mongo-co", "obj");
            co = mongo.getCollection(coName);
            co.drop();
        }
        return co;
    }

    public void cleanAllData() {
        // 清空 Mongo
        ZMoCo co = this.getMongoCollection();
        if (co.getDB().getNativeDB().collectionExists(co.getName())) {
            co.drop();
        }

        // 清空目录: 本地文件
        File d = getLocalFileHome();
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localfile")) {
                Files.clearDir(d);
            } else {
                throw Lang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }
        // 清空目录: 本地桶
        String bmPath = pp.get("io-bm-home");
        String bmHome = Disks.normalize(bmPath);
        d = new File(bmHome);
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localbm")) {
                File dBuck = Files.getFile(d, "buck");
                Files.clearDir(dBuck);
                File dSwap = Files.getFile(d, "swap");
                Files.clearDir(dSwap);
            } else {
                throw Lang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }
        // 清空 Redis 当前数据库
        WedisConfig conf = this.getWedisConfig();
        Wedis.run(conf, jed -> {
            jed.flushDB();
        });
    }

}
