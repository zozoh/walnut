/*
基本是用来占位用的 UI，用法为
new UIFake({
    text : "some text"
}).render();
就是在预定的区域输出一段文字
*/
define(function(require, exports, module) {
var html = function(){/*
<div class="ui-arena" ui-fitparent="true" style="text-align:center;"></div>
*/};
//===================================================================
var ZUI = require("zui");
module.exports = ZUI.def("ui.fake", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(options){
        this.options.fitparent = true;
    },
    //...............................................................
    redraw : function() {
        this.arena.text(this.options.text || "fake area");      
    },
    //...............................................................
    resize : function(){
        this.arena.css("padding-top", this.arena.height() * 0.3);
    }
    //...............................................................
});
//===================================================================
});