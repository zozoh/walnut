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
        	this.setData(Wn.app().obj);
        });
    }

    exports.init = init;
});