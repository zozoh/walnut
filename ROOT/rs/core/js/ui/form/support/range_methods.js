define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    __show_data : function() {
        var jSpan = this.arena.find(">footer span");

        // 得到旧值
        var old_v = jSpan.text();

        // 更新显示
        jSpan.text(this.getData() || this.msg("com.range.empty"));

        // 如果新值有变化，触发事件
        if(old_v != jSpan.text()) {
            this.__on_change();
        }
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jS = UI.arena.find(">section");
        var jF = UI.arena.find(">footer");
        var H  = UI.arena.height();
        jS.css("height", H - jF.outerHeight(true));

        var jDl = jS.find(">dl");
        var jDt = jDl.find(">dt");
        H = jDl.height();
        jDl.find(">dd").css({
            "width"  : jDt.outerWidth(true),
            "height" : H - jDt.outerHeight(true),
        });

        // 子类特殊的修改尺寸
        $z.invoke(UI, "__on_resize", []);
    },
    //...............................................................
    _set_data : function(s){
        var UI = this;
        // 空值
        if(!s) {
            UI._set_value();
        }

        // 分析
        var range = {};
        var m = /^[ ]*([\[\(])[ ]*([^,]*)[ ]*,[ ]*([^,]*)[ ]*([\)\]])[ ]*$/.exec(s);
        if(m) {
            UI._set_value(m[1]=="[", m[2], m[3], m[4]=="]");
        }
        // 否则清空
        else {
            UI._set_value();
        }

    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到顶级方法表
var ParentMethods = require("ui/form/support/form_ctrl");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
