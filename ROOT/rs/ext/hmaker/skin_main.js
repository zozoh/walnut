$(function(){
    seajs.use(window.skin_js_path || "./skin/skin.js", function(skin){
        var skinContext = {
            mode   : "runtime",
            doc    : document,
            win    : window,
            root   : document.documentElement,
            jQuery : window.jQuery
        };
        skin.on.call(skinContext);

        // 直接调用一遍 resize
        $z.do_change_root_fontSize(skinContext);
        skin.resize.call(skinContext);
        
        // 增加 resize 事件监听
        window.addEventListener("resize", function(){
            $z.do_change_root_fontSize(skinContext);
            skin.resize.call(skinContext);
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
            $z.do_change_root_fontSize(skinContext);
            skin.resize.call(skinContext);
        });
    });
});