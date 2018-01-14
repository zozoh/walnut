(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/o_view_obj/o_view_meta',
], function(ZUI, Wn, HmMethods, ObjMetaUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-other" ui-fitparent="yes" ui-gasket="meta"></div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_other", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new ObjMetaUI({
            parent : UI,
            gasketName : "meta",
            hideTitle  : true,
        }).render(function(){
            UI.defer_report("meta");
        });

        return ["meta"];
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);