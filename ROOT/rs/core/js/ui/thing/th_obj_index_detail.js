(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
], function(ZUI, Wn, DomUI, ThMethods){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-index-detail" ui-fitparent="true">
    I am detail
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj_index_detail", {
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
    _fill_context : function(uiSet) {
        uiSet.detail = this;
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var bus = UI.bus();
        //console.log("update index", o);
        
        
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);