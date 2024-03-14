var ioc = {
	conf : {
		type : 'com.site0.walnut.web.WnConfig',
		args : [ 'web.properties' ]
	},
	webscan : {
		type : 'com.site0.walnut.web.WnModuleScanner',
		fields : {
			pkgs : {
				java : '$conf.webModulePkgs'
			}
		}
	},
	loader : {
		type : 'com.site0.walnut.web.WnIocLoader',
		args : [ {
			refer : 'conf'
		} ]
	}
};