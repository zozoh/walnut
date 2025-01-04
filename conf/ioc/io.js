var ioc = {
  mimes: {
    type: "com.site0.walnut.core.MimeMapImpl",
    args: [
      {
        type: "org.nutz.ioc.impl.PropertiesProxy",
        args: [
          {
            java: '$conf.get("mime","mime.properties")'
          }
        ]
      }
    ]
  },
  lockApiByMemory: {
    type: "com.site0.walnut.impl.lock.memory.MemoryLockApi"
  },
  referApi: {
    type: "com.site0.walnut.core.refer.redis.RedisReferService",
    args: [{ refer: "redisConfForIoRefers" }]
  },
  referApiByMongo: {
    type: "com.site0.walnut.core.refer.mongo.MongoReferService",
    args: [{ java: '$mongoDB.getCollection("refs")' }]
  },
  redisBM: {
    type: "com.site0.walnut.core.bm.redis.RedisBM",
    args: [{ refer: "redisConfForIoBM" }]
  },
  ioHandleManager: {
    type: "com.site0.walnut.core.hdl.redis.RedisIoHandleManager",
    args: [
      { refer: "ioMappingFactory" },
      { java: '$conf.getInt("hdl-timeout", 20)' },
      { refer: "redisConfForIoHandle" }
    ]
  },
  ioHandleManagerByMemory: {
    type: "com.site0.walnut.core.hdl.memory.MemoryIoHandleManager",
    args: [
      { refer: "ioMappingFactory" },
      { java: '$conf.getInt("hdl-timeout", 20)' }
    ]
  },
  globalBM: {
    type: "com.site0.walnut.core.bm.localbm.LocalIoBM",
    args: [
      { refer: "ioHandleManager" },
      { java: '$conf.get("global-bm-bucket")' },
      { java: '$conf.get("global-bm-swap")' },
      { java: '$conf.getBoolean("global-bm-autocreate", true)' },
      { refer: "referApi" }
    ]
  },
  globalIndexer: {
    type: "com.site0.walnut.core.indexer.mongo.MongoIndexer",
    args: [
      { java: "$conf.rootTreeNode" },
      { refer: "mimes" },
      { java: '$mongoDB.getCollection("obj")' }
    ]
  },
  mongoFlatIndexerFactory: {
    type: "com.site0.walnut.core.mapping.indexer.MongoFlatIndexerFactory",
    args: [{ refer: "mimes" }, { refer: "mongoDB" }]
  },
  localFileIndexerFactory: {
    type: "com.site0.walnut.core.mapping.indexer.LocalFileIndexerFactory",
    args: [{ refer: "mimes" }]
  },
  localFileWIndexerFactory: {
    type: "com.site0.walnut.core.mapping.indexer.LocalFileWIndexerFactory",
    args: [{ refer: "mimes" }]
  },
  daoIndexerFactory: {
    type: "com.site0.walnut.core.mapping.indexer.DaoIndexerFactory",
    fields: {
      ioc: { refer: "$Ioc" },
      authServiceName: "sysAuthService",
      io: { refer: "io" },
      mimes: { refer: "mimes" },
      indexers: {
        "account": null,
        "payment": null
      }
    }
  },
  localIoBMFactory: {
    type: "com.site0.walnut.core.mapping.bm.LocalIoBMFactory",
    fields: {
      bms: {}
    }
  },
  redisBMFactory: {
    type: "com.site0.walnut.core.mapping.bm.RedisBMFactory",
    fields: {
      io: { refer: "io" },
      bms: {
        "_": { refer: "redisBM" }
      }
    }
  },
  localFileBMFactory: {
    type: "com.site0.walnut.core.mapping.bm.LocalFileBMFactory",
    args: [{ refer: "ioHandleManager" }]
  },
  localFileWBMFactory: {
    type: "com.site0.walnut.core.mapping.bm.LocalFileWBMFactory",
    args: [{ refer: "ioHandleManager" }]
  },
  ioMappingFactory: {
    type: "com.site0.walnut.core.mapping.WnIoMappingFactoryImpl",
    fields: {
      globalIndexer: { refer: "globalIndexer" },
      globalBM: { refer: "globalBM" },
      indexers: {
        "mongo": { refer: "mongoFlatIndexerFactory" },
        "file": { refer: "localFileIndexerFactory" },
        "filew": { refer: "localFileWIndexerFactory" },
        "dao": { refer: "daoIndexerFactory" },
        "mem": null,
        "redis": null
      },
      bms: {
        "lbm": { refer: "localIoBMFactory" },
        "redis": { refer: "redisBMFactory" },
        "file": { refer: "localFileBMFactory" },
        "filew": { refer: "localFileWBMFactory" }
      }
    }
  },
  rawIo: {
    type: "com.site0.walnut.core.io.WnIoImpl2",
    fields: {
      mappings: { refer: "ioMappingFactory" },
      locks: { refer: "lockApi" }
    }
  },
  hookedIo: {
    type: "com.site0.walnut.core.io.WnIoHookedWrapper",
    fields: {
      io: { refer: "rawIo" },
      expiTable: { refer: "safeExpiObjTable" }
    }
  },
  io: {
    type: "com.site0.walnut.core.io.WnIoSaftyWrapper",
    fields: {
      io: { refer: "hookedIo" }
    }
  }
};
