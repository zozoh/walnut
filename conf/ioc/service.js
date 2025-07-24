var ioc = {
	ioService: {
		events: {
			create: "on_create"
		},
		fields: {
			io: { refer: "io" }
		}
	},
	serviceFactory: {
		type: 'com.site0.walnut.api.box.WnServiceFactory',
		fields: {
			hookApi: { refer: "hookService" },
			loginApi: { refer: "sysLoginApi" },
			taskApi: { refer: "safeSysTaskService" },
			scheduleApi: { refer: "safeSysScheduleService" },
			cronApi: { refer: "sysCronService" },
			boxApi: { refer: "boxService" },
			referApi: { refer: "referApi" },
			lockApi: { refer: "lockApi" }
		}
	},
	rawUserStore: {
		type: 'com.site0.walnut.login.usr.WnStdUserStore',
		args: [{ refer: "io" }, { refer: "conf" }]
	},
	sysUserStore: {
		type: 'com.site0.walnut.login.usr.WnUserStoreProxy',
		args: [{ refer: "io" }, { refer: "rawUserStore" }]
	},
	rawSessionStore: {
		type: 'com.site0.walnut.login.session.WnStdSessionStore',
		args: [{ refer: "io" }, { refer: "conf" }]
	},
	sysSessionStore: {
		type: 'com.site0.walnut.login.session.WnSessionStoreProxy',
		args: [{ refer: "io" }, { refer: "rawSessionStore" }]
	},
	rawRoleStore: {
		type: 'com.site0.walnut.login.role.WnStdRoleStore',
		args: [{ refer: "io" }]
	},
	sysRoleStore: {
		type: 'com.site0.walnut.login.role.WnRoleStoreProxy',
		args: [{ refer: "io" }, { refer: "rawRoleStore" }]
	},
	sysXApi: {
		type: 'com.site0.walnut.ext.net.xapi.impl.WnXApi',
		args: [{ refer: "io" }]
	},
	sysLoginApi: {
		type: 'com.site0.walnut.login.WnSimpleLoginApi',
		args: [{ refer: 'io' }],
		fields: {
			users: { refer: 'sysUserStore' },
			sessions: { refer: 'sysSessionStore' },
			roles: { refer: 'sysRoleStore' },
			xapi: { refer: 'sysXApi' },
			domain: "root",
			sessionDuration: {
				java: "$conf.getInt('se-sys-du', 3600)"
			},
			sessionShortDu: {
				java: "$conf.getInt('se-tmp-du', 10)"
			},
			wechatMpOpenIdKey: {
				java: "$conf.get('wx-mp-open-id-key', 'wxmp_openid')"
			},
			wechatGhOpenIdKey: {
				java: "$conf.get('wx-gh-open-id-key', 'wxgh_openid')"
			}
		}
	},
	safeSysTaskService: {
		type: 'com.site0.walnut.ext.sys.task.impl.WnSafeSysTaskService',
		fields: {
			tasks: { refer: 'sysTaskService' },
			locks: { refer: 'lockApi' },
			tryLockDuration: {
				java: '$conf.getLong("sys-task-lockdu", 3000)'
			}
		}
	},
	sysTaskService: {
		type: 'com.site0.walnut.ext.sys.task.impl.WnSysTaskService',
		parent: "ioService",
		fields: {
			auth: { refer: "sysLoginApi" }
		}
	},
	safeSysScheduleService: {
		type: 'com.site0.walnut.ext.sys.schedule.impl.WnSafeSysScheduleService',
		fields: {
			schedules: { refer: 'sysScheduleService' },
			locks: { refer: 'lockApi' },
			tryLockDuration: {
				java: '$conf.getLong("sys-schedule-lockdu", 60000)'
			},
			tryAddTaskLockDuration: {
				java: '$conf.getLong("sys-schedule-task-lockdu", 10000)'
			},
			taskNotifyLock: { refer: 'safeSysTaskService' }
		}
	},
	sysScheduleService: {
		type: 'com.site0.walnut.ext.sys.schedule.impl.WnSysScheduleService',
		parent: "ioService",
		fields: {
			auth: { refer: "sysLoginApi" },
			cronApi: { refer: "sysCronService" },
			taskApi: { refer: "sysTaskService" }
		}
	},
	sysCronService: {
		type: 'com.site0.walnut.ext.sys.cron.impl.WnSysCronService',
		parent: "ioService",
		fields: {
			auth: { refer: "sysLoginApi" }
		}
	},
	sessionService: {
		type: 'com.site0.walnut.impl.usr.IoWnSessionService',
		parent: "ioService",
		fields: {
			usrs: { refer: "usrService" },
			duration: {
				java: '$conf.getInt("se-duration", 3600000)'
			}
		}
	},
	jvmExecutorFactory: {
		type: 'com.site0.walnut.impl.box.JvmExecutorFactory',
		fields: {
			scanPkgs: { java: '$conf.jvmboxPkgs' },
			ioc: { refer: '$Ioc' }
		}
	},
	boxService: {
		type: 'com.site0.walnut.impl.box.JvmBoxService',
		args: [{ refer: "jvmExecutorFactory" }]
	},
	hookService: {
		type: 'com.site0.walnut.impl.hook.CachedWnHookService',
		parent: "ioService",
		fields: {
			esi: { refer: "esi" }
		}
	},
	lookupMaker: {
		type: 'com.site0.walnut.lookup.impl.WnLookupMaker',
		args: [{ refer: "io" }]
	},
	lockApi: {
		type: 'com.site0.walnut.impl.lock.redis.QuickRedisLockApi',
		args: [{ refer: "redisConfForLockApi" }]
	},
	expiObjTable: {
		type: 'com.site0.walnut.core.eot.mongo.MongoExpiObjTable',
		args: [{
			java: '$mongoDB.getCollection("expi")'
		}]
	},
	safeExpiObjTable: {
		type: 'com.site0.walnut.core.eot.WnSafeExpiObjTable',
		fields: {
			table: { refer: 'expiObjTable' },
			locks: { refer: 'lockApi' },
			tryLockDuration: {
				java: '$conf.getLong("sys-expi-obj-table-lockdu", 5000)'
			}
		}
	}
}