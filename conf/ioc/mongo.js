var ioc = {
	mongoDB : {
		type : 'com.site0.walnut.util.MongoDB',
		events : {
			create : "on_create",
			depose : "on_depose"
		},
		fields : {
			host : {
				java : '$conf.get("mongo-host")'
			},
			port : {
				java : '$conf.getInt("mongo-port")'
			},
			usr : {
				java : '$conf.get("mongo-usr")'
			},
			pwd : {
				java : '$conf.get("mongo-pwd")'
			},
			db : {
				java : '$conf.get("mongo-db")'
			},
			uri : {
				java : '$conf.get("mongo-uri")'
			}
		}
	}
}