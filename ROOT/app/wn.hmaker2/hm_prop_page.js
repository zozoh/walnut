(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, FormUI, HmMethods){
//==============================================
var html = `
<div class="ui-arena hm-prop-page" ui-fitparent="yes">
    <section class="pp-attr" ui-gasket="attr"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hm_prop_page", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:page",  UI.refreshData);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        new FormUI({
            parent : UI,
            gasketName : "attr",
            on_change : function(){
                var attr = this.getData();
                UI.pageUI().setPageAttr(attr);
            },
            fields : [{
                key    : "color",
                title  : "i18n:hmaker.prop.color",
                type   : "string",
                editAs : "color",
            }, {
                key    : "background",
                title  : "i18n:hmaker.prop.background",
                type   : "string",
                nullAsUndefined : true,
                editAs : "background",
                uiConf : UI.getBackgroundImageEditConf()
            }]
        }).render(function(){
            UI.defer_report("attr");
        });

        return ["attr"];
    },
    //...............................................................
    refreshData : function(){
        var UI = this;
        var attr = UI.pageUI().getPageAttr();
        UI.gasket.attr.setData(attr);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);