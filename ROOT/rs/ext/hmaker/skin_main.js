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
        
        // 调整屏幕方向的监听
        window.addEventListener("orientationchange", function(){
            $z.do_change_root_fontSize(skinContext);
            skin.resize.call(skinContext);
        });
    });
});