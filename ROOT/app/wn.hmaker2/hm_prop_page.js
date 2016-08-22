(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-page" ui-fitparent="yes">
    I am page prop
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_page", {
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