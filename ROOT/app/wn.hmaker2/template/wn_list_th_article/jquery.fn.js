(function($, $z){
//..........................................................
var html = '<div class="wn-li-article">';
html += '<a class="wla-thumb"></a>';              // 预览区
html += '<ul class="wla-info">';
html += '<li class="wla-ih"><a></a></li>';  // 标题
html += '<li class="wla-is"></li>';         // 子标题
html += '<li class="wla-ii"><em></em><u></u><b></b></li>';  // 补充信息
html += '</ul>';
html += '</div>';
//..........................................................
function w_info(jMe, selector, val){
    if(!val)
        jMe.find(selector).remove();
    else
        jMe.find(selector).text(val);
}
//..........................................................
$.fn.extend({ "wn_list_th_article" : function(list, opt){
    var jList = this;
    
    // 首先得到字段映射
    var mapping = opt.displayText || {
        title : "=th_nm",
        brief : "=brief",
        linkText : "查看",
        info_em  : "A",
        info_u   : "B",
        info_b   : "C",
    };

    // 循环数据
    for(var i=0; i<list.length; i++) {
        var jMe = $(html).appendTo(jList);
        // 映射数据
        var obj = list[i];
        var o2  = $z.mappingObj(mapping, obj);
        
        // 指定显示图片
        jMe.find('.wla-thumb').css({
            "background-image" : "url(" + opt.API + "/thumb?"+obj.thumb+")"
        });
        // 指定链接
        if(opt.href){
            jMe.find("a").attr("href", $z.tmpl("{{h}}?{{k}}={{v}}")({
                h : opt.href,
                k : opt.paramName  || "id",
                v : obj[opt.objKey || "id"],
            }));
        }
        w_info(jMe, ".wla-ih a",  o2.title);
        w_info(jMe, ".wla-is",    o2.brief);
        w_info(jMe, ".wla-ii em", o2.info_em);
        w_info(jMe, ".wla-ii u",  o2.info_u);
        w_info(jMe, ".wla-ii b",  o2.info_b);
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);