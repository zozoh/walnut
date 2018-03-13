/**
Searcher 控件的运行时行为:

opt : {
    // 来自编辑器
    "placeholder"  : "请输入关键字",
    "defaultValue" : "搜索框默认值",
    "btnText"      : "立即搜索",
    "trimSpace"    : true,
    "maxLen"       : 23,

    // 下面由 IDE 指定
    forIDE : true,
    
}
*/
(function($, $z){
//...........................................................
var normalize_val_by_input = function(jInput, val) {
    var trimSpace = jInput.attr("trim-space");
    var maxLen = jInput.attr("maxlength") * 1;
    val = jInput.val();
    if(trimSpace){
        val = $.trim(val);
    }
    if(maxLen > 0 && val.length > maxLen){
        val = val.substring(0, maxLen);
    }
    return val;
};
//...........................................................
// 执行搜索
var do_search = function(opt, jq) {
    // 提交到某个页面
    if(opt.postAction) {
        var jInput = jq.find(".kwd-input input");

        var url = window.__ROOT_PATH;
        if(!url || /\/$/.test(url))
            url += opt.postAction.substring(1) + ".html";
        else
            url += opt.postAction + ".html";
        
        var target = opt.postTarget || "_blank";
        var key = opt.postParamName || "k";
        var val = jInput.val();
        if(opt.trimSpace) {
            val = $.trim(val);
        }

        console.log(target)
        $z.openUrl(url, target, "GET", $z.obj(key, val));
    }
    // 默认刷新与自己关联的动态数据控件
    else {
        HmRT.invokeDynamicReload(jq, true);
    }
};
//...........................................................
// 命令模式
var CMD = {
    value : function(val) {
        var jInput = this.find("input");
        // 获取
        if(_.isUndefined(val)){
            return normalize_val_by_input(jInput, jInput.val());
        }
        // 设置
        jInput.val(normalize_val_by_input(jInput, val));
        // 返回
        return this;
    }
}
//...........................................................
$.fn.extend({ "hmc_searcher" : function(opt){
    // 命令模式
    if(_.isString(opt)) {
        var args = Array.from(arguments);
        return CMD[opt].apply(this, args.slice(1));
    }
    
    // 得到自己所在控件
    var jq = this.empty();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 准备一下获取值
    var kwd = opt.defaultValue || "";
    var m = /^@<([^>]+)>$/.exec(kwd);
    if(m) {
        kwd = (window.__REQUEST || {})[m[1]] || "";
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 开始绘制
    var jInput = $('<div class="kwd-input"><input></div>')
                    .appendTo(jq)
                        .find("input");

    jInput.attr({
        "placeholder" : opt.placeholder || null,
        "maxlength"   : opt.maxLen > 0 ? opt.maxLen : null,
        "trim-space"  : opt.trimSpace ? "yes" : null,
        "spellcheck"  : false,
    }).val(kwd);

    // 按钮文字
    var jBtnW  = $('<div class="kwd-btn"><b></b></div>').appendTo(jq);
    var jB = jBtnW.show().children("b");
    jB.html(opt.btnText || '<i class="fa fa-search"></i>');

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        // 回车表示搜索
        jq.on("keydown", ".kwd-input input", function(e){
            if(13 == e.which) {
                do_search(opt, jq);
            }
        });
        // 点击搜索按钮
        jq.on("click", ".kwd-btn", function(){
            if($.trim(jInput.val())) {
                do_search(opt, jq);
            }
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

