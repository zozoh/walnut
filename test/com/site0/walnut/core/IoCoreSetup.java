package com.site0.walnut.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.web.setup.WnSetup;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Mirror;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMoCo;

import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.bm.localbm.LocalIoBM;
import com.site0.walnut.core.bm.localfile.LocalFileBM;
import com.site0.walnut.core.bm.localfile.LocalFileWBM;
import com.site0.walnut.core.bm.redis.RedisBM;
import com.site0.walnut.core.hdl.redis.RedisIoHandleManager;
import com.site0.walnut.core.indexer.localfile.LocalFileIndexer;
import com.site0.walnut.core.indexer.localfile.LocalFileWIndexer;
import com.site0.walnut.core.indexer.mongo.MongoIndexer;
import com.site0.walnut.core.io.WnIoCacheWrapper;
import com.site0.walnut.core.io.WnIoImpl2;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.core.mapping.WnIoMappingFactoryImpl;
import com.site0.walnut.core.mapping.bm.LocalFileBMFactory;
import com.site0.walnut.core.mapping.bm.LocalFileWBMFactory;
import com.site0.walnut.core.mapping.bm.LocalIoBMFactory;
import com.site0.walnut.core.mapping.bm.RedisBMFactory;
import com.site0.walnut.core.mapping.indexer.LocalFileIndexerFactory;
import com.site0.walnut.core.mapping.indexer.LocalFileWIndexerFactory;
import com.site0.walnut.core.refer.redis.RedisReferService;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.box.JvmBoxService;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.impl.hook.CachedWnHookService;
import com.site0.walnut.impl.lock.redis.QuickRedisLockApi;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.WnLoginRoleOptions;
import com.site0.walnut.login.WnLoginUserOptions;
import com.site0.walnut.login.session.WnLoginSessionOptions;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.Wn;

import org.nutz.web.WebConfig;

public class IoCoreSetup {

    private static MongoDB _mongo;

    private static ZMoCo _co_obj;

    private static ZMoCo _co_expi;

    private static PropertiesProxy _pp;

    private static MimeMap _mimes;

    private static WedisConfig _wedisConf;

    private static LocalIoBM _globalBM;

    private static MongoIndexer _globalIndexer;

    private static WnIoMappingFactoryImpl _mappings;

    private static WnIoHandleManager _handles;

    private static WnReferApi _refers;

    private static WnIo _io;

    private static WnBoxService _boxService;

    private static WnServiceFactory _services;

    // static {
    // // 测试配置初始化
    // if (null == _pp) {
    // _pp = new WebConfig("test.properties");
    // }
    //
    // // MimeMap 初始化
    // if (null == _mimes)
    // _mimes = new MimeMapImpl(new PropertiesProxy("mime.properties"));
    // }

    public IoCoreSetup() {
        // 测试配置初始化
        if (null == _pp) {
            _pp = new WebConfig("test.properties");
        }

        // MimeMap 初始化
        if (null == _mimes)
            _mimes = new MimeMapImpl(new PropertiesProxy("mime.properties"));
    }

    public String getConifg(String key) {
        return _pp.get(key);
    }

    public WnUser genAccount(String name) {
        WnUser u = new WnSimpleUser(name);
        u.setMainGroup(name);
        u.setId(Wn.genId());
        return u;
    }

    public WnIo getIo() {
        WnIo io = getRawIo();
        return new WnIoCacheWrapper(io);
    }

    public WnIo getRawIo() {
        if (null == _io) {
            WnIoMappingFactory mappings = this.getWnIoMappingFactory();
            // 全局索引和桶管理器
            _mappings.setGlobalIndexer(this.getGlobalIndexer());
            _mappings.setGlobalBM(this.getGlobalIoBM());

            // 建立 IO
            _io = new WnIoImpl2(mappings);

            // 准备系统关键的对象路径
            WnSetup.makeWalnutKeyDirIfNoExists(_io);

            // 设置映射工厂，有些类需要上面准备的关键路径
            this.setupWnIoMappingFactory(_io);

            // 稍后设置一下 RedisBMFactory 自己的实例
            getRedisBMFactory().setIo(_io);
        }
        return _io;
    }

