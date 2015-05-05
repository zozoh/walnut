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
	treeFactory : {
		type : 'org.nutz.walnut.impl.io.WnTreeFactoryImpl',
		args : [ {
			refer : 'mongoDB'
		} ]
	},
	iocMakeHolder : {
		type : 'org.nutz.walnut.web.WnIocMakeHolder',
		args : [ {
			refer : 'treeFactory'
		}, {
			java : '$conf.rootTreeNode'
		}, {
			refer : 'mongoDB'
		}, {
			java : '$conf.get("indexer-co","obj")'
		} ]
	},
	storeFactory : {
		type : 'org.nutz.walnut.impl.io.WnStoreFactoryImpl',
		args : [ {
			java : '$iocMakeHolder.indexer'
		}, {
			refer : 'mongoDB'
		}, {
			java : '$conf.check("local-sha1")'
		}, {
			java : '$conf.check("local-data")'
		} ]
	},
	io : {
		type : 'org.nutz.walnut.impl.io.WnIoImpl',
		fields : {
			mimes : {
				refer : 'mimes'
			},
			tree : {
				java : '$iocMakeHolder.tree'
			},
			indexer : {
				java : '$iocMakeHolder.indexer'
			},
			stores : {
				refer : 'storeFactory'
			}
		}
	}
}