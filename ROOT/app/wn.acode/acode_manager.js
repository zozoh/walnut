(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search/search',
], function(ZUI, Wn, SearchUI){
//==============================================
var html = `
<div class="ui-arena acode-manager" ui-fitparent="yes">
    I am acode manager
</div>`;
//==============================================
return ZUI.def("app.wn.acode_manager", {
    dom  : html,
    css  : "theme/app/wn.acode/acode_manager.css",
    i18n : "app/wn.acode/i18n/{{lang}}.js",
    //...............................................................
    redraw : function(){
    },
    //...............................................................
    // 这个木啥用了，就是一个空函数，以便 browser 来调用
    update : function(o) {
        // var UI = this;
        // UI.arena.find(".pvg-users-menu .menu-item").first().click();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);