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
        type : 'com.site0.walnut.api.box.WnServiceFactory',
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
            },
            lockApi : {
                refer : "lockApi"
            }
        }
    },
    sysAuthService : {
        type : 'com.site0.walnut.impl.auth.WnSysAuthServiceWrapper',
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
        type : 'com.site0.walnut.ext.sys.task.impl.WnSafeSysTaskService',
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
        type : 'com.site0.walnut.ext.sys.task.impl.WnSysTaskService',
        parent : "ioService",
        fields : {
            auth : {
                refer : "sysAuthService"
            }
        }
    },
    safeSysScheduleService : {
        type : 'com.site0.walnut.ext.sys.schedule.impl.WnSafeSysScheduleService',
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
        type : 'com.site0.walnut.ext.sys.schedule.impl.WnSysScheduleService',
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
        type : 'com.site0.walnut.ext.sys.cron.impl.WnSysCronService',
        parent : "ioService",
        fields : {
            auth : {
                refer : "sysAuthService"
            }
        }
    },
    sessionService : {
        type : 'com.site0.walnut.impl.usr.IoWnSessionService',
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
        type : 'com.site0.walnut.impl.box.JvmExecutorFactory',
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
        type : 'com.site0.walnut.impl.box.JvmBoxService',
        args : [ {
            refer : "jvmExecutorFactory"
        } ]
    },
    hookService : {
        type : 'com.site0.walnut.impl.hook.CachedWnHookService',
        parent : "ioService",
        fields : {
            esi : {
                refer : "esi"
            }
        }
    },
    lookupMaker: {
		type : 'com.site0.walnut.lookup.impl.WnLookupMaker',
		args : [{refer : "io"}]
	},
    lockApi : {
        type : 'com.site0.walnut.impl.lock.redis.QuickRedisLockApi',
        args : [{
            refer : "redisConfForLockApi"
        }]
    },
    expiObjTable : {
        type : 'com.site0.walnut.core.eot.mongo.MongoExpiObjTable',
        args : [{
            java  : '$mongoDB.getCollection("expi")'
        }]
    },
    safeExpiObjTable : {
        type : 'com.site0.walnut.core.eot.WnSafeExpiObjTable',
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