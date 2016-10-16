define(function (require, exports, module) {

    var WJSON = require("ui/wjson/wjson");
    var Wn = require("wn/util");

    function init() {
        new WJSON({
            $pel: $(document.body),
            exec: Wn.exec,
            app: Wn.app(),
            sjson: true,
            sroot: false
        }).render();
    }

    exports.init = init;
});