var ioc = {
    ioService : {
        events : {
            create : "on_create"
        },
        fields : {
            io : {
                refer : "io"
            }
        }
    },
    sysAuthService : {
        type : 'org.nutz.walnut.impl.auth.WnSysAuthServiceWrapper',
        parent : "ioService",
        fields : {
            initEnvs : {
                java : '$conf.initUsrEnvs'
            },
            rootDefaultPasswd : {
                java : '$conf.getTrim("root-init-passwd", "123456")'
            },
            seDftDu : {
                java : '$conf.getLong("se-sys-du", 3600)'
            },
            seTmpDu : {
                java : '$conf.getLong("se-tmp-du", 60)'
            },
            defaultQuitUrl : {
                java : '$conf.get("sys-entry-url", "/a/login/")'
            }
        }
    },
    usrService : {
        type : 'org.nutz.walnut.impl.usr.IoWnUsrService',
        parent : "ioService",
        fields : {
            initEnvs : {
                java : '$conf.initUsrEnvs'
            }
        }
    },
    sessionService : {
        type : 'org.nutz.walnut.impl.usr.IoWnSessionService',
        parent : "ioService",
        fields : {
            usrs : {
                refer : "usrService"
            },
            duration : {
                java : '$conf.getInt("se-duration", 3600000)'
            }
        }
    },
    jvmExecutorFactory : {
        type : 'org.nutz.walnut.impl.box.JvmExecutorFactory',
        fields : {
            scanPkgs : {
                java : '$conf.jvmboxPkgs'
            },
            ioc : {
                refer : '$Ioc'
            }
        }
    },
    boxService : {
        type : 'org.nutz.walnut.impl.box.JvmBoxService',
        args : [ {
            refer : "jvmExecutorFactory"
        } ]
    },
    hookService : {
        type : 'org.nutz.walnut.impl.hook.CachedWnHookService',
        parent : "ioService",
        fields : {
            esi : {
                refer : "esi"
            }
        }
    },
    lockApi : {
        type : 'org.nutz.walnut.impl.lock.redis.RedisLockApi',
        args : [{
            refer : "redisConfForLockApi"
        }]
    },
    expiObjTable : {
        type : 'org.nutz.walnut.core.eot.mongo.MongoExpiObjTable',
        args : [{
            java  : '$mongoDB.getCollection("expi")'
        }]
    },
    safeExpiObjTable : {
        type : 'org.nutz.walnut.core.eot.WnSafeExpiObjTable',
        fields : {
            table : {
                refer : 'expiObjTable'
            },
            locks : {
                refer : 'lockApi'
            },
            tryLockDuration : {
                java : '$conf.getLong("sys-expi-obj-table-lockdu", 5000)'
            }
        }
    }
}