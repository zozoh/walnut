define(function (require, exports, module) {
var jType = require('ui/jtypes');
// ....................................
// 方法表
var methods = {
    //...............................................................
    // 接受 @xxx 形式的字符串，如果不是，返回 null
    __get_fld_quick_uiType : function(str) {
        if(!str || !/^@/.test(str)) {
            return null;
        }
        str = str.substring(1);
        // 各种 picker
        if(/picker$/.test(str)){
            return "ui/picker/" + str;
        }
        // 其他的算作内置类型
        return "ui/form/c_" + str;
    },
    //...............................................................
    _get_fld_uiType : function(fld, dftUIType) {
        var uiType = this.__get_fld_quick_uiType(fld.uiType);
        // 快捷方式
        if(uiType) {
            return uiType;
        }
        // 有了明确定义直接返回
        if(fld.uiType)
            return fld.uiType;

        // 之前老版本的快捷方式
        if(fld.editAs) {
            return this.__get_fld_quick_uiType("@" + fld.editAs);
        }

        // 用默认值
        return dftUIType || "ui/form/c_label";
    },
    //...............................................................
    _normalize_fld_define : function(fld, dftUIType) {
        fld.type = fld.type || "string";
        fld.uiType = this._get_fld_uiType(fld, dftUIType);
        fld.uiConf = fld.uiConf || {};
        fld.JsObjType = jType(fld);
        return fld;
    },
    //...............................................................
    _load_fld_ui : function(fld, callback) {
        var uiType = fld.uiType;
    }
    //...............................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
