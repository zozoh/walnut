(function($, $z){
//..........................................................
var html = '<div class="wn-li-article">';
html += '<h4><a></a></h4>';                // 标题
html += '<a class="wla-thumb"><img></a>';  // 预览区
html += '<ul class="wla-info">';
html += '<li key="brief"></li>';         // 摘要
html += '<li key="category"></li>';      // 分类
html += '<li key="date"></li>';          // 日期
html += '<li key="source"></li>';        // 来源
html += '</ul>';
html += '</div>';
//..........................................................
function w_info(jAr, obj, key){
    var jLi = jAr.find('li[key="'+key+'"]');
    var val = obj[key];
    // 显示之
    if((_.isNumber(val) || val) && jLi.size() > 0) {
        jLi.text(val);
    }
    // 移除之
    else {
        jLi.remove();
    }
}
//..........................................................
$.fn.extend({ "_std_th_list_article" : function(list, opt){
    var jList = this;
   
    // 首先得到字段映射
    var mapping = opt.displayText || {
        "title"    : "=th_nm",
        "brief"    : "=brief",
        "date"     : "=th_date",
        "source"   : "=th_source",
    };

    // 循环数据
    for(var i=0; i<list.length; i++) {
        var jAr = $(html).appendTo(jList);
        // 映射数据
        var obj = list[i];
        var o2  = $z.mappingObj(mapping, obj);

        // 指定标题
        jAr.find(">h4>a").text(o2.title);
        
        // 指定封面图片
        var jArThumb = jAr.find('>a.wla-thumb');
        var jArImg = jArThumb.find("img");
        if(obj.thumb) {
            jArImg.attr({
                "src" : opt.API + "/thumb?"+obj.thumb
            });
        }
        // 那就先显示一个灰块
        else {
            jArImg.remove();
            jArThumb.html('<i class="zmdi zmdi-image"></i>');
        }

        // 指定其他补充信息
        w_info(jAr, o2, "brief");
        w_info(jAr, o2, "category");
        w_info(jAr, o2, "date");
        w_info(jAr, o2, "source");

        // 指定链接（包括封面图片和标题）
        var href = HmRT.explainHref(opt.href, obj);
        if(href) {
            jAr.find("a").attr("href", $z.tmpl("{{h}}?{{k}}={{v}}")({
                h : href,
                k : opt.paramName  || "id",
                v : obj[opt.objKey || "id"],
            }));
        }
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);