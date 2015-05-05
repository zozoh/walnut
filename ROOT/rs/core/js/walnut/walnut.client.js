define(function(require, exports, module){
//======================================================================
    var Walnut = require("walnut");
    module.exports = Walnut.def("walnut.client", {
        // 初始化 ...
        init : function(app){
            this.set("history", []);
            // require(["ext/abc"], function(ABC){
            //     console.log(ABC);
            // });
        },
        //..............................................................
        resetCommandIndex : function(){
            var Mod = this;
            Mod.unset("his_index");
        },
        //..............................................................
        prevCommand : function(){
            var Mod = this;
            var history = Mod.get("history");
            if(!history || history.length == 0){
                return "";
            }
            var index = Mod.get("his_index") || 0;
            if(Math.abs(index) < history.length){
                index --;
                var n = history.length + index;
                if(n>=0){
                    //L("prev:" + index)
                    Mod.set("his_index", index);
                }
                return history[n];
            }
            return history[0];
        },
        //..............................................................
        nextCommand : function(){
            var Mod = this;
            var history = Mod.get("history");
            if(!history || history.length == 0 || !Mod.has("his_index")){
                return "";
            }
            var index = Mod.get("his_index");
            index ++;
            var n = history.length + index;
            if(n < history.length){
                //L("next:" + index)
                Mod.set("his_index", index);
                return history[n];
            }

            Mod.unset("his_index");
            return "";
        }
    });
//===================================================================
});