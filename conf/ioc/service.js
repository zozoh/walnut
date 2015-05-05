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
			},
			regexName : {
				java : '$conf.get("usr-name")'
			},
			regexPhone : {
				java : '$conf.get("usr-phone")'
			},
			regexEmail : {
				java : '$conf.get("usr-email")'
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
	boxService : {
		type : 'org.nutz.walnut.impl.box.JvmBoxService',
		args : [ {
			type : 'org.nutz.walnut.impl.box.JvmExecutorFactory',
			fields : {
				scanPkgs : {
					java : '$conf.jvmboxPkgs'
				}
			}
		} ]
	}

}