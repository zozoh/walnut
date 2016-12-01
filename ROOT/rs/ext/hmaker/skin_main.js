$(function(){
    seajs.use(window.skin_js_path || "./skin/skin.js", function(skin){
        var skinContext = {
            doc    : document,
            win    : window,
            root   : document.documentElement,
            jQuery : window.jQuery
        };
        skin.on.call(skinContext);
        skin.resize.call(skinContext);
        
        window.addEventListener("resize", function(){
            skin.resize.call(skinContext);
        });
    });
});