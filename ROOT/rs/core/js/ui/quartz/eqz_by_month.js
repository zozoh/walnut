(function($z){
$z.declare([
    'zui',
    "ui/quartz/quartz",
], function(ZUI, Quartz){
//==============================================
var html = function(){/*
<div class="ui-arena qz_month" ui-fitparent="yes">
    
</div>
*/};
//==============================================
return ZUI.def("ui.quartz_by_month", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    setData : function(qz){

    },
    //...............................................................
    getData : function(){
        return // QZ
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);