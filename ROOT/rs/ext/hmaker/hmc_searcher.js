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
function normalize_val_by_input(jInput, val) {
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
}
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
    // 开始绘制
    var jInput = $('<div class="kwd-input"><input></div>').appendTo(jq).find("input");

    jInput.attr({
        "placeholder" : opt.placeholder || null,
        "maxlength"   : opt.maxLen > 0 ? opt.maxLen : null,
        "trim-space"  : opt.trimSpace ? "yes" : null,
    }).val(opt.defaultValue || "");

    // 按钮文字
    var jBtnW  = $('<div class="kwd-btn"><b></b></div>').appendTo(jq);
    var jB = jBtnW.show().children("b");
    jB.html(opt.btnText || '<i class="fa fa-search"></i>');

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 监控事件
    if(!opt.forIDE) {
        // 修改输入框内容
        jq.on("change", ".kwd-input input", function(){
            HmRT.invokeDynamicReload($(this), true);
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

