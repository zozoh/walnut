(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel_layout',
    'ui/menu/menu',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, MenuUI, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-rows-prop" ui-fitparent="yes">
    <section class="crp-actions" ui-gasket="actions"></section>
    <section class="crp-layout">
        <ul></ul>
    </section>
    <section class="crp-form" ui-gasket="form"></secton>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_rows_prop", HmMethods({
    dom  : html,
    //...............................................................
    redraw : function() {
        var UI = this;
        
        // 创建动作菜单
        new MenuUI({
            parent : UI,
            gasketName : "actions",
            setup : [{
                icon : '<i class="zmdi zmdi-plus"></i>',
                text : 'i18n:hmaker.com._area.add',
                handler : function(){
                    UI.uiCom.addArea();
                    UI.update();
                }
            }, {
                icon : '<i class="zmdi zmdi-delete"></i>',
                handler : function(){
                    alert("delete")
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                handler : function(){
                    alert("up")
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                handler : function(){
                    alert("down")
                }
            }]
        }).render(function(){
            UI.defer_report("actions");
        });
        
        // 创建区域属性表单
        new FormUI({
            parent : UI,
            gasketName : "form",
            fields : [{
                key : "areaId", 
                title : "i18n:hmaker.com._area.id"
            }, {
                key : "aaa", 
                title : "i18n:hmaker.com._area.id"
            }, {
                key : "bbbb", 
                title : "i18n:hmaker.com._area.id"
            }, {
                key : "ccc", 
                title : "i18n:hmaker.com._area.id"
            }]
        }).render(function(){
            UI.defer_report("form");
        });
        
        // 返回延迟加载
        return ["actions", "form"];
    },
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);