define(function (require, exports, module) {

    var UI = require("ui/console/console");
    var Wn = require("wn/util");

    function init() {
        new UI({
        	$pel  : $(document.body),
        	exec  : Wn.exec,
            app   : Wn.app()
        }).render(function(){
        	this.on_cmd_wait();
        });
    }

    exports.init = init;
});