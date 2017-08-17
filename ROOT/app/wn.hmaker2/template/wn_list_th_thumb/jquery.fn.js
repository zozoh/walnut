(function($, $z){
//..........................................................
var html = '<a><section></section></a>';
//..........................................................
function w_info(jMe, selector, val){
    if(!_.isNumber(val) && !val)
        jMe.find(selector).remove();
    else
        jMe.find(selector).text(val);
}
//..........................................................
$.fn.extend({ "wn_list_th_thumb" : function(list, opt){
    var jList = this;
    
    // 首先得到字段映射
    var mapping = opt.displayText || {
        text : "=th_nm",
    };

    // 循环数据
    for(var i=0; i<list.length; i++) {
        var jMe = $(html).appendTo(jList);
        // 映射数据
        var obj = list[i];
        var o2  = $z.mappingObj(mapping, obj);
        console.log(o2)
        
        // 指定显示图片
        jMe.css({
            "background-image" : "url(" + opt.API + "/thumb?"+obj.thumb+")"
        });
        // 指定链接
        if(opt.href){
            jMe.attr("href", $z.tmpl("{{h}}?{{k}}={{v}}")({
                h : opt.href,
                k : opt.paramName  || "id",
                v : obj[opt.objKey || "id"],
            }));
        }
        w_info(jMe, "section", o2.text);
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);