(function($, $z){
//..........................................................
$.fn.extend({ "_std_th_wall_obj" : function(list, opt){
    var jList = this;
   
    // 首先得到布局
    var str = opt.layout;

    // 默认布局
    if(!str) {
        str = ".th_nm";
    }

    // 解析布局
    var layout = HmRT.parseLayout(str);

    // 循环数据
    for(var i=0; i<list.length; i++) {
        // 数据
        var obj = list[i];

        // DOM
        var jDiv = $('<div class="wn-li-obj">').appendTo(jList);

        // 指定链接（包括封面图片和标题）
        var href = HmRT.explainHref(opt.href, obj);
        // if(href) {
        //     jAr.find("a").attr("href", $z.tmpl("{{h}}?{{k}}={{v}}")({
        //         h : href,
        //         k : opt.paramName  || "id",
        //         v : obj[opt.objKey || "id"],
        //     }));
        // }

        // 渲染
        HmRT.renderLayout(opt, jDiv, layout, obj, href);

    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);