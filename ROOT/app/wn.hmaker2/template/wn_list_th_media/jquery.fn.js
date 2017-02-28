(function($, $z){
//..........................................................
var html = '<div class="wn-li-media">';
html += '<div class="wlm-thumb">';                // 预览区
html += '<em></em><u></u><b></b>';                 // 预览区信息部分，预留3个标签
html += '<a class="wlm-ta"><span></span></a>';    // 预览区链接部分
html += '</div>';
html += '<ul class="wlm-info">';
html += '<li class="wlm-ih"><a></a></li>';  // 标题
html += '<li class="wlm-is"></li>';         // 子标题
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
$.fn.extend({ "wn_lst_th_media" : function(list, opt){
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
        jMe.find('.wlm-thumb').css({
            "background-image" : "url(" + opt.API + "/gbox/thumb?"+obj.thumb+")"
        });
        // 指定链接
        if(opt.href){
            jMe.find("a").attr("href", $z.tmpl("{{h}}?{{k}}={{v}}")({
                h : opt.href,
                k : opt.paramName  || "id",
                v : obj[opt.objKey || "id"],
            }));
        }
        w_info(jMe, ".wlm-ih a", o2.title);
        w_info(jMe, ".wlm-is", o2.brief);
        w_info(jMe, ".wlm-thumb em", o2.info_em);
        w_info(jMe, ".wlm-thumb u", o2.info_u);
        w_info(jMe, ".wlm-thumb b", o2.info_b);
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);