define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    // 设置默认取值方法
    init : function(opt){
        this.__setup_dft_display_func(opt);
    },
    //...............................................................
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul").empty();
        var context = opt.context || UI.parent;

        if(!_.isArray(items))
            return;

        var hasIcon = false;
        for(var i=0; i<items.length; i++){
            var item = items[i];
            var val  = opt.value.call(context, item, i, UI); 

            var jLi = $('<li>').appendTo(jUl)
                .attr("index", i)
                .data("@VAL", val);

            // 选择框
            $('<span it="but">'+UI._list_item_icon+'</span>').appendTo(jLi);

            // 图标
            var icon = _.isString(opt.icon)
                                ? $z.tmpl(opt.icon)(item)
                                : opt.icon.call(context, item, i, UI);
            jIcon = $('<span it="icon">').appendTo(jLi);
            if(_.isString(icon)){
                jIcon.html(icon);
                hasIcon = true;
            }

            // 文字
            var text = val;
            if(_.isString(opt.text))
                text = $z.tmpl(opt.text)(item);
            else if(_.isFunction(opt.text))
                text = opt.text.call(context, item, i, UI);

            $('<b it="text">').text(UI.text(text)).appendTo(jLi);
        }

        // 没有 Icon 就全部移除
        if(!hasIcon){
            UI.arena.find("span[it='icon']").remove();
        }
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到枚举列表的顶级方法 
var ParentMethods = require("ui/form/support/enum_list");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
