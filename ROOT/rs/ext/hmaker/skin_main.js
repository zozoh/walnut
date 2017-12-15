$(function(){
    //........................................................
    // 声明一下函数
    var on_screen_resize = function(SC){
        skin = SC.skin;
        // 判断一下如果宽度小于给定宽度，则为移动设备，否则为桌面
        // 默认 640 作为移动设备和桌面设备的屏幕宽度分隔线
        skin.designWidth = skin.designWidth || 640;
        SC.screen = SC.win.innerWidth > skin.designWidth 
                            ? "desktop"
                            : "mobile";
        // 修改顶级元素 <HTML> 的 fontSize 以便适用 rem
        $z.do_change_root_fontSize(SC);
        // 回调皮肤的自定义 resize 函数
        $z.invoke(SC.skin, "resize", [], SC);
    };
    //........................................................
    // 加载
    seajs.use(window.skin_js_path || "./skin/skin.js", function(skin){
        // 准备上下文
        var SC = {
            mode   : "runtime",
            doc    : document,
            win    : window,
            root   : document.documentElement,
            jQuery : window.jQuery,
            skin   : skin,
        };

        // 根据屏幕初始化尺寸
        on_screen_resize(SC);

        // 启用皮肤
        $z.invoke(skin, "on", [], SC);
        
        // 增加 resize 事件监听
        window.addEventListener("resize", function(){
            on_screen_resize(SC);            
        });

        // 增加 scroll 事件监听
        window.addEventListener("scroll", function(){
            var jWin = $(window);
            // 得到窗口的矩形
            var rectWin = $D.dom.winsz();
            rectWin.top = jWin.scrollTop();
            rectWin.left = jWin.scrollLeft();
            $D.rect.count_tlwh(rectWin);

            //console.log($D.rect.dumpValues(rectWin));

            // 循环所有的组件，计算其所在矩形是否在窗口之内
            $(document.body).find(".hm-com").each(function(){
                var rect = $D.rect.gen(this);
                var inview = $D.rect.is_overlap(rectWin, rect);
                $(this).attr({
                    "hm-scroll-inview"  : (inview ? "yes" : null),
                    "hm-scroll-outview" : (inview ? null  : "yes"),
                });
            });
        });
        
        // 调整屏幕方向的监听
        window.addEventListener("orientationchange", function(){
            on_screen_resize(SC);
        });
    });
    //........................................................
});