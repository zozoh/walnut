(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/form/form',
], function(ZUI, Wn, DomUI, ThMethods, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-index-meta"
    ui-fitparent="true" ui-gasket="form"></div>
*/};
//==============================================
return ZUI.def("ui.th_obj_index_meta", {
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
        
        new FormUI(_.extend({
            fields  : conf.fields,
            uiWidth : "all",
        }, conf.meta.setup, {
            parent : UI,
            gasketName : "form",
            arenaClass : "obj-meta-form",
            on_change : function(key){
                var uiForm = this;
                var obj    = uiForm.getData();
                uiForm.showPrompt(key, "spinning");
                UI.invokeConfCallback("meta", "update", [obj, key, function(){
                    uiForm.hidePrompt(key);
                    // 通知界面其他部分更新
                    UI.fire("change:meta", [obj, key]);
                }]);
            }
        })).render(function(){
            UI.defer_report("form");
        });

        return ["form"];
    },
    //..............................................
    update : function(o, callback) {
        this.gasket.form.setData(o);
        $z.doCallback(callback, [], this);
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);