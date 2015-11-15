var ioc = {
	conf : {
		type : 'org.nutz.walnut.web.WnConfig',
		args : [ 'web.properties' ]
	},
	webscan : {
		type : 'org.nutz.walnut.web.WnModuleScanner',
		fields : {
			pkgs : {
				java : '$conf.webModulePkgs'
			}
		}
	},
	loader : {
		type : 'org.nutz.walnut.web.WnIocLoader',
		args : [ {
			refer : 'conf'
		} ]
	}
};