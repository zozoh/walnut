/**
Searcher 控件的运行时行为:

opt : {
    // 来自编辑器
    "placeholder"  : "请输入关键字",
    "defaultValue" : "搜索框默认值",
    "btnText"      : "立即搜索",
    "trimSpace"    : true,
    "maxLen"       : 23,
    
}
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_searcher" : function(opt){
    // 得到自己所在控件
    var jq = this.empty();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    // 开始绘制
    var jInput = $('<div class="kwd-input"><input></div>').appendTo(jq).find("input");

    jInput.attr({
        "placeholder" : opt.placeholder || null,
        "maxlength"   : opt.maxLen > 0 ? opt.maxLen : null
    }).val(opt.defaultValue || "");

    // 按钮文字
    if(opt.btnText){
        var jBtnW  = $('<div class="kwd-btn"><b></b></div>').appendTo(jq);
        jBtnW.show().children("b").text(opt.btnText);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

