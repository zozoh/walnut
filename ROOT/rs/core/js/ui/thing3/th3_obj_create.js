(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing3/support/th3_methods',
    'ui/form/form',
], function(ZUI, Wn, DomUI, ThMethods, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-obj-create" ui-fitparent="true">
    <header></header>
    <section ui-gasket="form"><section>
    <footer></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.thing.th_obj_create", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);
    },
    //..............................................
    update : function() {
        console.log("I am update")
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);