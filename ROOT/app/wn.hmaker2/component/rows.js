(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_layout'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-rows"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_rows", HmComMethods({
    keepDom : true,
    dom     : html,
    className : "!hm-layout hm-com-rows",
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/rows_prop.js',
            uiConf : {}
        };
    }
}));
//===================================================================
});
})(window.NutzUtil);