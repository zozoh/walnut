(function($z){
$z.declare([
    'zui',
    'ui/jtypes',
    'ui/support/list_methods'
], function(ZUI, jType, ListMethods){
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
    <div code-id="empty" class="empty">
        <i class="zmdi zmdi-alert-circle-o"></i> <em>{{empty}}</em>
    </div>
</div>
<div class="ui-arena tbl" ui-fitparent="yes"></div>
*/};
//==============================================
return ZUI.def("ui.table", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/table/table.css",
    //...............................................................
    init : function(options){
        var UI  = ListMethods(this);
        var opt = options;

        // 父类
        UI.__setup_options(opt);

        // 初始化自己的属性
        $z.setUndefined(opt, "resizable", true);
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
    },
    //...............................................................
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
            this._do_click_list_item(e, false);
        },
        "click .tbl-row-checker" : function(e){
            e.stopPropagation();
            this.toggle(e.currentTarget);
        },
        "click .ui-arena" : function(e){
            // 只有点击空白区域，才会失去焦点
            if($(e.target).closest(".list-item").length == 0) {
                this.setAllBlur();
            }
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
        },
        // 打开
        "dblclick .tbl-row .tbl-row-td0" : function(e){
            var UI  = this;
            var opt = UI.options;
            var context = opt.context || UI;

            var jq  = $(e.currentTarget);
            var obj = this.getData(jq);

            $z.invoke(opt, "on_open", [obj, context]);
        }
    },
    //...............................................................
    getCheckedRow : function(){
        return this.$checked();
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
        var row_nb_checked = UI.$checked().length;
        var row_nb_unchecked = row_nb - row_nb_checked;

        // console.log(row_nb, row_nb_checked, row_nb_unchecked)

        // 全都选中了
        if(row_nb > 0 && row_nb_unchecked==0){
            jChecker.attr("tp", "all");
        }
        // 全木选中
        else if(row_nb == row_nb_unchecked){
            jChecker.attr("tp", "none");
        }
        // 部分选中
        else{
            jChecker.attr("tp", "part");
        }
    },
    //...............................................................
    __after_actived : function() {
        this.__sync_checker();
    },
    __after_blur : function() {
        this.__sync_checker();
    },
    __after_checked : function() {
        this.__sync_checker();
    },
    __after_toggle : function() {
        this.__sync_checker();
    },
    //...............................................................
    $listBody : function(){
        return this.arena.find(".tbl-body-t tbody");
    },
    //...............................................................
    _draw_empty : function() {
        var UI = this;
        UI.arena.append(UI.ccode("empty"));
    },
    //...............................................................
    __before_draw_data : function(objs) {
        var UI  = this;

        // 注销事件
        UI.arena.find(".tbl-body").unbind();

        // 清除内容区域
        UI.arena.empty();

        // 有内容
        UI.arena.append(UI.ccode("data.table"));
    },
    __after_draw_data : function(objs) {
        // 如果没有内容，也没必要做什么后续处理了
        if(objs.length == 0)
            return;

        // 后续处理
        var UI  = this;
        var opt = UI.options;

        var jq = UI.arena.children(".tbl");

        // 得到表格体
        var jTBody = jq.find(".tbl-body-t");
        
        // 输出表头
        var jTHead = UI._draw_header();
        var jHead = jTHead.parent();

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

        // 重新调整尺寸 
        UI._cols_org_size = null;

        // 监听表格体滚动事件
        UI.arena.find(".tbl-body").scroll(function(e){
            var left = $(this).scrollLeft();
            UI.arena.find(".tbl-head").css("left", left * -1);
        });
    },
    //...............................................................
    $createItem : function(){
        return $('<tr class="tbl-row">');
    },
    //...............................................................
    _draw_item : function(jRow, obj){
        var UI  = this;
        var opt = UI.options;
        
        // 循环输出每一列
        opt.__jsos.forEach(function(jso, colIndex){
            if(jso.type().hide)
                return;
            var jTd = $('<td>');
            UI._draw_cell(jTd, jso, obj);
            // 标记第一列
            if(colIndex == 0){
                jTd.addClass("tbl-row-td0");
            }
            jTd.appendTo(jRow);
        });

        // 如果需要显示选择框
        if(opt.checkable){
            jRow.find("td:first-child").prepend(UI.ccode("checker"));
        }
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
        var s  = fld.__dis_obj.call(context, o, jso, UI);

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
        
        // console.log("do count resize")

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

        // console.log("A:sum", w_sum, "cols:", _ws);

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