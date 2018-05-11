(function($, $z){
//..........................................................
var IS_RUNTIME = $('html[hmaker-runtime]').size() > 0;
//..........................................................
var media_pic_src = function(opt, th, mediaIndex) {
    // 缩略图
    if(mediaIndex < 0) {
        return opt.API + "/thumb?" + th.thumb;
    }
    // 返回某个媒体
    return opt.API
            + "/thing/media?pid=" + th.th_set
            + "&id="  + th.id
            + "&fnm=" + th.th_media_list[mediaIndex].nm;
};
//..........................................................
var media_pic_url = function(opt, th, mediaIndex) {
    return 'url("' + media_pic_src(opt, th, mediaIndex) + '")';
};
//..........................................................
var thumb_url = function(opt, obj) {
    var thumb = _.isString(obj) ? obj : obj.thumb;
    return 'url("' + opt.API + "/thumb?" + thumb + '")';
};
//..........................................................
var output_dom = function(jData, obj, opt) {
    jData.empty();
    //console.log(obj)

    // 准备 DOM 结构
    var html = '<div class="op-info">';
    html += '  <div class="op-header">';
    html += '    <div class="op-tags"><ul></ul></div>';
    html += '    <div class="op-title"></div>';
    html += '    <div class="op-slogan"></div>';
    html += '  </div>';
    html += '  <div class="op-brief"></div>';
    html += '</div>';
    html += '<div class="op-main">';
    html += '  <div class="op-preview">';
    html += '    <div class="op-image"></div>';
    html += '    <div class="op-plist"><ul></ul></div>';
    html += '  </div>';   
    html += '<div class="op-detail"></div>';
    jData.html(html);

    // 得到关键元素
    var jImage  = jData.find(".op-image");
    var jPList  = jData.find(".op-plist");
    var jTitle  = jData.find(".op-title");
    var jTags   = jData.find(".op-tags");
    var jSlogan = jData.find(".op-slogan");
    var jBrief  = jData.find(".op-brief");
    var jDetail = jData.find(".op-detail");
    var jMain   = jData.find(".op-main");
    var jUl, jSpan;

    // 显示空图标
    if(!_.isArray(obj.th_media_list) || obj.th_media_list.length == 0) {
        // 采用缩略图
        if(obj.thumb) {
            jImage.attr({
                "photo" : "yes"
            }).empty();
            $('<img>').attr({
                "src" : media_pic_src(opt, obj, -1),
            }).appendTo(jImage);
        }
        // 空图标
        else {
            jImage.attr({
                "photo" : "none"
            }).empty();
            $('<span><i class="zmdi zmdi-image"></i></span>')
                .appendTo(jImage);
        }
        jPList.remove();
    }
    // 显示详细的图片
    else {
        // 缩略图
        jImage.attr({
            "photo" : "yes"
        }).empty();
        var mi = obj.th_media_list[0];
        // 视频
        if(/^video\//.test(mi.mime)) {
            var jV = $('<video controls>').attr({
                "src" : media_pic_src(opt, obj, 0),
            }).appendTo(jImage);
            $z.wrapVideoSimplePlayCtrl(jV, {
                watchClick : IS_RUNTIME
            });
        }
        // 图片
        else {
            $('<img>').attr({
                "src" : media_pic_src(opt, obj, 0),
            }).appendTo(jImage);
        }

        // 可选图片列表
        if(obj.th_media_list.length>1) {
            jUl = jPList.children("ul");
            for(var i=0; i<obj.th_media_list.length; i++) {
                mi = obj.th_media_list[i];
                var jLi = $('<li>').attr({
                        "current" : 0 == i ? "yes" : null,
                        "li-src"  : media_pic_src(opt, obj, i),
                        "li-mime" : mi.mime,
                    }).appendTo(jUl);
                // 视频
                if(/^video\//.test(mi.mime)) {
                    $('<span>').css({
                        "background-image" : thumb_url(opt, mi),
                    }).appendTo(jLi);
                }
                // 图片
                else {
                    $('<span>').css({
                        "background-image" : media_pic_url(opt, obj, i),
                    }).appendTo(jLi);
                }
            }
        }
        // 只有一个图片就别选了
        else {
            jPList.remove();
        }
    }

    //-------------------------------------------
    // 输出简介区域
    // 标题
    $('<span>').text(obj.th_nm).appendTo(jTitle);
    // if(obj.th_nm != oDetail.th_nm)
    //     $('<em>').text(oDetail.th_nm).appendTo(jTitle);

    // 标签
    if(_.isArray(obj.lbls) && obj.lbls.length > 0) {
        jUl = jTags.find("ul");
        for(var i=0; i<obj.lbls.length; i++) {
            $('<li>').text(obj.lbls[i]).appendTo(jUl);
        }
    } else {
        jTags.remove();
    }
    

    // 口号
    if(obj.th_slogan) {
        $('<span>').text(obj.th_slogan).appendTo(jSlogan);
    } else {
        jSlogan.remove();
    }

    // 简介
    if(obj.brief) {
        $('<span>').text(obj.brief).appendTo(jBrief);
    } else {
        jBrief.remove();
    }

    //-------------------------------------------
    // 输出商品详情区域
    if(obj.content){
        jMain.attr("detail", "yes");
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
            .html($z.markdownToHtml(obj.content, {
                media : formatMedia,
                context : obj,
            })).appendTo(jDetail);
        
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

    } else {
        jMain.attr("detail", "no");
        jDetail.remove();
    }

    //-------------------------------------------
    // 搞定
};
//..........................................................
$.fn.extend({ "_std_th_show_operation" : function(obj, opt){
    var jData = this;

    //console.log(obj, opt)
    
    //.........................................
    // 输出 DOM
    output_dom(jData, obj, opt)

    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);