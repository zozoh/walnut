(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `<div class="ui-arena hmc-searcher">
    <div class="kwd-input"><input></div>
    <div class="kwd-btn"><b></b></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_searcher", {
    dom     : html,
    keepDom   : true,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
        var jInput = UI.arena.find("input");
        var jBtnW  = UI.arena.find(".kwd-btn");

        // 输入框属性
        jInput.attr({
            "placeholder" : com.placeholder || null,
            "maxlength"   : com.maxLen > 0 ? com.maxLen : null
        }).val(com.defaultValue || "");

        // 按钮文字
        if(com.btnText){
            jBtnW.show().children("b").text(com.btnText);
        }
        // 隐藏文字
        else {
            jBtnW.hide();
        }
        
    },
    //...............................................................
    getBlockPropFields : function(block) {
        return [block.mode == 'inflow' ? "margin" : null,
                "padding","border","borderRadius",
                "color", "background",
                "boxShadow","overflow"];
    },
    //...............................................................
    getDefaultData : function(){
        return {
            // "lineHeight" : ".24rem",
            // "fontSize"   : ".14rem",
        };
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/searcher_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);