/**
Navmenu 控件的运行时行为:

opt : {
    // 来自编辑器
    ...

    // 下面是服务器端转换增加的属性
    ...
}
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_video" : function(opt){
    // 得到自己所在控件
    var jq = this.closest(".hmc-video");
    var jVid = jq.find('video');

    // 包裹简单控制器
    $z.wrapVideoSimplePlayCtrl(jVid);

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

