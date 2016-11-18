(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'app/wn.hmaker2/support/dynamic_data_setting',
], function(ZUI, Wn, HmMethods, DynamicDataSettingUI){
//==============================================
var html = `
<div class="ui-arena hmc-objshow-prop" ui-fitparent="yes">
    <section class="olstp-dds">
        <h4><i class="fa fa-database"></i> <span>{{hmaker.com.objlist.dds}}</span></h4>
        <div ui-gasket="dds"></div>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objshow_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 打开提示
        UI.balloon();

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // DDS
        new DynamicDataSettingUI({
            parent : UI,
            gasketName : "dds",
            on_init : function(){
                this.uiCom = UI.uiCom;
            },
            on_change : function(com) {
                // console.log("haha", com)
                UI.notifyComChange(com, true);
            }
        }).render(function(){
            UI.defer_report("dds");
        });

        // 返回延迟加载
        return ["dds"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        
        // 更新数据源
        UI.gasket.dds.update(com);
    },
});
//===================================================================
});
})(window.NutzUtil);