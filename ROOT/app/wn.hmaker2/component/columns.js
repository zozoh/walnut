(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_layout'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-columns"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_columns", HmComMethods({
    keepDom : true,
    dom     : html,
    className : "!hm-layout hm-com-columns",
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/columns_prop.js',
            uiConf : {}
        };
    }
}));
//===================================================================
});
})(window.NutzUtil);