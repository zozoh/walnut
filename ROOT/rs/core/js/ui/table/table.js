(function($z){
$z.declare([
    'zui',
    'ui/jtypes'
], function(ZUI, jType){
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
            <table class="tbl-body-t"><colgroup></colgroup><tbody></tbody></table>
            <div class="tbl-ruler"></div>
        </div>
    </div>
    <div code-id="checker" class="tbl-row-checker">
        <i tp="checkbox" class="fa fa-square-o current"></i>
        <i tp="checkbox" class="fa fa-check-square"></i>
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
        $z.setUndefined(opt, "escapeHtml", true);
        $z.setUndefined(opt, "fields",   []);
        $z.setUndefined(opt, "layout",   {});
        //layout 的默认
        $z.setUndefined(opt.layout, "sizeHint"  , -1);
        $z.setUndefined(opt.layout, "cellWrap"  , "nowrap");
        $z.setUndefined(opt.layout, "withHeader", true);

        // 展开所有的 fields 
        opt.__fields = [];
        opt.fields.forEach(function(fld){
            // 字段添加
            if(fld.key){
                opt.__fields.push(fld);
            }
            // 字段组，添加子
            else if(_.isArray(fld.fields)){
                opt.__fields = opt.__fields.concat(fld.fields);
            }
        });

        // 预先编译每个字段的显示
        opt.__jsos = [];
        opt.__fields.forEach(function(fld){
            $z.evalFldDisplay(fld);
            opt.__jsos.push(jType(fld));
        });

        // 最后等重绘完毕模拟点击
        UI.on("table:change", function(){
            UI.arena.find(".tbl-body").scroll(function(e){
                var left = $(this).scrollLeft();
                UI.arena.find(".tbl-head").css("left", left * -1);
            });
        });
    },
    //...............................................................
    //
    getFieldType : function(key) {
        var opt = this.options;
        for(var i=0; i<opt.__fields.length; i++) {
            var fld = opt.__fields[i];
            if(fld && fld.key == key)
                return fld.type || "string";
        }
    },
    //...............................................................
    depose : function(){
        var UI = this;
        UI.arena.find(".tbl-body").unbind();
    },
    //...............................................................
    events : {
        "click .tbl-row" : function(e){
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
                    var jA = UI.arena.find(".tbl-row-actived");

                    // 没有项目被激活，那么从头选
                    if(jA.size() == 0){
                        jA = UI.arena.find(".tbl-row:eq(0)");
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
    isActived : function(arg){
        var jRow = this.$item(arg);
        return jRow.hasClass("tbl-row-actived");
    },
    //...............................................................
    setActived : function(arg){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 不许激活项目 ...
        if(!opt.activable)
            return;

        // 未给入参数，相当于 blur
        if(_.isUndefined(arg) || _.isNull(arg)){
            UI.setAllBure();
            return;
        }

        // 执行查找
        var jRow = UI.$item(arg).first();
        if(jRow.size()>0 && !UI.isActived(jRow)){
            // 得到数据
            var o = jRow.data("OBJ");

            // 看看是不是调用者要禁止激活
            var cancelIt = $z.doCallback(opt.on_before_actived, [o, jRow], context);
            if(false === cancelIt){
                return;
            }

            // 取消其他的激活
            UI.setAllBure(o, jRow);
            jRow.addClass("tbl-row-actived tbl-row-checked");

            // 触发消息 
            UI.trigger("table:actived", o, jRow);
            $z.invoke(opt, "on_actived", [o, jRow], context);
        }
        // 同步选择器 
        UI.__sync_checker();
    },
    //...............................................................
    blur : function(){
        this.setAllBure();
    },
    //...............................................................
    setAllBure : function(nextObj, nextRow){
        var UI = this;
        var jRows = UI.arena.find(".tbl-row-checked");

        if(jRows.size() > 0){
            // 移除标记
            jRows.removeClass("tbl-row-actived tbl-row-checked");

            // 获取数据
            var objs = [];
            jRows.each(function(){
                objs.push($(this).data("OBJ"));
            });

            // 同步选择器 
            UI.__sync_checker();

            // 触发消息 
            UI.trigger("table:blur", objs, jRows, nextObj, nextRow);
            $z.invoke(UI.options, "on_blur", [objs, jRows, nextObj, nextRow], UI);
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
    isChecked : function(arg){
        var jRow = this.$item(arg);
        return jRow.hasClass("tbl-row-checked");
    },
    //...............................................................
    getCheckedRow : function(){
        return this.arena.find(".tbl-row-checked");
    },
    //...............................................................
    check : function(arg){
        var UI     = this;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jRows  = _.isUndefined(arg)?jTbody.find(".tbl-row"):UI.$item(arg);
        jRows.not(".tbl-row-checked");
        if(jRows.size()>0){
            jRows.addClass("tbl-row-checked");
            var objs = UI.getChecked();
            // 触发消息 
            UI.trigger("table:checked", objs, jRows);
            $z.invoke(UI.options, "on_checked", [objs, jRows], UI);

            // 同步选择器 
            UI.__sync_checker();
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI     = this;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jRows  = _.isUndefined(arg)?jTbody.find(".tbl-row-checked"):UI.$item(arg);
        jRows.not(":not(.tbl-row-checked)");
        if(jRows.size()>0){
            var objs = [];
            jRows.removeClass("tbl-row-checked tbl-row-actived").each(function(){
                objs.push($(this).data("OBJ"));
            });
            // 触发消息 
            UI.trigger("table:unchecked", objs, jRows);
            $z.invoke(UI.options, "on_unchecked", [objs, jRows], UI);

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
        var UI  = this;
        var opt = UI.options;
        var jTbody = UI.arena.find(".tbl-body-t>tbody");
        var jRows = $z.jq(jTbody, arg, ".tbl-row");
        if(jRows.size()>0){
            var checkeds = [];
            var unchecks = [];
            jRows.each(function(){
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
                UI.trigger("table:checked", checkeds);
                $z.invoke(opt, "on_checked", [checkeds], UI);    
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("table:uncheck", unchecks);
                $z.invoke(opt, "on_uncheck", [unchecks], UI);    
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
            UI.arena.find('.tbl-row').each(function(){
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
    add : function(objs, it, direction) {
        var UI = this;
        objs = _.isArray(objs) ? objs : [objs];

        objs.forEach(function(o, index){
            UI._upsert_row(o, null, UI.$item(it), direction);
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
        if(UI.isActived(jRow) || UI.isChecked(jRow)){
            jN2 = jRow.last().next();
            if(jN2.size() == 0){
                jN2 = jRow.first().prev();
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
        this._upsert_row(obj, null, this.$item(it || this.getObjId(obj)), 0);
    },
    //...............................................................
    _draw_data : function(objs){
        var UI  = this;
        var opt = UI.options;

        var jq = UI.ccode("data.table");

        UI.arena.find(".tbl-body").unbind();
        UI.arena.empty().append(jq);
        
        var jTBody = jq.find(".tbl-body-t");

        // 输出表头
        var jTHead = UI._draw_header();
        var jHead = jTHead.parent();

        // 检查要输出的数据
        if(!_.isArray(objs))
            return;

        // 输出表格内容 
        objs.forEach(function(o, index){
            UI._upsert_row(o, jTBody);
        });

        // 如果需要显示选择框 ...
        if(opt.checkable){
            jq.addClass("tbl-show-checkbox");
            jHead.children('.tbl-checker').show();
        }
        // 否则，顶层没必要显示全局选择器
        else{
            jHead.children('.tbl-checker').hide();
        }

        // 设置固定属性，以便 resize 函数时时计算宽度
        jTHead.css("table-layout", "fixed");
        jTBody.css("table-layout", "fixed");

        // 重新计算尺寸
        UI.resize();

        // 最后触发消息
        UI.trigger("table:change", objs);
        $z.invoke(opt, "on_change", [objs], UI);
    },
    //...............................................................
    _upsert_row : function(o, jTBody, jReferRow, direction){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        
        jTBody = jTBody || UI.arena.find(".tbl-body-t");

        // 创建行
        var jRow;
        // 没有参考 row，插入到表格后头
        if(!jReferRow || jReferRow.size() == 0){
            jTBody = jTBody || UI.arena.find(".tbl-body-t");
            jRow = $('<tr class="tbl-row">').appendTo(jTBody);
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
        var colIndex = 0;
        opt.__jsos.forEach(function(jso){
            if(jso.type().hide)
                return;
            var jTd = $('<td>');
            UI._draw_cell(jTd, jso, o);
            // 标记第一列
            if((colIndex++) == 0){
                jTd.addClass("tbl-row-td0");
            }
            jTd.appendTo(jRow);
        });

        // 如果需要显示选择框
        if(opt.checkable){
            jRow.find("td:first-child").prepend(UI.ccode("checker"));
        }

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_row", [jRow, o], context);

        // 如果原来就已经是高亮的，那么还需要回调一下激活事件
        if(jRow.hasClass("tbl-row-actived")){
            UI.trigger("table:actived", o, jRow);
            $z.invoke(opt, "on_actived", [o, jRow], context);
        }

        // 添加到表格
        return jRow;
    },
    //...............................................................
    _draw_cell : function(jTd, jso, o){
        var UI   = this;
        var fld  = jso.type();
        var context = UI.options.context || UI;
        jTd.attr("key", fld.key);
        if(fld.className)
            jTd.addClass(fld.className);

        // 获取字段显示值
        var s  = fld.__dis_obj.call(context, o, jso);

        // 国际化
        s = UI.text(s);

        // 逃逸 HTML
        if(fld.escapeHtml === true)
            jTd.text(s || '');
        else
            jTd.html(s || '');

    },
    //...............................................................
    _draw_header : function(){
        var UI  = this;
        var opt = UI.options;
        var jTHead   = UI.arena.find(".tbl-head-t");
        var jColGrp  = UI.arena.find("colgroup");
        var jHeadRow = jTHead.find("tr").first();

        // 循环输出每一列
        var colIndex = 0;
        opt.__fields.forEach(function(fld){
            if(fld.hide)
                return;
            // 表头
            var jTd = $('<td class="tbl-col">').appendTo(jHeadRow);
            jTd.append(UI.ccode("thead.cell"));
            jTd.find(".tbl-col-tt").text(UI.text(fld.title || fld.key));

            // 标记第一列
            if((colIndex++) == 0){
                jTd.addClass("tbl-col-0");
            }

            // 表体，添加每一列的控制
            jColGrp.append($('<col>'));
        });

        return jTHead;
    },
    //...............................................................
    __check_cols_org_size : function(jTHead, jTBody){
        var UI = this;
        if(!UI._cols_org_size){
            // 记录一下各个列原始的宽度
            var colszs = [];
            jTBody.find("tr:visible").first().children("td").each(function(index){
                var w = $(this).outerWidth(true);
                colszs[index] = w;
            });
            // 计算总宽度
            var colsum = 0;
            for(var i=0;i<colszs.length;i++)
                colsum += colszs[i];

            if(colsum == 0)
                return false;

            // 连同 Header 的原始宽度也一并计算
            var jTHeadTds = jTHead.find("tr:first-child td");
            if(UI.options.layout.withHeader){
                jTHeadTds.each(function(index){
                    var jTd = $(this);
                    var w = Math.max(jTd.outerWidth(true), colszs[index]);
                    colszs[index] = w;
                    jTd.attr("org-width", w);
                });
            }

            // 记录每列的原始宽度
            jTHeadTds.each(function(index){
                $(this).attr("org-width", colszs[index]);
            });

            // 记录到 UI 对象里，以便随时取用
            UI._cols_org_size = colszs;
        }
        // 返回 true 表示，这是个有效的原始宽度记录
        return true;
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jHead  = UI.arena.find(".tbl-head");

        // 还木有装载表格数据呢，不搞
        if(jHead.size()==0)
            return;

        var jTHead = jHead.children(".tbl-head-t");
        var jBody  = UI.arena.find(".tbl-body");
        var jTBody = jBody.children(".tbl-body-t");
        var jRuler = UI.arena.find(".tbl-ruler");

        // 确保记录了原始的列宽度。如果计算不成功，不搞
        if(!UI.__check_cols_org_size(jTHead, jTBody)){
            return;
        }
        
        // 开始计算吧，少年
        var W = jRuler.width() - 1;   // 视口的宽度 

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
                else if('*' == szht[i])
                    _ws[i] = -1;
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
        jTHead.css("width", w_table);
        jTBody.css("width", w_table);


        // 好，那么开始调整表头的第一列
        jTHead.find("tr:first-child td").each(function(index){
            $(this).css("width", _ws[index]);
        });

        // 继续调整表体的第一列
        jTBody.find("colgroup col").each(function(index){
            $(this).css("width", _ws[index]);
        });

        // 嗯，搞定收工
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);