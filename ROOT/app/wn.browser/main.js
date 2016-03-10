define(function(require, exports, module) {

	var UI = require("ui/obrowser/obrowser");
	var Wn = require("wn/util");

	// 看看是不是要加载自定义的多国语言字符串
	var i18nStr = Wn.exec("appi18n " + window.$zui_i18n);
	if (!/^e[.]io/.test(i18nStr)) {
		i18n = $z.fromJson(i18nStr);
		_.extend(ZUI.g_msg_map, i18n);
	}

    function init() {
        new UI({
        	$pel  : $(document.body),
        	exec  : Wn.exec,
            app   : Wn.app(),
            editable : true,
            sidebar  : true,
            history  : true,
            canOpen  : function(o){
            	return true;
            },
            appSetup : "auto"
            //lastObjId : "app-browser"
        }).render(function(){
        	this.setData(Wn.app().obj);
        });
    }

//	function init() {
//		new UI({
//			$pel : $(document.body),
//			exec : Wn.exec,
//			app : Wn.app(),
//			history : true,
//			editable : true,
//			sidebar    : false,
//			skybar     : false,
//			footbar    : false,
//			history    : true,
//            multi      : false,
//			canOpen : function(o) {
//				return true;
//			},
//			appSetup : "auto"
//		//lastObjId : "app-browser"
//		}).render(function() {
//			this.setData(Wn.app().obj);
//		});
//	}

	exports.init = init;
});