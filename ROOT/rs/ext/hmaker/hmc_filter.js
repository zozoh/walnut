/**
Searcher 控件的运行时行为:

opt : {
    // 来自编辑器
    "fields": [{
            "text": "价格",
            "name": "price",
            "multi": false,
            "hide": false,
            "items": [{
                    "text": "白菜价",
                    "type": "number_range",
                    "value": "(,10)"
                }]
        }, {..}]
    "btnExtTextShow"     : "展开折叠项",
    "btnMultiText"       : "多选",
    "btnExtTextHide"     : "收起折叠项目",
    "btnMultiOkText"     : "确认",
    "btnMultiCancelText" : "取消",
    "btnFldMoreText"     : "更多",
    "btnFldLessText"     : "收起",

    // 下面是调用者自由增加的属性
    emptyHtml : '<i class="zmdi zmdi-alert-circle-o"></i> Empty',

    // 下面由 IDE 指定
    forIDE : true,
}
*/
(function($, $z){
//...........................................................
// 绘制项目
function __draw_fld(opt, fld, jList) {
    var jFld = $('<div class="hmcf-fld">').attr({
        "key"         : fld.name,
        "less-row-nb" : 1,          // 显示更少选项的时候，保留几行
    });
    
    // 绘制字段标题
    var jFldInfo = $('<span class="fld-info">').appendTo(jFld);
    $('<em>').text(fld.text).appendTo(jFldInfo);

    // 绘制选项
    var jDiv = $('<div class="fld-items">').appendTo(jFld);
    // 选项表
    var jUl  = $("<ul>").appendTo(jDiv);
    if(_.isArray(fld.items) && fld.items.length > 0) {
        for(var i=0; i<fld.items.length; i++) {
            var item = fld.items[i];
            var jLi  = $('<li>').appendTo(jUl);
            jLi.attr({
                "it-type"  : item.type,
                "it-value" : item.value,
            });
            $('<em><i></i></em>').appendTo(jLi);
            $('<span>').text(item.text).appendTo(jLi);
        }
    }
    // 多选确认按钮
    var jMB = $('<div class="fld-it-check">').appendTo(jDiv);
    $('<span class="fld-it-check-ok"><b></b></span>').appendTo(jMB)
        .find("b").text(opt.btnMultiOkText);
    $('<span class="fld-it-check-cancel"><b></b></span>').appendTo(jMB)
        .find("b").text(opt.btnMultiCancelText);

    // 绘制可多选
    if(fld.multi){
        $('<span class="fld-multi"><b></b></span>').appendTo(jFld)
            .find("b").text(opt.btnMultiText || "Multi");
    }

    // 绘制更多
    $('<span class="fld-more"><b><em></em><i></i></b></span>').appendTo(jFld)
            .find("b").attr({
                "msg-more" : opt.btnFldMoreText || "More",
                "msg-less" : opt.btnFldLessText || "Less",
            }).find("em").text(opt.btnFldMoreText || "More");

    // 加入 DOM
    jFld.appendTo(jList);
}
//...........................................................
function __redraw(jq, opt) {
    // 空数据
    if(!_.isArray(opt.fields) || opt.fields.length == 0) {
        $('<div class="empty">')
            .html(opt.emptyHtml || "No Setting")
                .appendTo(jq);
        return jq;
    }

    // 标识
    jq.attr({
        "for-ide" : opt.forIDE ? "yes" : null,
        "for-rt"  : opt.forIDE ? null  : "yes",
        "more-items-mode" : opt.moreItemsMode || "auto",
    });

    // 准备折叠项目列表
    var folders = [];
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 绘制显示项目
    var jList = $('<div class="hmcf-list">').appendTo(jq);
    for(var i=0; i<opt.fields.length; i++) {
        var fld = opt.fields[i];
        // 折叠项目
        if(fld.hide) {
            folders.push(fld);
        }
        // 绘制项目
        else {
            __draw_fld(opt, fld, jList);
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 绘制折叠项目
    if(folders.length > 0) {
        var jFolder = $('<div class="hmcf-folder">').appendTo(jq);
        for(var i=0; i<folders.length; i++) {
            __draw_fld(opt, folders[i], jFolder);
        }
        var jExts = $('<div class="hmcf-exts">').appendTo(jq);
        $('<b>').attr({
            "msg-show" : opt.btnExtTextShow || "More",
            "msg-hide" : opt.btnExtTextHide || "Hide",
        }).text(opt.btnExtTextShow).appendTo(jExts);      
    }
}
//...........................................................
function getValue(jLi) {
    return jLi.attr("it-value");
}
//...........................................................
// 命令模式
var CMD = {
    // 启用一个或者多个项目
    selectItem : function(ele, reloadDynamic) {
        var jFld = $(ele).closest(".hmcf-fld");
        var jUl  = jFld.find(".fld-items ul");
        var jLi  = ele ? $(ele).closest('li[it-type]') : null;
        // 仅仅是取消全部选择
        if(!jLi || jLi.length == 0) {
            jUl.find("li[it-type]").removeAttr("it-checked");
        }
        // 多选模式
        if(jUl.closest('.hmcf-fld[enable-multi]').length > 0) {
            $z.toggleAttr(jLi, "multi-checked", "yes");
        }
        // 普通模式
        else {
            jUl.find("li[it-type]").removeAttr("it-checked");
            jLi.attr("it-checked", "yes");
            if(reloadDynamic)
                HmRT.invokeDynamicReload(this);
        }
    },
    // 切换折叠项的显示
    toggleFolder : function() {
        $z.toggleAttr(this, "folder-show", "yes");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(this.attr("folder-show") 
                    ? jB.attr("msg-hide")
                    : jB.attr("msg-show"));
        return this;
    },
    // 启用多选 
    enableMulti : function(ele) {
        var jFld = $(ele).closest(".hmcf-fld");
        // 显示更多选项
        CMD.moreItems.call(this, jFld);

        // 启用多选样式
        jFld.attr("enable-multi", "yes")
            .find("li[it-type]").each(function(){
                var jLi = $(this);
                var cc  = jLi.attr("it-checked") ? "yes" : null;
                jLi.attr({
                    "multi-checked"  : cc,
                    "org-it-checked" : cc,
                });
                jLi.removeAttr("it-checked");
            });
    },
    // 取消多选
    disableMulti : function(ele) {
        var jFld = $(ele).closest(".hmcf-fld");
        
        // 恢复多选样式
        jFld.find("li[it-type]").each(function(){
            var jLi = $(this);
            jLi.attr("it-checked", jLi.attr("org-it-checked") ? "yes" : null);
            jLi.removeAttr("multi-checked");
            jLi.removeAttr("org-it-checked");
        })
        jFld.removeAttr("enable-multi");

        // 显示更少选项
        CMD.lessItems.call(this, jFld);
    },
    // 确认多选 
    applyMulti : function(ele, reloadDynamic) {
        var jFld = $(ele).closest(".hmcf-fld");

        // 恢复多选样式
        jFld.find("li[it-type]").each(function(){
            var jLi = $(this);
            jLi.attr("it-checked", jLi.attr("multi-checked") ? "yes" : null);
            jLi.removeAttr("multi-checked");
            jLi.removeAttr("org-it-checked");
        })
        jFld.removeAttr("enable-multi");

        // 显示更少选项
        CMD.lessItems.call(this, jFld);

        if(reloadDynamic)
            HmRT.invokeDynamicReload(this);
    },
    // 重新计算选项们的行号
    recountItemsRowNb : function(ele) {
        var jFld = $(ele).closest(".hmcf-fld");

        // 那么自动隐藏时，要保留几行呢？ 先得到值，后面会用到
        var lessRowNb = jFld.attr("less-row-nb") * 1;

        // 得到当前项的 Y 坐标，-1 表示未初始化
        var offTop = -1;
        var row_nb = 0;   // 这个是行号

        // 计算每个项目的行号
        jFld.find("li[it-type]").each(function(){
            var jLi = $(this);
            var y   = jLi.offset().top;
            if(y > offTop) {
                offTop = y;
                row_nb ++;
            }
            jLi.attr({
                "row-nb" : row_nb,
                "off-y"  : y,
            });
        });

        // 记录最大的行号
        jFld.attr({
            "max-row-nb"   : row_nb,
            "more-useless" : (row_nb <= lessRowNb ? "yes" : null),
        });
    },
    // 显示更少选项
    lessItems : function(ele){
        var jFld = $(ele).closest(".hmcf-fld");
        var mode = this.attr("more-items-mode");

        // 那么自动隐藏时，要保留几行呢？ 先得到值，后面会用到
        var lessRowNb = jFld.attr("less-row-nb") * 1;

        // 一定是要显示出来的
        if("never" == mode) {
            jFld.find("lit[it-type]").removeAttr("auto-hide");
        }
        // 自动判断（实际上 always 选项只是控制"更多"按钮，行为和这个一致
        else {
            // 计算每个项目，行号超过的，隐藏
            jFld.find("li[it-type]").each(function(){
                var jLi = $(this);
                var row_nb = jLi.attr("row-nb") * 1;
                // 选中的项目，无论如何也要显示的
                if(jLi.attr("it-checked")) {
                    jLi.removeAttr("auto-hide");
                }
                // 剩下的按照行号判断
                else {
                    jLi.attr("auto-hide", row_nb <= lessRowNb ? null : "yes");
                }
            });
        }

        // 更新文本
        var jB = jFld.find(".fld-more b");
        jB.attr("mode", "more").find("em").text(jB.attr("msg-more"));
    },
    // 显示更多选项
    moreItems : function(ele){
        var jFld = $(ele).closest(".hmcf-fld");
        var mode = this.attr("more-items-mode");
        // 确保每个选项都是显示的
        jFld.find("li[it-type]").removeAttr("auto-hide");
        // 之后重新计算一遍行号
        CMD.recountItemsRowNb.call(this, jFld);
        // 更新文本
        var jB = jFld.find(".fld-more b");
        jB.attr("mode", "less").find("em").text(jB.attr("msg-less"));
    },
    // 切换选项的 更多/更少 显示
    toggleItems : function(ele) {
        var jFld = $(ele).closest(".hmcf-fld");
        var jB   = jFld.find(".fld-more b");
        // 显示更多
        if(jB.attr("mode") == "more") {
            CMD.moreItems.call(this, jFld);
        }
        // 显示更少
        else {
            // 如果开启了多选模式，直接取消多选
            if(jFld.attr("enable-multi")) {
                CMD.disableMulti.call(this, jFld);
            }
            // 收起选项 
            else {
                CMD.lessItems.call(this, jFld);
            }
        }
    },
    // 对所有的字段，显示更多选项
    moreAllItem : function(recount){
        var jq = this;
        jq.find(".hmcf-fld").each(function(){
            if(recount)
                CMD.recountItemsRowNb.call(jq, this);
            CMD.moreItems.call(jq, this);
        });
    },
    // 对所有的字段，显示更少选项
    lessAllItem : function(recount){
        var jq = this;
        jq.find(".hmcf-fld").each(function(){
            if(recount)
                CMD.recountItemsRowNb.call(jq, this);
            CMD.lessItems.call(jq, this);
        });
    },
    // 显示折叠项
    showFolder : function() {
        this.attr("folder-show", "yes");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(jB.attr("msg-hide"));
        return this;
    },
    // 隐藏折叠项
    hideFolder : function() {
        this.removeAttr("folder-show");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(jB.attr("msg-show"));
        return this;
    },
    value : function(flt) {
        var jFlds = this.find(".hmcf-fld");
        // 获取
        if(_.isUndefined(flt)){
            flt = {};
            jFlds.each(function(){
                var jFld  = $(this);
                var vList = [];
                jFld.find("li[it-type][it-checked]").each(function(){
                    var vi  = getValue($(this));
                    vList.push(vi);
                });
                var val = vList.join("||");
                if(val)
                    flt[jFld.attr("key")] = val;
            });
            //console.log("getValue:", flt);
            return flt;
        }
        // 设置某个高亮段
        //console.log("setValue:", flt);
        jFlds.each(function(){
            var jFld = $(this);
            var jUl  = jFld.find("ul");
            jUl.children("li[it-type]").removeAttr("it-checked");
            var key = jFld.attr("key");
            var val = flt[key];
            if(!val) 
                return;
            var vList = val.split(/[ ]*[|][|][ ]*/g);
            jUl.find("li[it-type]").each(function(){
                var jLi = $(this);
                var vi  = getValue(jLi);
                if(vList.indexOf(vi) >= 0)
                    jLi.attr("it-checked", "yes");
            });
        });
        // 返回
        return this;
    }
}
//...........................................................
$.fn.extend({ "hmc_filter" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        var args = Array.from(arguments);
        return CMD[opt].apply(this, args.slice(1));
    }

    // 得到自己所在控件
    var jq = this.empty();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    __redraw(jq, opt);

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        // 非 IDE 环境，显示更少选项
        CMD.lessAllItem.call(jq, true);
        // 展开收起折叠项目
        jq.on("click", ".hmcf-exts b", function(){
            CMD.toggleFolder.call(jq);
        });
        // 选择选项
        jq.on("click", "li[it-type]", function(){
            CMD.selectItem.call(jq, $(this), true);
        });
        // 清除全部选项
        jq.on("click", ".fld-info em", function(){
            CMD.selectItem.call(jq, this, true);
        });
        // 多选开关: 启用
        jq.on("click", ".fld-multi b", function(){
            CMD.enableMulti.call(jq, this);
        });
        // 多选开关: 关闭
        jq.on("click", ".fld-it-check-cancel", function(){
            CMD.disableMulti.call(jq, this);
        });
        // 多选开关: 确认
        jq.on("click", ".fld-it-check-ok", function(){
            CMD.applyMulti.call(jq, this, true);
        });
        // 切换选项的更多/更少的开关
        jq.on("click", ".fld-more b", function(){
            CMD.toggleItems.call(jq, this);
        });
    }
    // IDE 环境，显示更多选项
    else {
        CMD.moreAllItem.call(jq, true);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

