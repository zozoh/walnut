(function($, $z){
//..........................................................
$.fn.extend({ "wn_obj_th_article" : function(obj, opt){
    var jData = this;
    
    // 首先得到字段映射
    var mapping = opt.displayText || {
        title : "=th_nm",
        brief : "=brief",
        content : "=th_detail",
        author : "=th_ow",
        pubat  : "=lm",
        source : "=th_cate",
    };
    
    // 映射数据
    var o2  = $z.mappingObj(mapping, obj);
    
    // 渲染: 标题
    var jHeader = $('<header class="md-title">').appendTo(jData);
    jHeader.text(o2.title)
    
    // 补充信息:
    var jInfo = $('<aside class="md-info">').appendTo(jData);
    if(o2.pubat){
        if(_.isNumber(o2.pubat)){
            var d = $z.parseDate(o2.pubat);
            o2.pubat = d.format("yyyy-mm-dd");
        }
        $('<span class="mdi-pubat">').text(o2.pubat).appendTo(jInfo);
    }
    if(o2.author)
        $('<span class="mdi-author">').text(o2.author).appendTo(jInfo);
    if(o2.source)
        $('<span class="mdi-source">').text(o2.source).appendTo(jInfo);
    
    // 摘要
    var jBrief = $('<div class="md-brief">').appendTo(jData);
    jBrief.text(o2.brief);
    
    // 正文
    var jArticle = $('<article class="md-content">').appendTo(jData);
    jArticle.html($z.markdownToHtml(o2.content));
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);