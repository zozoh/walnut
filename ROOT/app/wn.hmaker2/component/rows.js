(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_layout'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = '<div class="ui-arena hmc-rows"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_rows", {
    keepDom : true,
    dom     : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;
                
        // 确保有一个区域
        var jAreas = UI.arena.children(".hm-area");
        if(jAreas.length == 0) {
            UI.addArea();
        }
        
        // 检查每个区域，确保有辅助节点
        jAreas.each(function(){
            UI.checkAreaAssistDOM(this);
        });
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/rows_prop.js',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);