(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods_panel',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = `
<div class="ui-arena hm-prop-folder" ui-fitparent="yes">
    I am folder prop
</div>
`;
//==============================================
return ZUI.def("app.wn.hm_prop_folder", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);