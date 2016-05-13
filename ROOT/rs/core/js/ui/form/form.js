(function($z){
$z.declare([
    'zui',
    'ui/jtypes'
], function(ZUI, jType){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="group" class="form-group">
        <div class="fg-title"></div>
        <div class="fg-fields"></div>
    </div>
    <div code-id="field" class="form-fld"><div
        class="ff-txt"></div><div
        class="ff-val"><div class="ffv-ui"></div><div class="ffv-tip"></div></div></div>
</div>
<div class="ui-arena form" ui-fitparent="yes">
    <div class="form-title"></div>
    <div class="form-body"><div class="form-body-wrapper"></div></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["theme/ui/form/form.css", "theme/ui/form/component.css"],
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(options){
        var UI = this;
        //$z.evalFunctionField(options);
        $z.setUndefined(options, "mergeData", true);
        $z.setUndefined(options, "idKey", "id");
        $z.setUndefined(options, "uiWidth", "auto");
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 整理 fields 字段
        var grpList = [];
        // 创建第一个组
        var grp = {
            fields : []
        };
        // 将所有字段归纳为组为单位的列表
        options.fields.forEach(function(fld){
            // 是字段组，考虑创建新组
            if(!_.isString(fld.key)){
                // 将之前的收集字段用组添加进列表
                if(grp.fields.length>0){
                    grpList.push(grp);
                    grp = {fields : []};
                }
                // 添加自身
                grpList.push(fld);
            }
            // 普通字段，归纳到组里
            else{
                grp.fields.push(fld);
            }
        });
        // 最后确保最后一段被添加了
        if(grp.fields.length > 0){
            grpList.push(grp);
        }
        // 嗯，那么现在所有的顶层对象都是组了
        UI.groups = grpList;

        // 给每个字段做个编号
        UI._fld_form_keys = [];
        var _fld_form_keys_seq = 0;

        // 重新分析所有的字段，确保都有 uiType/uiConf
        // 同时归纳需要加载的 UI 类型
        var uiTypeMap = {};
        for(var i=0;i<UI.groups.length;i++){
            var grp = UI.groups[i];
            for(var m=0;m<grp.fields.length;m++){
                var fld = grp.fields[m];

                // 给自己分配一个唯一的键值，这个键值基本是用来做 defer_report 的
                fld._form_key = "_form_fld_" + (_fld_form_keys_seq++);
                UI._fld_form_keys.push(fld._form_key);

                // 字段的类型默认为 string
                fld.type = fld.type || "string";
                // 做了自定义显示
                if(fld.uiType){
                    // 那就啥也不做了
                }
                // 有快捷定义 ..
                else if(fld.editAs){
                    // 内置
                    if(/^(input|color|label|switch|text|link|(drop|check|radio)list)$/.test(fld.editAs)){
                        fld.uiType = "ui/form/c_" + fld.editAs;
                    }
                    // 各种 picker
                    else if(/^(o|date|time)picker$/.test(fld.editAs)){
                        fld.uiType = "ui/picker/" + fld.editAs;
                    }
                    // 靠，不支持
                    else{
                        alert("Unknown form component: " + fld.editAs);
                        throw "Unknown form component: " + fld.editAs;
                    }
                }
                // 采用默认的
                else{
                    fld.uiType = "ui/form/c_input";
                }
                // 确保 uiConf
                fld.uiConf = fld.uiConf || {};

                // 确保 acceptForFormUpdate
                $z.setUndefined(fld, "acceptForFormUpdate", function(obj){
                    return !_.isUndefined($z.getValue(obj, this.key));
                });

                // 归纳控件类型
                uiTypeMap[fld.uiType] = true;
            }
        }
        // 计入
        UI.uiTypes = [];
        for(var key in uiTypeMap)
            UI.uiTypes.push(key);
    },
    //...............................................................
    events : {
        "click .fg-title" : function(e){
            var jG = $(e.currentTarget).closest(".form-group");
            jG.toggleClass("form-group-hide");
        }
    },
    //...............................................................
    _draw_field : function(jG, fld, grp){
        var UI  = this;
        var opt = UI.options;
        var uiw = fld.uiWidth || grp.uiWidth || opt.uiWidth;
        var jF  = UI.ccode("field")
                    .attr("ui-width", uiw)
                    .attr("fld-key", fld.key)
                    .data("@FLD", fld)
                    .data("@jOBJ", jType(fld))
                    .appendTo(jG);
        // 指定的宽度，需要特殊标记
        if(!isNaN(uiw * 1)){
            jF.attr("ui-fixed-width", uiw);
        }

        // 自定义的类
        if(fld.className)
            jF.addClass(grp.className);

        var jTxt = jF.children(".ff-txt");
        var jFui = jF.find(".ffv-ui");
        var jTip = jF.find(".ffv-tip");
        // 绘制标题
        if(fld.required || fld.icon || fld.title){
            if(fld.required)
                jTxt.attr("required","yes");
            if(fld.icon)
                $(fld.icon).appendTo(jTxt);
            if(fld.title)
                $('<span class="fft-text">').html(UI.text(fld.title)).appendTo(jTxt);
        }
        // 靠，啥都木有，搞掉标题区
        else {
            jTxt.remove();
        }


        // 绘制值
        seajs.use(fld.uiType, function(TheUI){
            var theConf = {};
            // 如果用户定义了 display 函数，则自动应用到控件 UI 里
            if(!fld.uiConf.display && _.isFunction(fld.display)){
                theConf.display = fld.display;
            }
            // 如果用户没有定义控件的 title 属性，将字段的弄过去
            if(!fld.uiConf.title && fld.title){
                theConf.title = fld.title;
            }
            // 渲染 UI 控件 
            var theUI = new TheUI(_.extend(theConf, fld.uiConf, {
                gasketName : fld.key,
                $pel       : jFui,
                context    : fld,
                on_change  : function(v){
                    UI.__on_change(this, v);
                }
            })).render(function(){
                // console.log("UI.defer_report:", fld.uiType, fld._form_key);
                UI.defer_report(fld._form_key);
            });
            // 检查 UI 控件的合法性
            if(!_.isFunction(theUI.setData) || !_.isFunction(theUI.getData)){
                alert("field '" + fld.key + "' has invalid UIComponent : " + fld.uiType + " : without get/setData()");
                throw "field '" + fld.key + "' has invalid UIComponent : " + fld.uiType + " : without get/setData()";
            }
            // 记录实例的引用
            jF.data("@UI", theUI);
        });

        // 绘制补充说明
        if(fld.tip){
            jTip.html(UI.text(fld.tip));
        }else{
            jTip.remove();
        }
    },
    //...............................................................
    __on_change : function(fld, v){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var val = jType(fld).parse(v).toNative();
        $z.invoke(opt, "on_change", [fld.key, val], context);
        UI.trigger("form:change", fld.key, val);
    },
    //...............................................................
    _draw_group : function(grp){
        var UI  = this;
        var opt = UI.options;
        var jG  = UI.ccode("group")
                    .data("@GRP",grp)
                    .attr("ui-width", grp.uiWidth || opt.uiWidth)
                    .appendTo(UI.arena.find(".form-body-wrapper"));
        var jGtt  = jG.children(".fg-title");
        var jGff  = jG.children(".fg-fields");

        // 自定义的类
        if(grp.className)
            jG.addClass(grp.className);

        // 标记多重组
        if(grp.cols > 1){   
            jG.attr("multi-cols", "yes");
        }
        
        // 绘制字段标题
        if(grp.icon || grp.title){
            if(grp.icon)
                $('<span class="fg-tt-icon">').html(grp.icon).appendTo(jGtt);
            if(grp.title)
                $('<span class="fg-tt-text">').html(UI.text(grp.title)).appendTo(jGtt);
        }
        // 移除标题
        else{
            jGtt.remove();
        }

        // 绘制每个字段 
        for(var i=0;i<grp.fields.length;i++){
            UI._draw_field(jGff, grp.fields[i], grp);
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;
        var jTitle = UI.arena.children(".form-title");
        var jBody  = UI.arena.children(".form-body");
        var jBodyW = jBody.children(".form-body-wrapper");
        jBodyW.empty();

        // 设置标题区域
        if(opt.title){
            jTitle.html(UI.text(opt.title));
        }
        else{
            jTitle.remove();
        }

        // 首先加载所有的子控件
        seajs.use(UI.uiTypes, function(){
            // 循环绘制组，在字段的绘制里会 defer_report
            for(var i=0;i<UI.groups.length;i++){
                UI._draw_group(UI.groups[i]);
            }
        });

        //console.log("form redraw defer:", UI._fld_form_keys);
        return UI._fld_form_keys;
    },
    //...............................................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;

        // 得到整体的宽度
        var jTitle = UI.arena.children(".form-title");
        var jBody  = UI.arena.children(".form-body");

        // 还没准备好，拒绝重绘
        if(jBody.width() == 0 && jBody.height() == 0){
            return;
        }

        jBody.css("height", UI.arena.height() - jTitle.outerHeight(true));

        var W = jBody.children(".form-body-wrapper").width();
        
        // 首先计算列，看看一个列有多宽
        var colNb       = opt.cols || 1;
        var colSizeHint = opt.colSizeHint || [0];
        var col_widths  = [];
        var col_wsum    = W;
        var col_autoN   = 0;
        for(var i=0;i<colNb;i++){
            var c_W = colSizeHint.length >i ? colSizeHint[i] : 0;  // 0 表示自动分配
            // 浮点数表示比例
            if(c_W > 0 && c_W < 1){
                c_W = Math.round(W * c_W);
                col_wsum -= c_W;
            }
            // 大于1 取整后表示绝对数
            else if(c_W > 1) {
                c_W = Math.round(c_W);
            }
            // 其他的作为自动分配
            else{
                c_W = 0; 
                col_autoN ++;
            }
            // 计入
            col_widths[i] = c_W;
        }
        // 最后搞下自动分配
        if(col_autoN > 0) {
            var grp_autoW = parseInt(col_wsum / col_autoN);
            var grp_auto_indexes = [];
            for(var i=0;i<col_widths.length;i++){
                if(col_widths[i] == 0) {
                    col_widths[i] = grp_autoW;
                    col_wsum -= grp_autoW;
                    grp_auto_indexes.push(i);
                }
            }
            // 处理自动分配剩下的余数
            if(col_wsum>0) {
                for(var i=0;i<col_wsum;i++){
                    col_widths[grp_auto_indexes[i]]++;
                }
            }
        }

        // 拿到组集合
        var jGrps = UI.arena.find(".form-group");

        // 然后依次计算每个组宽度，和其内的字段的宽度
        jGrps.each(function(index){
            var jG    = $(this).css("width", col_widths[index % colNb]);
            var grp   = jG.data("@GRP");
            var colnb = Math.max(grp.cols || 1, 1);
            var fbW   = $(this).find(".fg-fields").width(); 
            var fldW = parseInt(fbW / Math.max(colnb, 1)) - (colnb>1?1:0);

            // 同时归纳最大的字段标题宽度
            var maxFFW = 0;
            jG.find(".form-fld").each(function(index, ele){
                var jF   = $(this);
                var fld  = jF.data("@FLD");
                var span = Math.max(fld.span || 1, 1);
                var theW = Math.min(fldW * span, fbW);
                jF.css("width", theW);

                // 看看当前字段的标题
                var jTxt = jF.children(".ff-txt");
                if(jTxt.size()>0){
                    if(jTxt.attr("org-width")){
                        maxFFW = Math.max(maxFFW, jTxt.attr("org-width")*1);
                    }
                    // 从来没记录过原始宽度，嗯 ...
                    else{
                        var w = jTxt.innerWidth();
                        maxFFW = Math.max(maxFFW, w);
                        jTxt.attr("org-width", w);
                    }
                }
            });

            // 设置本组最大的标题宽度
            jG.find(".ff-txt").css("width", maxFFW);
            
            // 继续设置所有的值应该的宽度
            jG.find(".form-fld").each(function(){
                var jF   = $(this);
                var fld  = jF.data("@FLD");
                var jTxt = jF.children(".ff-txt");
                var jVal = jF.children(".ff-val");
                var jUi  = jVal.children(".ffv-ui");

                // 计算值区域的宽度
                var vW = jF.width();
                if(jTxt.size()>0){
                    vW  = vW - jTxt.outerWidth(true) - 1;
                }
                jVal.css("width", vW);

                // 指定标题行的高度
                if(jTxt.size()>0 && (fld.autoLineHeight || grp.autoLineHeight)){
                    jTxt.css({
                        "line-height" : jVal.outerHeight()+"px",
                        "height"      : jVal.outerHeight(),
                        "padding-top" : 0,
                        "padding-bottom" : 0
                    });
                }
                
                // 指定宽度
                var uiw = jF.attr("ui-fixed-width");
                if(uiw){
                    var uiWidth = $z.dimension(uiw * 1, vW);
                    jUi.css("width", uiWidth);
                }
                
            });

        });
    },
    //...............................................................
    update : function(key, val){
        var UI  = this;
        // 解析参数
        var obj = {};
        if(_.isString(key)){
            obj[key] = val;
        }else{
            _.extend(obj, key);
        }
        // 执行更新
        UI.ui_parse_data(obj, function(o){
            // 设置每个字段
            UI.arena.find(".form-fld").each(function(){
                var jF  = $(this);
                var jso = jF.data("@jOBJ");
                var fui = jF.data("@UI");
                if(jso.type().acceptForFormUpdate(o)){
                    var val = jso.parseByObj(o).value();
                    fui.setData(val, jso);
                }
            });
        });
    },
    //...............................................................
    setData : function(obj){
        var UI = this;
        UI.ui_parse_data(obj, function(o){
            // 记录当前数据
            UI.$el.data("@DATA", o);

            // 设置每个字段
            UI.arena.find(".form-fld").each(function(){
                var jF  = $(this);
                var jso = jF.data("@jOBJ");
                var fui = jF.data("@UI"); 
                var val = jso.parseByObj(o).value();
                fui.setData(val, jso);
            });
        });
    },
    //...............................................................
    getFormCtrl : function(key) {
        var UI = this;
        var jF = UI.arena.find('.form-fld[fld-key="'+key+'"]');
        return jF.data("@UI");
    },
    //...............................................................
    getFormDataObj : function(){
        return this.$el.data("@DATA");
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            // 准备返回值
            var re = opt.mergeData ? _.extend({}, UI.$el.data("@DATA")) : {};

            // 读取每个字段的返回值
            UI.arena.find(".form-fld").each(function(){
                var jF  = $(this);
                var jso = jF.data("@jOBJ");
                var fui = jF.data("@UI"); 
                var v  = fui.getData();
                jso.parse(v).setToObj(re);
            });

            // 返回值
            return re;
        });
    },
    //...............................................................
    getObjId : function(obj){
        return obj[this.options.idKey];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);