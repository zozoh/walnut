(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/tabs/tabs',
], function(ZUI, Wn, ThMethods, TabsUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj" ui-fitparent="true"
    ui-gasket="main">I am thobj</div>
*/};
//==============================================
return ZUI.def("ui.th_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    update : function(o) {
        
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);