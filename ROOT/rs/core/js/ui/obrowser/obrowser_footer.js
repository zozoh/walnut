(function($z){
$z.declare(['zui'], function(ZUI, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-footer ui-clr" ui-fitparent="yes">I am browser footer</div>
*/};
//==============================================
return ZUI.def("ui.obrowser_footer", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    update : function(UIBrowser, o){
        var UI = this;
        UI.browser = UIBrowser;  // 记录一下，让事件们访问能方便一下

    },
    //..............................................
    resize : function(){

    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);



