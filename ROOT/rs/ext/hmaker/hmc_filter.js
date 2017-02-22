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
    "btnExtTextShow" : "展开",
    "btnExtTextHide" : "收起"
    "btnMultiText"   : "多选",

    // 下面是调用者自由增加的属性
    emptyHtml : '<i class="zmdi zmdi-alert-circle-o"></i> Empty',

    // 下面由 IDE 指定
    ignoreShowHideEvent : true,
}
*/
(function($, $z){
//...........................................................
// 绘制项目
function __draw_fld(opt, fld, jList) {
    var jDiv = $('<div class="hmcf-fld">').attr("key", fld.name);
    
    // 绘制字段标题
    var jFldInfo = $('<span class="fld-info">').appendTo(jDiv);
    $('<em>').text(fld.text).appendTo(jFldInfo);

    // 绘制选项
    var jUl = $("<ul>").appendTo(jDiv);
    if(_.isArray(fld.items) && fld.items.length > 0) {
        for(var i=0; i<fld.items.length; i++) {
            var item = fld.items[i];
            var jLi  = $('<li>').appendTo(jUl);
            jLi.attr({
                "it-type"  : item.type,
                "it-value" : item.value,
            });
            $('<span>').text(item.text).appendTo(jLi);
        }
    }

    // 绘制可多选
    if(fld.multi){
        $('<span class="fld-multi"><b></b></span>').appendTo(jDiv)
            .find("b").text(opt.btnMultiText || "Multi");
    }

    // 加入 DOM
    jDiv.appendTo(jList);
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
// 命令模式
var CMD = {
    // 切换折叠项的显示
    toggleFolder : function() {
        $z.toggleAttr(this, "folder-show", "yes");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(this.attr("folder-show") 
                    ? jB.attr("msg-show")
                    : jB.attr("msg-hide"));
    },
    // 显示折叠项
    showFolder : function() {
        this.attr("folder-show", "yes");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(jB.attr("msg-show"));
    },
    // 隐藏折叠项
    hideFolder : function() {
        this.removeAttr("folder-show");
        var jB = this.find(">.hmcf-exts>b");
        jB.text(jB.attr("msg-hide"));
    },
}
//...........................................................
$.fn.extend({ "hmc_filter" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        // 显示/隐藏折叠项目
        var args = Array.from(arguments);
        CMD[opt].apply(this, args.slice(1));
        // 返回自身以便链式赋值
        return this;
    }

    // 得到自己所在控件
    var jq = this.empty();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    __redraw(jq, opt);

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.ignoreShowHideEvent) {
        jq.on("click", ".hmcf-exts b", function(){
            jq.hmc_filter("toggleFolder");
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

