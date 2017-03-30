(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var do_close_allmenu = function(ignore_cids){
    if(!ignore_cids){
        ignore_cids = [];
    }
    if(!_.isArray(ignore_cids)){
        ignore_cids = [ignore_cids];
    }
    $('.ui-menu').each(function(){
        var UI = ZUI(this);
        if(ignore_cids.indexOf(UI.cid)>=0){
            return;
        }
        // 上下文菜单，销毁
        if(UI.isContextmenu()){
            UI.destroy();
        }
        // 普通菜单，关闭组
        else{
            UI.closeGroup();
        }
    });
};
//==============================================
var html = function(){/*
<div class="ui-arena menu"></div>
*/};
//==============================================
return ZUI.def("ui.menu", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/menu/theme/menu-{{theme}}.css",
    //..............................................
    init : function(options){
        var UI  = this;
        var opt = options;
        // 注册全局事件，控制子菜单的关闭
        var on_close_group = function(e){
            UI.closeGroup(UI.$el.find(".menu-item[open]"));
        };
        UI.watchMouse("click", on_close_group);
        UI.watchKey(27, on_close_group);

        // 注册全局关闭
        if(opt.position){
            UI.watchMouse("click", do_close_allmenu);
            UI.watchKey(27, do_close_allmenu);            
        }

        // 全局其他的菜单统统关闭
        do_close_allmenu(UI.cid);
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
                
                // 调用回调
                $z.invoke(mi, "on_change", [val, mi], context);
                
                if(_.isFunction(context.trigger)){
                    context.trigger("menu:"+(mi.key||"status"), val);
                }
            }
        },
        "click .menu-item[tp=boolean]" : function(e){
            var context = this.options.context || this.parent || this;
            var jItem = $(e.currentTarget);
            var mi    =  jItem.data("@DATA");
            mi.on = mi.on ? false : true;
            
            // 调用回调
            $z.invoke(mi, "on_change", [mi.on, mi], context);
            
            if(_.isFunction(context.trigger)){
                context.trigger("menu:"+(mi.key||"boolean"), mi.on);
            }
            // 重绘自身
            var jq = jItem.children(".menu-item-wrapper").empty();
            this.__draw_boolean(mi, jq, jItem);
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
        var UI = this;
        // 还没初始化完的，无视 
        if(!UI.arena)
            return;

        // 指定一个组
        if(ele){
            var jq = $(ele);
            if(jq.hasClass("menu-item")){
                jq.removeAttr("open");
                jq.children(".menu-sub").remove();   
                return;
            }
        }
        // 所有组
        else{
            UI.closeGroup(UI.arena.find(".menu-item[open]"));
        }
    },
    //..............................................
    openGroup : function(ele){
        var UI  = this;
        var opt = UI.options;
        var jq  = $(ele);
        var mi  = jq.data("@DATA");
        var context = UI.options.context || UI.parent || UI;
        // 关闭其他菜单的组
        do_close_allmenu(UI.cid);

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
            var dockMode = "H";
            if(opt.position || jq.closest(".menu-sub").size()>0){
                dockMode = "V";
            }
            $z.dock(jq, jTa.css("z-index", 1000+depth), dockMode);
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
        var UI  = this;
        var opt = UI.options;
        var items = UI.options.setup;
        // 清空内容，依次重绘
        UI.arena.empty();

        // 如果是绝对位置打开的菜单，则确保将其移动到 body 下面，是一个绝对定位
        if(opt.position){
            if(UI.pel != document.body){
                UI.$el.appendTo(document.body);
            }
            // 做点标记
            UI.$el.attr("contextmenu", "yes").css({
                top  : opt.position.y,
                left : opt.position.x
            });
            UI._draw_items(items, UI.arena);
        }
        // 否则在给定选区绘制菜单项
        else{
            UI._draw_items(items, UI.arena);
        }
    },
    //..............................................
    isContextmenu : function(){
        return "yes" == this.$el.attr("contextmenu");
    },
    //..............................................
    _draw_items : function(items, jP){
        var UI  = this;
        var opt = UI.options;
        var context = UI.options.context || UI.parent || UI;

        // 没啥可绘制的 ...
        if(!_.isArray(items) || items.length == 0)
            return;

        // 循环绘制每个项目
        for(var i=0; i<items.length; i++){
            var mi = items[i];
            
            // 看看是否有必要调用初始化函数
            $z.invoke(mi, "init", [mi], context);

            // 绘制主体
            mi.is_top_item = !UI.$el.attr("contextmenu") && jP.hasClass("menu");
            var jItem  = $('<div class="menu-item">').appendTo(jP)
                        .attr("md", mi.is_top_item ? "top" : "sub");
            var jq = $('<div class="menu-item-wrapper"></div>').appendTo(jItem);

            // 保存一下这个项目的配置信息
            jItem.data("@DATA", mi);

            // 按钮
            if(mi.type == "button" || _.isFunction(mi.handler)){
                UI.__draw_button(mi, jq, jItem);
            }
            // 子菜单 
            else if(mi.type == "group" || _.isArray(mi.items) || _.isFunction(mi.items)){
                UI.__draw_group(mi, jq, jItem);
            }
            // 状态按钮
            else if(mi.type == "status" || _.isArray(mi.status)){
                UI.__draw_status(mi, jq, jItem);
            }
            // 布尔项目
            else if(mi.type == "boolean") {
                UI.__draw_boolean(mi, jq, jItem);
            }
            // 分隔符
            else if(mi.type == "separator"){
                // 后面没有项目或者后面还有分隔符，那么本分隔符就没必要绘制
                var will_rm_sep = (0 == i) || (i == items.length-1);
                if(!will_rm_sep)
                    will_rm_sep = ("separator" == items[i+1].type);

                if(will_rm_sep){
                    jItem.remove();
                }
                // 绘制
                else{
                    UI.__draw_separator(mi, jq, jItem);
                }
            }
            // 靠，不认识，打印个错误
            else{
                console.warn("unknown menu item: ", mi);
                jItem.remove();
            }
        }
    },
    //..............................................
    __draw_boolean : function(mi, jq, jItem){
        var UI = this;
        jItem.attr("tp", "boolean");

        // 初始化 icon
        mi.icon = mi.on ? mi.icon_on : mi.icon_off;

        // 绘制主体
        UI.__draw_fireable(mi, jq, jItem);

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
            jSi.attr("val", si.value || si.val);
            if(si.icon){
                $('<span class="menu-item-si-icon">').appendTo(jSi).html(si.icon);
            }
            if(si.text){
                $('<span class="menu-item-si-text">').appendTo(jSi).text(UI.text(si.text));
            }
        });

        // 找到第一个标记 on 的状态钮进行高亮，其他的标记off
        for(var i=0;i<mi.status.length;i++){
            if(mi.status[i].on){
                jStatus.children(":eq("+i+")").addClass("menu-item-si-on");
            }else{
                jStatus.children(":eq("+i+")").addClass("menu-item-si-off");
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
        var UI  = this;
        var opt = UI.options;

        // 增加自定义的类选择器
        if(mi.className){
            jq.addClass(mi.className);
        }

        // 图标：不是顶层项目，一律添加图标以便下拉时对其
        var jIcon;
        if(!mi.is_top_item || mi.icon){
            jIcon = $('<span class="menu-item-icon">').appendTo(jq)
            if(mi.icon){
                jIcon.html(mi.icon);
            }
        }

        // 文字
        var jT;
        if(mi.text){
            jT = $('<span class="menu-item-text">');
            if(mi.iconAtRight){
                jT.prependTo(jq);
            }else{
                jT.appendTo(jq);
            }
            jT.text(UI.text(mi.text));
        }

        // 添加提示文字
        if(mi.tip){
            if(jT || jIcon) {
                (jT || jIcon).attr({
                    "data-balloon" : UI.text(mi.tip),
                    "data-balloon-pos" : opt.tipDirection || "down"
                });
            }
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);