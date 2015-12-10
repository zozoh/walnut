define(function (require, exports, module) {

    var UI = require("ui/obrowser/obrowser");
    var Wn = require("wn/util");

    function init() {
        new UI({
        	$pel  : $(document.body),
        	exec  : Wn.exec,
            app   : Wn.app()
        }).render();
    }

    exports.init = init;
});