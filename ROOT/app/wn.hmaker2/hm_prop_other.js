(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-folder" ui-fitparent="yes">
    I am other prop
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_other", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);