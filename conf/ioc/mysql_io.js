var ioc = {
	dataSource : {
		type : "com.alibaba.druid.pool.DruidDataSource",
		events : {
			depose : 'close'
		},
		fields : {
			driverClassName : "com.mysql.jdbc.Driver",
			url : "jdbc:mysql://localhost/walnut",
			username : "root",
			password : "root"
		}
	},
	dao : {
		type : "org.nutz.dao.impl.NutDao",
		args : [ {
			refer : "dataSource"
		} ]
	},
	// ----------------------------------------- IO
	io2 : {
		// type : 'org.nutz.walnut.impl.light.LightIo',
		type : 'org.nutz.walnut.impl.mysql.MysqlIo',
		events : {
			create : "init"
		},
		fields : {
			dao : {
				refer : "dao"
			},
			contexts : {
				type : 'org.nutz.walnut.impl.light.LightIoContextManager'
			},
			sync : {
				type : 'org.nutz.walnut.impl.light.LightSync'
			},
			swaps : {
				type : 'org.nutz.walnut.impl.light.LightIoSwap',
				fields : {
					home : {
						java : '$conf.swapHome'
					},
					writeBufferSize : 8192,
					deleteCateIfEmpty : true
				}
			},
			persist : {
				type : 'org.nutz.walnut.impl.light.LightIoPersistence',
				fields : {
					home : {
						java : '$conf.dataHome'
					},
					writeBufferSize : 8192,
					deleteCateIfEmpty : true
				}
			}
		}
	}
}