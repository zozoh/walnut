(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena wn-layout" ui-fitparent="yes">
    I am layout
</div>
*/};
//==============================================
return ZUI.def("ui.layout", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/layout/theme/layout-{{theme}}.css",
    //..............................................
    events : {
        
    },
    //..............................................
    redraw : function() {
        
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);