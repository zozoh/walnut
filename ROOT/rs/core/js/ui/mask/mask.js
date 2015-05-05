/*
 UI::mask 将在选区内显示一个遮罩

 遮罩的 DOM 结构为:

    DIV.z-mask
        I.ui-mask-closer          # 关闭按钮
        DIV.ui-mask-main          # 主区域

调用方式
    new Mask({
        closer : true | false,    // 是否显示关闭按钮
        escape : true | false     // 是否支持 esc 键退出
    }).render(function(){
        // 创建一个新 UI，并将 UI 插入 mask 的 "main" 扩展点 
        new OtherUI({
            parent     : this,
            gasketName : "main"
        });
    });
    
*/
define(function(require, exports, module) {
//===================================================================
    var ZUI = ZUI || require("zui");
    module.exports = ZUI.def("ui.mask", {
        //...............................................................
        dom  : "ui/mask/mask.html",
        css  : "ui/mask/mask.css",
        //...............................................................
        init : function(options){
            this.options = options;
        },
        //...............................................................
        redraw : function() {
            var options = this.options;
            this.$el.prevAll().addClass("ui-mask-others");
            if(options.closer === false) {
                this.arena.find(".ui-mask-closer").hide();
            }
            if(!(options.escape === false)) {
                this.watchKey(27, function(e){
                    this.destroy();
                    //console.log(ZUI.keymap);
                });
            }
        },
        //...............................................................
        events : {
            "click .ui-mask-closer" : function(e){
                this.destroy();
            }
        },
        //...............................................................
        resize : function(W, H){
            var options = this.options;
            // 计算主区域的宽高和位置
            var mW = this.options.width  || parseInt(W * 0.618);
            var mH = this.options.height || parseInt(H * 0.618);
            var mL = (W-mW)/2
            var mT = (H-mH)*0.382;
            this.arena.find(".ui-mask-main").css({
                "width" : mW,
                "height": mH,
                "left"  : mL,
                "top"   : mT,
            });
            var jCloser = this.arena.find(".ui-mask-closer");
            var iR = Math.max(0, mL - jCloser.width()/2);
            var iT = Math.max(0, mT - jCloser.height()/2);
            jCloser.css({
                "right": iR, "top" : iT
            });
            if(this.options.resize){
                this.options.resize.call(this, mW, mH);
            }
        },
        //...............................................................
        depose : function(){
            // 将之前的对象的半透明度，都设置回来
            this.$el.prevAll().removeClass("ui-mask-others");
        }
    });
//===================================================================
});