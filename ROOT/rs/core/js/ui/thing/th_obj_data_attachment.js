(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods'
], function(ZUI, Wn, DomUI, ThMethods){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-data-attachment"
    ui-fitparent="true" ui-gasket="main">Attachment</div>
*/};
//==============================================
return ZUI.def("ui.th_obj_data_attachment", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
        
    },
    //..............................................
    update : function(o, callback) {
        
    },
    
    //..............................................
});
//==================================================
});
})(window.NutzUtil);