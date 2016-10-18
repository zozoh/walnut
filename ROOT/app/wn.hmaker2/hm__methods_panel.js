define(function (require, exports, module) {
var methods = {
    // 设置面板标题
    setTitle : function(titleKey) {
        this.arena.find(">header .hmpn-tt").html(this.msg(titleKey));
    },
    // 发出块属性修改通知，固定添加忽略面板更新的的标识
    notifyBlockChange : function(prop) {
        if(prop)
            this.fire("change:block", _.extend(prop, {
                __prop_ignore_update : true
            }));
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    notifyComChange : function(com) {
        if(com)
            this.fire("change:com", _.extend(com, {
                __prop_ignore_update : true
            }));
    },
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/hm__methods");

//====================================================================
// 输出
module.exports = function(uiPanel){
    return _.extend(HmMethods(uiPanel), methods);
};
//=======================================================================
});