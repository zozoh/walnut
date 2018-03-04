$(function(){
    //........................................................
    // 事件:屏幕 resize
    var on_screen_resize = function(SC){
        skin = SC.skin;
        // 判断一下如果宽度小于给定宽度，则为移动设备，否则为桌面
        // 默认 640 作为移动设备和桌面设备的屏幕宽度分隔线
        SC.screen = SC.win.innerWidth > skin.designWidth 
                            ? "desktop"
                            : "mobile";
        // 修改顶级元素 <HTML> 的 fontSize 以便适用 rem
        $z.do_change_root_fontSize(SC);

        // 重新应用滚动
        on_page_scroll(SC);

        // 回调皮肤的自定义 resize 函数
        $z.invoke(SC.skin, "resize", [], SC);
    };
    //........................................................
    // 事件:页面滚动
    var on_page_scroll = function(SC){
        var jWin = $(window);
        // 得到窗口的矩形
        var rectWin = $D.dom.winsz();
        // rectWin.top = jWin.scrollTop();
        // rectWin.left = jWin.scrollLeft();
        $D.rect.count_tlwh(rectWin);

        //console.log($D.rect.dumpValues(rectWin));

        // 循环所有的组件，计算其所在矩形是否在窗口之内
        $(document.body).find(".hm-com, .hm-watch-scroll").each(function(){
            var jCom = $(this);
            var rect = $D.rect.gen(jCom);
            //console.log(" > ", jCom.attr("id")+":", $D.rect.dumpValues(rect));
            var inview = $D.rect.is_overlap(rectWin, rect);
            var once = jCom.attr("hm-once-inview") || inview ? true : false;
            $(this).attr({
                "hm-never-inview"   : (once ? null  : "yes"),
                "hm-once-inview"    : (once ? "yes" : null),
                "hm-scroll-inview"  : (inview ? "yes" : null),
                "hm-scroll-outview" : (inview ? null  : "yes"),
            });
        });

        // 回调皮肤的自定义 scroll 函数
        $z.invoke(SC.skin, "scroll", [], SC);
    };
    //........................................................
    // 入口函数
    var __main__ = function(skin){
        // 准备上下文
        var SC = {
            mode   : "runtime",
            doc    : document,
            win    : window,
            root   : document.documentElement,
            jQuery : window.jQuery,
            skin   : skin,
        };

        // 决定屏幕形式
        skin.designWidth = skin.designWidth || 640;
        SC.screen = SC.win.innerWidth > skin.designWidth 
                            ? "desktop"
                            : "mobile";

        // 启用皮肤
        $z.invoke(skin, "on", [], SC);
        $z.invoke(skin, "ready", [], SC);

        // 根据屏幕初始化尺寸
        on_screen_resize(SC);
        
        // 增加 resize 事件监听
        window.addEventListener("resize", function(){
            on_screen_resize(SC);            
        });

        // 增加 scroll 事件监听
        window.addEventListener("scroll", function(){
            on_page_scroll(SC);
        });
        
        // 调整屏幕方向的监听
        window.addEventListener("orientationchange", function(){
            on_screen_resize(SC);
        });
    };
    //........................................................
    // 加载
    seajs.use(window.skin_js_path || "./skin/skin.js", function(skin){
        window.setTimeout(function(){
            __main__(skin);
        }, 0);
    });
    //........................................................
});