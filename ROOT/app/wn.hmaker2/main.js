define(function(require, exports, module) {

	var UI = require("ui/obrowser/obrowser");
	var Wn = require("wn/util");

	// 标记顶级 app 的名称, 以便 Wn.update_wnobj_thumbnail 使用
	window.wn_browser_appName = "wn.hmaker2";

	// 看看是不是要加载自定义的多国语言字符串
	var i18nStr = Wn.exec("app i18n -merge " + window.$zui_i18n);
	if (!/^e[.]io/.test(i18nStr)) {
		i18n = $z.fromJson(i18nStr);
		_.extend(ZUI.g_msg_map, i18n);
	}

	function init() {
		new UI({
			$pel : $(".ui-body"),
			exec : Wn.exec,
			app : Wn.app(),
			renameable : true,
			sidebar : {
				path : "/app/wn.hmaker2/sidebar.js"
			},
			history : true,
			myInfo : {
				logout : "/u/do/logout",
				avata : '<i class="fa fa-user"></i>',
				name : Wn.app().session.me,
			},
			canOpen : function(o) {
				return true;
			},
			appSetup : "auto",
		}).render(function() {
			this.setData(Wn.app().obj, function() {
			//$('.menu-item[md="top"]').eq(1).click();
			});
		});
	}

	exports.init = init;
});