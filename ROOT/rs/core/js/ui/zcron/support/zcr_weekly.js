(function($z){
$z.declare([
    'zui',
    'ui/form/c_array',
], function(ZUI, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-weekly" ui-gasket="data">
    
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_weekly", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        this.listenParent("data:change", this.update);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 日期范围
        new ArrayUI({
            parent : UI,
            gasketName : "data",
            items : [1,2,3,4,5,6,7],
            text  : function(v) {
                return UI.msg("zcron.exp.week.dict")[v-1];
            },
            on_change : function(v){
                console.log(v);
            }
            
        }).render(function(){
            UI.defer_report("data");
        });

        // 返回延迟加载
        return ["data"];
    },
    //...............................................................
    update : function(zr) {

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);