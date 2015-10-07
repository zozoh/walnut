(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <table class="menu-top-table">
        <tr class="menu-tops"></tr>
    </table>
</div>
*/};
//==============================================
return ZUI.def("ui.menu", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/menu/menu.css",
    //..............................................
    events : {
        "click .menu-item" : function(e){
            this.fire(e.currentTarget);
        }
    },
    //..............................................
    fire : function(item){
        var jq = $(item);
        if(!jq.hasClass("menu-item")){
            jq = jq.parents(".menu-item");
        }
        if(jq.size() == 0)
            throw "ui.menu said: I can not fire : " + item;

        var tp = jq.attr("tp");
        var mi = jq.data("@DATA");
        // 按钮
        if("button" == tp){
            var UI = ZUI.checkInstance(jq);
            var context = mi.context || UI.options.context || UI.parent || UI;
            if(_.isFunction(mi.handler)){
                mi.handler.apply(context, mi.args || []);
            }
        }
        // 处理不了
        else{
            throw "ui.menu said: I can not fire item type '" + tp +"' : " + item;
        }
        
    },
    //..............................................
    redraw : function(){
        var UI = this;
        var items = UI.options.setup;
        if(!_.isArray(items)){
            items = items ? [items] : [];
        }
        var jTops = UI.arena.find(".menu-tops").empty();
        for(var i=0; i<items.length; i++){
            var mi = items[i];
            // button
            if(mi.type == "button" || _.isFunction(mi.handler)){
                UI.__draw_button(mi, jTops);
            }
            // 靠，不认识
            else{
                throw "ui.menu said: AO! Dont know this: " + $z.toJson(mi);
            }
        }
        console.log("I am menu redraw")
    },
    //..............................................
    __draw_button : function(mi, jP){
        var UI = this;
        var isTopItem = jP.hasClass("menu-tops");
        var jq = isTopItem ? $('<td>') : $('<div>');
        jq.addClass("menu-item").attr("tp","button");
        // 图标：不是顶层项目，一律添加图标以便下拉时对其
        if(!isTopItem){
            jq.attr("md", "sub");
            var jIcon = $('<span class="menu-item-icon">').appendTo(jq);
            if(mi.icon){
                jIcon.html(mi.icon);
            }
        }
        // 顶层项目添加特殊属性
        else{
            jq.attr("md", "top");
        }
        // 文字
        if(mi.text){
            var jT = $('<span class="menu-item-text">').appendTo(jq);
            jT.text(UI.text(mi.text));
        }
        // 函数
        if(_.isFunction(mi.handler))
            jq.data("@DATA", mi);
        else
            throw "ui.menu said: button item require handler as a function, but it is: " + mi.handler;
        // 附加到 DOM 树
        jq.appendTo(jP);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);