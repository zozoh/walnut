var ioc = {
	mimes : {
		type : 'org.nutz.walnut.impl.io.MimeMapImpl',
		args : [ {
			type : 'org.nutz.ioc.impl.PropertiesProxy',
			args : [ {
				java : '$conf.get("mime","mime.properties")'
			} ]
		} ]
	},
	tree : {
		type : 'org.nutz.walnut.impl.io.mongo.MongoWnTree',
		args : [ {
			java : '$mongoDB.getCollection("obj")'
		}, {
			java : '$conf.rootTreeNode'
		}, {
			refer : 'mimes'
		} ]
	},
	store : {
		type : 'org.nutz.walnut.impl.io.WnStoreImpl',
		fields : {
			buckets : {
				type : 'org.nutz.walnut.impl.io.mongo.MongoLocalBucketManager',
				args : [ {
					java : '$conf.bucketHome'
				}, {
					java : '$mongoDB.getCollection("bucket")'
				} ]
			},
			handles : {
				type : 'org.nutz.walnut.impl.io.handle.WnHandleManagerImpl'
			}
		}
	},
	io : {
		type : 'org.nutz.walnut.impl.io.WnIoImpl',
		fields : {
			mimes : {
				refer : 'mimes'
			},
			tree : {
				refer : 'tree'
			},
			store : {
				refer : 'store'
			}
		}
	}
}