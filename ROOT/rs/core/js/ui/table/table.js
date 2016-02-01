(function($z){
$z.declare([
    'zui',
    'ui/jtypes'
], function(ZUI, JsType){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="data.table" class="tbl">
        <div class="tbl-head">
            <table class="tbl-head-t"><tr></tr></table>
            <div class="tbl-checker" tp="none">
                <i tp="all" class="fa fa-check-square"></i>
                <i tp="none" class="fa fa-square-o current"></i>
                <i tp="part" class="fa fa-check-square"></i>
            </div>
        </div>
        <div class="tbl-body">
            <table class="tbl-body-t"><tbody></tbody></table>
            <div class="tbl-ruler"></div>
        </div>
    </div>
    <div code-id="checker" class="tbl-row-checker">
        <i tp="checkbox" class="fa fa-square-o current"></i>
        <i tp="checkbox" class="fa fa-check-square"></i>
    </div>
    <div code-id="data.loading" class="ui-loading">
        <i class="fa fa-spinner fa-pulse"></i> <span>{{loading}}</span>
    </div>
    <div code-id="thead.cell">
        <b class="tbl-col-tt"></b>
    </div>
</div>
<div class="ui-arena tbl" ui-fitparent="yes">
    I am table haha
</div>
*/};
//==============================================
return ZUI.def("ui.table", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/table/table.css",
    //...............................................................
    init : function(options){
        var UI  = this;
        var opt = options;
        $z.setUndefined(opt, "activable", true);
        $z.setUndefined(opt, "blurable", true);
        $z.setUndefined(opt, "resizable", true);
        $z.setUndefined(opt, "checkable", true);
        $z.setUndefined(opt, "multi",  opt.checkable?true:false);
        $z.setUndefined(opt, "idKey",     "id");
        $z.setUndefined(opt, "fields",   []);
        $z.setUndefined(opt, "layout",   {});
        //layout 的默认
        $z.setUndefined(opt.layout, "sizeHint"  , -1);
        $z.setUndefined(opt.layout, "cellWrap"  , "nowrap");
        $z.setUndefined(opt.layout, "withHeader", true);


        // 最后等重绘完毕模拟点击
        UI.on("tbl:change", function(){
            var jHead = UI.arena.find(".tbl-head");
            UI.arena.find(".tbl-body").scroll(function(e){
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
        UI.arena.find(".tbl-body").unbind();
    },
    //...............................................................
    events : {
        "click .tbl-row" : function(e){
            this.setActived(e.currentTarget);
        },
        "click .tbl-row-checker" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(e){
            var jq = $(e.target);
            var jRow = jq.parents(".tbl-row");
            if(this.options.blurable && !jRow.hasClass("tbl-row-actived"))
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
    getObjId : function(obj){
        return obj[this.options.idKey];
    },
    getObjName : function(obj){
        return obj[this.options.nmKey];
    },
    //...............................................................
    getActived : function(){
        return this.arena.find(".tbl-row-actived").data("OBJ");
    },
    getActivedId : function(){
        var obj = this.getActived();
        return obj ? this.getObjId(obj) : null;
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
        var jq = $z.jq(UI.arena, arg, ".tbl-row").first();
        if(jq.hasClass("tbl-row") && !jq.hasClass("tbl-row-actived")){
            UI.blur();
            jq.addClass("tbl-row-actived tbl-row-checked");
            var o = jq.data("OBJ");
            var index = jq.attr("index") * 1;
            // 触发消息 
            UI.trigger("tbl:actived", o, index);
            $z.invoke(UI.options, "on_actived", [o, index], UI);
        }
        // 同步选择器 
        UI.__sync_checker();
    },
    //...............................................................
    blur : function(){
        var UI = this;
        var jq = UI.arena.find(".tbl-row-checked");

        if(jq.size() > 0){
            var o = jq.removeClass("tbl-row-actived tbl-row-checked").data("OBJ");

            // 同步
            UI.__sync_checker();

            // 触发消息 
            UI.trigger("tbl:blur", o);
            $z.invoke(UI.options, "on_blur", [o], UI);
        }
    },
    //...............................................................
    getChecked : function(){
        var UI = this;
        var objs = [];
        UI.arena.find(".tbl-row-checked").each(function(){
            objs.push($(this).data("OBJ"));
        });
        return objs;
    },
    //...............................................................
    check : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".tbl-row").not(".tbl-row-checked");
        if(jq.size()>0){
            var objs = [];
            jq.addClass("tbl-row-checked").each(function(){
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("tbl:checked", objs);
            $z.invoke(UI.options, "on_checked", [objs], UI);

            // 同步选择器 
            UI.__sync_checker();
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".tbl-row-checked");
        if(jq.size()>0){
            var objs = [];
            jq.removeClass("tbl-row-checked tbl-row-actived").each(function(){
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
    __sync_checker : function(){
        var UI = this;
        var jChecker = UI.arena.find(".tbl-checker:visible");

        if(jChecker.size()<=0){
            return;
        }

        var jRows = UI.arena.find(".tbl-row");
        var row_nb = jRows.size();
        var row_unchecked = jRows.not(".tbl-row-checked").size();

        // 全都选中了
        if(row_nb > 0 && row_unchecked==0){
            jChecker.attr("tp", "all");
        }
        // 全木选中
        else if(row_nb == row_unchecked){
            jChecker.attr("tp", "none");
        }
        // 部分选中
        else{
            jChecker.attr("tp", "part");
        }
    },
    //...............................................................
    toggle : function(arg){
        var UI = this;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jq = $z.jq(jTbody, arg, ".tbl-row");
        if(jq.size()>0){
            var checkeds = [];
            var unchecks = [];
            jq.each(function(){
                var jRow = $(this).closest(".tbl-row");
                if(jRow.size()==0){
                    throw "tbl: not row or row item : " + this;
                }

                var o = jRow.data("OBJ");
                if(jRow.hasClass("tbl-row-checked")){
                    jRow.removeClass("tbl-row-actived tbl-row-checked");
                    unchecks.push(o);
                }else{
                    jRow.addClass("tbl-row-checked");
                    checkeds.push(o);
                }
            });

            // 同步选择器 
            UI.__sync_checker();

            // 触发消息 : checked
            if(checkeds.length > 0) {
                UI.trigger("tbl:checked", checkeds);
                $z.invoke(UI.options, "on_checked", [checkeds], UI);    
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("tbl:uncheck", unchecks);
                $z.invoke(UI.options, "on_uncheck", [unchecks], UI);    
            }
        }
    },
    //...............................................................
    getData : function(arg){
        var UI = this;
        var jTBody = UI.arena.find(".tbl-body-t>tbody");
        // 本身就是元素
        if($z.isjQuery(arg) || _.isElement(arg)){
            var jq = $(arg).closest(".tbl-row");
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
        return UI.ui_format_data(function(opt){
            var objs = [];
            jTBody.children('.tbl-row').each(function(){
                objs.push($(this).data("OBJ"));
            });
            return objs;
        });;
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
            return UI.arena.find(".tbl-row-actived");
        }
        // 如果是字符串表示 ID
        else if(_.isString(it)){
            return UI.arena.find(".tbl-row[oid="+it+"]");
        }
        // 本身就是 dom
        else if(_.isElement(it) || $z.isjQuery(it)){
            return $(it).closest(".tbl-row");
        }
        // 数字
        else if(_.isNumber(it)){
            return $z.jq(UI.arena, it, ".tbl-row");
        }
        // 靠不晓得了
        else {
            throw "unknowns row selector: " + row;
        }
    },
    //...............................................................
    add : function(obj, it, direction) {
        var UI = this;
        var objs = _.isArray(obj) ? obj : [obj];

        objs.forEach(function(o, index){
            UI._upset_row(o, null, UI.$item(it), direction);
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
    update : function(obj, it) {
        this._upset_row(obj, null, this.$item(it), 0);
    },
    //...............................................................
    showLoading : function(){
        var UI = this;
        var jq = UI.ccode("data.loading");
        UI.arena.empty().append(jq);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI  = this;
        var opt = UI.options;

        var jq = UI.ccode("data.table");

        UI.arena.find(".tbl-body").unbind();
        UI.arena.empty().append(jq);
        
        var jBodyT = jq.find(".tbl-body-t");

        // 输出表头
        var jHeadT = UI._redraw_header();
        var jHead = jHeadT.parent();

        // 检查要输出的数据
        if(!objs)
            return;

        objs = _.isArray(objs) ? objs : [objs];

        // 输出表格内容 
        objs.forEach(function(o, index){
            UI._upset_row(o, jBodyT);
        });

        // 如果需要显示选择框
        if(opt.checkable){
            jq.addClass("tbl-show-checkbox");
            jHead.children('.tbl-checker').show();
        }else{
            jHead.children('.tbl-checker').hide();
        }

        // 设置固定属性，以便 resize 函数时时计算宽度
        jHeadT.css("table-layout", "fixed");
        jBodyT.css("table-layout", "fixed");

        // 重新计算尺寸
        UI.resize();

        // 最后触发消息
        UI.trigger("tbl:change", objs);
        $z.invoke(opt, "on_change", [objs], UI);
    },
    //...............................................................
    _upset_row : function(o, jBodyT, jReferRow, direction){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        
        jBodyT = jBodyT || UI.arena.find(".tbl-body-t");

        // 创建行
        var jRow;
        // 没有参考 row，插入到表格后头
        if(!jReferRow || jReferRow.size() == 0){
            jBodyT = jBodyT || UI.arena.find(".tbl-body-t");
            jRow = $('<tr class="tbl-row">').appendTo(jBodyT);
        }
        // 有的话，看方向 0 表示替换
        else if(0 === direction){
            jRow = jReferRow.empty();
        }
        // 正数查到前面
        else if(direction>0){
            jRow = $('<tr class="tbl-row">').insertBefore(jReferRow);
        }
        // 负数插到后面
        else {
            jRow = $('<tr class="tbl-row">').insertAfter(jReferRow);
        }

        // 添加必要属性
        jRow.data("OBJ", o);
        if(o[opt.idKey])
            jRow.attr("oid", o[opt.idKey]);

        if(opt.nmKey && o[opt.nmKey])
            jRow.attr("onm", o[opt.nmKey]);

        // 循环输出每一列
        UI.options.fields.forEach(function(fld){
            if(fld.hide)
                return;
            var jTd = $('<td>');
            UI._draw_cell(jTd, fld, o);
            jTd.appendTo(jRow);
        });

        // 如果需要显示选择框
        if(opt.checkable){
            jRow.find("td:first-child").prepend(UI.ccode("checker"));
        }

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_row", [jRow, o], context);

        // 添加到表格
        return jRow;
    },
    //...............................................................
    _draw_cell : function(jTd, fld, o){
        var UI = this;
        jTd.attr("key", fld.key);
        // 取得标准值
        var ftype = JsType[fld.type];
        if(!ftype){
            alert("Unsupport fld.type " + fld.type);
            throw "Unsupport fld.type " + fld.type;
        }
        var val = $z.getValue(o, fld.key);
        var v = ftype.parse(fld, val);

        // 是不是自定义
        if(_.isFunction(fld.display)){
            s = fld.display.call(UI.options.context||UI, o, fld, v);
        }
        // 否则变成字符串
        else{
            s = ftype.toText(fld, v);
        }

        // 国际化
        s = UI.text(s);

        // 逃逸 HTML
        if(_.isUndefined(fld.escapeHtml) || fld.escapeHtml)
            jTd.text(s || '');
        else
            jTd.html(s || '');
    },
    //...............................................................
    _redraw_header : function(){
        var UI  = this;
        var opt = UI.options;
        var jHeadT = UI.arena.find(".tbl-head-t");
        var jRow = jHeadT.find("tr").first();

        // 循环输出每一列
        opt.fields.forEach(function(fld){
            if(fld.hide)
                return;
            var jTd = $('<td class="tbl-col-o">').appendTo(jRow);
            jTd.append(UI.ccode("thead.cell"));
            jTd.find(".tbl-col-tt").text(UI.text(fld.text || col.key));
        });

        return jHeadT;
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
        var jHead  = UI.arena.find(".tbl-head");

        // 还木有装载表格数据呢，不搞
        if(jHead.size()==0)
            return;

        var jHeadT = jHead.children(".tbl-head-t");
        var jBody  = UI.arena.find(".tbl-body");
        var jBodyT = jBody.children(".tbl-body-t");
        var jRuler = UI.arena.find(".tbl-ruler");

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