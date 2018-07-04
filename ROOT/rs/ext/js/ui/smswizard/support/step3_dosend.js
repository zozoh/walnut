(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena smsw-step3-dosend" ui-fitparent="yes" ui-gasket="main">
I am dosend
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smsw_step3_dosend", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        
    },
    //...............................................................
    isDataReady : function(){
    	return true;
    },
    //...............................................................
    getData : function(data) {

    },
    //...............................................................
    setData : function(data) {

    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);