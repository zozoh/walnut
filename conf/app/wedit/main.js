define(function(require, exports, module) {

	var UI  = require("ui/wedit/wedit");
	var Mod = require("walnut/walnut.obj"); 
	
	function init(){
        new UI({
            $pel  : $("#app"),
            model : new Mod(window._app)
        }).render();
    }

    exports.init = init;
});