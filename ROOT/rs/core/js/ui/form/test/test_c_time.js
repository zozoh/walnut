(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_time'
], function(ZUI, Wn, TimeUI){
//==============================================
var html = function(){/*
<div class="ui-arena" style="padding:20px;">
    <div ui-gasket="t0"></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test_time", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        //...........................................................
        new TimeUI({
            parent : UI,
            gasketName : "t0",
            on_change : function(val) {
                console.log("new value::", val);
            },
        }).render(function(){
            UI.defer_report("t0");
        });

        
        //...........................................................
        return ["t0"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);