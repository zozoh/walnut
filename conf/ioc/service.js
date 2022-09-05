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
    serviceFactory : {
        type : 'org.nutz.walnut.api.box.WnServiceFactory',
        fields : {
            authApi : {
                refer : "sysAuthService"
            },
            taskApi : {
                refer : "safeSysTaskService"
            },
            scheduleApi : {
                refer : "safeSysScheduleService"
            },
            cronApi : {
                refer : "sysCronService"
            },
            boxApi : {
                refer : "boxService"
            },
            hookApi : {
                refer : "hookService"
            },
            referApi : {
                refer : "referApi"
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
    safeSysTaskService : {
        type : 'org.nutz.walnut.ext.sys.task.impl.WnSafeSysTaskService',
        fields : {
            tasks : {
                refer : 'sysTaskService'
            },
            locks : {
                refer : 'lockApi'
            },
            tryLockDuration : {
                java : '$conf.getLong("sys-task-lockdu", 3000)'
            }
        }
    },
    sysTaskService : {
        type : 'org.nutz.walnut.ext.sys.task.impl.WnSysTaskService',
        parent : "ioService",
        fields : {
            auth : {
                refer : "sysAuthService"
            }
        }
    },
    safeSysScheduleService : {
        type : 'org.nutz.walnut.ext.sys.schedule.impl.WnSafeSysScheduleService',
        fields : {
            schedules : {
                refer : 'sysScheduleService'
            },
            locks : {
                refer : 'lockApi'
            },
            tryLockDuration : {
                java : '$conf.getLong("sys-schedule-lockdu", 60000)'
            },
            tryAddTaskLockDuration : {
                java : '$conf.getLong("sys-schedule-task-lockdu", 10000)'
            },
            taskNotifyLock : {
                refer : 'safeSysTaskService'
            }
        }
    },
    sysScheduleService : {
        type : 'org.nutz.walnut.ext.sys.schedule.impl.WnSysScheduleService',
        parent : "ioService",
        fields : {
            auth : {
                refer : "sysAuthService"
            },
            cronApi : {
                refer : "sysCronService"
            },
            taskApi : {
                refer : "sysTaskService"
            }
        }
    },
    sysCronService : {
        type : 'org.nutz.walnut.ext.sys.cron.impl.WnSysCronService',
        parent : "ioService",
        fields : {
            auth : {
                refer : "sysAuthService"
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
        type : 'org.nutz.walnut.impl.lock.redis.QuickRedisLockApi',
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