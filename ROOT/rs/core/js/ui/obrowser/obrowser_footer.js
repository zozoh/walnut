(function($z){
$z.declare(['zui'], function(ZUI, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-footer ui-clr" ui-fitparent="yes">&nbsp;</div>
*/};
//==============================================
return ZUI.def("ui.obrowser_footer", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        var UI = this;
        var browser = UI.parent;
        UI.listenUI(browser, "browser:info", function(html){
            UI.show_info(html)
        });
    },
    //..............................................
    show_info : function(html){
        this.arena.html(html);
    },
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



