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
			authApi: { refer: "sysAuthService" },
			taskApi: { refer: "safeSysTaskService" },
			scheduleApi: { refer: "safeSysScheduleService" },
			loginApi: { refer: "sysLoginApi" },
			cronApi: { refer: "sysCronService" },
			boxApi: { refer: "boxService" },
			hookApi: { refer: "hookService" },
			referApi: { refer: "referApi" },
			lockApi: { refer: "lockApi" }
		}
	},
	sysUserStoreService: {
		type: 'com.site0.walnut.login.usr.WnStdUserStore',
		args: [{ refer: "io" }]
	},
	sysSessionStoreService: {
		type: 'com.site0.walnut.login.session.WnStdSessionStore',
		args: [{ refer: "io" }],
		fields: {
			defaultEnv: { java: '$conf.sessionDefaultEnv' }
		}
	},
	sysXApi: {
		type: 'com.site0.walnut.ext.net.xapi.impl.WnXApi',
		args: [{ refer: "io" }]
	},
	sysLoginSetup: {
		type: 'com.site0.walnut.login.WnLoginSetup',
		fields: {
			users: { refer: "sysUserStoreService" },
			sessions: { refer: "sysSessionStoreService" },
			xapi: { refer: "sysXApi" },
			domain: "root",
			sessionDuration: { java: '$conf.getLong("se-sys-du", 3600)' },
			wechatMpOpenIdKey: {
				java: '$conf.get("login-wxmp-openid-key", "wxmp_openid")'
			},
			wechatGhOpenIdKey: {
				java: '$conf.get("login-wxgh-openid-key", "wxgh_openid")'
			},
		}
	},
	sysLoginApi: {
		type: 'com.site0.walnut.login.WnLoginApi',
		args: [{ refer: 'sysLoginSetup' }]
	},
	sysAuthService: {
		type: 'com.site0.walnut.impl.auth.WnSysAuthServiceWrapper',
		parent: "ioService",
		fields: {
			initEnvs: {
				java: '$conf.initUsrEnvs'
			},
			rootDefaultPasswd: {
				java: '$conf.getTrim("root-init-passwd", "123456")'
			},
			seDftDu: {
				java: '$conf.getLong("se-sys-du", 3600)'
			},
			seTmpDu: {
				java: '$conf.getLong("se-tmp-du", 60)'
			},
			defaultQuitUrl: {
				java: '$conf.get("sys-entry-url", "/a/login/")'
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
			auth: { refer: "sysAuthService" }
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
			auth: { refer: "sysAuthService" },
			cronApi: { refer: "sysCronService" },
			taskApi: { refer: "sysTaskService" }
		}
	},
	sysCronService: {
		type: 'com.site0.walnut.ext.sys.cron.impl.WnSysCronService',
		parent: "ioService",
		fields: {
			auth: { refer: "sysAuthService" }
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