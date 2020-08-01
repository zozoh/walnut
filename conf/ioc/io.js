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
    referApi: {
        type : "org.nutz.walnut.core.refer.redis.RedisReferService",
        args : [{refer:"redis"}]
    },
    ioHandleManager : {
        type : "org.nutz.walnut.core.hdl.redis.RedisIoHandleManager",
        args : [
            {refer : "ioMappingFactory"},
            {java  : '$conf.getInt("hdl-timeout", 20)'},
            {refer : "redis"}]
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
    "localIoBMFactory" : {
        type : "org.nutz.walnut.core.mapping.bm.LocalIoBMFactory",
        fields : {
            bms : {}
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
                "dao"   : null,
                "mem"   : null,
                "redis" : null,
                "mq"    : null
                
            },
            bms : {
                "lbm": {refer:"localIoBMFactory"},
                "file": {refer:"localFileBMFactory"},
                "filew": {refer:"localFileWBMFactory"},
            }
        }
    },
    rawIo: {
        type : 'org.nutz.walnut.core.io.WnIoImpl2',
        args : [{refer:"ioMappingFactory"}]
    },
    io : {
        type : 'org.nutz.walnut.core.io.WnIoHookedWrapper',
        args : [{refer:"rawIo"}]
    }
}