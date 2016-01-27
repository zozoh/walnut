(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena bp-prop" ui-fitparent="yes">
    I am prop
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_ide_prop", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 添加子 UI
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        //console.log("I am screen update:", o);

        // 读取 screen
        // Wn.read(o, function(json){
        //     var scrn = $z.fromJson(json);
        //     UI._draw_screen(scrn);
        // });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);