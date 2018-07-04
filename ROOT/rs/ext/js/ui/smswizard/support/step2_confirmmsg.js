(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena smsw-step2-confirmmsg" ui-fitparent="yes" ui-gasket="main">
I am confirmmsg
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smsw_step2_confirmmsg", {
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