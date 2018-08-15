(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
], function(ZUI, Wn, LayoutUI){
//==============================================
var html = function(){/*
<div class="ui-arena test-for-layout" ui-fitparent="true">
    <div class="tfl-menu" ui-gasket="menu"></div>
    <div class="tfl-main" ui-gasket="main"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.test_for_thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI = this;

        new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : 'ui/layout/test/layout_a.xml'
        }).render(function(){
            UI.defer_report('main');
        });

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    update : function(o) {
        // this.gasket.main.update(o);
    },
    resize : function(){
        var UI = this;
        var jMenu = UI.arena.find('>.tfl-menu');
        var jMain = UI.arena.find('>.tfl-main');
        jMain.css({
            "height" : UI.arena.height() - jMenu.outerHeight()
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);