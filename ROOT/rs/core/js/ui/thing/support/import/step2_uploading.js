(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-2-uploading" ui-fitparent="yes">
    I am upload file
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_2_uploading", {
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