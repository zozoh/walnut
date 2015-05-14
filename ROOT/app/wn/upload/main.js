define(function(require, exports, module) {

	var UIDef  = require("ui/upload/upload");
	var ModDef = require("wn/walnut.client"); 
	
	function init(){
		var app = window._app;
		var Mod = new ModDef(app);
		// 有目标的话，直接渲染
		if(app.obj) {
			new UIDef({
	            $pel  : $(document.body),
	            model : Mod,
	            target : app.obj.ph
	        }).render();
		}
		// 没有的话，获取当前目录
		else{
			Mod.trigger("cmd:exec", "pwd", function(re){
				new UIDef({
		            $pel  : $(document.body),
		            model : Mod,
		            target : $.trim(re)
		        }).render();
			});
		}
    }

    exports.init = init;
});