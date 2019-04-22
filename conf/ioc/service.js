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
	}
}