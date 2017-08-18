(function($, $z){
//..........................................................
$.fn.extend({ "wn_obj_th_article" : function(obj, opt){
    var jData = this;

    //console.log(obj, opt)
    
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
    var o2 = $z.mappingObj(mapping, obj);
    
    // 渲染: 标题
    if(o2.title){
        $('<header class="md-title">').text(o2.title).appendTo(jData);
    }
    
    // 补充信息:
    var jInfo = $('<aside class="md-info">').appendTo(jData);
    if(o2.author)
        $('<span class="mdi-author">').text(o2.author).appendTo(jInfo);
    if(o2.source)
        $('<span class="mdi-source">').text(o2.source).appendTo(jInfo);
    if(o2.pubat){
        if(_.isNumber(o2.pubat)){
            var d = $z.parseDate(o2.pubat);
            o2.pubat = d.format("yyyy-mm-dd");
        }
        $('<span class="mdi-pubat">').text(o2.pubat).appendTo(jInfo);
    }
    
    // 摘要
    if(o2.brief) {
        $('<div class="md-brief">').text(o2.brief).appendTo(jData);
    }
    
    // 正文
    var jArticle = $('<article class="md-content">').appendTo(jData);
    if(o2.content)
        jArticle.html($z.markdownToHtml(o2.content, {
            media : function(src){
                // 看看是否是媒体
                var m = /^media\/(.+)$/.exec(src);
                if(m){
                    return opt.API + "/thing/media"
                            + "?pid=" + obj.th_set
                            + "&id="  + obj.id
                            + "&fnm=" + m[1];
                }
                // 原样返回
                return src;
            }
        }));
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);