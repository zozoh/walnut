define(function (require, exports, module) {
var methods = {
    events : {
        'click .crp-layout li' : function(){
            this.uiCom.highlightArea($(this).text());
        }
    },
    //...............................................................
    depose : function(){
        this.uiCom.highlightArea(false);
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 首先绘制块
        var jUl  = UI.arena.find(".crp-layout > ul").empty();
        var list = UI.uiCom.getAreaObjList();
        for(var ao of list) {
            var jLi = $('<li>').appendTo(jUl);
            if(ao.highlight)
                jLi.attr("highlight", "yes");
            jLi.text(ao.areaId);
        }

        // 最后在调用一遍 resize
        //UI.resize(true);
    }
    //...............................................................
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/support/hm__methods_panel");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});