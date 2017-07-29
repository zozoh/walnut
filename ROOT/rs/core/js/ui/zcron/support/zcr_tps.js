(function($z){
$z.declare([
    'zui',
    'ui/zcron/support/zcr_methods',
    'ui/form/c_array',
], function(ZUI, ZCronMethods, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcr-tps" ui-gasket="list">
</div>
*/};
//==============================================
return ZUI.def("ui.zcron_tps", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    // 监听父控件的消息
    init : function(opt) {
        var UI = ZCronMethods(this);
        UI.listenUI(this.parent.parent, "data:change", this.update);
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        new ArrayUI({
            parent : UI,
            gasketName : "list",
            groupSize  : 6,
            items : function(){
                var re = [];
                for(var i=0;i<48;i++)
                    re[i] = i * 1800;
                return re;
            },
            text : function(v) {
                return $z.parseTimeInfo(v).toString(true);
            },
            on_change : function(v){
                UI.notifyChange();
            }
            
        }).render(function(){
            UI.defer_report("list");
        });
        //..............................................
        // 返回延迟加载
        return ["list"];
    },
    //...............................................................
    notifyChange : function(){
        var str = this.getData();
        this.cronUI().setPart(0, str).setPart(1, null);
    },
    //...............................................................
    update : function(ozc) {
        this.setCronToArrayUI(this.gasket.list, ozc, "matchTime");
    },
    //...............................................................
    getData : function(){
        var tps = this.gasket.list.getData();
        if(tps.length == 0)
            tps.push(0);

        for(var i=0;i<tps.length;i++) {
            var sec = tps[i];
            var ti  = $z.parseTimeInfo(sec);
            tps[i]  = ti.toString(true);
        }
        
        return "T{" + tps.join(",") + "}";
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);