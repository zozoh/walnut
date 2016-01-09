(function($z){
$z.declare(['zui'], function(ZUI, TableUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-vmd-columns"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_vmd_columns", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        
    },
    //..............................................
    events : {
    },
    //..............................................
    update : function(o, UIBrowser){
        var UI = this;



        // 最后重新计算一下尺寸
        UI.resize();
    },
    
    //..............................................
    resize : function(){

    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);