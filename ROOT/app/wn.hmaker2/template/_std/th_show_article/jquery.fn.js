(function($, $z){
//..........................................................
var IS_RUNTIME = $('html[hmaker-runtime]').size() > 0;
//..........................................................
$.fn.extend({ "_std_th_show_article" : function(obj, opt){
    var jData = this;

    //console.log(obj, opt)
    
    // 首先得到字段映射
    var mapping = opt.displayText || {
        "title"   : "=th_nm",
        "brief"   : "=brief",
        "content" : "=content",
        "date"    : "=th_date",
        "source"  : "=th_source",
    };
    
    // 映射数据
    var o2 = $z.mappingObj(mapping, obj);
    
    // 渲染: 标题
    if(o2.title){
        $('<header class="md-title">').text(o2.title).appendTo(jData);
    }
    
    // 补充信息:
    var jInfo = $('<aside class="md-info">').appendTo(jData);
    if(o2.source)
        $('<span class="mdi-source">').text(o2.source).appendTo(jInfo);
    if(o2.date){
        if(_.isNumber(o2.date)){
            var d = $z.parseDate(o2.date);
            o2.date = d.format("yyyy-mm-dd");
        }
        $('<span class="mdi-pubat">').text(o2.date).appendTo(jInfo);
    }
    
    // 摘要
    if(o2.brief) {
        $('<aside class="md-brief">').text(o2.brief).appendTo(jData);
    }
    
    //-------------------------------------------
    // 正文
    // 解析媒体的回调
    var formatMedia = function(src){
        var obj = this;
        // 看看是否是媒体
        var m = /^(media|attachment)\/(.+)$/.exec(src);
        //console.log(m)
        if(m){
            return opt.API + "/thing/"+m[1]
                    + "?pid=" + obj.th_set
                    + "&id="  + obj.id
                    + "&fnm=" + m[2];
        }
        // 原样返回
        return src;
    };
    // 转换 markdown 内容
    var jAr = $('<article class="md-content">')
        .html($z.markdownToHtml(o2.content, {
            media : formatMedia,
            context : obj,
        })).appendTo(jData);
    
    // 标识标题
    jAr.find("h1,h2,h3,h4,h5,h6").addClass("md-header");

    // 解析一下海报
    jPoster = jAr.find('pre[code-type="poster"]');
    $z.explainPoster(jPoster, {
        media : formatMedia,
        context : obj,
    });

    // 处理一下视频
    $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
        watchClick : IS_RUNTIME
    });
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);