(function($, $z){
//..........................................................
var html = '<div class="wn-li-item">';
html += '<header></header>';        // 标题
html += '<section>';
html += '<a class="wla-thumb"><img></a>';
html += '</section>';
html += '</div>';
//..........................................................
function w_info(jIt, obj, key){
    // 得到值
    var val = obj[key];
    if(!val && obj._obj_cate)
        val = obj._obj_cate[key];

    // 显示之
    if(_.isNumber(val) || val) {
        $('<p>').attr("key", key).text(val)
            .appendTo(jIt.find(">section"));
    }
}
//..........................................................
$.fn.extend({ "_std_th_list_any" : function(list, opt){
    var jList = this;

    // 判断当前环境是否是 IDE
    var isIDE = this.closest('html[hmaker-ide]').length > 0;
    
    // 循环数据
    for(var i=0; i<list.length; i++) {
        var jIt = $(html).appendTo(jList);
        // 映射数据
        var obj = list[i];
        var oCa = obj._obj_cate;
        // console.log(obj)

        // 指定标题
        var jTitle = jIt.find(">header");
        var jTAn = $('<a>').appendTo(jTitle);
        if(oCa) {
            $('<em>').text(oCa.th_nm).appendTo(jTAn);
        }
        $('<span>').text(obj.title||obj.th_nm||obj.nm).appendTo(jTAn);
        if(obj.th_set_nm) {
            $('<u>').text(obj.th_set_nm).appendTo(jTitle);
        }
        
        // 指定封面图片
        var jItThumb = jIt.find('>section>a.wla-thumb');
        var jItImg = jItThumb.find("img");
        if(obj.thumb) {
            jItImg.attr({
                "src" : opt.API + "/thumb?"+obj.thumb
            });
        }
        // 不显示
        else {
            jItThumb.remove();
        }

        // 指定其他补充信息
        w_info(jIt, obj, "th_slogan");
        w_info(jIt, obj, "brief");
        w_info(jIt, obj, "th_price");

        // 补充自定义元数据
        if(opt.more) {
            // 首先要替换一下
            var more = opt.more;
            try{
                more = $z.tmpl(more)(obj);
            }catch(E){}

            // 执行 markdown 替换
            var moreHtml = $z.markdownToHtml(more);
            $('<div class="it-more">').html(moreHtml).appendTo(jIt);
        }

        //...............................
        // 寻找详情页面
        var href = HmRT.explainHref(opt.href, obj, isIDE);

        // 指定链接（包括封面图片和标题）
        if(href){
            var taId = obj.id;
            var panm = opt.paramName || "id";
            if(taId){
                jIt.find("a").attr({
                    "target" : "_blank",
                    "href"   : href + "?" + panm + "=" + taId,
                });
            }
        }
    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);