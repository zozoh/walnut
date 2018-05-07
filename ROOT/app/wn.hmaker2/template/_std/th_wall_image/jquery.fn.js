(function($, $z){
//..........................................................
$.fn.extend({ "_std_th_wall_image" : function(list, opt){
    var jList = this.empty();
    var jW = $('<div class="wp-list">').appendTo(jList);

    // 得到要显示的字段列表
    var keys = opt.showKeys || [
        "th_city", "th_place", "th_address",
        "th_price", "th_phone", "th_email",
    ];
    if(!_.isArray(keys)) {
        keys = keys.split(/[，, \t]+/g);
    }

    // 循环数据
    for(var i=0; i<list.length; i++) {
        // 映射数据
        var obj = list[i];

        //console.log(obj)
        var jIt = $('<div class="wp-item">').appendTo(jW);
        var jA = $('<a>').appendTo(jIt);

        // 链接
        var href = HmRT.explainHref(opt.href, obj);
        if(href) {
            var taId = obj.id;
            var panm = opt.paramName || "id";
            if(taId) {
                jA.attr({
                    "href"   : href + "?" + panm + "=" + taId,
                    "target" : "_blank",
                });
            }
        }


        // 标签
        if(_.isArray(obj.lbls) && obj.lbls.length > 0) {
            var jLbls = $('<ul class="wpi-lbls">').appendTo(jA);
            for(var x=0; x<obj.lbls.length; x++) {
                $('<li>').text(obj.lbls[x]).appendTo(jLbls);
            }
        }

        // 缩略图
        if(obj.thumb) {
            var jThumb = $('<div class="wpi-thumb">').css({
                "background-image" : 'url("' + opt.API + "/thumb?" + obj.thumb + '")'
            }).appendTo(jA);
        }
        // 仅仅显示一个空的图标
        else {
            $('<div class="wpi-thumb" thumb-icon="yes">')
                .html('<i class="zmdi zmdi-image"></i>')
                    .appendTo(jA);
        }

        // 准备信息部分
        var jInfo = $('<div class="wpi-info">').appendTo(jA);

        // 标题
        $('<h4>').text(obj.title||obj.th_nm||obj.nm).appendTo(jInfo);

        // 口号
        if(obj.slogan) {
            $('<div class="wpi-slogan">').text(obj.slogan).appendTo(jInfo);
        }

        // 摘要
        if(obj.brief) {
            $('<div class="wpi-brief">').text(obj.slogan).appendTo(jInfo);
        }


        // 自定义字段
        if(keys && keys.length > 0) {
            var jMeta = $('<div class="wpi-meta">');
            var jUl   = $('<ul>').appendTo(jMeta);
            var isNoMeta = true;
            for(var x=0; x < keys.length; x++) {
                var key = keys[x];
                var val = obj[key];
                if(_.isNull(val) || _.isUndefined(val))
                    continue;
                isNoMeta = false;
                $('<li>').attr({
                    key : key
                }).text(val).appendTo(jUl);
            }
            if(!isNoMeta)
                jMeta.appendTo(jInfo);
        }


        // 指定链接
        if(obj.th_link) {
            $('<div class="wpi-link">')
                .append($('<a>').attr({
                            "href"   : obj.th_link,
                            "target" : "_blank"
                        }).html(opt.linkText 
                             || '<i class="zmdi zmdi-open-in-new"></i><span>Detail</span>'))
                    .appendTo(jInfo);
        }

    }
    
    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);