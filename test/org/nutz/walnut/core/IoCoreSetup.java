package org.nutz.walnut.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nutz.dao.Dao;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.lock.WnLockApi;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.core.bm.localbm.LocalIoBM;
import org.nutz.walnut.core.bm.localfile.LocalFileBM;
import org.nutz.walnut.core.bm.localfile.LocalFileWBM;
import org.nutz.walnut.core.bm.redis.RedisBM;
import org.nutz.walnut.core.hdl.redis.RedisIoHandleManager;
import org.nutz.walnut.core.indexer.dao.DaoIndexer;
import org.nutz.walnut.core.indexer.dao.WnObjEntity;
import org.nutz.walnut.core.indexer.dao.WnObjEntityGenerating;
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
import org.nutz.walnut.core.mapping.bm.RedisBMFactory;
import org.nutz.walnut.core.mapping.indexer.LocalFileIndexerFactory;
import org.nutz.walnut.core.mapping.indexer.LocalFileWIndexerFactory;
import org.nutz.walnut.core.refer.redis.RedisReferService;
import org.nutz.walnut.ext.sys.redis.Wedis;
import org.nutz.walnut.ext.sys.redis.WedisConfig;
import org.nutz.walnut.ext.sys.sql.WnDaoMappingConfig;
import org.nutz.walnut.ext.sys.sql.WnDaos;
import org.nutz.walnut.impl.lock.redis.RedisLockApi;
import org.nutz.walnut.util.MongoDB;
import org.nutz.walnut.util.Wn;

public class IoCoreSetup {

    private static MongoDB mongo;

    private static ZMoCo co_obj;

    private static ZMoCo co_expi;

    private static PropertiesProxy pp;

    private static MimeMap mimes;

    private static WedisConfig wedisConf;

    private static LocalIoBM globalBM;

    private static MongoIndexer globalIndexer;

    private static WnDaoMappingConfig daoConfig;

    private static DaoIndexer daoIndexer;

    private static WnDaoMappingConfig daoNoNameConfig;

    private static DaoIndexer daoNoNameIndexer;

    private static WnIoMappingFactoryImpl mappings;

    private static WnIoHandleManager handles;

    private static WnReferApi refers;

    private static WnIo io;

    private static MockDaoIndexerFactory daoIndexerFactory;

    private static RedisBMFactory redisBMFactory;

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
            io = new WnIoImpl2(mappings);
            this.setupWnIoMappingFactory(io);

