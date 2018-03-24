/**
主要是用来运行时处理图片上叠加视频
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_image" : function(opt){
    // 得到自己所在控件
    var jq = this;

    // 处理 Markdown 生成的内容
    var jAr = jq.find("article.md-content");
    if(jAr.size() > 0) {

        // 处理一下视频
        $z.wrapVideoSimplePlayCtrl(jAr.find('video'));
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

