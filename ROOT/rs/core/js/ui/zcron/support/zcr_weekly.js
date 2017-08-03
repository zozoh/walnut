(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
    'ui/form/c_array',
], function(ZUI, ZCronMethods, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-weekly" ui-gasket="list">
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_weekly", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        ZCronMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 日期范围
        new ArrayUI({
            parent : UI,
            gasketName : "list",
            items : [1,2,3,4,5,6,7],
            text  : function(v) {
                return UI.msg("zcron.exp.week.dict")[v-1];
            },
            on_change : function(v){
                UI.cronUI().notifyStdDatePartChange();
            }
            
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["list"];
    },
    //...............................................................
    update : function(ozc) {
        this.setCronToArrayUI(this.gasket.list, ozc, "matchDayInWeek");
    },
    //...............................................................
    getData : function(){
        return this.getStrFromArrayUI(this.gasket.list, "1-7", "?");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);