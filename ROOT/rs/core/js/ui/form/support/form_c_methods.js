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
        var v_old = UI.__old_val;
        //console.log(v, v_old)

        // 和旧值比较一下，有更新才通知
        if(_.isFunction(UI.__equals)){
            if(UI.__equals(v, v_old))
                return;
        }
        // 直接比较: undefined
        else if(_.isUndefined(v)) {
            if(_.isUndefined(v_old))
                return;
        }
        // 直接比较: null
        else if(_.isNull(v)) {
            if(_.isNull(v_old))
                return;
        }
        // 比较数组
        else if(_.isArray(v) && _.isArray(v_old)) {
            if(v.length == v_old.length) {
                var is_equals = true;
                for(var i=0; i<v.length; i++) {
                    if(v[i] != v_old[i]){
                        is_equals = false;
                        break;
                    }
                }
                if(is_equals)
                    return;
            }
        }
        // 比较日期
        else if(_.isDate(v) && _.isDate(v_old)) {
            if(v.getTime() == v_old.getTime())
                return;
        }
        // 直接比较
        else if(v == v_old){
            return;
        }

        // 更新旧值
        UI.__old_val = v;

        // 通知
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger('change:value', v);

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
