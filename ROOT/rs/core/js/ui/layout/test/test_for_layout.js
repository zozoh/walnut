(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/layout/layout',
], function(ZUI, Wn, MenuUI, LayoutUI){
//==============================================
var html = function(){/*
<div class="ui-arena test-for-layout" ui-fitparent="true">
    <div class="tfl-menu" ui-gasket="menu" style="border-bottom:1px solid #DDD;"></div>
    <div class="tfl-main" ui-gasket="main"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.test_for_thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI = this;

        var uiLa = new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : 'ui/layout/test/layout_a.xml'
        }).render(function(){
            UI.defer_report('main');
        });

        new MenuUI({
            parent : UI,
            gasketName : 'menu',
            context : uiLa,
            setup : [{
                text : "显示Box",
                handler : function(){
                    this.showArea('box1');
                }
            }, {
                text : "切换chute",
                handler : function(){
                    this.toggleArea('chute');
                }
            }]
        }).render(function(){
            UI.defer_report('menu');
        });

        // 返回延迟加载
        return ["menu", "main"];
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