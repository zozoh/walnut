define(function(require, exports, module) {

	var UI  = require("ui/wedit/wedit");
	var Mod = require("wn/walnut.obj"); 
	
	function init(){
        new UI({
            $pel  : $(document.body),
            model : new Mod(window._app)
        }).render();
    }

    exports.init = init;
});