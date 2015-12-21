(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena menu"></div>
*/};
//==============================================
return ZUI.def("ui.menu", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/menu/menu.css",
    //..............................................
    init : function(){
        // 注册全局事件，控制子菜单的关闭
        var on_close_group = function(e){
            this.closeGroup(this.arena.find(".menu-item[open]"));
        };
        this.watchMouse("click", on_close_group);
        this.watchKey(27, on_close_group);
    },
    //..............................................
    events : {
        "click .menu-item[tp=button]" : function(e){
            this.fireButton(e.currentTarget);
        },
        "click .menu-item[tp=group]" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            var is_top_item = jq.parent().hasClass("menu");
            // 顶级菜单是防止冒泡的
            if(is_top_item){
                e.stopPropagation();
            }

            // 点击事件实际上是 toggle 菜单组
            if(jq.attr("open")){
                UI.closeGroup(jq);
            }else{
                UI.openGroup(jq);
            }
        },
        "mouseenter .menu-item[md=sub]" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);
            // 子组，进入时有特殊操作
            if(jq.attr("tp") == "group"){
                if(jq.attr("open")){
                    return;
                }
                // 关闭其他的组
                UI.closeGroup(jq.parent().children(".menu-item[open]"));
                // 打开自己
                UI.openGroup(jq);
            }
            // 其他的项目，仅仅是关闭组就好了
            else{
                UI.closeGroup(jq.parent().children(".menu-item[open]"));
            }
        },
        "click .menu-item-si" : function(e){
            var jq = $(e.currentTarget);
            if(!jq.hasClass("menu-item-si-on")){
                var jItem = jq.parents(".menu-item");
                var mi    = jItem.data("@DATA");
                var val   = jq.attr("val");
                jItem.find(".menu-item-si").removeClass("menu-item-si-on");
                jq.addClass("menu-item-si-on");

                var context = this.options.context || this.parent || this;
                if(_.isFunction(context.trigger)){
                    context.trigger("menu:"+(mi.key||"status"), val);
                }
            }
        }
    },
    //..............................................
    fireButton : function(ele){
        var UI = this;
        var jq = $(ele);
        var mi = jq.data("@DATA");
        var context = UI.options.context || UI.parent || UI;
        if(_.isFunction(mi.handler)){
            mi.handler.apply(context, [jq, mi]);
        }
    },
    //..............................................
    // 假想给定的 ele 是 .ment-item
    closeGroup : function(ele){
        var jq = $(ele);
        if(jq.hasClass("menu-item")){
            jq.removeAttr("open");
            jq.children(".menu-sub").remove();   
            return;
        }
    },
    //..............................................
    openGroup : function(ele){
        var UI = this;
        var jq = $(ele);
        var mi = jq.data("@DATA");
        var context = UI.options.context || UI.parent || UI;
        // 如果菜单组已经被打开了，忽略
        if(jq.attr("open"))
            return;

        // 找到其他的打开的菜单组，将其关闭
        UI.closeGroup(jq.parent().children(".menu-item[open]"));

        // 标记菜单组状态
        jq.attr("open", true);

        // 创建菜单组
        var jTa = $('<div class="menu-sub">').appendTo(jq);

        // 绘制的回调
        var do_draw_sub = function(items){
            jTa.empty();
            UI._draw_items(items, jTa);

            // 准备停靠，根据菜单的深度增加 z-index，以便能够遮盖
            var depth = jq.parents(".menu-sub").size();
            $z.dock(jq, jTa.css("z-index", 1000+depth), depth>0?"V":"H");
        };

        // 动态创建
        if(_.isFunction(mi.items)){
            jTa.text(UI.msg("loading"));
            mi.items.apply(context,[jq, mi, do_draw_sub]);
        }
        // 直接显示
        else{
            do_draw_sub(mi.items);
        }
    },
    //..............................................
    redraw : function(){
        var UI = this;
        var items = UI.options.setup;
        // 清空内容，依次重绘
        UI.arena.empty();
        UI._draw_items(items, UI.arena);
    },
    //..............................................
    _draw_items : function(items, jP){
        var UI = this;
        var context = UI.options.context || UI.parent || UI;

        // 弄个开关，防止重绘分隔符
        var last_is_separator;

        // 循环绘制每个项目
        for(var i=0; i<items.length; i++){
            var mi = items[i];
            
            // 看看是否有必要调用初始化函数
            $z.invoke(mi, "init", [mi], context);

            // 绘制主体
            mi.is_top_item = jP.hasClass("menu");
            var jItem  = $('<div class="menu-item">').appendTo(jP)
                        .attr("md", mi.is_top_item ? "top" : "sub");
            var jq = $('<div class="menu-item-wrapper"></div>').appendTo(jItem);

            // 保存一下这个项目的配置信息
            jItem.data("@DATA", mi);

            // 按类型绘制
            // 按钮
            if(mi.type == "button" || _.isFunction(mi.handler)){
                last_is_separator = false;
                UI.__draw_button(mi, jq, jItem);
            }
            // 子菜单 
            else if(mi.type == "group" || _.isArray(mi.items) || _.isFunction(mi.items)){
                last_is_separator = false;
                UI.__draw_group(mi, jq, jItem);
            }
            // 状态按钮
            else if(mi.type == "status" || _.isArray(mi.status)){
                last_is_separator = false;
                UI.__draw_status(mi, jq, jItem);
            }
            // 禁止绘制重复的分隔符
            else if(!last_is_separator){
                UI.__draw_separator(mi, jq, jItem);
                last_is_separator = true;
            }
            // 靠，不认识删除吧
            else{
                jItem.remove();
            }
        }
    },
    //..............................................
    __draw_status : function(mi, jq, jItem){
        var UI = this;
        jItem.attr("tp", "status");

        // 绘制主体
        if(!mi.is_top_item){
            UI.__draw_fireable(mi, jq, jItem);
        }

        // 绘制状态按钮
        var jStatus = $('<span class="menu-item-status">').appendTo(jq);
        mi.status.forEach(function(si){
            var jSi = $('<span class="menu-item-si">').appendTo(jStatus);
            jSi.attr("val", si.val);
            if(si.icon){
                $('<span class="menu-item-si-icon">').appendTo(jSi).html(si.icon);
            }
            if(si.text){
                $('<span class="menu-item-si-text">').appendTo(jSi).text(UI.text(si.text));
            }
        });

        // 找到第一个标记 on 的状态钮进行高亮
        for(var i=0;i<mi.status.length;i++){
            if(mi.status[i].on){
                jStatus.children(":eq("+i+")").addClass("menu-item-si-on");
                break;
            }
        }
    },
    //..............................................
    __draw_group : function(mi, jq, jItem){
        var UI = this;
        jItem.attr("tp", "group");

        // 绘制主体
        UI.__draw_fireable(mi, jq, jItem);

        // 如果是子菜单项目，那么还需要绘制一个标志，表示有子菜单
        if(!mi.is_top_item){
            $('<span class="menu-item-more">')
                .appendTo(jq)
                .html('<i class="fa fa-caret-right"></i>');
        }
    },
    //..............................................
    __draw_separator : function(mi, jq, jItem){
        jItem.empty().attr("tp", "separator");
    },
    //..............................................
    __draw_button : function(mi, jq, jItem){
        jItem.attr("tp", "button");
        this.__draw_fireable(mi, jq, jItem);
    },
    //..............................................
    __draw_fireable : function(mi, jq, jItem){
        var UI = this;
        // 图标：不是顶层项目，一律添加图标以便下拉时对其
        if(!mi.is_top_item || mi.icon){
            var jIcon = $('<span class="menu-item-icon">').appendTo(jq);
            if(mi.icon){
                jIcon.html(mi.icon);
            }
        }

        // 文字
        if(mi.text){
            var jT = $('<span class="menu-item-text">').appendTo(jq);
            jT.text(UI.text(mi.text));
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);