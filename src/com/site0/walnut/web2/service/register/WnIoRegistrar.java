package com.site0.walnut.web2.service.register;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.site0.walnut.log.WnLog;
import org.nutz.mongo.ZMoCo;

import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.MimeMapImpl;
import com.site0.walnut.core.bm.bml.LocalSha1BM;
import com.site0.walnut.core.bm.redis.RedisBM;
import com.site0.walnut.core.eot.WnSafeExpiObjTable;
import com.site0.walnut.core.hdl.memory.MemoryIoHandleManager;
import com.site0.walnut.core.hdl.redis.RedisIoHandleManager;
import com.site0.walnut.core.indexer.mongo.MongoIndexer;
import com.site0.walnut.core.io.WnIoCacheWrapper;
import com.site0.walnut.core.io.WnIoHookedWrapper;
import com.site0.walnut.core.io.WnIoImpl2;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.core.mapping.WnIoMappingFactoryImpl;
import com.site0.walnut.core.mapping.bm.LocalFileBMFactory;
import com.site0.walnut.core.mapping.bm.LocalFileWBMFactory;
import com.site0.walnut.core.mapping.bm.LocalSha1BMFactory;
import com.site0.walnut.core.mapping.bm.RedisBMFactory;
import com.site0.walnut.core.mapping.bm.SqlBMFactory;
import com.site0.walnut.core.mapping.bm.VXDataSignBMFactory;
import com.site0.walnut.core.mapping.bm.VofsBMFactory;
import com.site0.walnut.core.mapping.indexer.LocalFileIndexerFactory;
import com.site0.walnut.core.mapping.indexer.LocalFileWIndexerFactory;
import com.site0.walnut.core.mapping.indexer.MongoFlatIndexerFactory;
import com.site0.walnut.core.mapping.indexer.SqlIndexerFactory;
import com.site0.walnut.core.mapping.indexer.VofsIndexerFactory;
import com.site0.walnut.core.refer.mongo.MongoReferService;
import com.site0.walnut.core.refer.redis.RedisReferService;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 IO 主链与映射工厂（旧 {@code conf/ioc/io.js}）：
 * mimes / referApi / redisBM / IO 句柄管理器 / 全局 indexer+BM /
 * indexer+BM 工厂群 / rawIo-hookedIo-io-__io。
 * <p>
 * 依赖（由前面的 registrar 注册）：{@code mongoDB}、4 个 {@code redisConfFor*}
 * （本类只用 IoRefers/IoBM/IoHandle 三份）、{@code lockApi}、{@code safeExpiObjTable}。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnIoRegistrar {

    private static final WnLog log = Wlog.getMAIN();

    private WnIoRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        MongoDB mongoDB = factory.fetch("mongoDB", MongoDB.class);
        WedisConfig redisConfForIoRefers = factory.fetch("redisConfForIoRefers",
                                                         WedisConfig.class);
        WedisConfig redisConfForIoBM = factory.fetch("redisConfForIoBM", WedisConfig.class);
        WedisConfig redisConfForIoHandle = factory.fetch("redisConfForIoHandle",
                                                         WedisConfig.class);
        WnLockApi lockApi = factory.fetch("lockApi", WnLockApi.class);
        WnSafeExpiObjTable safeExpiObjTable = factory.fetch("safeExpiObjTable",
                                                            WnSafeExpiObjTable.class);

        // 1. 引用计数
        RedisReferService referApi = new RedisReferService(redisConfForIoRefers);
        factory.register("referApi", referApi);

        ZMoCo refsCo = mongoDB.getCollection("refs");
        factory.register("referApiByMongo", new MongoReferService(refsCo));

        factory.register("redisBM", new RedisBM(redisConfForIoBM));

        // 2. Mime 表（旧实现用 Nutz PropertiesProxy，这里用 java.util.Properties 等价加载）
        MimeMapImpl mimes = new MimeMapImpl(__loadMimeMap(conf.get("mime", "mime.properties")));
        factory.register("mimes", mimes);

        // 3. IO 句柄管理器
        WnIoMappingFactoryImpl ioMappingFactory = new WnIoMappingFactoryImpl();
        factory.register("ioMappingFactory", ioMappingFactory);

        int hdlTimeout = conf.getInt("hdl-timeout", 20);
        RedisIoHandleManager ioHandleManager = new RedisIoHandleManager(ioMappingFactory,
                                                                       hdlTimeout,
                                                                       redisConfForIoHandle);
        factory.register("ioHandleManager", ioHandleManager);

        MemoryIoHandleManager ioHandleManagerByMemory = new MemoryIoHandleManager(ioMappingFactory,
                                                                                 hdlTimeout);
        factory.register("ioHandleManagerByMemory", ioHandleManagerByMemory);

        // 4. 全局索引/桶管理器
        MongoIndexer globalIndexer = new MongoIndexer(conf.getRootTreeNode(),
                                                      mimes,
                                                      mongoDB.getCollection("obj"));
        factory.register("globalIndexer", globalIndexer);

        LocalSha1BM globalBM = new LocalSha1BM(ioHandleManager,
                                               conf.get("global-bm-bucket"),
                                               conf.get("global-bm-swap"),
                                               conf.getBoolean("global-bm-autocreate", true),
                                               referApi);
        factory.register("globalBM", globalBM);

        // 5. 索引器工厂群
        MongoFlatIndexerFactory mongoFlatIndexerFactory = new MongoFlatIndexerFactory(mimes,
                                                                                     mongoDB);
        factory.register("mongoFlatIndexerFactory", mongoFlatIndexerFactory);

        LocalFileIndexerFactory localFileIndexerFactory = new LocalFileIndexerFactory(mimes);
        factory.register("localFileIndexerFactory", localFileIndexerFactory);

        LocalFileWIndexerFactory localFileWIndexerFactory = new LocalFileWIndexerFactory(mimes);
        factory.register("localFileWIndexerFactory", localFileWIndexerFactory);

        SqlIndexerFactory sqlIndexerFactory = new SqlIndexerFactory();
        factory.register("sqlIndexerFactory", sqlIndexerFactory);

        VofsIndexerFactory vofsIndexerFactory = new VofsIndexerFactory();
        factory.register("vofsIndexerFactory", vofsIndexerFactory);

        // 6. 桶管理器工厂群
        LocalSha1BMFactory localSha1BMFactory = new LocalSha1BMFactory();
        localSha1BMFactory.setBms(new HashMap<String, LocalSha1BM>());
        factory.register("localSha1BMFactory", localSha1BMFactory);

        RedisBMFactory redisBMFactory = new RedisBMFactory();
        HashMap<String, RedisBM> redisBms = new HashMap<>();
        redisBms.put("_", factory.fetch("redisBM", RedisBM.class));
        redisBMFactory.setBms(redisBms);
        factory.register("redisBMFactory", redisBMFactory);

        LocalFileBMFactory localFileBMFactory = new LocalFileBMFactory(ioHandleManager);
        factory.register("localFileBMFactory", localFileBMFactory);

        LocalFileWBMFactory localFileWBMFactory = new LocalFileWBMFactory(ioHandleManager);
        factory.register("localFileWBMFactory", localFileWBMFactory);

        String swapPath = conf.get("global-bm-swap");
        SqlBMFactory sqlBMFactory = new SqlBMFactory();
        sqlBMFactory.setHandles(ioHandleManager);
        sqlBMFactory.setSwapPath(swapPath);
        factory.register("sqlBMFactory", sqlBMFactory);

        VofsBMFactory vofsBMFactory = new VofsBMFactory();
        vofsBMFactory.setHandles(ioHandleManager);
        vofsBMFactory.setSwapPath(swapPath);
        factory.register("vofsBMFactory", vofsBMFactory);

        VXDataSignBMFactory vsDataSignBMFactory = new VXDataSignBMFactory();
        vsDataSignBMFactory.setConf(conf);
        vsDataSignBMFactory.setHandles(ioHandleManager);
        vsDataSignBMFactory.setSwapPath(swapPath);
        vsDataSignBMFactory.setAutoCreateSwap(conf.getBoolean("global-bm-autocreate", true));
        factory.register("vsDataSignBMFactory", vsDataSignBMFactory);

        // 7. IO 主链： io <- hookedIo <- rawIo，另有一个 __io 旁路
        WnIoImpl2 rawIo = new WnIoImpl2(ioMappingFactory);
        rawIo.setLocks(lockApi);
        factory.register("rawIo", rawIo);

        WnIoHookedWrapper hookedIo = new WnIoHookedWrapper(rawIo);
        hookedIo.setExpiTable(safeExpiObjTable);
        factory.register("hookedIo", hookedIo);

        WnIoCacheWrapper io = new WnIoCacheWrapper(hookedIo);
        factory.register("io", io);

        WnIoHookedWrapper __io = new WnIoHookedWrapper(rawIo);
        __io.setExpiTable(safeExpiObjTable);
        factory.register("__io", __io);

        // 8. 回填需要 io 的工厂
        sqlIndexerFactory.setIo(io);
        sqlIndexerFactory.setMimes(mimes);
        vofsIndexerFactory.setIo(io);
        vofsIndexerFactory.setMimes(mimes);
        redisBMFactory.setIo(io);
        sqlBMFactory.setIo(io);
        vofsBMFactory.setIo(io);
        vsDataSignBMFactory.setIo(io);

        // 9. 组装映射工厂
        HashMap<String, WnIndexerFactory> indexers = new HashMap<>();
        indexers.put("mongo", mongoFlatIndexerFactory);
        indexers.put("file", localFileIndexerFactory);
        indexers.put("filew", localFileWIndexerFactory);
        indexers.put("sql", sqlIndexerFactory);
        indexers.put("vofs", vofsIndexerFactory);
        indexers.put("mem", null);
        indexers.put("redis", null);
        ioMappingFactory.setIndexers(indexers);

        HashMap<String, WnBMFactory> bms = new HashMap<>();
        bms.put("sha1", localSha1BMFactory);
        bms.put("redis", redisBMFactory);
        bms.put("file", localFileBMFactory);
        bms.put("filew", localFileWBMFactory);
        bms.put("sql", sqlBMFactory);
        bms.put("vofs", vofsBMFactory);
        bms.put("vxds", vsDataSignBMFactory);
        ioMappingFactory.setBms(bms);

        ioMappingFactory.setGlobalIndexer(globalIndexer);
        ioMappingFactory.setGlobalBM(globalBM);
    }

    /**
     * 等价旧 io.js 的 mimes（PropertiesProxy）：先按工作目录找，再按 classpath 找。
     */
    private static Map<String, String> __loadMimeMap(String path) {
        Properties props = new Properties();
        try (InputStream ins = __openProps(path)) {
            if (null != ins) {
                props.load(new InputStreamReader(ins, StandardCharsets.UTF_8));
            }
        }
        catch (Exception e) {
            log.warnf("WnIoRegistrar: fail to load mime '%s'", path, e);
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        if (map.isEmpty()) {
            log.warnf("WnIoRegistrar: mime '%s' is empty", path);
        }
        return map;
    }

    private static InputStream __openProps(String path) {
        File f = new File(path);
        if (f.isFile()) {
            try {
                return new FileInputStream(f);
            }
            catch (Exception e) {
                log.warnf("WnIoRegistrar: fail to open '%s'", path, e);
            }
        }
        return WnIoRegistrar.class.getClassLoader().getResourceAsStream(path);
    }

}
