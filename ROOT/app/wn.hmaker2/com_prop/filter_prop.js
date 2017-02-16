(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-filter-prop" ui-fitparent="yes" ui-gasket="form">
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_filter_prop", HmMethods({
    dom  : html,
    //...............................................................
    redraw : function() {
        var UI  = this;

        

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    update : function(com) {
        //this.gasket.form.setData(com);
    },
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);