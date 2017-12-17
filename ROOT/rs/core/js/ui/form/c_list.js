(function($z){
$z.declare([
    'zui',
    'ui/form/support/enum_list',
], function(ZUI, EnumListSupport){
//==============================================
var html = function(){/*
<div class="ui-arena com-list">
    <ul></ul>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_list", EnumListSupport({
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        $z.setUndefined(opt, "value", opt.text);

        this.__setup_dft_display_func(opt);

        $z.setUndefined(opt, "escapeHtml", true);
        $z.setUndefined(opt, "drawOnSetData", true);
    },
    //...............................................................
    events : {
        // 鼠标按下改变高亮 
        "mousedown li" : function(e){
            this.arena.find("[current]").removeAttr("current");
            $(e.currentTarget).attr("current", "yes");
        },
        // 完成点击
        "click li" : function(){
            this.__on_change();
            // 尝试让父控件（比如作为 c_input 的助理）也发出改变事件
            $z.invoke(this.parent, "__on_change");
        }
    },
    //...............................................................
    _before_load : function() {
        var pUI = this.parent;
        $z.invoke(pUI, "mergeAssist", [{
            padding : 0,
            closeOnChange : true,
            adaptEvents : {
                "UP"   : "selectPrev",
                "DOWN" : "selectNext",
            }
        }, true]);
    },
    //...............................................................
    selectPrev : function(){
        var UI  = this;
        var jLi = UI.arena.find("[current]");
        if(jLi.length == 0) {
            jLi = UI.arena.find("li:first-child");
        }
        // 选择前一个
        else {
            jLi = jLi.prev();
        }
        if(jLi.length > 0) {
            this.arena.find("[current]").removeAttr("current");
            jLi.attr("current", "yes");
            this.__on_change();
            return jLi;
        }
    },
    //...............................................................
    selectNext : function(){
        var UI  = this;
        var jLi = UI.arena.find("[current]");
        if(jLi.length == 0) {
            jLi = UI.arena.find("li:first-child");
        }
        // 选择前一个
        else {
            jLi = jLi.next();
        }
        if(jLi.length > 0) {
            this.arena.find("[current]").removeAttr("current");
            jLi.attr("current", "yes");
            this.__on_change();
            return jLi;
        }
    },
    //...............................................................
    _draw_items : function(items){
        var UI  = this;
        var opt = UI.options;
        var jUl = UI.arena.find("ul").removeAttr("empty").empty();
        var context = opt.context || UI.parent;

        if(!_.isArray(items) || items.length == 0) {
            jUl.attr("empty", "yes").text(UI.msg("empty"));
            return;
        }

        var hasIcon = false;
        for(var i=0; i<items.length; i++){
            var item = items[i];
            var val  = opt.value.call(context, item, i, UI); 

            var jLi = $('<li>').appendTo(jUl)
                .attr("index", i)
                .data("@VAL", val);

            // 图标
            var icon = _.isString(opt.icon)
                                ? $z.tmpl(opt.icon)(item)
                                : opt.icon.call(context, item, i, UI);
            jIcon = $('<span it="icon">').appendTo(jLi);
            if(_.isString(icon)){
                jIcon.html(icon);
                hasIcon = true;
            }
            //console.log(val)

            // 文字
            var text = val;
            if(_.isString(opt.text))
                text = $z.tmpl(opt.text)(item);
            else if(_.isFunction(opt.text))
                text = opt.text.call(context, item, i, UI);

            // 直接逃逸文字
            if(opt.escapeHtml) {
                $('<b it="text">').text(UI.text(text)).appendTo(jLi);
            }
            // 否则输出 HTML
            else {
                $('<b it="text">').html(UI.compactHTML(text)).appendTo(jLi);
            }
        }

        // 没有 Icon 就全部移除
        if(!hasIcon){
            UI.arena.find("span[it='icon']").remove();
        }
    },
    //...............................................................
    _get_data : function(){
        var UI  = this;
        var opt = UI.options;
        var jLi = this.arena.find("li[current]");
        var val = jLi.data("@VAL");

        if(opt.fullData) {
            return {
                text  : jLi.find('b[it]').text(),
                value : val
            }
        }

        return val;
    },
    //...............................................................
    __set_current : function(val) {
        var UI = this;
        var jLis = UI.arena.find("li").removeAttr("current");
        for(var i=0;i<jLis.length;i++){
            var jLi = jLis.eq(i);
            var v0  = jLi.data("@VAL");
            if(v0 == val){
                jLi.attr("current", "yes");
                break;
            }
        }
    },
    //...............................................................
    _set_data : function(val){
        var UI  = this;
        var opt = UI.options;

        // 如果自己的项目不是数组，则表示是动态数据
        if(!_.isArray(opt.items)) {
            var params;
            // 数字型的为可执行命令，为其准备参数
            if(_.isString(opt.items)) {
                params = _.extend({}, opt.itemArgs, {val:val})
            }
            // 否则，就将 val 作为参数
            else {
                params = val;
            }
            // 重新解析项目
            UI.setItems(opt.items, function(){
                UI.__set_current(val);
                // 尝试调用父控件的 redock，通常是作为 c_input 的 combolist 会用到
                $z.invoke(UI.parent, "redockAssist");
            }, params);
        }
        // 静态的，嗯，直接设置就好了
        else {
            UI.__set_current(val);
        }
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);