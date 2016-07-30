define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    }
}; // ~End wn
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmUiMethods = require("app/wn.hmaker2/hm_ui_methods");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmUiMethods(uiPanel), methods);
};
//=======================================================================
});