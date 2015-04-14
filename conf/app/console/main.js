define(function (require, exports, module) {

    var UI = require("ui/console/console");
    var Mod = require("walnut/walnut.client");


    function init() {
        new UI({
            $pel: $("#app"),
            model: new Mod(window._app)
        }).render();
    }

    exports.init = init;
});