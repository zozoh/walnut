(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-4-done" ui-fitparent="yes">
    I am import DONE!
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_4_done", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;
        

    },
    //...............................................................
    isDataReady : function() {
        return true;
    },
    //...............................................................
    getData : function(){
        
    },
    //...............................................................
    setData : function() {
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);