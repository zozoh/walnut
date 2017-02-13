(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmMethods, FormUI, DroplistUI){
//==============================================
var html = `
<div class="ui-arena hmc-dynamic-prop" ui-fitparent="yes">
    
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_dynamic_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);