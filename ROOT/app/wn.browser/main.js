define(function (require, exports, module) {

    var UI = require("ui/obrowser/obrowser");
    var Wn = require("wn/util");

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
        	var UI = this;
        	UI.setData(Wn.app().obj);
        	//UI.setData("~/www/admin/index.wnml");
        	//UI.setData("id:3ph3gd633kjvvr66lhq3ma3g9m");
        	window.setTimeout(function(){
        		//console.log(UI.getPathObj())
        	},1000);
        });
    }

    exports.init = init;
});