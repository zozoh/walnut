define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();

        // 和旧值比较一下，有更新才通知
        if(_.isFunction(UI.__equals)){
            if(UI.__equals(v, UI.__old_val))
                return;
        }
        // 直接比较: undefined
        else if(_.isUndefined(v) && _.isUndefined(UI.__old_val)) {
            return;
        }
        // 直接比较: null
        else if(_.isNull(v) && _.isNull(UI.__old_val)) {
            return;
        }
        // 直接比较
        else if(v == UI.__old_val){
            return;
        }

        // 更新旧值
        UI.__old_val = v;

        // 通知
        $z.invoke(opt, "on_change", [v], context);

        // 看看控件还有没有后续处理
        $z.invoke(UI, "on_after_change", [v]);
        // UI.trigger("change", v);
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到顶级方法表
var ParentMethods = require("ui/form/support/form_methods");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
