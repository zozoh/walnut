define(function (require, exports, module) {

    var UI = require("ui/console/console");

    function init() {
        new UI({$pel: $(document.body)}).render();
    }

    exports.init = init;
});