    public WnLoginApi getLoginApi() {
        WnIo io2 = getIo();
        // 创建对象
        WnLoginOptions options = new WnLoginOptions();
        options.session = new WnLoginSessionOptions();
        options.session.path = "/var/session";
        options.user = new WnLoginUserOptions();
        options.user.path = "/sys/usr";
        options.role = new WnLoginRoleOptions();
        options.role.path = "/sys/role";
        options.domain = "root";
        options.sessionDuration = 3600;
        options.sessionShortDu = 3;
        options.wechatMpOpenIdKey = "wxmp_openid";
        options.wechatGhOpenIdKey = "wxgh_openid";

        // 建立接口
        WnLoginApi auth = WnLoginApiMaker.forSys().make(io2, new NutMap(), options);
        return auth;
    }

    public WnBoxService getBoxService() {
        if (null == _boxService) {
            JvmExecutorFactory jef = new JvmExecutorFactory();
            Mirror.me(jef).setValue(jef, "scanPkgs", Wlang.array("com.site0.walnut.impl.box.cmd"));
            _boxService = new JvmBoxService(jef);
        }
        return _boxService;
    }

    public WnServiceFactory getServiceFactory() {
        if (null == _services) {
            _services = new WnServiceFactory();
            _services.setLoginApi(getLoginApi());
            _services.setHookApi(new CachedWnHookService(getIo()));
            _services.setBoxApi(getBoxService());
            _services.setReferApi(getWnReferApi());
            _services.setLockApi(getRedisLockApi());
        }
        return _services;
    }

    public void setupWnIoMappingFactory(WnIo io) {
        // 索引管理器工厂映射
        HashMap<String, WnIndexerFactory> indexers = new HashMap<>();
        indexers.put("file", new LocalFileIndexerFactory(_mimes));
        indexers.put("filew", new LocalFileWIndexerFactory(_mimes));
        // TODO 还有 "mem|redis|mq" 几种索引管理器
        // ...
        _mappings.setIndexers(indexers);

        // 桶管理器工厂映射
        WnIoHandleManager handles = this.getWnIoHandleManager();
        HashMap<String, WnBMFactory> bmfs = new HashMap<>();
        bmfs.put("lbm", new LocalIoBMFactory());
        bmfs.put("redis", this.getRedisBMFactory());
        bmfs.put("file", new LocalFileBMFactory(handles));
        bmfs.put("filew", new LocalFileWBMFactory(handles));
        _mappings.setBms(bmfs);
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
        if (null == _globalBM) {
            String phBucket = Disks.normalize(_pp.get("io-bm-bucket"));
            String phSwap = Disks.normalize(_pp.get("io-bm-swap"));
            WnIoHandleManager handles = this.getWnIoHandleManager();
            WnReferApi refers = this.getWnReferApi();
            _globalBM = new LocalIoBM(handles, phBucket, phSwap, true, refers);
        }
        return _globalBM;
    }

    public WnIoHandleManager getWnIoHandleManager() {
        if (null == _handles) {
            WnIoMappingFactory mf = this.getWnIoMappingFactory();
            int timeout = _pp.getInt("hdl-timeout", 20);
            WedisConfig conf = this.getWedisConfig();
            _handles = new RedisIoHandleManager(mf, timeout, conf);
        }
        return _handles;
    }

    public WnIoMappingFactory getWnIoMappingFactory() {
        if (null == _mappings) {
            _mappings = new WnIoMappingFactoryImpl();
        }
        return _mappings;
    }

    public WnReferApi getWnReferApi() {
        if (null == _refers) {
            _refers = new RedisReferService(this.getWedisConfig());
        }
        return _refers;
    }

    public WnLockApi getRedisLockApi() {
        WedisConfig conf = this.getWedisConfig();
        conf = conf.clone();
        // conf.setup().put("ask-du", askDu);
        return new QuickRedisLockApi(conf);
    }

    public WedisConfig getWedisConfig() {
        if (null == _wedisConf) {
            _wedisConf = new WedisConfig();
            _wedisConf.setHost(_pp.get("redis-host"));
            _wedisConf.setPort(_pp.getInt("redis-port"));
            _wedisConf.setSsl(_pp.getBoolean("redis-ssl"));
            _wedisConf.setPassword(_pp.get("redis-password", null));
            _wedisConf.setDatabase(_pp.getInt("redis-database", 0));
        }
        return _wedisConf;
    }

