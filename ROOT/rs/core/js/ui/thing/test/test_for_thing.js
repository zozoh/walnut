(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/th_manager',
], function(ZUI, Wn, ThManagerUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-manager" ui-fitparent="true" ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.test_for_thing", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI = this;

        // 加载对象编辑器
        new ThManagerUI({
            parent : UI,
            gasketName : "main",
            dataMode : "thing",
            fields : [{
                key   : "id",
                title : "ID",
            }, {
                key   : "nm",
                title : "名称",
            }]
        }).render(function(){
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    update : function(o) {
        this.gasket.main.update(o);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);