/*
给定选区是一个 thingset，它的用法大约是
$(context).thingset({
    // 需要调用的 httpapi，返回必须是 AJAX
    api : {
        query : "/thing/query"
    },
    // 基于这个进行条件筛选
    match : {
        "th" : "xxxx"
    },
    // 字段映射
    mapping : {
        "aaa" : "xxxx"
    },

});
对于选区，它假想的结构是
<div class="hm-com" id="com1">
    <section hidden="yes">
        <!--// 这里就是 Template 的innerHTML -->
    </section>
</div>
*/
(function($, $z){

//...........................................................
$.fn.extend({ "objlist" : function(opt){

    // 显示主
    $("<b>haha, Iam thingset JS agent! ^_*</b>").appendTo(this);

    console.log(opt)

    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