    public LocalFileBM getLocalFileBM() {
        WnIoHandleManager handles = this.getWnIoHandleManager();
        String ph = _pp.get("local-file-home");
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
        RedisBMFactory bmf = new RedisBMFactory();
        bmf.setAuth(getLoginApi());
        bmf.setIo(getIo());
        Map<String, RedisBM> bms = new HashMap<>();
        bms.put("_", this.getRedisBM());
        bmf.setBms(bms);
        return bmf;
    }

    public LocalFileIndexer getLocalFileIndexer() {
        File dHome = getLocalFileHome();
        if (!dHome.exists()) {
            Files.createDirIfNoExists(dHome);
        }
        WnObj oHome = this.getRootNode();
        return new LocalFileWIndexer(oHome, _mimes, dHome);
    }

    public File getLocalFileHome() {
        String ph = _pp.get("local-file-home");
        String aph = Disks.normalize(ph);
        File dHome = new File(aph);
        return dHome;
    }

    public MongoIndexer getGlobalIndexer() {
        if (null == _globalIndexer) {
            ZMoCo co = this.getMongoCoObj();
            WnObj root = this.getRootNode();
            MimeMap mimes = this.getMimes();
            _globalIndexer = new MongoIndexer(root, mimes, co);
        }
        return _globalIndexer;
    }

    public PropertiesProxy getProperties() {
        return _pp;
    }

    public String explainConfig(String text) {
        NutBean ctx = new NutMap();
        ctx.putAll(_pp.toMap());
        return WnTmpl.exec(text, ctx);
    }

    public MimeMap getMimes() {
        return _mimes;
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
        if (null == _mongo) {
            _mongo = new MongoDB();
            _mongo.host = _pp.get("mongo-host");
            _mongo.port = _pp.getInt("mongo-port", 27017);
            _mongo.usr = _pp.get("mongo-usr");
            _mongo.pwd = _pp.get("mongo-pwd");
            _mongo.db = _pp.get("mongo-db", "walnut_unit");
            _mongo.on_create();
        }
        if (null == _co_obj) {
            String coName = _pp.get("mongo-co-obj", "obj");
            _co_obj = _mongo.getCollection(coName);
            _co_obj.drop();
        }
        return _co_obj;
    }

    public ZMoCo getMongoCoExpi() {
        // MongoDB 连接初始化
        if (null == _mongo) {
            _mongo = new MongoDB();
            _mongo.host = _pp.get("mongo-host");
            _mongo.port = _pp.getInt("mongo-port", 27017);
            _mongo.usr = _pp.get("mongo-usr");
            _mongo.pwd = _pp.get("mongo-pwd");
            _mongo.db = _pp.get("mongo-db", "walnut_unit");
            _mongo.on_create();
        }
        if (null == _co_expi) {
            String coName = _pp.get("mongo-co-expi", "expi");
            _co_expi = _mongo.getCollection(coName);
            _co_expi.drop();
        }
        return _co_expi;
    }

    public void cleanAllData() {
        // 清空 Mongo
        cleanMongo();

        // 清空目录: 本地文件
        cleanLocalFileHome();

        // 清空目录: 本地桶
        cleanLocalIoBM();

        // 清空 Redis 当前数据库
        cleanRedisData();
    }

    public void cleanMongo() {
        ZMoCo co = this.getMongoCoObj();
        if (_mongo.existsCollection(co.getNamespace().getCollectionName())) {
            co.drop();
        }
    }

    public void cleanLocalFileHome() {
        File d = getLocalFileHome();
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localfile")) {
                Files.clearDir(d);
            } else {
                throw Wlang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }
    }

    public void cleanLocalIoBM() {
        File d;
        String phBucket = _pp.get("io-bm-bucket");
        String aphBucket = Disks.normalize(phBucket);
        d = new File(aphBucket);
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localbm/bucket")) {
                Files.clearDir(d);
            } else {
                throw Wlang.makeThrow("!!!删除这个路径有点危险：%s", aph);
            }
        }

        String phSwap = _pp.get("io-bm-swap");
        String aphSwap = Disks.normalize(phSwap);
        d = new File(aphSwap);
        if (d.exists()) {
            String aph = Files.getAbsPath(d);
            if (aph.endsWith(".walnut/test/localbm/swap")) {
                Files.clearDir(d);
            } else {
                throw Wlang.makeThrow("!!!删除这个路径有点危险：%s", aph);
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
