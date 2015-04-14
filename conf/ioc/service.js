var ioc = {
	zIoService : {
		fields : {
			io : {
				refer : "io"
			}
		}
	},
	usrService : {
		type : 'org.nutz.walnut.api.usr.ZUsrService',
		parent : "zIoService",
		fields : {
			regexUsrName : {
				java : '$conf.get("regex-usr")'
			},
			regexPassword : {
				java : '$conf.get("regex-pwd")'
			}
		}
	},
	sessionService : {
		type : 'org.nutz.walnut.api.usr.ZSessionService',
		parent : "zIoService",
		fields : {
			usrs : {
				refer : "usrService"
			},
			sessionExpired : {
				java : '$conf.getInt("session-expired")'
			}
		}
	},
	processService : {
		type : 'org.nutz.walnut.impl.light.LightProcessService',
		parent : "zIoService",
		fields : {
			ioc : {
				refer : "$Ioc"
			},
			sess : {
				refer : "sessionService"
			}
		}
	}

}