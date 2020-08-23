var ioc = {
    mimes : {
        type : 'org.nutz.walnut.impl.io.MimeMapImpl',
        args : [ {
            type : 'org.nutz.ioc.impl.PropertiesProxy',
            args : [ {
                java : '$conf.get("mime","mime.properties")'
            } ]
        } ]
    },
    lockApi: {
        type : "org.nutz.walnut.impl.lock.redis.RedisLockApi",
        args : [{refer:"redisConfForLockApi"}]
    },
    referApi: {
        type : "org.nutz.walnut.core.refer.redis.RedisReferService",
        args : [{refer:"redisConfForIoRefers"}]
    },
    redisBM : {
        type : 'org.nutz.walnut.core.bm.redis.RedisBM',
        args : [{refer : 'redisConfForIoBM'}]
    },
    ioHandleManager : {
        type : "org.nutz.walnut.core.hdl.redis.RedisIoHandleManager",
        args : [
            {refer : "ioMappingFactory"},
            {java  : '$conf.getInt("hdl-timeout", 20)'},
            {refer : "redisConfForIoHandle"}]
    },
    globalBM : {
        type : 'org.nutz.walnut.core.bm.localbm.LocalIoBM',
        args : [
            {refer : 'ioHandleManager'},
            {java  :'$conf.get("bucket-home")'},
            true,
            {refer : 'referApi'},
        ]
    },
    globalIndexer : {
        type : 'org.nutz.walnut.core.indexer.mongo.MongoIndexer',
        args : [
            {java  : '$conf.rootTreeNode'},
            {refer : 'mimes'}, 
            {java  : '$mongoDB.getCollection("obj")'}]
    },
    "localFileIndexerFactory" : {
        type : "org.nutz.walnut.core.mapping.indexer.LocalFileIndexerFactory",
        args : [{refer:"mimes"}]
    },
    "daoIndexerFactory" : {
        type : "org.nutz.walnut.core.mapping.indexer.DaoIndexerFactory",
        fields: {
            ioc   : {refer: "$Ioc"},
            authServiceName: "sysAuthService",
            io    : {refer: "io"},
            mimes : {refer : 'mimes'},
            indexers: {
                "account": null,
                "payment": null
            }
        }
    },
    "localIoBMFactory" : {
        type : "org.nutz.walnut.core.mapping.bm.LocalIoBMFactory",
        fields : {
            bms : {}
        }
    },
    "redisBMFactory": {
        type : "org.nutz.walnut.core.mapping.bm.RedisBMFactory",
        fields : {
            io  : {refer:"io"},
            bms : {
                "_": {refer:"redisBM"}
            }
        }
    },
    "localFileBMFactory" : {
        type : "org.nutz.walnut.core.mapping.bm.LocalFileBMFactory",
        args : [{refer:"ioHandleManager"}]
    },
    "localFileWBMFactory" : {
        type : "org.nutz.walnut.core.mapping.bm.LocalFileWBMFactory",
        args : [{refer:"ioHandleManager"}]
    },
    ioMappingFactory : {
        type : 'org.nutz.walnut.core.mapping.WnIoMappingFactoryImpl',
        fields : {
            globalIndexer : {refer:"globalIndexer"},
            globalBM : {refer:"globalBM"},
            indexers : {
                "file"  : {refer:"localFileIndexerFactory"},
                "dao"   : {refer:"daoIndexerFactory"},
                "mem"   : null,
                "redis" : null,
                "mq"    : null
                
            },
            bms : {
                "lbm": {refer:"localIoBMFactory"},
                "redis": {refer:"redisBMFactory"},
                "file": {refer:"localFileBMFactory"},
                "filew": {refer:"localFileWBMFactory"},
            }
        }
    },
    rawIo: {
        type : 'org.nutz.walnut.core.io.WnIoImpl2',
        fields: {
            mappings: {refer:"ioMappingFactory"}
        }
    },
    io : {
        type : 'org.nutz.walnut.core.io.WnIoHookedWrapper',
        fields: {
            io: {refer:"rawIo"},
            locks: {refer: "lockApi"}
        }
    }
}