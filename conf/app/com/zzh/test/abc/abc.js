define(function(require, exports, module) {
	
	var ZUI = require("zui")
	
	module.exports = ZUI.def("com.zzh.test.abc", {
		css  : "app/abc/abc.css",
		dom  : "app/abc/abc.html",
		i18n : "app/abc/i18n/{{lang}}.json",
		
		init : function(options){
			console.log("I am abc's init():");
			console.log(options);
		},
		
		redraw : function(){
			console.log("I am redraw!!");
		}
	});
	
});