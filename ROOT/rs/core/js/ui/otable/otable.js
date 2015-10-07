(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <div class="otable-head">
        <table class="otable-head-t"><tbody></tbody></table>
    </div>
    <div class="otable-body">
        <table class="otable-body-t"><tbody></tbody></table>
    </div>
</div>
*/};
//=======================================================================
return ZUI.def("ui.otable", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/otable/otable.css",
    //...............................................................
    init : function(options){
        var UI = this;
        $z.setUndefined(options, "fitparent", true);
        $z.setUndefined(options, "activable", true);
        $z.setUndefined(options, "blurable",  true);
        $z.setUndefined(options, "idKey",     "id");
        $z.setUndefined(options, "columns",   []);
        if(options.checkable === true) {
            options.checkable = {
                checked : "fa fa-check-square-o",
                normal  : "fa fa-square-o"
            };
        }
        // 准备 iconClass 的解析函数
        if(options.icon){
            if(_.isFunction(options.iconClass)){
                this._eval_icon_class = options.iconClass;
            }else if(_.isObject(options.iconClass)){
                this._eval_icon_class = function(o){
                    var key = this.options.iconClass.key;
                    var map = this.options.iconClass.map;
                    var dft = this.options.iconClass.dft;
                    var val = o[key];
                    return map[val] || dft;
                }
            }
        }
        // 处理每个列的显示函数 
        options.columns.forEach(function(col){
            col._disfunc = UI.eval_tmpl_func(col, "display")
                           || function(o, key){return o[key];};
            $z.setUndefined(col, "escapeHtml",  true);
        });
    },
    //...............................................................
    events : {
        "click .otable-row" : function(e){
            e.stopPropagation();
            this.setActived(e.currentTarget);
        },
        "click .otable-row [tp=checkbox]" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(){
            if(this.options.blurable)
                this.blur();
        }
    },
    //...............................................................
    getActived : function(){
        return UI.arena.find(".otable-row-actived").data("OBJ");
    },
    //...............................................................
    setActived : function(arg){
        var UI = this;
        if(!UI.options.activable)
            return;
        var jq = $z.jq(UI.arena, arg, ".otable-row").first();

        if(jq.hasClass("otable-row") && !jq.hasClass("otable-row-actived")){
            UI.blur();
            jq.addClass("otable-row-actived");
            var o = jq.data("OBJ");
            // 触发消息 
            UI.trigger("otable:actived", o);
            $z.invoke(UI.options, "on_actived", [o], UI);
        }
    },
    //...............................................................
    blur : function(){
        var UI = this;
        var jq = UI.arena.find(".otable-row-actived");

        if(jq.size() > 0){
            var o = jq.removeClass("otable-row-actived").data("OBJ");
            // 触发消息 
            UI.trigger("otable:blur", o);
            $z.invoke(UI.options, "on_blur", [o], UI);
        }
    },
    //...............................................................
    getChecked : function(){
        var objs = [];
        UI.arena.children(".otable-row-checked").each(function(){
            objs.push($(this).data("OBJ"));
        });
        return objs;
    },
    //...............................................................
    check : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".otable-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".otable-row").not(".otable-row-checked");
        if(jq.size()>0){
            var objs = [];
            jq.addClass("otable-row-checked").each(function(){
                $(this).find("[tp=checkbox]")
                    .prop("className", UI.options.checkable.checked);
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("otable:checked", objs);
            $z.invoke(UI.options, "on_checked", [objs], UI);
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".otable-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".otable-row-checked");
        if(jq.size()>0){
            var objs = [];
            jq.removeClass("otable-row-checked").each(function(){
                $(this).find("[tp=checkbox]")
                    .prop("className", UI.options.checkable.normal);
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("otable:uncheck", objs);
            $z.invoke(UI.options, "on_uncheck", [objs], UI);
        }
    },
    //...............................................................
    toggle : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".otable-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".otable-row");
        if(jq.size()>0){
            var checkeds = [];
            var unchecks = [];
            jq.each(function(){
                var jRow = $(this);
                if(!jRow.hasClass("otable-row")){
                    jRow = jRow.parents(".otable-row");
                }
                if(jRow.size()==0){
                    throw "otable: not row or row item : " + this;
                }

                var o = jRow.data("OBJ");
                if(jRow.hasClass("otable-row-checked")){
                    jRow.removeClass("otable-row-checked")
                        .find('[tp="checkbox"]')
                        .prop("className", UI.options.checkable.normal);
                    unchecks.push(o);
                }else{
                    jRow.addClass("otable-row-checked")
                        .find('[tp="checkbox"]')
                        .prop("className", UI.options.checkable.checked);
                    checkeds.push(o);
                }
            });
            // 触发消息 : checked
            if(checkeds.length > 0) {
                UI.trigger("otable:checked", checkeds);
                $z.invoke(UI.options, "on_checked", [checkeds], UI);    
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("otable:uncheck", unchecks);
                $z.invoke(UI.options, "on_uncheck", [unchecks], UI);    
            }
        }
    },
    //...............................................................
    getData : function(){
        var objs = [];
        this.arena.find('.otable-row').each(function(){
            objs.push($(this).data("OBJ"));
        });
    },
    //...............................................................
    setData : function(d, permanent, callback){
        var UI = this;
        if(permanent)
            UI.options.data = d;
        UI.refresh.call(UI, d, callback);
    },
    //...............................................................
    redraw : function(callback){
        this.refresh(null, callback);
        return _.isFunction(callback);
    },
    //..............................................
    refresh : function(d, callback){
        var UI = this;
        $z.evalData(d || UI.options.data, null, function(objs){
            UI._draw_data(objs);
            if(_.isFunction(callback)){
                callback.call(UI, objs);
            }
        }, UI);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI = this;
        var idKey = UI.options.idKey;

        var iconFunc  = UI.eval_tmpl_func(UI.options, "icon");
        var checkable = UI.options.checkable;
        
        var jHeadT = UI.arena.find(".otable-head-t");
        var jBodyT = UI.arena.find(".otable-body-t");

        jHeadT.empty();
        jBodyT.empty();

        if(!objs)
            return;

        objs = _.isArray(objs) ? objs : [objs];
        
        // 输出表格内容 
        objs.forEach(function(o, index){
            var jTr = $('<tr class="otable-row">');
            jTr.attr("index", index).data("OBJ", o);
            if(o[idKey])
                jTr.attr("oid", o[idKey]);
            // .............................. 选择框
            if(checkable){
                jTr.append('<td><i class="' + checkable.normal + '" tp="checkbox">');
            }
            // .............................. icon
            if(UI._eval_icon_class){
                o['_icon_class'] = UI._eval_icon_class(o) || "";
            }
            var iconHtml = iconFunc ? iconFunc(o) : null;
            if(iconHtml)
                $('<td>'+iconHtml+'</td>').attr("tp","icon").appendTo(jTr);
            // .............................. 循环输出每一列
            // 依次创建单元格
            UI.options.columns.forEach(function(col){
                var jTd = $('<td>');
                UI._draw_col(jTd, col, o);
                jTd.appendTo(jTr);
            });

            // 添加到表格
            jTr.appendTo(jBodyT);
        });
        // 最后触发消息
        UI.trigger("otable:change", objs);
        $z.invoke(UI.options, "on_change", [objs], UI);
    },
    //...............................................................
    _draw_col : function(jTd, col, o){
        jTd.attr("key", col.key);
        var val = col._disfunc(o, col.key);
        if(col.escapeHtml)
            val = $('<div/>').text(val).html();
        jTd.html(val);
    },
    //...............................................................
    resize : function(){
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);