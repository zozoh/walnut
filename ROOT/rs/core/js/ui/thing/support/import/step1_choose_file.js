(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-1-choose-file" ui-fitparent="yes">
    I am choose file
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_1_choose_file", {
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