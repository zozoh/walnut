var ioc = {
	mime : {
		type : 'org.nutz.walnut.impl.light.LightMimeMap',
		args : [ {
			type : 'org.nutz.ioc.impl.PropertiesProxy',
			args : [ 'mime.properties' ]
		} ]
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
	},
	io : {
		type : 'org.nutz.walnut.impl.light.LightIo',
		fields : {
			bufferSize : 8192,
			mime : {
				refer : 'mime'
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
				refer : "persist"
			},
			co : {
				java : '$lightDB.db.cc("obj",false)'
			},
			chis : {
				java : '$lightDB.db.cc("his",false)'
			}
		}
	}
}