            // 稍后设置一下 RedisBMFactory 自己的实例
            redisBMFactory.setIo(io);
        }
        return io;
    }

    public void setupWnIoMappingFactory(WnIo io) {
        // 全局索引和桶管理器
        mappings.setGlobalIndexer(this.getGlobalIndexer());
        mappings.setGlobalBM(this.getGlobalIoBM());

        // 索引管理器工厂映射
        HashMap<String, WnIndexerFactory> indexers = new HashMap<>();
        indexers.put("file", new LocalFileIndexerFactory(mimes));
        indexers.put("filew", new LocalFileWIndexerFactory(mimes));
        indexers.put("dao", getDaoIndexerFactory());
        // TODO 还有 "mem|redis|mq" 几种索引管理器
        // ...
        mappings.setIndexers(indexers);

        // 桶管理器工厂映射
        WnIoHandleManager handles = this.getWnIoHandleManager();
        HashMap<String, WnBMFactory> bmfs = new HashMap<>();
        bmfs.put("lbm", new LocalIoBMFactory());
        bmfs.put("redis", this.getRedisBMFactory());
        bmfs.put("file", new LocalFileBMFactory(handles));
        bmfs.put("filew", new LocalFileWBMFactory(handles));
        mappings.setBms(bmfs);
    }

    public MockDaoIndexerFactory getDaoIndexerFactory() {
        if (null == daoIndexerFactory) {
            // DaoIndexerFactory dif = new DaoIndexerFactory();
            // dif.setIo(io);
            // dif.setMimes(this.getMimes());
            //
            // Map<String, DaoIndexer> indexers = new HashMap<>();
            // dif.setIndexers(indexers);
            //
            // daoIndexerFactory = dif;

            MockDaoIndexerFactory dif = new MockDaoIndexerFactory();
            dif.setMimes(this.getMimes());
            dif.setUnitSetup(pp);

            daoIndexerFactory = dif;
        }
        return daoIndexerFactory;
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

    public WnIoMapping getGlobalRedisBMMapping() {
        WnIoIndexer indexer = this.getGlobalIndexer();
        WnIoBM bm = this.getRedisBM();
        return new WnIoMapping(indexer, bm);
    }

    public LocalIoBM getGlobalIoBM() {
        if (null == globalBM) {
            String phBucket = Disks.normalize(pp.get("io-bm-bucket"));
            String phSwap = Disks.normalize(pp.get("io-bm-swap"));
            WnIoHandleManager handles = this.getWnIoHandleManager();
            WnReferApi refers = this.getWnReferApi();
            globalBM = new LocalIoBM(handles, phBucket, phSwap, true, refers);
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

    public WnLockApi getRedisLockApi(int askDu) {
        WedisConfig conf = this.getWedisConfig();
        conf = conf.clone();
        conf.setup().put("ask-du", askDu);
        return new RedisLockApi(conf);
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

    public RedisBM getRedisBM() {
        WedisConfig conf = this.getWedisConfig();
        return new RedisBM(conf);
    }

    public RedisBMFactory getRedisBMFactory() {
        if (null == redisBMFactory) {
            redisBMFactory = new RedisBMFactory();
            Map<String, RedisBM> bms = new HashMap<>();
            bms.put("_", this.getRedisBM());
            redisBMFactory.setBms(bms);
        }
        return redisBMFactory;
    }

    public LocalFileIndexer getLocalFileIndexer() {
        File dHome = getLocalFileHome();
        if (!dHome.exists()) {
            Files.createDirIfNoExists(dHome);
        }
        WnObj oHome = this.getRootNode();
        return new LocalFileWIndexer(oHome, mimes, dHome);
    }

    public File getLocalFileHome() {
        String ph = pp.get("local-file-home");
        String aph = Disks.normalize(ph);
        File dHome = new File(aph);
        return dHome;
    }

    public MongoIndexer getGlobalIndexer() {
        if (null == globalIndexer) {
            ZMoCo co = this.getMongoCoObj();
            WnObj root = this.getRootNode();
            MimeMap mimes = this.getMimes();
            globalIndexer = new MongoIndexer(root, mimes, co);
        }
        return globalIndexer;
    }

    public DaoIndexer getDaoIndexer() {
        if (null == daoIndexer) {
            WnDaoMappingConfig conf = getWnDaoConfig();
            WnObj root = this.getRootNode();
            MimeMap mimes = this.getMimes();
            daoIndexer = new DaoIndexer(root, mimes, conf);
        }
        return daoIndexer;
    }

    public WnDaoMappingConfig getWnDaoConfig() {
        if (null == daoConfig) {
            String aph = "org/nutz/walnut/core/indexer/dao/dao_indexer.json";
            String json = Files.read(aph);
            json = explainConfig(json);
            daoConfig = Json.fromJson(WnDaoMappingConfig.class, json);
        }
        return daoConfig;
    }

    public DaoIndexer getDaoNoNameIndexer() {
        if (null == daoNoNameIndexer) {
            WnDaoMappingConfig conf = getWnDaoNoNameConfig();
            WnObj root = this.getRootNode();
            MimeMap mimes = this.getMimes();
            daoNoNameIndexer = new DaoIndexer(root, mimes, conf);
        }
        return daoNoNameIndexer;
    }

    public WnDaoMappingConfig getWnDaoNoNameConfig() {
        if (null == daoNoNameConfig) {
            String aph = "org/nutz/walnut/core/indexer/dao/dao_noname_indexer.json";
            String json = Files.read(aph);
            json = explainConfig(json);
            daoNoNameConfig = Json.fromJson(WnDaoMappingConfig.class, json);
        }
        return daoNoNameConfig;
    }

    public PropertiesProxy getProperties() {
        return pp;
    }

    public String explainConfig(String text) {
        NutBean ctx = new NutMap();
        ctx.putAll(pp.toMap());
        return Tmpl.exec(text, ctx);
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
        o.lastModified(Wn.now());
        o.createTime(Wn.now());
        o.creator("root").mender("root").group("root");
        o.mode(0755);

        return o;
    }

    public ZMoCo getMongoCoObj() {
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
        if (null == co_obj) {
            String coName = pp.get("mongo-co-obj", "obj");
            co_obj = mongo.getCollection(coName);
            co_obj.drop();
        }
        return co_obj;
    }

    public ZMoCo getMongoCoExpi() {
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
        if (null == co_expi) {
            String coName = pp.get("mongo-co-expi", "expi");
            co_expi = mongo.getCollection(coName);
            co_expi.drop();
        }
        return co_expi;
    }

    public void cleanAllData() {
        // 清空 Mongo
        cleanMongo();

        // 清空 MySQL
        cleanDaoData();

        // 清空目录: 本地文件
        cleanLocalFileHome();

        // 清空目录: 本地桶
        cleanLocalIoBM();

        // 清空 Redis 当前数据库
        cleanRedisData();
    }

    public void cleanMongo() {
        ZMoCo co = this.getMongoCoObj();
        if (co.getDB().getNativeDB().collectionExists(co.getName())) {
            co.drop();
        }
    }

    public void cleanDaoData() {
        WnDaoMappingConfig daoConf = this.getWnDaoConfig();
        Dao dao = WnDaos.get(daoConf.getAuth());
        WnObjEntityGenerating ing = new WnObjEntityGenerating(null, daoConf, dao.getJdbcExpert());
        WnObjEntity entity = ing.generate();

        // 自动创建创建表（标准）
        dao.create(entity, true);
        
        daoConf = this.getWnDaoNoNameConfig();
        dao = WnDaos.get(daoConf.getAuth());
        ing = new WnObjEntityGenerating(null, daoConf, dao.getJdbcExpert());
        entity = ing.generate();

        // 自动创建创建表（无名）
        dao.create(entity, true);
    }

    public void cleanLocalFileHome() {
        File d = getLocalFileHome();
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localfile")) {
                Files.clearDir(d);
            } else {
                throw Lang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }
    }

    public void cleanLocalIoBM() {
        File d;
        String phBucket = pp.get("io-bm-bucket");
        String aphBucket = Disks.normalize(phBucket);
        d = new File(aphBucket);
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localbm/bucket")) {
                Files.clearDir(d);
            } else {
                throw Lang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }

        String phSwap = pp.get("io-bm-swap");
        String aphSwap = Disks.normalize(phSwap);
        d = new File(aphSwap);
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localbm/swap")) {
                Files.clearDir(d);
            } else {
                throw Lang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }
    }

    public void cleanRedisData() {
        WedisConfig redisConf = this.getWedisConfig();
        Wedis.run(redisConf, jed -> {
            jed.flushDB();
        });
    }

}
