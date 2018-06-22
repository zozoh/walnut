(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-3-import" ui-fitparent="yes">
    I am import
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_3_import", {
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