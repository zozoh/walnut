define(function (require, exports, module) {
//=======================================================================
function navmenu_appendIcon(SC, jCom, cName) {
    jCom.attr("skin-reset", cName);
    // 记录原始宽高，以备点击后，创建占位对象使用
    window.setTimeout(function(){
        jCom.attr("primer-width",  jCom.outerWidth(true));
        jCom.attr("primer-height", jCom.outerHeight(true));
    }, 0);
    // 来吧
    var jMenu = jCom.find(".hmc-navmenu");
    var jIcon = jMenu.find(">.nmi-icon");
    if(jIcon.size() == 0) {
        // 增加一个图标
        jIcon = $('<div class="nmi-icon hm-del-save">')
            .html('<span><b></b><b></b><b></b></span>')
                .appendTo(jMenu);
        // 绑定事件
        // 如果开启了弹出模式，那么为了保证它是最高层的
        // 必须将其移动到 body 最后一个元素
        // 同时在原来的位置放置一个展位
        jIcon.on("click", function(){
            // IDE 环境下，只有激活的项目才执行
            if("IDE" == SC.mode && !jCom.attr("hm-actived")){
                return
            }
            // 已经开启了，那么收起    
            if(jMenu.attr("open")) {
                var jPlaceholder = jCom.data("@placeholder");
                if(jPlaceholder) {
                    jCom.insertBefore(jPlaceholder).removeData("@placeholder");
                    jPlaceholder.remove();
                }
                // 移除效果: 这里不延迟一下，动画出不来
                window.setTimeout(function(){
                    jMenu.removeAttr("open");
                }, 100)
            }
            // 未开启，那么开启一下
            else {
                // 首先放置展位
                var jPlaceholder = $('<div>').insertBefore(jCom).css({
                    "width"  : jCom.attr("primer-width") * 1,
                    "height" : jCom.attr("primer-height") * 1,
                });
                jCom.appendTo(SC.doc.body).data("@placeholder", jPlaceholder);
                // 开启效果: 这里不延迟一下，动画出不来
                window.setTimeout(function(){
                    jMenu.attr("open", "yes");
                }, 100)
            }
        });
    }
}
//.....................................................
// 本函数只有在桌面版时才会被调用
// 又或者手机版，在 IDE 环境下才会被调用
// 因为需要左右按钮切换当前幻灯片
function setupSlider(ME, SC, jCom, quiet) {
    
    jCom.off().attr("skin-reset", "slider");
    // 准备创建两个按钮
    ME.__desktop_switcher_init(jCom, "left");
    ME.__desktop_switcher_init(jCom, "right");

    // 创建滑动指示器
    ME.__create_dotted(jCom);

    // 准备切换函数
    var do_switch = function(SC, jCom, index){
        // 手机
        if("mobile" == SC.screen && "runtime" == SC.mode) {
            ME.__mobile_switch_current(SC, jCom, index);
        }
        // 桌面
        else {
            ME.__desktop_switch_current(SC, jCom, index);
        }
    };

    // 确保当前高亮项目为当前项目
    var hIndex = 0;
    // IDE 里面看一下当前高亮模式高亮了哪个区域
    if("IDE" == SC.mode && jCom.attr("highlight-mode")) {
        var jArea = jCom.find('> .hm-com-W > .hmc-rows > .hm-area[highlight]');
        hIndex = jArea.prevAll('.hm-area').length;
    }
    //console.log("ready", hIndex)
    ME.__desktop_switch_current(SC, jCom, hIndex, quiet);

    // 监控按钮事件
    jCom.on("click", ">.hm-com-W>.hmc-rows>.skin-rows-slider-btn", function(){
        var dire = $(this).attr("direction");
        var index = ME.__get_index(jCom);
        var off = ({
            "left"  : -1,
            "right" : 1
        })[dire];

        index = index + off;

        // 要操作的两个元素集合
        var jRows = jCom.find(">.hm-com-W>.hmc-rows");
        var jAs   = jRows.find('>.hm-area');
        var jDs   = jRows.find(">.skin-rows-slider-dots>ul>li");

        // 如果超过最后一个，选第一个
        if(index >= jAs.size()) {
            index = 0;
        }
        // 如果比0小，选最后一个
        else if(index < 0) {
            index = jAs.size() - 1;
        }

        // 切换
        do_switch(SC, jCom, index);
    });
    jCom.on("click", ">.hm-com-W>.hmc-rows>.skin-rows-slider-dots>ul>li", function(){
        var index = $(this).attr("area-index")*1;
        // 切换
        do_switch(SC, jCom, index);
    });
    //console.log(SC)

    // 得到自动切换方式
    var autorun = jCom.find(">.hm-com-W>[sa-autorun]").attr("sa-autorun");
    //console.log("autorun", autorun);
    
    // 运行时:启用自动切换
    if("runtime" == SC.mode && ("both"==autorun || SC.screen==autorun)) {
        // 注册一个 Timer，默认时间间隔为 3 秒
        var interval = ({
            "never"  : -1,
            "long"   : 8000,
            "normal" : 5000,
            "short"  : 3000,
        })[jCom.find(">.hm-com-W>.hmc-rows").attr("sa-interval")] || 5000;
        if(interval > 0) {
            var auto_switch = function(){
                // 得到上次被切换的时间
                var lastST = parseInt(jCom.attr("last-switch-time"));

                // 看看要不要再等等
                var now = Date.now() + 1;  // 加1毫秒，确保在极快的CPU下也能正常工作
                // console.log("auto_switch", now, "-", lastST, "=", now-lastST, 
                //                 "interval:", interval);
                if(lastST > (now - interval)) {
                    //console.log("wait", lastST + interval - now)
                    window.setTimeout(auto_switch, Math.max(100, lastST + interval - now));
                    return;
                }

                // 嗯，执行切换吧
                var index = ME.__get_index(jCom) + 1;

                // 要操作的两个元素集合
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                var jAs   = jRows.find('>.hm-area');
                var jDs   = jRows.find(">.skin-rows-slider-dots>ul>li");

                // 如果超过最后一个，选第一个
                if(index >= jAs.size()) {
                    index = 0;
                }
                // 如果比0小，选最后一个
                else if(index < 0) {
                    index = jAs.size() - 1;
                }

                //runtime.slider.ready.__desktop_switch_current(SC, jCom, index+1);
                do_switch(SC, jCom, index);

                // 准备执行下一次
                window.setTimeout(auto_switch, interval);
            };
            // 调用第一次
            window.setTimeout(auto_switch, interval);
        }
    }
}
//=======================================================================
var runtime = {
    // 初始化各个组件
    __invoke : function(eventName, SC, comName, selector) {
        $(SC.root).find(selector).each(function(){
            var com  = runtime[comName];
            if(!com)
                return;
            var methods = com[eventName];
            var jCom = $(this).closest(".hm-com, .hm-area, .wn-obj-layout, .md-code-poster");
            // 如果是函数，直接调用
            if(_.isFunction(methods)){
                methods.apply(runtime, [SC, jCom]);
            }
            // 否则，根据屏幕类型调用
            else {
                $z.invoke(methods, SC.screen, [SC, jCom]);
            }
        });
    },
    //.....................................................
    // 海报
    poster : {
        resize : {
            desktop : function(SC, jCom) {
                var w = jCom.attr('layout-desktop-width');
                var h = jCom.attr('layout-desktop-height');
                if('hidden' == h || 'hidden' == w) {
                    jCom.hide();
                }
                // 否则显示
                else {
                    jCom.show().find('>.md-code-poster-con').css({
                        "width" : w || "",
                        'height': h || ""
                    });
                }
            },
            mobile : function(SC, jCom) {
                var w = jCom.attr('layout-mobile-width');
                var h = jCom.attr('layout-mobile-height');
                if('hidden' == h || 'hidden' == w) {
                    jCom.hide();
                }
                // 否则显示
                else {
                    jCom.show().find('>.md-code-poster-con').css({
                        "width" : w || "",
                        'height': h || ""
                    });
                }
            }
        }
    },
    //.....................................................
    // 布局
    layout : {
        __ar_mode_jump : function(jAr) {
            // 修改 DOM
            $z.tabArticle(jAr);

            // 所有归纳块，都要搞一下滚动感知
            jAr.find('.hm-watch-scroll').removeClass("hm-watch-scroll");
            jAr.find('.z-article-block').addClass("hm-watch-scroll");

            // 事件
            jAr.off().on("click", ".z-article-tabs li", function(){
                var jLi = $(this);
                var index = jLi.attr("b") * 1;
                var jAr = jLi.closest("article, .z-article-block");
                var jArena = jAr.closest(".hmc-dynamic");
                var jStub;

                console.log("I am click")

                var jCon = jLi.closest('.z-article-block');
                // 嗯，顶级标签
                if(jCon.length == 0) {
                    jStub = jAr.find('>.z-article-block[b="'+index+'"]');
                }
                // 二级标签的话
                else {
                    jStub = jCon.find('>.z-article-blcon > .z-article-block[b="'+index+'"]');
                }
                
                // 跳转
                if(jStub.length > 0) {
                    var doc = jAr[0].ownerDocument;
                    var off = jStub.offset();
                    $([doc.documentElement, doc.body]).animate({
                        scrollTop : off.top - 150
                    }, 500);
                }
            });
        },
        __ar_mode_tabs : function(jAr) {
            // 得到所有的子
            var jq = jAr.children('hr');

            // 分析分隔线的类型
            for(var i=0; i<jq.length; i++) {
                var ele = jq[i];
                var jHr = $(ele);
                // 如果是分隔线
                if(ele.tagName == "HR") {
                    // 前后都是海报
                    if(jHr.prev().hasClass("md-code-poster")
                        && jHr.next().hasClass("md-code-poster")){
                        jHr.addClass("poster-sep");
                    }
                }
            }

            // 修改 DOM
            $z.tabArticle(jAr);

            // 事件
            jAr.off().on("click", ">.z-article-tabs li", function(){
                var jLi = $(this);
                var index = jLi.attr("b");
                jLi.closest("ul").find("li").removeAttr("current");
                jLi.attr("current", "yes");
                jLi.closest("article")
                    .find('div.z-article-block')
                        .removeAttr("current")
                            .filter('[b="'+index+'"]')
                                .attr("current", "yes");
            });

            // 设置一下初始的状态
            jAr.find('>.z-article-tabs li').first().attr("current", "yes");
            jAr.find('>.z-article-block').first().attr("current", "yes");
        },
        ready : function(SC, jLayout) {
            var jAr = jLayout.find("article.md-content");
            var jArena = jAr.closest(".hmc-dynamic");
            //console.log("hahah", jArena.attr("sa-docktabs"));

            // 标识一下自动停靠
            if(jArena.attr("sa-docktabs")) {
                window.setTimeout(function(){
                    $z.pageDock.mark(jAr.find('>.z-article-tabs'),
                        {factor:0, reset:true});
                    $z.pageDock.mark(
                        jAr.find('>.z-article-block >.z-article-tabs'), 
                        {factor:-.5, fitpage:false, detectParent:true}
                    );
                }, 500);
            } else {
                $z.pageDock.unmark(jAr.find('.z-article-tabs'));
            }    

            // 如果不是要自动归纳标签，确保释放
            var autotabs = jArena.attr("sa-autotabs");
            var tabsmode = jArena.attr("sa-tabsmode") || "jump";
            //console.log(autotabs)
            if("both"!=autotabs && SC.screen != autotabs) {
                runtime.layout.destroy.apply(this, [SC, jLayout]);
                return;
            }
            // 已经标识过了，就不要再标识了
            if(jAr.attr("ar-tabs-mode") == tabsmode){
                return;
            }

            // 首先清理一下
            runtime.layout.destroy.apply(this, [SC, jLayout]);

            // 先标识一下
            jAr.attr("ar-tabs-mode", tabsmode);

            // 归纳标签
            if("tabs" == tabsmode) {
                runtime.layout.__ar_mode_tabs(jAr);
            }
            // 生成索引
            else if("jump" == tabsmode) {
                runtime.layout.__ar_mode_jump(jAr);
            }       
        },
        destroy : function(SC, jLayout) {
            var jAr = jLayout.find("article.md-content");
            jAr.removeAttr("ar-tabs-mode");
            $z.untabArticle(jAr);
        },
        scroll : function(SC, jLayout) {
            var jAr = jLayout.find("article.md-content");
            if(jAr.length == 0)
                return;
            $z.tabArticleMarkCurrent(jAr, ">[b][hm-scroll-inview]");
            jAr.find('.z-article-block[z-article-tab-already]').each(function(){
                var jBlock = $(this);
                $z.tabArticleMarkCurrent(jBlock, ">.z-article-blcon >[b][hm-scroll-inview]");
            });
        },
        resize : function(SC, jLayout){
            //console.log(SC)
            var jArena = jLayout.closest(".hmc-dynamic");
            if(jArena.length == 0)
                return;

            // 得到缩放方式
            var scale = jArena.attr("sa-obj-it-preview-scale") || "16x9";
            var m = /^([\d.]+)[xX]([\d.]+)$/.exec(scale);
            var sW,sH;
            if(m) {
                sW = m[1] * 1;
                sH = m[2] * 1;
            }

            // 开关
            var isHeadingFirst = jArena.attr("sa-h-keep-first") == SC.screen;

            // 预先得到第一个子
            var jC0 = jLayout.children(":first-child");
            if(jC0.length == 0)
                return;
            var eleC0 = jC0[0];
            
            // 逐个处理字段项
            //window.setTimeout(function(){
                jLayout.find('section, [fld-display], [fld-type]').each(function(){
                    var jIt = $(this);

                    // 更新一下宽度
                    var attrName = "desktop" == SC.screen
                                     ? "layout-desktop-width"
                                     : "layout-mobile-width";
                    var w = jIt.attr(attrName);
                    jIt.css("width", w ? w + "%" : "");

                    // 标题：前置
                    if(this.tagName == 'H4') {
                        //console.log("haha", this.tagName)
                        // 来吧：确保在第一个
                        var fldKey = jIt.attr("fld-key");
                        if(isHeadingFirst) {
                            // 自己就是第一个子: 无视
                            if(eleC0 == this) {
                                return;
                            }
                            // 自己的组是第一个子，自己还是组的第一个子，无视
                            var jG = jIt.parent("section");
                            if(jG.length > 0 && jG[0] == eleC0 && jIt.prev().length==0){
                                return;
                            }
                            // 来吧，随便插入一个占位的桩子
                            $('<br>').hide().attr({
                                "shadow-fld" : fldKey
                            }).insertBefore(jIt);
                            jIt.attr("moved-first","yes").prependTo(jLayout);
                        }
                        // 否则：确保挪回来
                        else if(jIt.attr("moved-first")) {
                            var jBr = jLayout.find('br[shadow-fld="'+fldKey+'"]');
                            jIt.removeAttr("moved-first").insertBefore(jBr);
                            jBr.remove();
                        }
                    }

                    // 预览图：根据比例计算高度
                    if(jIt.attr("fld-display") == "Preview") {
                        // 得到图形
                        var jSpan = jIt.children("span");
                        if(m) {
                            var W = jSpan.outerWidth();
                            var H = W * sH / sW;
                            jSpan.css("height", H);
                        }
                        // 否则清除设定
                        else {
                            jSpan.css("height", "");
                        }
                    }
                });
            //}, 0);
        }
    },
    //.....................................................
    // 面包屑
    crumb : {
        ready : function(SC, jCom) {
            var jArena = jCom.find(".hmc-htmlcode").empty();
            var hierarchy = "";
            var pagePath  = "";
            //console.log(SC)
            // IDE 环境
            if("IDE" == SC.mode) {
                var uiCom = ZUI(jCom);
                var pageUI = uiCom ? uiCom.pageUI() : null;
                if(pageUI) {
                    var oPg   = ZUI(jCom).pageUI().getCurrentEditObj(true);
                    var oHome = pageUI.getHomeObj();
                    hierarchy = oPg.hm_hierarchy || "";
                    pagePath  = "/" + Wn.getRelativePath(oHome, oPg);
                }
                // 否则显示一个页面加载中
                else {
                    jArena.text("loading...");
                    return;
                }
            }
            // runtime 环境
            else {
                hierarchy = window.__HM_HIERARCHY || "";
                pagePath  = "/" + window.__PAGE_PATH;
            }
            //...........................................
            // 如果设置了控件自己的代码
            var code = jCom.data("@code");
            if(code)
                hierarchy = $z.unescapeHtml(code);
            //...........................................
            // 分析合并逻辑行
            var str   = $.trim(hierarchy);
            var lines = str.split(/\r?\n/);
            var hier = [];
            var last;
            for(var i=0; i<lines.length; i++) {
                var line = $.trim(lines[i]);
                // 新开头
                if(/^[+]/.test(line)) {
                    if(last)
                        hier.push(last);
                    last = line;
                }
                // 追加
                else if(last) {
                    last += line;
                }
            }
            // 最后一行
            if(last)
                hier.push(last);
            //console.log(hier)
            //...........................................
            // 空内容
            if(hier.length == 0) {
                jArena.text("> Blank Crumb");
                return;
            }
            //...........................................
            // 清空选区
            var jUl = $('<ul>').appendTo(jArena);
            //...........................................
            // 逐行添加项目
            /*
            每个行的结构类似
                + #dynamic_0.th_cate:/product_list_{{th_cate}}:A=原汁机,B=破壁机
            */
            for(var i=0; i<hier.length; i++) {
                var line = hier[i];
                var m = /^[+][ \t]*([^:]+)[ \t]*(:([^:]*))?(:(.+))?$/.exec(line);
                if(!m)
                    continue;
                var text = $.trim(m[1]);
                var href = $.trim(m[3]);
                var mapping = $.trim(m[5]);
                // 动态链接的话，需要搞一下
                var m2 = /^(#[^.]+)[.](.+)$/.exec(text);
                if(m2) {
                    var jTaCom = $(SC.doc).find(m2[1]);
                    var obj = jTaCom.data("@WNDATA");
                    //console.log(obj)
                    if(obj) {
                        text = $z.getValue(obj, m2[2], text);
                        // 处理一下映射  ":A=原汁机,B=破壁机,C=原汁破壁机"
                        if(mapping) {
                            var ss = mapping.split(/,/);
                            for(var x=0;x<ss.length;x++) {
                                var s = $.trim(ss[x]);
                                var pp = s.split('=');
                                if(pp.length>1) {
                                    var key = $.trim(pp[0]);
                                    var val = $.trim(pp[1]);
                                    if(text == key) {
                                        text = val;
                                        break;
                                    }
                                }
                            }
                        }
                        // 如果是链接，则搞一下替换
                        if(href) {
                            try{
                                href = $z.tmpl(href)(obj);
                            }catch(E){}
                        }
                    }
                }
                // 处理一下链接
                if(href) {
                    var ho = $z.parseHref(href);
                    if(ho.href) {
                        if(!/[.]html?$/.test(ho.href))
                            ho.href += ".html";
                        // 处理成相对链接
                        ho.href = $z.getRelativePath(pagePath, ho.href);
                        // 确保全小写
                        ho.href = ho.href.toLowerCase();
                    }
                    href = $z.renderHref(ho);
                }

                // 生成元素吧
                var jLi = $('<li>').appendTo(jUl);
                if(href) {
                    $('<a>').attr("href", href).text(text).appendTo(jLi);
                }
                // 否则来个 <b>
                else {
                    $('<b>').text(text).appendTo(jLi);
                }
            }

            // console.log(hier)
        }
    },
    //.....................................................
    // 展示操作步骤
    showOperation : {
        setup : function(SC, jCom) {
            jCom.on("click", ".op-plist li", function(){
                var jLi = $(this);
                var jUl = jLi.parent();
                var jImage = jCom.find(".op-preview .op-image").empty();
                jUl.find("li").removeAttr("current");
                jLi.attr("current", "yes");

                var mime = jLi.attr("li-mime");
                var src  = jLi.attr("li-src");
                //console.log(mime, src)

                // 视频
                if(/^video\//.test(mime)) {
                    //console.log("aaaa")
                    var jV = $('<video controls>').attr({
                        "src" : src,
                    }).appendTo(jImage);
                    $z.wrapVideoSimplePlayCtrl(jV, {
                        watchClick : true
                    });
                }
                // 图片
                else {
                    $('<img>').attr({
                        "src" : src,
                    }).appendTo(jImage);
                }
            });
        }
    },
    //.....................................................
    // 视频
    video : {
        resize : function(SC, jCom){
            jCom.find(".hmcv-s-blank").css({
                width  : jCom.width(),
                height : jCom.height()
            });
        }
    },
    //.....................................................
    // 收纳盒
    conbox : {
        __hide_cb_last : function(jArena, jCBFcon) {
            var jCBFcon = jCBFcon || jArena.find(">.hm-area[cb-last]");

            if(jArena.attr("sa-float-last") && jCBFcon && jCBFcon.length > 0) {
                var bwPx = $D.dom.getProp(jArena, "border-bottom-width");
                var bw = $z.toPixel(bwPx, 0, 0);
                jCBFcon.css("bottom", (jCBFcon.outerHeight(true) * -1) - bw);
            }

        },
        setup : function(SC, jCom) {
            var jArena = jCom.find('>.hm-com-W>.hmc-rows[sa-float-last]');
            console.log(jArena.length)
            // 绑定鼠标事件
            if(jArena.length > 0) {
                var jCBF = jArena.find('>.hm-area[cb-last]');
                var jCBFcon = jCBF.find(">.hm-area-con");

                jCom.on("mouseenter", function(){
                    jCBFcon.css("bottom", 0);
                }).on("mouseleave", function(){
                    runtime.conbox.__hide_cb_last(jArena, jCBFcon);
                });
            }
        },
        resize : function(SC, jCom) {
            var jArena = jCom.find('>.hm-com-W>.hmc-rows');
            var jAreas = jArena.find('>.hm-area').removeAttr('cb-last');
            //console.log(jAreas.length)
            // 只有两个以上的区域，才开启盒子模式
            if(jAreas.length < 2)
                return;

            // 标识最后一个区域为浮动
            var jCBF = jAreas.last().attr("cb-last", "yes");
            var jCBFcon = jCBF.find(">.hm-area-con");

            // 取消了浮动，无视
            if(!jArena.attr("sa-float-last")){
                return;
            }

            // 运行时，或者编辑时的高亮模式，隐藏浮空块
            if("runtime" == SC.mode 
                || ("IDE" == SC.mode
                        && jCom.attr("highlight-mode")
                        && !jCBF.attr("highlight"))) {
                runtime.conbox.__hide_cb_last(jArena, jCBFcon);
            }
            // 否则重置一下 bottom 以便显示
            else {
                jCBFcon.css("bottom", 0);
            }
        }
    },
    //.....................................................
    // 自动停靠页眉
    rowsSky : {
        ready : function(SC, jCom) {
            if(jCom.find('.hmc-rows[sa-autodock]').length > 0) {
                $z.pageDock.mark(jCom, true);
            } else {
                $z.pageDock.unmark(jCom);
            }
        },
    },
    //.....................................................
    // 顶级菜单
    navmenuSky : {
        ready : function(SC, jCom) {
            navmenu_appendIcon(SC, jCom, "navmenuSky");
        },
        reset : function(SC, jCom) {
            var jMenu = jCom.find(".hmc-navmenu");
            var jIcon = jMenu.find(">.nmi-icon");
            if(jMenu.attr("open"))
                jIcon.click();
            jIcon.off().remove();
        },
        resize : {
            // 桌面版设置最小宽度
            desktop : function(SC, jCom) {
                // 桌面版设置最小宽度
                jCom.attr("primer-width",  jCom.outerWidth(true));
                jCom.attr("primer-height", jCom.outerHeight(true));
                jCom.find(".li-top").each(function(){
                    var jTop = $(this);
                    jTop.find(".ul-sub-0").css("min-width", jTop.outerWidth());
                });
            },
            // 手机版移除最小宽度
            mobile : function(SC, jCom) {
                jCom.find(".li-top").each(function(){
                    var jTop = $(this);
                    jTop.find(".ul-sub-0").css("min-width", "");
                });
            }
        }
    },
    //.....................................................
    // 图标式弹出菜单
    navmenuIcon : {
        ready : function(SC, jCom) {
            navmenu_appendIcon(SC, jCom, "navmenuIcon");
        },
        reset : function(SC, jCom) {
            var jMenu = jCom.find(".hmc-navmenu");
            var jIcon = jMenu.find(">.nmi-icon");
            if(jMenu.attr("open"))
                jIcon.click();
            jIcon.off().remove();
        },
        resize : function(SC, jCom) {
            jCom.attr("primer-width",  jCom.outerWidth(true));
            jCom.attr("primer-height", jCom.outerHeight(true));
        }
    },
    //.....................................................
    // 垂直自适应分栏
    columnsAutofit : {
        resize : {
            // 桌面版加回宽高限制
            desktop : function(SC, jCom) {
                jCom.find(">.hm-com-W>.hmc-columns>.hm-area").each(function(){
                    var jArea = $(this);
                    var asize = jArea.attr("area-size");
                    // 设置数字
                    if(/^([\d.]+)(px|rem|%)?$/.test(asize)) {
                        jArea.css({
                            "width" : asize,
                            "flex"  : "0 0 auto"
                        });
                    }
                    // 仅仅是紧凑模式
                    else if("compact" == asize) {
                        jArea.css({
                            "width" : "",
                            "flex"  : "0 0 auto"
                        });   
                    }
                    // 否则全部去掉
                    else {
                        jArea.css({
                            "width" : "",
                            "flex"  : ""
                        });   
                    }
                });
            },
            // 移动设备，去掉宽高等限制
            mobile : function(SC, jCom) {
                jCom.find(">.hm-com-W>.hmc-columns>.hm-area").each(function(){
                    $(this).css({
                        "width" : "",
                        "flex"  : ""
                    });
                });
            }
        }
    },
    //.....................................................
    // 固定式停靠条
    fixbar : {
        ready : function(SC, jCom) {
            if(jCom.data("is_ready"))
                return;
            jCom.data("is_ready", "yes");

            // 滚动到顶部
            jCom.on('click', 'li[a="top"]', function(){
                $z.doAnimatDocumentScrollTop(jCom, 500, 0);
            });
            // 展开隐藏项
            jCom.on("mouseenter", '.hmc-htmlcode > ul > li', function(){
                var jLi = $(this);
                var jES = jLi.find("> .enter-show");
                if(jES.length > 0) {
                    jES.show();
                    $z.dock(jLi, jES, 'V');
                }
            });
            // 收起隐藏项
            jCom.on("mouseleave", '.hmc-htmlcode > ul > li', function(){
                var jLi = $(this);
                var jES = jLi.find("> .enter-show");
                if(jES.length > 0) {
                    jES.hide();
                }
            });
        }
    },
    //.....................................................
    // 图片墙
    wallImage : {
        resize : function(SC, jCom){
            // 准备关键变量
            var jArena = jCom.find(".skin-dynamic-_std-th_wall_image");
            var jThumb = jArena.find(".wpi-thumb");

            // 得到缩放方式
            var scale = jArena.attr("sa-it-thumb-scale") || "16x9";
            var m = /^([\d.]+)[xX]([\d.]+)$/.exec(scale);
            

            if(m) {
                var sW = m[1] * 1;
                var sH = m[2] * 1;
                var W = jThumb.width();
                var H = W * sH / sW;
                jThumb.css("height", H);
            }
            // 否则清除设定
            else {
                jThumb.css("height", "");
            }
        }
    },
    //.....................................................
    // 多媒体展示
    showMedia : {
        ready : {
            desktop : function(SC, jCom) {
                var jPlist = jCom.find(".tm-play-list");
                if(jPlist.length > 0) {
                    jCom.find(".skin-dynamic-_std-th_show_image").attr({
                        "show-plist" : "yes"
                    });
                    jCom.find(".tm-play").removeAttr("click-hide-plist");
                }
            },
            mobile : function(SC, jCom) {
                var jPlist = jCom.find(".tm-play-list");
                if(jPlist.length > 0) {
                    jCom.find(".skin-dynamic-_std-th_show_image")
                        .removeAttr("show-plist");
                    jCom.find(".tm-play").attr("click-hide-plist", "yes");
                }
            },
        },
    },
    //.....................................................
    // 按钮搜索器
    btnLikeSearcher : {
        setup : function(SC, jCom) {
            var jS = jCom.find(">.skin-searcher-btnlike");
            // 展开
            jCom.on("click", ".kwd-btn", function(){
                jS.attr("show-input", "yes");
                jS.find("input").focus();
            });
            // 收起
            jCom.on("blur", "input", function(){
                jS.removeAttr("show-input");
            });
        }
    },
    //.....................................................
    // 商品详情
    goodsDetail : {
        setup : function(SC, jCom) {
            jCom.on("click", ".go-detail-tabs li", function(){
                var jLi = $(this);
                var index = jLi.attr("b");
                jLi.closest("ul").find("li").removeAttr("current");
                jLi.attr("current", "yes");
                jLi.closest("article")
                    .find('div.go-detail-block')
                        .removeAttr("current")
                            .filter('[b="'+index+'"]')
                                .attr("current", "yes");
            });
        },
        destroy : function(SC, jCom) {
            var jArena = jCom.find(".hmc-dynamic");
            var jAr = jCom.find(".go-detail article.md-content");
            // 未标识过，无需释放
            if(!jAr.attr("ar-goods-formated"))
                return;

            // 得到关键变量
            var jTabs = jAr.find('.go-detail-tabs');
            

            // 循环块
            jAr.find('.go-detail-block').each(function(){
                var jBlock = $(this);
                var index  = jBlock.attr("b");

                // 恢复标题等
                $('<hr>').insertBefore(jBlock);
                var jLi = jTabs.find('li[b="'+index+'"]');
                $('<h1>').html(jLi.html()).insertBefore(jBlock);

                // 改变子
                jBlock.children().insertAfter(jBlock);

                // 去掉自身
                jBlock.remove();
            });

            // 删除标签部分
            jTabs.remove();
            jAr.removeAttr("ar-goods-formated");
        },
        ready : function(SC, jCom) {
            var jArena = jCom.find(".hmc-dynamic");
            var jAr = jCom.find(".go-detail article.md-content");
            //console.log("hahah", jArena.attr("sa-autotabs"));

            // 如果不是要自动归纳标签，确保释放
            if(!jArena.attr("sa-autotabs")) {
                runtime.goodsDetail.destroy.apply(this, [SC, jCom]);
                return;
            }
            // 已经标识过了，就不要再标识了
            if(jAr.attr("ar-goods-formated"))
                return;

            // 得到所有的子
            var jq = jAr.children('hr');

            // 分析分隔线的类型
            for(var i=0; i<jq.length; i++) {
                var ele = jq[i];
                var jHr = $(ele);
                // 如果是分隔线
                if(ele.tagName == "HR") {
                    // 前后都是海报
                    if(jHr.prev().hasClass("md-code-poster")
                        && jHr.next().hasClass("md-code-poster")){
                        jHr.addClass("poster-sep");
                    }
                }
            }

            // 来吧，搞一个格式化，把所有 hr 后面有 H1 的都缩起来
            jq = jAr.children();
            var block = {title:null, eles:[]};
            var blist = [];
            var lastIndex = jq.length - 1;
            for(var i=0; i<jq.length; i++) {
                var ele = jq[i];
                // 当前是 Hr 且前面是一个 H1，准备收缩的块
                if(i < lastIndex 
                    && 'HR' == ele.tagName 
                    && 'H1' == jq[i+1].tagName) {
                    // 先看看之前的块要不要推入
                    // 有标题的才推入
                    if(block.title) {
                        blist.push(block);
                    }
                    // 开始一个新块
                    block = {
                        title : $(jq[i+1]).html(),
                        eles  : []
                    };
                    // 嗯，移除这俩吧
                    $(ele).remove();
                    jq.eq(i+1).remove();
                    // 后面的 H1 不用看了
                    i++;
                }
                // 增加到当前块里
                else {
                    block.eles.push(ele);
                }
            }
            // 推入最后一个块
            if(block.title) {
                blist.push(block);
            }

            // 将所有有标题的块生成一下索引
            if(blist.length > 0) {
                var jTabs = $('<div class="go-detail-tabs">').appendTo(jAr);
                var jUl = $('<ul>').appendTo(jTabs);
                for(var i=0; i<blist.length; i++) {
                    var b = blist[i];
                    $('<li>').html(b.title).attr("b", i).appendTo(jUl);
                }

                // 依次将块缩入一个 DIV
                for(var i=0; i<blist.length; i++) {
                    var b = blist[i];
                    $('<div class="go-detail-block">')
                        .attr("b", i)
                            .append($(b.eles))
                                .appendTo(jAr);
                }

                // 第一个块被标识高亮
                jUl.children().first()
                    .attr("current", "yes");
                jAr.children('div.go-detail-block').first()
                    .attr("current", "yes");
            }

            // 搞定，标识一下
            jAr.attr("ar-goods-formated", "yes");
        },
        scroll : function(SC, jCom) {
            // var jAr    = jCom.find("article.md-content");
            // if(jAr.attr("ar-goods-formated")) {
            //     var jTabs  = jCom.find(".go-detail-tabs");
            //     var jBlock = jCom.find(".go-detail-block[current]");
            //     // 每当当前块的位置要超过屏幕的时候，就标识一下 jTabs
            //     var rect = $D.rect.gen(jBlock);
            //     if(rect) {
            //     //console.log($D.rect.dumpValues(rect))
            //         jAr.attr("float-tabs", rect.top<=jTabs.outerHeight() ? "yes" : null);
            //     }
            // }
        },
        resize : {
            desktop : function(SC, jCom){
                // 关键元素
                var jAr      = jCom.find("article.md-content");
                var jPreview = jCom.find(".go-preview");
                var jPhoto   = jCom.find(".go-photo");
                var jInfo    = jCom.find(".go-info");
                // 首先去掉宽度约束
                jPhoto.css({
                    "width"  : "",
                    "height" : "",
                });
                // 判断一下，最好不要超过右侧的高度
                var Hi = jInfo.outerHeight();
                jPhoto.css({
                    "width"  : Hi,
                    "height" : Hi,
                });

                // 详情区域，非悬浮时总是更新其左右边距离
                // var jTabs = jCom.find(".go-detail-tabs");
                // if(!jAr.attr("float-tabs") && jTabs.length > 0 ) {
                //     jTabs.css("position", "unset");
                //     var tabRect = $D.rect.gen(jTabs);
                //     var winRect = $D.rect.gen(SC.root);
                //     // console.log($D.rect.dumpValues(tabRect));
                //     // console.log($D.rect.dumpValues(winRect));
                //     jTabs.css({
                //         "position" : "",
                //         "left"  : tabRect.left,
                //         "right" : winRect.width - tabRect.right,
                //     });
                // }
            },
            mobile : function(SC, jCom){
                // 关键元素
                var jPhoto   = jCom.find(".go-photo");
                var jTabs    = jCom.find(".go-detail-tabs");
                // 首先去掉宽度约束
                jPhoto.css({
                    "width"  : "",
                    "height" : "",
                });
                jTabs.css({
                    "position" : "",
                    "left"  : "",
                    "right" : "",
                });
            }
        },
    },
    //.....................................................
    // 商品切换预览
    goodsPreview : {
        setup : function(SC, jCom) {
            jCom.on("mouseenter", ".go-list li", function(){
                var jLi = $(this);
                var jUl = jLi.parent();
                var jIntro = jLi.closest(".go-intro");
                var jPhoto = jIntro.find(".go-photo");
                jUl.find("li").removeAttr("current");
                jLi.attr("current", "yes");
                jPhoto.html(jLi.html());
            });
        }
    },
    //.....................................................
    // 滑出式收纳栏
    menubox : {
        setup : function(SC, jArea) {
            jArea.click(function(e){
                //console.log("click area!!!")
                // 只有点在 UL 才有效，桌面版一般点不到
                if($(this).closest("ul").size() == 0)
                    $z.toggleAttr(this, "highlight", "yes");
            });
        }
    },
    //.....................................................
    // 幻灯片
    slider : {
        setup : function(SC, jCom) {
            jCom.off();
            var jRows = jCom.find(">.hm-com-W>.hmc-rows");
            jRows.find(">.skin-rows-slider-dots, .skin-rows-slider-btn").remove();
        },
        reset : function(SC, jCom) {
            jCom.off();
            var jRows = jCom.find(">.hm-com-W>.hmc-rows");
            jRows.find(">.skin-rows-slider-dots, .skin-rows-slider-btn").remove();
        },
        ready : {
            // 创建滑动指示器
            __create_dotted : function(jCom) {
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                var jDiv  = jRows.find(">.skin-rows-slider-dots");
                if(jDiv.size() == 0){
                    jDiv = $('<div class="skin-rows-slider-dots">')
                                .appendTo(jRows);
                }
                var jUl = $('<ul>').appendTo(jDiv.empty());
                jCom.find(">.hm-com-W>.hmc-rows>.hm-area")
                    .each(function(index){
                        $('<li>').attr({
                            "area-index" : index,
                            "area-id" : $(this).attr("area-id"),
                        }).appendTo(jUl);
                    });
            },
            // 桌面版:执行切换
            __desktop_switch_current : function(SC, jCom, index, quiet) {
                //console.log("__desktop_switch_current", index);
                // 确保下标是数字
                index = parseInt(index);

                // 要操作的两个元素集合
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                var jAs   = jRows.find('>.hm-area');
                var jDs   = jRows.find(">.skin-rows-slider-dots>ul>li");

                // index 不能超过限制
                if(index >= jAs.size() || index < 0) {
                    return;
                }

                // // 如果超过最后一个，选第一个
                // if(index >= jAs.size()) {
                //     index = 0;
                // }
                // // 如果比0小，选最后一个
                // else if(index < 0) {
                //     index = jAs.size() - 1;
                // }

                // 找到要高亮的元素和圆点
                var jA = jAs.eq(index);
                var jD = jDs.eq(index);
                
                // 改变
                jAs.removeAttr("current");
                jDs.removeAttr("current");
                jA.attr("current", "yes");
                jD.attr("current", "yes");

                // 最后标识一下切换的时间
                var now = Date.now();
                var lastST = parseInt(jCom.attr("last-switch-time"));
                jCom.attr("last-switch-time", now);

                // 如果是编辑环境下，则用标识高亮模式的方法，切换当前区域
                if("IDE" == SC.mode && !quiet) {
                    var uiCom = ZUI(jCom);
                    if(uiCom) {
                        uiCom.highlightArea(jA);
                        uiCom.notifyDataChange("page", uiCom);
                    }
                }
            },
            // 桌面版:创建切换按钮
            __desktop_switcher_init : function(jCom, dire) {
                //console.log(dire)
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                var jDiv  = jRows.find('>.skin-rows-slider-btn[direction="'+dire+'"]');
                if(jDiv.size() == 0){
                    jDiv = $('<div class="skin-rows-slider-btn">')
                        .attr("direction", dire)
                            .html('<i class="zmdi zmdi-chevron-'+dire+'"></i>')
                                .appendTo(jRows);
                }
                return jDiv;
            },
            // 得到当前下标
            __get_index : function(jCom) {
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                return jRows.find(">.skin-rows-slider-dots>ul>li[current]")
                            .attr("area-index")*1;
            },
            // 桌面版
            "desktop" : function(SC, jCom) {
                // console.log("slider setup desktop", this);
                setupSlider(this, SC, jCom, true);                
            },
            // 手机版:执行切换
            __mobile_switch_current : function(SC, jCom, index) {
                // 确保下标是数字
                index = parseInt(index);

                // 要操作的两个元素集合
                var jRows = jCom.find(">.hm-com-W>.hmc-rows");
                var jAs   = jRows.find('>.hm-area');
                var jDs   = jRows.find(">.skin-rows-slider-dots>ul>li");

                // index 不能超过限制
                if(index >= jAs.size() || index < 0) {
                    return;
                }

                // 找到要高亮圆点
                var jA = jAs.eq(index);
                var jD = jDs.eq(index);
                
                // 改变
                jAs.removeAttr("current");
                jDs.removeAttr("current");
                jA.attr("current", "yes");
                jD.attr("current", "yes");

                // 将所有的 .hm-area 位移
                var W = jA.width();
                jAs.each(function(i){
                    var off = (i-index) * W;
                    $(this).css({
                        "transform" : "translateX("+off+"px)"
                    });
                });

                // 如果是编辑环境下，则用标识高亮模式的方法，切换当前区域
                if("IDE" == SC.mode && !quiet) {
                    var uiCom = ZUI(jCom);
                    if(uiCom) {
                        uiCom.highlightArea(jA);
                        uiCom.notifyDataChange("page", uiCom);
                    }
                }
            },
            // 手持设备版
            "mobile" : function(SC, jCom) {
                // console.log("slider setup mobile", this);               
                // 运行时，才启用手持版特性
                if("runtime" == SC.mode) {
                    setupSlider(this, SC, jCom);
                    // // 创建滑动指示器
                    // this.__create_dotted(jCom);

                    // // 确保第一个是当前的
                    // this.__mobile_switch_current(jCom, 0);

                    // 运行时:启用手势监控
                    var ME = this;
                    new AlloyFinger(jCom[0], {
                        swipe: function (e) {
                            //console.log("swipe" + e.direction, e);
                            var index = ME.__get_index(jCom);
                            var off = ({
                                "Left"  : 1,
                                "Right" : -1
                            })[e.direction];
                            if(_.isNumber(off)) {
                                ME.__mobile_switch_current(SC, jCom, index + off);
                            }
                        }
                    });
                }
                // 编辑时，还是用桌面版特性比较好
                else if("IDE" == SC.mode) {
                    setupSlider(this, SC, jCom, true);
                }
            },
        }, // ~ slider.setup
    },
    //.....................................................
};
//=======================================================================
/*
SC = {
    mode   : "runtime",
    screen : "desktop|mobile",
    doc    : Document,
    jQuery : jQuery,
    root   : <HTML>
    win    : Window,
    skin   : skin,
} // SkinContext
*/
module.exports = {
    // 需要幂等
    on : function(){
        var SC = this;
        //console.log("skin on");
        // 绑定事件: runtime
        if("runtime" == this.mode) {
            runtime.__invoke("setup", SC, "conbox", ".skin-rows-conbox");
            runtime.__invoke("setup", SC, "slider", ".skin-rows-slider");
            runtime.__invoke("setup", SC, "menubox", ".skin-area-Lmenubox");
            runtime.__invoke("setup", SC, "showOperation", ".skin-dynamic-_std-th_show_operation");
            runtime.__invoke("setup", SC, "goodsPreview", ".skin-dynamic-_std-th_show_goods");
            runtime.__invoke("setup", SC, "goodsDetail", ".skin-dynamic-_std-th_show_goods");
            runtime.__invoke("setup", SC, "btnLikeSearcher", ".skin-searcher-btnlike");
        }
    },
    // 需要幂等
    ready : function(){
        var SC = this;
        //console.log("skin ready", SC);
        runtime.__invoke("ready", SC, "layout", ".wn-obj-layout");
        runtime.__invoke("ready", SC, "slider", ".skin-rows-slider");
        runtime.__invoke("ready", SC, "rowsSky", ".skin-rows-sky");
        runtime.__invoke("ready", SC, "crumb", ".skin-htmlcode-crumb");
        runtime.__invoke("ready", SC, "navmenuSky", ".skin-navmenu-sky");
        runtime.__invoke("ready", SC, "navmenuIcon", ".skin-navmenu-icon");
        runtime.__invoke("ready", SC, "goodsDetail", ".skin-dynamic-_std-th_show_goods");
        runtime.__invoke("ready", SC, "showMedia", ".skin-dynamic-_std-th_show_image");
        runtime.__invoke("ready", SC, "fixbar", ".skin-htmlcode-fixbar");

        // 处理自动停靠
        $z.pageDock.adjust(SC.doc);
    },
    // 需要幂等
    scroll : function(){
        var SC = this;
        //console.log("skin scroll");
        // 自动停靠页眉
        runtime.__invoke("scroll", SC, "rowsSky", ".skin-rows-sky");
        // 修正产品详情的 DOM 结构
        runtime.__invoke("scroll", SC, "goodsDetail", ".skin-dynamic-_std-th_show_goods");
        // 布局控件对于滚动的感知
        runtime.__invoke("scroll", SC, "layout", ".wn-obj-layout");

        // 处理自动停靠
        $z.pageDock.adjust(SC.doc);
    },
    // 需要幂等
    off : function(){
        var SC = this;
        //console.log("skin off");
    },
    // 更换皮肤前，对控件的修改
    reset : function(jCom) {
        var SC = this;
        //console.log("I am reset", jCom);
        var resetKey = jCom.attr("skin-reset");
        if(resetKey) {
            runtime.__invoke("reset", SC, resetKey, jCom);
            jCom.removeAttr("skin-reset");
        }
    },
    // 需要幂等
    resize : function(){
        var SC = this;
        //console.log("skin resize");

        // IDE 和 runtime 都需要的特性
        runtime.__invoke("resize", SC, "conbox", ".skin-rows-conbox");
        runtime.__invoke("resize", SC, "columnsAutofit", ".skin-columns-autofit");
        runtime.__invoke("resize", SC, "navmenuSky", ".skin-navmenu-sky");
        runtime.__invoke("resize", SC, "navmenuIcon", ".skin-navmenu-icon");
        runtime.__invoke("resize", SC, "layout", ".wn-obj-layout");
        runtime.__invoke("resize", SC, "video", ".hm-com-video");
        runtime.__invoke("resize", SC, "poster", ".md-code-poster");

        // 确保商品的详情图片是方的
        window.setTimeout(function(){
            runtime.__invoke("resize", SC, "goodsDetail", ".skin-dynamic-_std-th_show_goods");
            runtime.__invoke("resize", SC, "wallImage", ".skin-dynamic-_std-th_wall_image");
        }, 0);
    }
};
//=======================================================================
});

