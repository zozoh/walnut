define(function (require, exports, module) {
    exports.open = function ($sel, obj) {
        var UI = require("ui/wedit/wedit");
        var Mod = require("wn/walnut.obj");

        window._app.obj = obj;

        new UI({
            $pel: $sel,
            model: new Mod(window._app)
        }).render();
    }
});
