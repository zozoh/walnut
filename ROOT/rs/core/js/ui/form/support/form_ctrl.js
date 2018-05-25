define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI = this;
        // 存储旧值
        UI.__old_val = val;
        // 设置到控件
        this.ui_parse_data(val, function(v){
            UI._set_data(v, jso);
        });
    }
    //...............................................................
}; // ~End methods
//====================================================================
// 得到顶级方法表
var ParentMethods = require("ui/form/support/form_c_methods");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
