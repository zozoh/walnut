define(function(require, exports, module) {


    function init() {
        console.log("I am main!");

        var Abc = require("./abc/abc");
        console.log(Abc);

        console.log("new Abc()");
        var abc = new Abc({
            $pel : $("#app")
        });

        console.log("render abc");
        abc.render();

        console.log("main DONE!");
    }

    exports.init = init;
    
});