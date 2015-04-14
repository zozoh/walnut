var ioc = {
	lightDB : {
		type : 'org.nutz.walnut.impl.light.LightDB',
		events : {
			create : "on_create",
			depose : "on_depose"
		},
		fields : {
			host : {
				java : '$conf.get("mongo-host")'
			},
			port : {
				java : '$conf.getInt("mongo-port", -1)'
			},
			usr : {
				java : '$conf.get("mongo-usr")'
			},
			pwd : {
				java : '$conf.get("mongo-pwd")'
			},
			db : {
				java : '$conf.get("mongo-db")'
			}
		}
	}
}