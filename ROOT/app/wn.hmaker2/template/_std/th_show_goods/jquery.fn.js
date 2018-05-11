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
var output_dom = function(jData, obj, opt) {
    jData.empty();
    opt = opt || {};

    // 首先得到字段映射
    var mapping = opt.displayText || {
        "model"  : "选择型号",
        "color"  : "选择颜色",
        "buy"    : "立即购买",
    };

    // 准备 DOM 结构
    var html = '<div class="go-intro">';
    html += '<div class="go-preview">';
    html += '  <div class="go-photo"></div>';
        html += '</div>';
    html += '<div class="go-info">';
    html += '  <div class="go-tags"><ul></ul></div>';
    html += '  <div class="go-title"></div>';
    html += '  <div class="go-slogan"></div>';
    html += '  <div class="go-brief"></div>';
    html += '  <div class="go-price"></div>';
    html += '  <div class="go-models go-row"><span></span><ul></ul></div>';
    html += '  <div class="go-colors go-row"><span></span><ul></ul></div>';
    html += '  <div class="go-list"><ul></ul></div>';
    html += '  <div class="go-buy"></div>';
    html += '</div></div>';
    html += '<div class="go-detail">';
    html += '</div>';
    jData.html(html);
    //-------------------------------------------
    // 输出商品图片区域
    var oMeta   = _.extend({}, obj.detail, obj.meta);
    if(obj.detail) {
        oMeta.brief   = oMeta.brief || obj.detail.brief;
        oMeta.content = oMeta.content || obj.detail.content;
    }

    // 得到关键元素
    var jPhoto  = jData.find(".go-photo");
    var jPoList = jData.find(".go-list");
    var jTitle  = jData.find(".go-title");
    var jTags   = jData.find(".go-tags");
    var jSlogan = jData.find(".go-slogan");
    var jBrief  = jData.find(".go-brief");
    var jPrice  = jData.find(".go-price");
    var jBuy    = jData.find(".go-buy");
    var jModels = jData.find(".go-models");
    var jColors = jData.find(".go-colors");
    var jDetail = jData.find(".go-detail");
    var jUl, jSpan;

    // 显示空图标
    if(!_.isArray(oMeta.th_media_list) || oMeta.th_media_list.length == 0) {
        // 采用缩略图
        if(oMeta.thumb) {
            jPhoto.attr({
                "photo" : "yes"
            }).empty();
            $('<span>').css({
                    "background-image" : media_pic_url(opt, oMeta, -1),
                }).appendTo(jPhoto);
        }
        // 空图标
        else {
            jPhoto.attr({
                "photo" : "none"
            }).empty();
            $('<span><i class="zmdi zmdi-image"></i></span>')
                .appendTo(jPhoto);
        }
        jPoList.remove();
    }
    // 显示详细的图片
    else {
        // 缩略图
        jPhoto.attr({
            "photo" : "yes"
        }).empty();
        $('<span>').css({
                "background-image" : media_pic_url(opt, oMeta, 0),
            }).appendTo(jPhoto);

        // 可选图片列表
        if(oMeta.th_media_list.length>1) {
            jUl = jPoList.children("ul");
            for(var i=0; i<oMeta.th_media_list.length; i++) {
                var mi = oMeta.th_media_list[i];
                var jLi = $('<li>').attr({
                        "current" : 0 == i ? "yes" : null
                    }).appendTo(jUl);
                $('<span>').css({
                    "background-image" : media_pic_url(opt, oMeta, i),
                }).appendTo(jLi);
            }
        }
        // 只有一个图片就别选了
        else {
            jPoList.remove();
        }
    }

    //-------------------------------------------
    // 购买按钮
    var buyLink = oMeta.th_link;
    if(buyLink || "always" == opt.buybtn) {
        $('<a>')
            .attr({
                "target" : IS_RUNTIME && buyLink
                                ? "_blank" : null,
                "href"   : IS_RUNTIME && buyLink
                                ? buyLink : "#",
            }).appendTo(jBuy)
                .append($('<span>').html(mapping.buy || "Buy Now"));
    } else {
        jBuy.remove();
    }

    //-------------------------------------------
    // 输出商品简介区域
    // 标题
    var disTitle = opt.disTitle || "{{th_nm}}";
    //console.log(disTitle)
    var title = $z.tmpl(disTitle)(oMeta);
    $('<span>').text(title).appendTo(jTitle);
    // if(oMeta.th_nm != oDetail.th_nm)
    //     $('<em>').text(oDetail.th_nm).appendTo(jTitle);

    // 标签
    if(_.isArray(oMeta.lbls) && oMeta.lbls.length > 0) {
        jUl = jTags.find("ul");
        for(var i=0; i<oMeta.lbls.length; i++) {
            $('<li>').text(oMeta.lbls[i]).appendTo(jUl);
        }
    } else {
        jTags.remove();
    }
    

    // 口号
    if(oMeta.th_slogan) {
        $('<span>').text(oMeta.th_slogan).appendTo(jSlogan);
    } else {
        jSlogan.remove();
    }

    // 价格信息
    var price = oMeta.th_price;
    if(price) {
        $('<em>').text("￥" + price).appendTo(jPrice);
    } else {
        jPrice.remove();
    }

    // 简介
    if(oMeta.brief) {
        $('<span>').text(oMeta.brief).appendTo(jBrief);
    } else {
        jBrief.remove();
    }

    // 得到商品页面地址
    var href = opt.href || window.location.pathname;

    // 可选型号
    if(mapping.model && _.isArray(obj.models) && obj.models.length>0) {
        jSpan = jModels.children("span");
        jSpan.text(mapping.model);
        jUl = jModels.children("ul");
        // 排序
        obj.models.sort(function(a, b){
            if(a.text == b.text)
                return 0;
            return a.text > b.text ? 1 : -1;
        });
        // 输出
        for(var i=0; i<obj.models.length; i++) {
            var mo = obj.models[i];
            var jLi = $('<li>').attr({
                    "current" : mo.text == (oMeta.th_model || oMeta.th_nm)
                                    ? "yes"
                                    : null
                }).appendTo(jUl);
            $('<a>').attr({
                "href" : IS_RUNTIME ? href + "?id=" + mo.id
                                    : ""
            }).text(mo.text).appendTo(jLi);

        }
    } else {
        jModels.remove();
    }

    // 可选颜色
    if(mapping.color && _.isArray(obj.colors) && obj.colors.length>0) {
        jSpan = jColors.children("span");
        jSpan.text(mapping.color);
        jUl = jColors.children("ul");
        // 排序
        obj.colors.sort(function(a, b){
            if(a.text == b.text)
                return 0;
            return a.text > b.text ? 1 : -1;
        });
        // 输出
        for(var i=0; i<obj.colors.length; i++) {
            var co = obj.colors[i];
            var jLi = $('<li>').attr({
                    "current" : co.id == oMeta.id ? "yes" : null
                }).appendTo(jUl);
            $('<a>').attr({
                "href" : IS_RUNTIME ? href + "?id=" + co.id
                                    : ""
            }).text(co.text).appendTo(jLi);

        }
    } else {
        jColors.remove();
    }
    //console.log(oMeta, obj)

    //-------------------------------------------
    // 输出商品详情区域
    if(oMeta.content){
        // 解析媒体的回调
        var formatMedia = function(src){
            var obj = this;
            // 看看是否是媒体
            var m = /^(media|attachment)\/(.+)$/.exec(src);
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
            .html($z.markdownToHtml(oMeta.content, {
                media : formatMedia,
                context : obj.detail || obj.meta,
            })).appendTo(jDetail);
        
        // 标识标题
        jAr.find("h1,h2,h3,h4,h5,h6").addClass("md-header");

        // 解析一下海报
        jPoster = jAr.find('pre[code-type="poster"]');
        $z.explainPoster(jPoster, {
            media : formatMedia,
            context : obj.detail || obj.meta,
        });

        // 处理一下视频
        $z.wrapVideoSimplePlayCtrl(jAr.find('video'), {
            watchClick : IS_RUNTIME,
        });

    } else {
        jDetail.remove();
    }

    //-------------------------------------------
    // 搞定
};
//..........................................................
$.fn.extend({ "_std_th_show_goods" : function(obj, opt){
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