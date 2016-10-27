(function($, $z){
//...........................................................
$.fn.extend({ "objshow" : function(opt){

    // 显示主
    $("<b>haha, Iam objshow JS agent! ^_*</b>").appendTo(this);

    console.log(opt)

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

