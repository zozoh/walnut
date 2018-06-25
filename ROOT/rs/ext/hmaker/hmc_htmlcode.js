/**
主要是用来运行时处理海报的支持
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_htmlcode" : function(opt){
    // 得到自己所在控件
    var jq = this;

    // 如果有 code，记录到 jQuery Data
    if(opt.code) {
        jq.data("@code", opt.code);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

