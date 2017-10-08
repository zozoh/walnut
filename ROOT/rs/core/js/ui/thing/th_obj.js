(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/support/dom',
], function(ZUI, Wn, ThMethods, DomUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj" ui-fitparent="true">
    <div class="th-obj-index-con" ui-gasket="index"></div>
    <div class="th-obj-data-con"  ui-gasket="data"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    redraw : function(){
        this.showBlank();
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.obj = this;
    },
    //..............................................
    update : function(o) {
        console.log("update", o);
        this.gasket.index.destroy();
    },
    //..............................................
    showBlank : function(){
        var UI = this;
        var jIndex = UI.arena.find(">.th-obj-index-con");
        var jData  = UI.arena.find(">.th-obj-data-con");

        // 标识
        UI.arena.attr("dis-mode", "index-only");

        // 替换掉索引项
        new DomUI({
            parent : UI,
            gasketName : "index",
            dom : `<div class="th-obj-blank">
                <i class="fa fa-hand-o-left"></i>
                {{th.obj_blank}}
            </div>`
        }).render();

        // 清空 data 项目
        if(UI.gasket.data) {
            UI.gasket.data.destroy();
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);