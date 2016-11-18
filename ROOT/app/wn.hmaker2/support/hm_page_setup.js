define(function (require, exports, module) {
var methods = {
    //...............................................................
    _H(jHead, selector, html) {
        var jq = jHead.children(selector);
        // 确保存在
        if(html){
            if(jq.size() == 0){
                jHead.prepend($(html));
            }
        }
        // 确保删除
        else {
            jq.remove();
        } 
    },
    //...............................................................
    __setup_page_head : function() {
        var UI = this;

        // 首先清空
        var jHead = UI._C.iedit.$head.empty();

        // 头部元数据
        UI._H(jHead, 'meta[charset="utf-8"]',
            '<meta charset="utf-8">');
        UI._H(jHead, 'meta[name="viewport"]',
            '<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0">');
        UI._H(jHead, 'meta[http-equiv="X-UA-Compatible"]',
            '<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">');
        

        // 链入固定的 CSS 
        UI._H(jHead, 'link[href*="normalize.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/normalize.css">');
        UI._H(jHead, 'link[href*="font-awesome.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-awesome-4.5.0/css/font-awesome.css">');
        UI._H(jHead, 'link[href*="material-design-iconic-font.css"]',
            '<link rel="stylesheet" type="text/css" href="/gu/rs/core/css/font-md/css/material-design-iconic-font.css">');
        UI._H(jHead, 'link[href*="hmaker_editing.css"]',
            '<link rel="stylesheet" type="text/css" href="/a/load/wn.hmaker2/hmaker_editing.css">');
        UI._H(jHead, 'link[href*="moveresizing.css"]',
            '<link rel="stylesheet" type="text/css" href="/theme/r/jqp/moveresizing/moveresizing.css">');

        // _H(jHead, 'script[src*="zutil.js"]',
        //     '<script src="/gu/rs/core/js/nutz/zutil.js"></script>');
        // _H(jHead, 'script[src*="seajs"]',
        //     '<script src="/gu/rs/core/js/seajs/seajs-2.3.0/sea-debug.js" id="seajsnode"></script>');
        // _H(jHead, 'script[src*="jquery-2.1.3"]',
        //     '<script src="/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js"></script>');

        // 设置皮肤
        UI.doChangeSkin();
    },
    //...............................................................
    __setup_page_events : function() {
        var UI = this;

        // 首先所有元素的点击事件，全部禁止默认行为
        UI._C.iedit.$root.on("click", "*", function(e){
            e.preventDefault();
            // console.log("hm_page.js: click", this.tagName, this.className);

            var jq = $(this);

            // 如果点在了块里，激活块，然后就不要冒泡了
            if(jq.hasClass("hm-com")){
                e.stopPropagation();
                
                // 已经激活就不再激活了
                if(jq.attr("hm-actived"))
                    return;
                
                // 得到组件的 UI
                var uiCom = ZUI(jq);
                            
                // 通知激活控件
                uiCom.notifyActived();
                
                // 通知改动
                uiCom.notifyBlockChange("page", uiCom.getBlock());
                uiCom.notifyDataChange("page", uiCom.getData());
            }
            // 如果点到了 body，那么激活页
            else if('BODY' == this.tagName){
                UI.fire("active:page", UI._page_obj);
            }

            // 不管怎样，模拟一下父框架页面的点击
            // $(document.body).click();
        });

        // 截获所有的键事件
        UI._C.iedit.$body.on("keydown", function(e){
            // 删除
            if(8 == e.which || 46 == e.which) {
                UI.on_block_delete(e);
            }
        });
        UI._C.iedit.$body.on("keyup", function(e){
            // Shift 将表示开关移动遮罩的  no-drag
            if(16 == e.which) {
                var mvMask = UI._C.iedit.$body.children(".pmv-mask");
                if(mvMask.length > 0) {
                    $z.toggleAttr(mvMask, "no-drag", "yes");
                }
            }
        });
    },
    //...............................................................
    __setup_page_moveresizing : function() {
        var UI = this;
        
        UI._C.iedit.$body.pmoving({
            trigger    : '.hm-com',
            maskClass  : 'hm-page-move-mask',
        });
        
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(uiCom, methods);
};
//=======================================================================
});