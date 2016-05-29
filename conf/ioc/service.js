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
		fields : {
			io : {
				refer : "io"
			},
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
			},
			sessionHome : {
				java : '$conf.get("se-home", "/sys/session")'
			}
		}
	},
	boxService : {
		type : 'org.nutz.walnut.impl.box.JvmBoxService',
		args : [ {
			type : 'org.nutz.walnut.impl.box.JvmExecutorFactory',
			fields : {
				scanPkgs : {
					java : '$conf.jvmboxPkgs'
				},
				ioc : {
					refer : '$Ioc'
				}
			}
		} ]
	},
	hookService : {
		type : 'org.nutz.walnut.impl.hook.CachedWnHookService',
		parent : "ioService"
	}

}