(function($, $z){
// 判断当前运行环境
var IS_RUNTIME = $('html[hmaker-runtime]').length > 0;
//..........................................................
var media_src = function(opt, obj, mo, download) {
    // 使用媒体附件的缩略图
    if(_.isString(obj)) {
        return opt.API + "/thumb?" + obj;
    }
    // 媒体附件
    else if(mo) {
        return opt.API
            + "/thing/media?pid=" + obj.th_set
            + "&id="  + obj.id
            + "&fnm=" + mo.nm
            + (download ? "&d=true" : "");
    }
    // 采用对象缩略图
    return opt.API + "/thumb?" + obj.thumb;
};
//..........................................................
var media_css_url = function(opt, obj, mo) {
    return 'url("' + media_src(opt, obj, mo) + '")';
};
//..........................................................
// 根据配置项，对播放列表进行排序
var sort_play_list = function(opt, mediaList) {
    // 准备排序函数
    var sort = opt.sortOrder == "asc" ? 1 : -1;
    var sorting = function(m1, m2) {
        if(m1.nm > m2.nm) 
            return sort;
        
        if(m1.nm < m2.nm)
            return sort * -1;
        
        return 0;
    };

    // 准备列表
    var video = [];
    var pic   = [];
    var list  = []; 
    var isGroup = /^(video|pic|v\/p|p\/v)$/.test(opt.playMedia);

    // 循环处理媒体
    if(_.isArray(mediaList)) {
        for(var i=0; i<mediaList.length; i++) {
            var media = mediaList[i];
            // 分组
            if(isGroup) {
                // 图片
                if(/^image\//.test(media.mime)){
                    pic.push(media);
                }
                // 视频
                else if(/^video\//.test(media.mime)){
                    video.push(media);
                }
            }
            // 不分组
            else {
                list.push(media);
            }
        }
    }

    // 分别排序
    video.sort(sorting);
    pic.sort(sorting);
    list.sort(sorting);

    // 返回结果: 仅视频
    if("video" == opt.playMedia)
        return video;

    // 返回结果: 仅图片
    if("pic" == opt.playMedia)
        return pic;

    // 返回结果：视频在前
    if("v/p" == opt.playMedia)
        return video.concat(pic);

    // 返回结果：图片在前
    if("p/v" == opt.playMedia)
        return pic.concat(video);

    // 返回结果：不分组
    return list;
};
//..........................................................
var play_media = function(jData, opt, obj, mo) {
    // 得到关键元素
    var jPlist = jData.find('.tm-play-list');
    var jDown  = jData.find('.tm-abar a[a="download"]');
    var jPviewMain = jData.find('.tmp-view-main');

    // 清除右侧的选择和下载链接
    jPviewMain.empty();
    jPlist.find('li[current]').removeAttr("current");
    jDown.removeAttr("href");

    // 清除旧的循环
    var oldH = jData.data("loop-handle");
    if(oldH) {
        window.clearInterval(oldH);
    }

    // 播放预览图
    if(!mo) {
        __play_as_picture(opt, obj);
        return;
    }
    //console.log("play", mo)

    // 高亮右侧
    jPlist.find('li[mindex="'+mo.__index+'"]').attr({
        "current" : "yes"
    });

    // 修改下载链接
    jDown.attr("href", media_src(opt, obj, mo, true));

    // 图像
    if(/^image\//.test(mo.mime)) {
        __play_as_picture(jData, opt, obj, mo);
    }
    // 视频
    else if(/^video\//.test(mo.mime)) {
        __play_as_video(jData, opt, obj, mo);
    }
    // 其他就无视吧
    else {
        console.warn("th_show_media:: invalid media", mi);
    }
};
//..........................................................
// 播放下一个视频
var __get_next_media_in_loop = function(jData, opt){
    var jPlist = jData.find('.tm-play-list');
    // 循环一遍
    if("once" == opt.playLoop) {
        return jPlist.find('li[current]').next().data("MO");
    }
    // 永远循环
    else if("forever" == opt.playLoop) {
        var jLi = jPlist.find('li[current]').next();
        if(jLi.length == 0)
            jLi = jPlist.find('li:first-child');
        return jLi.data("MO");
    }
};
//..........................................................
// 显示图片播放
var __play_as_picture = function(jData, opt, obj, mo){
    $('<div class="tmp-view-pic">').css({
        "background-image" : media_css_url(opt, obj, mo)
    }).appendTo(jData.find('.tmp-view-main'));

    //.........................................
    // 自动循环
    //.........................................
    if(IS_RUNTIME) {
        var inteval = opt.playInterval || 12;
        var nextMo  = __get_next_media_in_loop(jData, opt);
        if(nextMo) {
            // 设置新的循环
            var H = window.setInterval(function(){
                play_media(jData, opt, obj, nextMo);
            }, inteval * 1000);
            // 记录柄
            jData.data("loop-handle", H);
        }
    }
};
//..........................................................
// 显示视频播放
var __play_as_video = function(jData, opt, obj, mo){
    var jVcon  = $('<div class="tmp-view-video">')
                    .appendTo(jData.find('.tmp-view-main'));
    var jVideo = $('<video width="100%" controls><source></source></video>')
                    .appendTo(jVcon);
    // 标识视频自动播放
    if(opt.autoPlayVideo) {
        jVideo.prop("autoplay", true);
    }
    // 设置视频源
    jVideo.find("source").prop({
        "src" : media_src(opt, obj, mo)
    });

    //.........................................
    // 自动循环
    //.........................................
    if(IS_RUNTIME) {
        var nextMo  = __get_next_media_in_loop(jData, opt);
        if(nextMo) {
            jVideo.one("ended", function(e){
                play_media(jData, opt, obj, nextMo);
            });
        }
    }
};
//..........................................................
var output_dom = function(jData, obj, opt) {
    jData.empty();

    // 准备 DOM 结构
    var html = '<header>';
    html += '  <div class="tm-tags"><ul></ul></div>';
    html += '  <div class="tm-title"></div>';
    html += '</header>';
    html += '<section class="tm-brief"></section>';
    html += '<section class="tm-play">';
    html += '  <div class="tm-play-view">';
    html += '     <div class="tmp-view-main"></div>';
    html += '     <div class="tmp-view-exitfullscreen">';
    html += '         <i class="zmdi zmdi-fullscreen-exit"></i>';
    html += '     </div>';
    html += '  </div>';
    html += '  <div class="tm-play-list"></div>';
    html += '  <div class="tm-play-qrcode"><div>';
    html += '     <img><div class="tmp-qrtext"></div>';
    html += '  </div></div>';
    html += '</section>';
    html += '<section class="tm-abar"></section>';
    html += '<section class="tm-content"></section>';
    jData.html(html);
    //-------------------------------------------
    // 得到关键元素
    var jTags    = jData.find(".tm-tags");
    var jTitle   = jData.find(".tm-title");
    var jBrief   = jData.find(".tm-brief");
    var jPview   = jData.find(".tm-play-view");
    var jPlist   = jData.find(".tm-play-list");
    var jPqrcode = jData.find(".tm-play-qrcode");
    var jABar    = jData.find(".tm-abar");
    var jContent = jData.find(".tm-content");
    var jUl, jSpan;

    //-------------------------------------------
    // 头部区域
    // 标题
    $('<span>').text(obj.th_nm || obj.nm).appendTo(jTitle);

    // 标签
    if(_.isArray(obj.lbls) && obj.lbls.length > 0) {
        jUl = jTags.find("ul");
        for(var i=0; i<obj.lbls.length; i++) {
            // 分析标签
            var lb = obj.lbls[i];
            var m = /^([^#]+)(#[0-9a-fA-F]{3,6})$/.exec(lb);
            // 生成元素
            var jLi = $('<li>').appendTo(jUl);
            // 带颜色的标签
            if(m) {

                jLi.text(m[1]).css({
                    "background" : m[2] || "",
                });
            }
            // 普通标签
            else {
                jLi.text(lb);
            }
        }
    } else {
        jTags.remove();
    }
    //-------------------------------------------
    // 摘要
    if(obj.brief) {
        jBrief.text(obj.brief);
    }
    // 摘要没必要存在
    else {
        jBrief.remove();
    }
    //-------------------------------------------
    // 播放区
    // 播放列表
    var plist  = sort_play_list(opt, obj.th_media_list);
    //console.log(plist)

    // 只有一个的话，就播放这个，并不绘制选集
    if(plist.length == 1) {
        jPlist.remove();
    }
    // 多个才绘制选集
    else if(plist.length>1) {
        // 循环绘制
        jUl = $('<ul>').appendTo(jPlist);
        for(var i=0; i<plist.length; i++) {
            var mo = plist[i];

            // 记录一下下标
            mo.__index = i;
            // 创建
            var jLi = $('<li>').attr({
                "mindex" : mo.__index
            }).appendTo(jUl).data("MO", mo);
            // 指示标
            $('<div class="pli-index">')
                .html('<span>'+(i+1)+'</span><i class="zmdi zmdi-play"></i>')
                    .appendTo(jLi);
            // 缩略图
            var jPiThumb = $('<div class="pli-thumb">').css({
                "background-image" : media_css_url(opt, mo)
            }).appendTo(jLi);
            // 时长
            if(mo.duration > 0) {
                var du = $z.parseTimeInfo(Math.ceil(mo.duration));
                $('<span key="duration">')
                    .text(du.toString("min"))
                        .appendTo(jPiThumb);
            }
            // 图标
            var mtp  = /^image\//.test(mo.mime) ? "image" : "video";
            var icon = "image" == mtp
                        ? '<i class="zmdi zmdi-image"></i>'
                        : '<i class="zmdi zmdi-videocam"></i>';
            $('<span key="type">').html(icon).appendTo(jPiThumb);
            // 标题部分
            var jPiTitle = $('<div class="pli-title">').appendTo(jLi);
            $('<span class="pli-tt-text">').text(mo.nm).appendTo(jPiTitle);
            // 尺寸
            if(mo.width > 0 && mo.height > 0) {
                $('<span class="pli-tt-size">')
                    .text(mo.width + "x" + mo.height)
                        .appendTo(jPiTitle);
            }
        }
    }
    // 移除选集，绘制只绘制自己的缩略图，用 -1 代表
    else {
        jPlist.remove();
    }

    // 显示播放的 qrcode 以便手机查看
    var pageURI = encodeURI(window.location.href);
    jPqrcode.find("img").prop({
        "src": "/gu/qrcode/?ts=1517074545766&m=1&s=256&d="+pageURI
    });
    jPqrcode.find(".tmp-qrtext").text("请用手机扫码二维码快速打开网页");

    
    //-------------------------------------------
    // 底部动作条
    // 分享
    // if(opt.shareText) {
    //     html = '<ul class="tm-a-share">';
    //     html += '<li><span>' + opt.shareText + '</span></li>';
    //     html += '<li><a to="weibo"><i class="fa fa-weibo"></i></a></li>';
    //     html += '<li><a to="weixin"><i class="fa fa-weixin"></i></a></li>';
    //     html += '<li><a to="qq"><i class="fa fa-qq"></i></a></li>';
    //     html += '</ul>';
    //     jUl = $(html).appendTo(jABar);
    // }
    var jAb;
    // 全屏
    if("hide" != opt.fullscreen) {
        jAb = $('<a a="fullscreen">')
                    .html('<i class="zmdi zmdi-fullscreen"></i>')
                        .appendTo(jABar);
        if(opt.fullscreen)
            $('<span>').text(opt.fullscreen).appendTo(jAb);
    }
    // 下载
    if("hide" != opt.download) {
        jAb = $('<a a="download" target="_blank">')
                    .html('<i class="zmdi zmdi-download"></i>')
                        .appendTo(jABar);
        if(opt.download)
            $('<span>').text(opt.download).appendTo(jAb);
    }
    // 网址二维码
    if("hide" != opt.qrcode) {
        jAb = $('<a a="qrcode">')
                    .html('<i class="zmdi zmdi-smartphone-iphone"></i>')
                        .appendTo(jABar);
        if(opt.qrcode)
            $('<span>').text(opt.qrcode).appendTo(jAb);
    }
    // 中间的分隔符号
    $('<span class="abar-space">').appendTo(jABar);
    // 选集按钮
    //console.log(plist)
    if("hide" != opt.plist && plist.length>1) {
        jAb = $('<a a="plist">')
                    .html('<i class="zmdi zmdi-format-list-bulleted"></i>')
                        .appendTo(jABar);
        if(opt.plist)
            $('<span>').text(opt.plist).appendTo(jAb);
    }

    //-------------------------------------------
    // 内容区
    if(obj.content){
        $('<article class="md-content">')
            .html($z.markdownToHtml(obj.content, {
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
            })).appendTo(jContent);
    }
    // 内容区没必要存在
    else {
        jContent.remove();
    }

    //-------------------------------------------
    // 返回第一个要显示的媒体
    return plist.length > 0 ? plist[0] : null;
};
//..........................................................
$.fn.extend({ "_std_th_show_image" : function(obj, opt){
    // console.log("数据：", obj);
    // console.log("配置：", opt);

    // 准备关键变量
    var jData = this.empty();

    //.........................................
    // 输出 DOM
    var currentMedia = output_dom(jData, obj, opt)
    
    //.........................................
    // 监控事件
    //.........................................
    // 切换选集列表显示模式
    jData.on("click", '.tm-abar a[a="plist"]', function(){
        $z.toggleAttr(jData, "show-plist", "yes");
    });
    // 自动隐藏选集
    jData.on("click", '.tm-play[click-hide-plist]', function(){
        jData.removeAttr("show-plist");
    });
    // 自动隐藏二维码
    jData.on("click", '.tm-play-qrcode', function(){
        jData.removeAttr("show-qrcode");
    });
    // 切换选集
    jData.on("click", '.tm-play-list li', function(){
        var mo = $(this).data("MO");
        play_media(jData, opt, obj, mo);
    });
    // 二维码
    jData.on("click", '.tm-abar a[a="qrcode"]', function(){
        $z.toggleAttr(jData, "show-qrcode", "yes");
    });
    // 全屏幕
    jData.on("click", '.tm-abar a[a="fullscreen"]', function(){
        jData.find(".tm-play").attr("fullscreen", "yes");
    });
    // 退出全屏
    jData.on("click", '.tmp-view-exitfullscreen', function(){
        jData.find(".tm-play").removeAttr("fullscreen");
    });

    //-------------------------------------------
    // 最后: 播放内容
    //var currentMedia = jData.find('.tm-play-list li').eq(0).data("MO");
    if(currentMedia)
        play_media(jData, opt, obj, currentMedia);

    // 返回自身以便链式赋值
    return this;
}});
//..........................................................
})(window.jQuery, window.NutzUtil);