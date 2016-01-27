(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="data.loading" class="ui-loading">
        <i class="fa fa-spinner fa-pulse"></i> <span>{{loading}}</span>
    </div>
</div>
<div class="ui-arena"></div>
*/};
//==============================================
return ZUI.def("ui.olist", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/olist/olist.css",
    init : function(options){
        var UI = this;

        $z.setUndefined(options, "fitparent", true);
        $z.setUndefined(options, "activable", true);
        $z.setUndefined(options, "blurable",  true);
        $z.setUndefined(options, "evalData", $z.evalData);
        $z.setUndefined(options, "idKey",     "id");
        $z.setUndefined(options, "nmKey",     "nm");
        if(options.checkable === true) {
            options.checkable = {
                checked : "fa fa-check-square-o",
                normal  : "fa fa-square-o"
            };
        }

        // 准备列表的显示项
        options.text = options.text 
                       || (options.display
                             ? '<b>{{_display.nm}}</b>'
                             : '<b>{{nm}}</b>');
        // 准备 iconClass 的解析函数
        if(_.isFunction(options.iconClass)){
            UI._eval_icon_class = options.iconClass;
        }
        // 用对象来整理（相当于 ZUI 基类 get_obj_val_by 方法的别名）
        else if(_.isObject(options.iconClass)){
            UI._eval_icon_class = function(o){
                return UI.get_obj_val_by(o, options.iconClass);
            };
        }

        // 图标自定义函数
        UI._iconFunc = UI.eval_tmpl_func(UI.options, "icon");

        // 准备文本显示的函数
        if(_.isFunction(options.display)){
            UI._eval_obj_display = options.display;
        }
        // 用对象来整理（相当于 ZUI 基类 get_obj_val_by 方法的别名）
        else if(_.isObject(options.display)){
            UI._eval_obj_display = function(o){
                return UI.get_obj_val_by(o, options.display);
            };
        }

        // 文本自定义函数
        UI._textFunc = UI.eval_tmpl_func(UI.options, "text");
    },
    //...............................................................
    events : {
        "click .olist-item" : function(e){
            this.setActived(e.currentTarget);
        },
        "click .olist-item [tp=checkbox]" : function(e){
            e.stopPropagation();
            this.toggle($(e.currentTarget).parent());
        },
        "click .ui-arena" : function(e){
            var jq = $(e.target);
            var jItem = jq.hasClass("olist-item") ? jq : jq.parents(".olist-item");
            if(this.options.blurable && !jItem.hasClass("olist-item-actived"))
                this.blur();
        }
    },
    //...............................................................
    getActived : function(){
        return this.arena.find(".olist-item-actived").data("OBJ");
    },
    //...............................................................
    active : function(arg){
        var UI = this;
        if(!UI.options.activable)
            return;
        // 字符串表示对象 ID
        if(_.isString(arg)){
            arg = '[oid="' + arg + '"]';
        }
        // 执行查找
        var jq = $z.jq(UI.arena, arg, ".olist-item").first();
        if(jq.hasClass("olist-item") && !jq.hasClass("olist-item-actived")){
            UI.blur();
            jq.addClass("olist-item-actived");
            var o = jq.data("OBJ");
            var index = jq.attr("index") * 1;
            // 触发消息 
            UI.trigger("olist:actived", o, index);
            $z.invoke(UI.options, "on_actived", [o, index], UI);
        }
    },
    // 一个保留的兼容函数，稍后会删掉
    setActived : function(arg){
        this.active(arg);
    },
    //...............................................................
    blur : function(){
        var UI = this;
        var jq = UI.arena.children(".olist-item-actived");

        if(jq.size() > 0){
            var o = jq.removeClass("olist-item-actived").data("OBJ");
            // 触发消息 
            UI.trigger("olist:blur", o);
            $z.invoke(UI.options, "on_blur", [o], UI);
        }
    },
    //...............................................................
    getChecked : function(){
        var UI = this;
        var objs = [];
        UI.arena.children(".olist-item-checked").each(function(){
            objs.push($(this).data("OBJ"));
        });
        return objs;
    },
    //...............................................................
    check : function(arg){
        var UI = this;
        var jq = $z.jq(UI.arena, arg, ".olist-item").not(".olist-item-checked");
        
        if(jq.size()>0){
            var objs = [];
            jq.addClass("olist-item-checked").each(function(){
                $(this).find("[tp=checkbox]")
                    .prop("className", UI.options.checkable.checked);
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("olist:checked", objs);
            $z.invoke(UI.options, "on_checked", [objs], UI);
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI = this;
        var jq = $z.jq(UI.arena, arg, ".olist-item-checked");
        console.log(jq.size())
        if(jq.size()>0){
            var objs = [];
            jq.removeClass("olist-item-checked").each(function(){
                $(this).find("[tp=checkbox]")
                    .prop("className", UI.options.checkable.normal);
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("olist:uncheck", objs);
            $z.invoke(UI.options, "on_uncheck", [objs], UI);
        }
    },
    //...............................................................
    toggle : function(arg){
        var UI = this;
        var jq = $z.jq(UI.arena, arg, ".olist-item");

        if(jq.size()>0){
            var checkeds = [];
            var unchecks = [];
            jq.each(function(){
                var o = $(this).data("OBJ");
                if($(this).hasClass("olist-item-checked")){
                    $(this)
                        .removeClass("olist-item-checked")
                        .find("[tp=checkbox]")
                            .prop("className", UI.options.checkable.normal);
                    unchecks.push(o);
                }else{
                    $(this)
                        .addClass("olist-item-checked")
                        .find("[tp=checkbox]")
                            .prop("className", UI.options.checkable.checked);
                    checkeds.push(o);
                }
            });
            // 触发消息 : checked
            if(checkeds.length > 0) {
                UI.trigger("olist:checked", checkeds);
                $z.invoke(UI.options, "on_checked", [checkeds], UI);    
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("olist:uncheck", unchecks);
                $z.invoke(UI.options, "on_uncheck", [unchecks], UI);    
            }
        }
    },
    //...............................................................
    getData : function(arg){
        var UI = this;
        // 数字下标
        if(_.isNumber(arg)){
            var jq = $z.jq(UI.arena, arg);
            return jq.data("OBJ");
        }
        // ID
        var m = /^id:(.+)$/g.exec(arg);
        if(m){
            var jq = UI.arena.children('[oid="' + m[1] + '"]');
            return jq.data("OBJ");
        }
        // Name
        if(_.isString(arg)){
            var jq = UI.arena.children('[onm="' + arg + '"]');
            return jq.data("OBJ");   
        }
        // 获取完整的列表
        var objs = [];
        this.arena.children('.olist-item').each(function(){
            objs.push($(this).data("OBJ"));
        });
        return objs;
    },
    //...............................................................
    setData : function(dc, callback){
        var UI = this;

        // 如果数据应该被忽略
        if($z.invoke(UI.options, "ignoreData", [dc], UI)){
            return;
        }
        
        // 如果是个数组，那么就认为这是一个被解析好的数据
        if(_.isArray(dc)){
            UI.options.data = dc;
        }
        // 否则认为这个对象是个上下文，需要被转换一下（异步）
        else{
            UI.options.dataContext = dc;
        }

        UI.refresh(callback);
    },
    //...............................................................
    addLast : function(obj) {
        var UI = this;
        var objs = _.isArray(obj) ? obj : [obj];

        objs.forEach(function(o, index){
            UI._append_item(o);
        });

        // 最后触发消息
        UI.trigger("olist:push", objs);
        $z.invoke(UI.options, "on_push", [objs], UI);

        objs = UI.getData();
        UI.trigger("olist:change", objs);
        $z.invoke(UI.options, "on_change", [objs], UI);
    },
    //...............................................................
    showLoading : function(){
        var UI = this;
        var jq = UI.ccode("data.loading");
        UI.arena.empty().append(jq);
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        UI.refresh(function(){
            UI.defer_report(0, "@DATA");
        });
        return UI.options.data ? ["@DATA"] : undefined;
    },
    //..............................................
    refresh : function(callback){
        var UI  = this;
        var opt = UI.options;
        UI.showLoading();
        UI.options.evalData.call(UI, opt.data, opt.dataContext, function(objs){
            if(_.isFunction(UI.options.filter)){
                var list = [];
                objs.forEach(function(o){
                    o = UI.options.filter(o);
                    if(o)
                        list.push(o);
                });
                objs = list;
            }

            if(_.isFunction(UI.options.comparer)){
                objs.sort(UI.options.comparer);
            }

            UI._draw_data(objs);
            if(_.isFunction(callback)){
                callback.call(UI, objs);
            }
        }, UI);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI = this;

        objs = _.isArray(objs) ? objs : [objs];

        UI.arena.empty();
        
        objs.forEach(function(o, index){
            UI._append_item(o, index);
        });
        // 最后触发消息
        UI.trigger("olist:change", objs);
        $z.invoke(UI.options, "on_change", [objs], UI);
    },
    //..............................................
    _append_item : function(o, index){
        var UI    = this;
        var opt   = UI.options;
        var idKey = opt.idKey;
        var nmKey = opt.nmKey;
        var iconFunc = UI._iconFunc;
        var textFunc = UI._textFunc;

        if(_.isUndefined(index)){
            index = UI.arena.children().size();
        }
        
        var checkable = opt.checkable;
        var jq = $('<div class="olist-item">').appendTo(UI.arena);
        jq.attr("index", index);
        if(o[idKey])
            jq.attr("oid", o[idKey]);

        if(o[nmKey])
            jq.attr("onm", o[nmKey]);

        if(checkable){
            jq.append('<i class="' + checkable.normal + '" tp="checkbox">');
        }

        // 分析 icon
        var iconHtml = iconFunc ? $(iconFunc(o)) : null;
        var jIcon;
        // 图标
        if(iconHtml){
            jIcon = $(iconHtml).attr("tp","icon").appendTo(jq);
        }
        // 补充类
        if(UI._eval_icon_class) {
            jIcon = jIcon || $('<i>').attr("tp","icon").appendTo(jq);
            iconClass = UI._eval_icon_class(o) || "";
            jIcon.addClass(iconClass);
        }
        
        // 解析列表项内容
        var jText = $(textFunc(o)).appendTo(jq);
        
        // 补充修改对象的文本
        if(UI._eval_obj_display) {
            var txt = UI._eval_obj_display(o);
            // 嗯，有滴，要修改
            if(txt) {
                txt = UI.text(txt);   // 支持 i18n:xxxx 的格式
                // 指定一个选择器
                if(opt.display.selector){
                    jText.find(opt.display.selector).text(txt);
                }
                // 修改顶级 
                else{
                    jText.text(txt);
                }
            }
        }

        // 记录数据
        jq.data("OBJ", o);
    },
    //..............................................
    resize : function(){
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);