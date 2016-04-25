(function($z){
$z.declare([
    'zui',
    'ui/jtypes'
], function(ZUI, JsType){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="checker" class="lst-item-checker">
        <i tp="checkbox" class="fa fa-square-o current"></i>
        <i tp="checkbox" class="fa fa-check-square"></i>
    </div>
</div>
<div class="ui-arena lst" ui-fitparent="true"></div>
*/};
//==============================================
return ZUI.def("ui.list", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/list/list.css",
    //..............................................
    init : function(options){
        var UI  = this;
        var opt = options;

        $z.setUndefined(opt, "activable", true);
        $z.setUndefined(opt, "blurable", true);
        $z.setUndefined(opt, "checkable", false);
        $z.setUndefined(opt, "multi",  opt.checkable?true:false);
        $z.setUndefined(opt, "idKey",     "id");
        $z.setUndefined(opt, "escapeHtml", true);

        // key 字段专门用来显示
        opt.key = opt.nmKey || opt.idKey;

        // 预先编译每个项目的显示方式
        $z.evalFldDisplay(options);
    },
    //..............................................
    events : {
        "click .lst-item" : function(e){
            var UI = this;
            // 如果支持多选 ...
            if(UI.options.multi){
                // 仅仅表示单击选中
                if(($z.os.mac && e.metaKey) || (!$z.os.mac && e.ctrlKey)){
                    UI.check(e.currentTarget);
                    return;
                }
                // shift 键表示多选
                else if(e.shiftKey){
                    // 准备计算需要选中的项目
                    var jRows;

                    // 得到自己
                    var jq = $(e.currentTarget);

                    // 找到激活项目的 ID
                    var jA = UI.arena.find(".lst-item-actived");

                    // 没有项目被激活，那么从头选
                    if(jA.size() == 0){
                        jA = UI.arena.find(".lst-item:eq(0)");
                    }
                    
                    // 激活项目在自己以前
                    var selector = "[oid=" + jA.attr("oid") + "]";
                    if(jq.prevAll(selector).size() > 0){
                        jRows = jq.prevUntil(jA).addBack().add(jA);
                    }
                    // 激活项目在自己以后
                    else if(jq.nextAll(selector).size() > 0){
                        jRows = jq.nextUntil(jA).addBack().add(jA);
                    }
                    // 那就是自己咯
                    else{
                        jRows = jq;
                    }

                    // 选中这些项目
                    UI.check(jRows);

                    // 防止冒泡
                    e.stopPropagation();
                    
                    // 不用继续了
                    return;
                }
            }
            // 否则就是激活
            UI.setActived(e.currentTarget);
        },
        "click .lst-item-checker" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(e){
            var jq = $(e.target);
            var jRow = jq.closest(".lst-item");
            if(this.options.blurable && !jRow.hasClass("lst-item-actived"))
                this.blur();
        },
        "click .tbl-checker" : function(e){
            e.stopPropagation();
            var UI = this;
            var jChecker = $(e.currentTarget);
            var tp = jChecker.attr("tp");
            // 全选
            if("none" == tp){
                UI.check();
            }
            // 全取消
            else{
                UI.uncheck();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 如果需要显示选择框 ...
        if(opt.checkable){
            UI.arena.addClass("lst-show-checkbox");
        }
    },
    //...............................................................
    getObjId : function(obj){
        return obj[this.options.idKey];
    },
    getObjName : function(obj){
        return obj[this.options.nmKey];
    },
    //...............................................................
    getActived : function(){
        return this.arena.find(".lst-item-actived").data("OBJ");
    },
    getActivedId : function(){
        var obj = this.getActived();
        return obj ? this.getObjId(obj) : null;
    },
    isActived : function(arg){
        var jRow = this.$item(arg);
        return jRow.hasClass("lst-item-actived");
    },
    //...............................................................
    setActived : function(arg){
        var UI  = this;
        var opt = UI.options;

        // 不许激活项目 ...
        if(!opt.activable)
            return;

        // 未给入参数，相当于 blur
        if(_.isUndefined(arg) || _.isNull(arg)){
            UI.blur();
            return;
        }

        // 执行查找
        var jRow = UI.$item(arg);
        if(jRow.size() > 0 && !UI.isActived(jRow)){
            UI.blur();
            jRow.addClass("lst-item-actived lst-item-checked");
            var o = jRow.data("OBJ");
            
            // 得到下标
            var index = jRow.prevAll().size();

            // 触发消息 
            UI.trigger("list:actived", o, jRow);
            $z.invoke(UI.options, "on_actived", [o, index, jRow], UI);
        }
    },
    //...............................................................
    blur : function(){
        var UI = this;
        var jRows = UI.arena.find(".lst-item-checked");

        if(jRows.size() > 0){
            // 移除标记
            jRows.removeClass("lst-item-actived lst-item-checked");

            // 获取数据
            var objs = [];
            jRows.each(function(){
                objs.push($(this).data("OBJ"));
            });

            // 触发消息 
            UI.trigger("list:blur", objs);
            $z.invoke(UI.options, "on_blur", [objs, jRows], UI);
        }
    },
    //...............................................................
    getChecked : function(){
        var UI = this;
        var objs = [];
        UI.arena.find(".lst-item-checked").each(function(){
            objs.push($(this).data("OBJ"));
        });
        return objs;
    },
    //...............................................................
    check : function(arg){
        var UI    = this;
        var jRows = _.isUndefined(arg)?UI.arena.find(".lst-item"):UI.$item(arg);
        jRows.not(".lst-item-checked");
        if(jRows.size()>0){
            var objs = [];
            jRows.addClass("lst-item-checked").each(function(){
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("tbl:checked", objs);
            $z.invoke(UI.options, "on_checked", [objs], UI);
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI    = this;
        var jRows = _.isUndefined(arg)?UI.arena.find(".lst-item-checked"):UI.$item(arg);
        jRows.not(":not(.tbl-row-checked)");
        if(jRows.size()>0){
            var objs = [];
            jRows.removeClass("lst-item-checked lst-item-actived").each(function(){
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("tbl:uncheck", objs);
            $z.invoke(UI.options, "on_uncheck", [objs], UI);

            // 同步选择器 
            UI.__sync_checker();
        }
    },
    //...............................................................
    toggle : function(arg){
        var UI    = this;
        var jRows = _.isUndefined(arg)?UI.arena.find(".lst-item"):UI.$item(arg);
        if(jRows.size()>0){
            var checkeds = [];
            var unchecks = [];
            jRows.each(function(){
                var jRow = $(this);
                var o = jRow.data("OBJ");
                if(jRow.hasClass("lst-item-checked")){
                    jRow.removeClass("lst-item-actived lst-item-checked");
                    unchecks.push(o);
                }else{
                    jRow.addClass("lst-item-checked");
                    checkeds.push(o);
                }
            });

            // 触发消息 : checked
            if(checkeds.length > 0) {
                UI.trigger("list:checked", checkeds);
                $z.invoke(UI.options, "on_checked", [checkeds], UI);    
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("list:uncheck", unchecks);
                $z.invoke(UI.options, "on_uncheck", [unchecks], UI);    
            }
        }
    },
    //...............................................................
    has: function(arg) {
        return this.$item(arg).size() > 0;
    },
    //...............................................................
    getData : function(arg){
        var UI = this;
        // 特指某个项目
        if(!_.isUndefined(arg)){
            return UI.$item(arg).data("OBJ");
        }
        // 获取完整的列表
        return UI.ui_format_data(function(opt){
            var objs = [];
            jTBody.children('.lst-item').each(function(){
                objs.push($(this).data("OBJ"));
            });
            return objs;
        });
    },
    //...............................................................
    setData : function(objs){
        this.ui_parse_data(objs, function(objs){
            this._draw_data(objs);
        });
    },
    //...............................................................
    $item : function(it){
        var UI = this;
        // 默认用更新
        if(_.isUndefined(it)){
            return UI.arena.find(".lst-item-actived");
        }
        // 如果是字符串表示 ID
        else if(_.isString(it)){
            return UI.arena.find(".lst-item[oid="+it+"]");
        }
        // 本身就是 dom
        else if(_.isElement(it) || $z.isjQuery(it)){
            return $(it).closest(".lst-item");
        }
        // 数字
        else if(_.isNumber(it)){
            return $z.jq(UI.arena, it, ".lst-item");
        }
        // 靠不晓得了
        else {
            throw "unknowns row selector: " + row;
        }
    },
    //...............................................................
    add : function(objs, it, direction) {
        var UI = this;
        objs = _.isArray(objs) ? objs : [objs];

        objs.forEach(function(o, index){
            UI._upsert_row(o, UI.$item(it), direction);
        });

        // 最后触发消息
        UI.trigger("table:add", objs);
        $z.invoke(UI.options, "on_add", [objs], UI);
        UI.trigger("table:change");
        $z.invoke(UI.options, "on_change", [], UI);

        // 返回自身
        return this;
    },
    //...............................................................
    remove : function(it, keepAtLeastOne) {
        var UI   = this;
        var jRow = UI.$item(it);

        // 如果没有匹配的行，啥也不做
        if(jRow.size() == 0)
            return;

        // 如果当前是高亮节点，则试图得到下一个高亮的节点，给调用者备选
        var jN2   = null;
        if(UI.isActived(jRow)){
            jN2 = jRow.next();
            if(jN2.size() == 0){
                jN2 = jRow.prev();
                // 返回 false 表示只剩下最后一个节点额
                if(jN2.size() == 0 && keepAtLeastOne){
                    return false;
                }
            }
        }

        // 删除当前节点
        jRow.remove();

        // 返回下一个要激活的节点，由调用者来决定是否激活它
        return jN2 && jN2.size() > 0 ? jN2 : null;
    },
    //...............................................................
    update : function(obj, it) {
        this._upsert_row(obj, this.$item(it || this.getObjId(obj)), 0);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI  = this;
        var opt = UI.options;

        // 清空
        UI.arena.empty();

        // 检查要输出的数据
        if(!_.isArray(objs))
            return;

        // 输出表格内容 
        objs.forEach(function(o, index){
            UI._upsert_row(o);
        });

        // 重新计算尺寸
        UI.resize();

        // 最后触发消息
        UI.trigger("list:change", objs);
        $z.invoke(opt, "on_change", [objs], UI);
    },
    //...............................................................
    _upsert_row : function(o, jReferRow, direction){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var jBody   = UI.arena;
        
        // 创建行
        var jRow;
        // 没有参考 row，插入到表格后头
        if(!jReferRow || jReferRow.size() == 0){
            jBody = jBody || UI.arena.find(".tbl-body-t");
            jRow = $('<div class="lst-item">').appendTo(jBody);
        }
        // 有的话，看方向 0 表示替换
        else if(0 === direction){
            jRow = jReferRow.empty();
        }
        // 正数查到前面
        else if(direction>0){
            jRow = $('<div class="lst-item">').insertBefore(jReferRow);
        }
        // 负数插到后面
        else {
            jRow = $('<div class="lst-item">').insertAfter(jReferRow);
        }

        // 添加必要属性
        jRow.data("OBJ", o);
        if(o[opt.idKey])
            jRow.attr("oid", o[opt.idKey]);

        if(opt.nmKey && o[opt.nmKey])
            jRow.attr("onm", o[opt.nmKey]);

        // 获取显示值
        var s  = opt.__dis_obj.call(context, o, opt);

        // 国际化
        s = UI.text(s);

        // 逃逸 HTML
        if(opt.escapeHtml === true)
            jRow.text(s || '');
        else
            jRow.html(s || '');

        // 如果需要显示选择框
        if(opt.checkable){
            jRow.prepend(UI.ccode("checker"));
        }

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_item", [jRow, o], context);

        // 添加到表格
        return jRow;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);