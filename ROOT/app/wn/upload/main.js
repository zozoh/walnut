define(function(require, exports, module) {

	var UIUpload  = require("ui/upload/upload");
	var ModDef = require("wn/walnut.client"); 
	
	function init(){
		var app = window._app;
		var Mod = new ModDef(app);
		// 有目标的话，直接渲染
		if(app.obj) {
			new UIUpload({
	            $pel    : $(document.body),
	            model   : Mod,
	            target  : app.obj
	        }).render();
		}
		// 没有的话，获取当前目录
		else{
			Mod.trigger("cmd:exec", "obj . -q", function(re){
				new UIUpload({
					replaceable : true,
		            $pel        : $(document.body),
		            model       : Mod,
		            target      : $z.fromJson(re)
		        }).render();
			});
		}
    }

    exports.init = init;
});