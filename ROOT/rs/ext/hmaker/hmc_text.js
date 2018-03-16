/**
主要是用来运行时处理海报的支持
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_text" : function(opt){
    // 得到自己所在控件
    var jq = this;

    // 处理 Markdown 生成的内容
    var jAr = jq.find("article.md-content");
    if(jAr.size() > 0) {
    
        // 解析一下海报
        jPoster = jAr.find('pre[code-type="poster"]');
        $z.explainPoster(jPoster, {
            media : function(src) {
                return src;
            }
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

