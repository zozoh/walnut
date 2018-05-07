(function($, $z){
//..........................................................
function parse_refers(refers) {
    var refList = [];
    if(refers) {
        var lines   = refers.split(/[,;\n]/);
        for(var i=0; i<lines.length; i++) {
            var line = $.trim(lines[i]);
            if(!line)
                continue;
            var pos = line.indexOf(':');
            if(pos>0) {
                var r_id = $.trim(line.substring(0,pos));
                var r_nm = $.trim(line.substring(pos+1));
                if(r_id && r_nm) {
                    refList.push({
                        id   : r_id,
                        text : r_nm,
                    });
                }
            }
            // 否则就是文本
            else {
                refList.push({text : line});
            }
        }
    }
    return refList;
}
//..........................................................
$.fn.extend({ "_std_th_wall_goods" : function(list, opt){
    var jList = this.empty();
    var jW = $('<div class="wg-list">').appendTo(jList);

    // 标识属性
    jList.attr("more-hover", opt.moreHover ? "yes" : null);

    // 判断当前环境是否是 IDE
    var isIDE = this.closest('html[hmaker-ide]').length > 0;

    // 首先得到字段映射
    var mapping = opt.displayText || {
        "title"    : "=th_nm",
        "labels"   : "=lbls",
        "thumb"    : "=thumb",
        "slogan"   : "=th_slogan",
        "price"    : "=th_price",
        "refers"   : "=th_refers",
    };

    // 得到链接信息
    var panm = opt.paramName || "id";
    
    // 循环数据
    for(var i=0; i<list.length; i++) {
        // 映射数据
        var obj = list[i];
        var o2  = $z.mappingObj(mapping, obj);
        //................................................
        //console.log(o2)
        var jIt = $('<div class="wg-item">').appendTo(jW);
        var jA = $('<a>').appendTo(jIt);
        //................................................
        // 链接
        var href = HmRT.explainHref(opt.href, obj, isIDE);
        if(href) {
            var taId = obj.id;
            if(taId) {
                jA.attr({
                    "href"   : href + "?" + panm + "=" + taId,
                    "target" : "_blank",
                });
            }
        }
        //................................................
        // 标签
        if(_.isArray(o2.labels) && o2.labels.length > 0) {
            var jLbls = $('<ul class="wgi-lbls">').appendTo(jA);
            for(var x=0; x<o2.labels.length; x++) {
                $('<li>').text(o2.labels[x]).appendTo(jLbls);
            }
        }

        // 缩略图
        if(o2.thumb) {
            var jThumb = $('<div class="wgi-thumb">').css({
                "background-image" : 'url("' + opt.API + "/thumb?" + o2.thumb + '")'
            }).appendTo(jA);
        }
        // 仅仅显示一个空的产品图标
        else {
            $('<div class="wgi-thumb-icon">')
                .html('<i class="fa fa-birthday-cake"></i>')
                    .appendTo(jA);
        }
        //................................................
        // 标题
        var jH = $('<h4>').appendTo(jA);
        var refList = parse_refers(o2.title);
        if(refList.length > 0) {
            for(var x=0; x<refList.length; x++) {
                if(x>0) {
                    $('<span>').text("/").appendTo(jH);
                }
                var ref = refList[x];
                var jAn = $('<a>').text(ref.text).appendTo(jH);
                if(href && ref.id) {
                    jAn.attr({
                        "href"   : href + "?" + panm + "=" + ref.id,
                        "target" : "_blank",
                    });
                }
            }
        }
        // 否则显示 ID
        else {
            jH.text(obj.th_nm || obj.id);
        }
        //................................................
        // 摘要
        if(o2.slogan)
            $('<blockquote>').text(o2.slogan).appendTo(jA);

        // 价格
        if(o2.price) {
            $('<div class="wgi-price">')
                .html('<em>' + o2.price + "</em><u>元</u>")
                    .appendTo(jA);
        }
        //................................................
        // 查看详情提示
        var jMore = $('<div class="wgi-more">')
            .html('<span>查看详情</span>')
                .appendTo(jA);
        //................................................
        // 更多参考
        if(o2.refers) {
            refList = parse_refers(o2.refers);
            // 显示
            if(refList.length > 0) {
                var jRefer = $('<ul class="wgi-refer">').appendTo(jMore);
                for(var x=0; x<refList.length; x++) {
                    var ref = refList[x];
                    var jLi = $('<li>').appendTo(jRefer);
                    var jAn = $('<a>').text(ref.text).appendTo(jLi);
                    if(href && ref.id) {
                        jAn.attr({
                            "href"   : href + "?" + panm + "=" + ref.id,
                            "target" : "_blank",
                        });
                    }
                }
            } // ~ if(refList.length > 0)
        }  // ~ if(o2.refers)

    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);