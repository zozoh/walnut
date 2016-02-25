(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena bp" ui-fitparent="yes">
    <div class="bp-con-nav"     ui-gasket="nav"></div>
    <div class="bp-con-main"    ui-gasket="main"></div>
    <div class="bp-con-footer"  ui-gasket="footer"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_ide", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.hmaker/hmaker.css",
    i18n : "app/wn.hmaker/i18n/{{lang}}.js",
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        //console.log("I am screen update:", o);

        // 更新 nav
        UI.uiNav.update(o);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);