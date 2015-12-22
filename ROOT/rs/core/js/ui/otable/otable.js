(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="data.table" class="otable">
        <div class="otable-head">
            <table class="otable-head-t"><tr></tr></table>
            <div class="otable-checker"></div>
        </div>
        <div class="otable-body">
            <table class="otable-body-t"><tbody></tbody></table>
            <div class="otable-ruler"></div>
        </div>
    </div>
    <div code-id="data.loading" class="ui-loading">
        <i class="fa fa-spinner fa-pulse"></i> <span>{{loading}}</span>
    </div>
    <div code-id="thead.cell">
        <b class="otable-col-tt"></b>
    </div>
</div>
<div class="ui-arena">
    I am table haha
</div>
*/};
//==============================================
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
        $z.setUndefined(options, "nmKey",     "nm");
        $z.setUndefined(options, "nameTitle", "i18n:title");
        $z.setUndefined(options, "columns",   []);
        $z.setUndefined(options, "layout",   {});
        $z.setUndefined(options.layout, "sizeHint"  , -1);
        $z.setUndefined(options.layout, "cellWrap"  , "nowrap");
        $z.setUndefined(options.layout, "withHeader", true);
        $z.setUndefined(options, "evalData", $z.evalData);

        //console.log(options.layout)

        if(options.checkable === true) {
            options.checkable = {
                checked : "fa fa-check-square-o",
                normal  : "fa fa-square-o"
            };
        }
        // 准备列表的显示项
        options.text = options.text || '<b tp="text">{{nm}}</b>';
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
            UI.normalize_field(col);
        });

        // 最后等重绘完毕模拟点击
        UI.on("otable:change", function(){
            var jHead = UI.arena.find(".otable-head");
            UI.arena.find(".otable-body").scroll(function(e){
                var jq = $(this);
                var left = jq.scrollLeft();
                //console.log(left, jq.scrollTop(),jHead.size());
                jHead.css("left", left * -1);
            });
        });
    },
    //...............................................................
    depose : function(){
        var UI = this;
        UI.arena.find(".otable-body").unbind();
    },
    //...............................................................
    events : {
        "click .otable-row" : function(e){
            this.setActived(e.currentTarget);
        },
        "click .otable-row [tp=checkbox]" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(e){
            var jq = $(e.target);
            var jRow = jq.parents(".otable-row");
            if(this.options.blurable && !jRow.hasClass("otable-row-actived"))
                this.blur();
        },
        "click .otable-checker>*" : function(e){
            var UI = this;
            var jChecker = UI.arena.find(".otable-checker");
            var tp = $(e.currentTarget).attr("tp");
            //console.log("tp", tp)
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
    getActived : function(){
        return this.arena.find(".otable-row-actived").data("OBJ");
    },
    //...............................................................
    setActived : function(arg){
        var UI = this;
        if(!UI.options.activable)
            return;
        // 字符串表示对象 ID
        if(_.isString(arg)){
            arg = '[oid="' + arg + '"]';
        }
        // 执行查找
        var jq = $z.jq(UI.arena, arg, ".otable-row").first();
        if(jq.hasClass("otable-row") && !jq.hasClass("otable-row-actived")){
            UI.blur();
            jq.addClass("otable-row-actived");
            var o = jq.data("OBJ");
            var index = jq.attr("index") * 1;
            // 触发消息 
            UI.trigger("otable:actived", o, index);
            $z.invoke(UI.options, "on_actived", [o, index], UI);
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
        var UI = this;
        var objs = [];
        UI.arena.find(".otable-row-checked").each(function(){
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

            // 同步选择器 
            UI.__sync_checker();
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

            // 同步选择器 
            UI.__sync_checker();
        }
    },
    //...............................................................
    __sync_checker : function(){
        var UI = this;
        var jChecker = UI.arena.find(".otable-checker:visible");

        if(jChecker.size()<=0){
            return;
        }

        var jRows = UI.arena.find(".otable-row");
        var row_nb = jRows.size();
        var row_unchecked = jRows.not(".otable-row-checked").size();

        // 全都选中了
        if(row_nb > 0 && row_unchecked==0){
            jChecker.children().removeClass("current").filter('[tp="all"]').addClass("current");   
        }
        // 全木选中
        else if(row_nb == row_unchecked){
            jChecker.children().removeClass("current").filter('[tp="none"]').addClass("current");   
        }
        // 部分选中
        else{
            jChecker.children().removeClass("current").filter('[tp="part"]').addClass("current");   
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

            // 同步选择器 
            UI.__sync_checker();

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
    getData : function(arg){
        var UI = this;
        var jTBody = UI.arena.find(".otable-body-t>tbody");
        // 本身就是元素
        if($z.isjQuery(arg) || _.isElement(arg)){
            var jq = $(arg).closest(".otable-row");
            return jq.data("OBJ");
        }
        // 数字下标
        if(_.isNumber(arg)){
            var jq = $z.jq(jTBody, arg);
            return jq.data("OBJ");
        }
        // ID
        var m = /^id:(.+)$/g.exec(arg);
        if(m){
            var jq = jTBody.children('[oid="' + m[1] + '"]');
            return jq.data("OBJ");
        }
        // Name
        if(_.isString(arg)){
            var jq = jTBody.children('[onm="' + arg + '"]');
            return jq.data("OBJ");   
        }
        // 获取完整的列表
        var objs = [];
        jTBody.children('.otable-row').each(function(){
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
            UI._append_row(o, -1);
        });

        // 最后触发消息
        UI.trigger("otable:push", objs);
        $z.invoke(UI.options, "on_push", [objs], UI);

        objs = UI.getData();
        UI.trigger("otable:change", objs);
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
        this.arena.empty();
        this.refresh();
    },
    //...............................................................
    refresh : function(callback){
        var UI  = this;
        var opt = UI.options;
        var UI = this;
        UI.options.evalData.call(UI, opt.data, opt.dataContext, function(objs){
            UI._draw_data(objs);
            if(_.isFunction(callback)){
                callback.call(UI, objs);
            }
        }, UI);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI = this;

        var iconFunc  = UI.eval_tmpl_func(UI.options, "icon");
        var textFunc  = UI.eval_tmpl_func(UI.options, "text");
        var checkable = UI.options.checkable;

        var jq = UI.ccode("data.table");

        UI.arena.find(".otable-body").unbind();
        UI.arena.empty().append(jq);
        
        var jBodyT = jq.find(".otable-body-t");

        if(!objs)
            return;

        objs = _.isArray(objs) ? objs : [objs];

        // 输出表头
        var jHeadT = UI._redraw_header();
        var jHead = jHeadT.parent();

        // 看看要不要输出 checkbox
        var jChecker = jHead.children('.otable-checker')
        if(UI.options.checkable){
            var cc = UI.options.checkable;
            $('<i tp="all">').addClass(cc.checked).appendTo(jChecker);
            $('<i tp="none" class="current">').addClass(cc.normal).appendTo(jChecker);
            $('<i tp="part">').addClass(cc.checked).appendTo(jChecker);
            jChecker.show();
        }else{
            jChecker.hide();
        }

        // 输出表格内容 
        objs.forEach(function(o, index){
            UI._append_row(o, index, iconFunc, textFunc, jBodyT);
        });

        // 设置固定属性，以便 resize 函数时时计算宽度
        jHeadT.css("table-layout", "fixed");
        jBodyT.css("table-layout", "fixed");

        // 重新计算尺寸
        UI.resize();

        // 最后触发消息
        UI.trigger("otable:change", objs);
        $z.invoke(UI.options, "on_change", [objs], UI);
    },
    //...............................................................
    _redraw_header : function(){
        var UI = this;
        var jHeadT = UI.arena.find(".otable-head-t");
        var jTr = jHeadT.find("tr").first();

        // 绘制标题列
        var jNm = $('<td class="otable-col-nm">').appendTo(jTr);
        jNm.append(UI.ccode("thead.cell"));
        jNm.find(".otable-col-tt").text(UI.text(UI.options.nameTitle));

        // 绘制其他列
        UI.options.columns.forEach(function(col){
            if(col.hide)
                return;
            var jTd = $('<td class="otable-col-o">').appendTo(jTr);
            jTd.append(UI.ccode("thead.cell"));
            jTd.find(".otable-col-tt").text(UI.text(col.title || col.key));
        });

        return jHeadT;
    },
    //...............................................................
    _append_row : function(o, index, iconFunc, textFunc, jBodyT){
        var UI = this;
        var idKey     = UI.options.idKey;
        var nmKey = UI.options.nmKey;
        var checkable = UI.options.checkable;

        iconFunc  = iconFunc || UI.eval_tmpl_func(UI.options, "icon");
        textFunc  = textFunc || UI.eval_tmpl_func(UI.options, "text");
        
        jBodyT = jBodyT || UI.arena.find(".otable-body-t");

        if(!_.isNumber(index) || index<0)
            index = jBodyT.children().size();

        var jTr = $('<tr class="otable-row">');
        jTr.attr("index", index).data("OBJ", o);
        if(o[idKey])
            jTr.attr("oid", o[idKey]);

        if(o[nmKey])
            jTr.attr("onm", o[nmKey]);

        // .....................................
        // 创建第一列单元格
        var jNm = $('<td class="otable-row-nm">').appendTo(jTr);
        // .............................. 选择框
        if(checkable){
            jNm.append('<i class="' + checkable.normal + '" tp="checkbox">');
        }
        // .............................. icon
        if(UI._eval_icon_class){
            o['_icon_class'] = UI._eval_icon_class(o) || "";
        }
        var iconHtml = iconFunc ? iconFunc(o) : null;
        if(iconHtml)
            $(iconHtml).attr("tp","icon").appendTo(jNm);
        // .............................. 输出文字
        if(textFunc){
            jNm.append($(textFunc(o)));
        }
        // .............................. 循环输出每一列
        // 依次创建单元格
        UI.options.columns.forEach(function(col){
            if(col.hide)
                return;
            var jTd = $('<td>');
            UI._draw_col(jTd, col, o);
            jTd.appendTo(jTr);
        });

        // 添加到表格
        jTr.appendTo(jBodyT);
    },
    //...............................................................
    _draw_col : function(jTd, col, o){
        var UI = this;
        jTd.attr("key", col.key);
        // console.log("_draw_col", col.key, o)
        var txt = UI.val_display(col, o);
        txt = UI.text(txt);
        if(col.escapeHtml)
            txt = $('<div>').text(txt).html();
        jTd.html(txt);
    },
    //...............................................................
    __check_cols_org_size : function(jHeadT, jBodyT){
        var UI = this;
        if(!UI._cols_org_size){
            // 记录一下各个列原始的宽度
            UI._cols_org_size = [];
            jBodyT.find("tr:first-child td").each(function(index){
                UI._cols_org_size[index] = $(this).outerWidth(true);
            });
            // 连同 Header 的原始宽度也一并计算 
            if(UI.options.layout.withHeader)
                jHeadT.find("tr:first-child td").each(function(index){
                    var jTd = $(this);
                    var w = Math.max(jTd.outerWidth(true), UI._cols_org_size[index]);
                    UI._cols_org_size[index] = w;
                    jTd.attr("org-width", w);
                });
        }
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jHead  = UI.arena.find(".otable-head");

        // 还木有装载表格数据呢，不搞
        if(jHead.size()==0)
            return;

        var jHeadT = jHead.children(".otable-head-t");
        var jBody  = UI.arena.find(".otable-body");
        var jBodyT = jBody.children(".otable-body-t");
        var jRuler = UI.arena.find(".otable-ruler");

        // 确保记录了原始的列daxiao
        UI.__check_cols_org_size(jHeadT, jBodyT);
        
        // 开始计算吧，少年
        var W = jRuler.outerWidth();   // 视口的宽度 
        // console.log(jBody.width()
        //             ,jBody.outerWidth()
        //             ,jBody.outerWidth(true)
        //             ,jBody.innerWidth()
        //             , "ruler:"
        //             ,jRuler.outerWidth()
        //             ,jRuler.innerWidth()
        //             ,jRuler.width())
        // 计算 body 区域，是否有滚动条，如果有滚动条，将标题区域也加一个边距
        var jSBWidth = jBody.width() - W;
        if(jSBWidth>0){
            jHead.css("padding-right", jSBWidth+"px");
        }
        // 计算各个列的初始的宽度
        var _ws = [].concat(UI._cols_org_size);

        // 根据配置的 sizeHint 来调整宽度 
        var szht = UI.options.layout.sizeHint;
        if(_.isArray(szht)){
            var len = Math.min(szht.length, _ws.length);
            for(var i=0;i<len;i++){
                if(_.isNumber(szht[i]) && szht[i]!=0 )
                    _ws[i] = szht[i];
            }
        }
        // 数字表示第一列
        else if(_.isNumber(szht) && szht!=0){
            _ws[0] = szht;
        }
        // 把 * 当做 -1 吧
        else if('*' == szht){
            _ws[0] = -1;
        }
        // 其他的无视
        else{
            // *_* 这里就是『无视』
        }

        // 计算一下和，以及需要分配的剩余宽度的列
        var w_sum = 0;
        var d_cols_index = [];
        for(var i=0;i<_ws.length;i++){
            var v = _ws[i];
            // 负数：参与自动分配，但是不会低于最小值
            if(v < 0){
                v = Math.abs(v);
                w_sum += v;
                _ws[i] = v;
                d_cols_index.push(i);
            }
            // 正数：不参与自动分配
            else{
                w_sum += v;
            }
        }

        //console.log("A:sum", w_sum, "cols:", _ws);

        // 开始分配剩余
        if(d_cols_index.length>0 && W > w_sum){
            var w_remain = W - w_sum;
            var w_ele = parseInt(w_remain / d_cols_index.length);
            // 再次分配的剩余
            var w_re2 = w_remain - (w_ele * d_cols_index.length);

            // 开始分配
            for(var x = 0; x<d_cols_index.length; x++){
                var index = d_cols_index[x];
                _ws[index] += w_ele;
            }
            // 二次分配
            for(var x = 0; x<w_re2; x++){
                var index = d_cols_index[x];
                _ws[index]++;
            }
        }

        //console.log("B:sum", w_sum, "cols:", _ws);

        // 计算总宽度 
        var w_table = 0;
        for(var x = 0; x<_ws.length; x++){
            w_table += _ws[x];
        }
        jHeadT.css("width", w_table);
        jBodyT.css("width", w_table);


        // 好，那么开始调整表头的第一列
        jHeadT.find("tr:first-child td").each(function(index){
            $(this).css("width", _ws[index]);
        });

        // 继续调整表体的第一列
        jBodyT.find("tr:first-child td").each(function(index){
            $(this).css("width", _ws[index]);
        });

        // 嗯，搞定收工
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);