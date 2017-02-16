(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com',
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-sorter"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_sorter", {
    dom     : html,
    keepDom   : true,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        
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
            uiType : 'app/wn.hmaker2/com_prop/sorter_prop',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);