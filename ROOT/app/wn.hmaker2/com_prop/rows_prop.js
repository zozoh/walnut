(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel_layout',
], function(ZUI, Wn, HmMethods, MenuUI, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-rows-prop hmc-layout-prop" ui-fitparent="yes">
    <section class="clp-actions" ui-gasket="actions"></section>
    <section class="clp-layout">
        <ul></ul>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_rows_prop", HmMethods({
    dom  : html,
    //...............................................................
    redraw : function() {
        return this._do_redraw(
            '<i class="zmdi zmdi-long-arrow-up"></i>',
            '<i class="zmdi zmdi-long-arrow-down"></i>'
        )
    },
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);